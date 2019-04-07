//Imports
import org.apache.spark.sql.{SparkSession}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import io.circe.Json
import org.apache.spark.rdd.RDD
import scala.collection.mutable
import scala.util.parsing.json.JSONObject

//Source
object Exercise1 extends App {

  //Apache Spark
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 1")
    .getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  //Retrieve data from JSON file (spells)
  val spells = spark.sqlContext.read
    .option("multiLine", true)
    .json("src/resources/JSON/spells.json")

  //Retrieve data from JSON file (monsters)
  val monsters = spark.sqlContext.read
    .option("multiLine", true)
    .json("src/resources/JSON/monster.json")

  //Create a batch view (Spells -> Spells data)
  //This view store in its values spells raw json data
  Layers.batch("spells_names:spells_data") = spells.rdd
    .map(row => (row.getAs[String]("name"), row.getValuesMap[Any](row.schema.fieldNames)))
    .groupByKey()
    .map{case (key, values) => (key, values.map(value => value.map{case (k, v) => (k, if (v.isInstanceOf[mutable.WrappedArray[String]]) v.asInstanceOf[mutable.WrappedArray[String]].toArray.mkString("[", ",", "]") else v) }))}
    .map{case (key, values) => (key, values.map(value => JSONObject(value.filter(_._2 != null)).toString()))}

  //Create a batch view (Spells -> Monsters)
  Layers.batch("spells_names:monsters") = monsters.rdd
    .map(row => (row.getAs[String]("name"), row.getAs[Seq[String]]("spells")))
    .flatMap{case (monster, spells) => spells.map(spell => (spell, monster))}
    .groupByKey()

  //Setup WebSockets server with akka
  implicit val system = ActorSystem("akka-system")
  implicit val materializer = ActorMaterializer()
  val route = MainService.route ~ SocketService.route

  //Start server
  val binding = Http().bindAndHandle(route, "localhost", 8080)
  println("Server is online")
}

//-------------------------------------------------------------------------------------------
//Lambda architecture

//Server layers
object Layers {
  val batch = mutable.Map.empty[String, RDD[(String, Iterable[String])]]
}

//-------------------------------------------------------------------------------------------
//Web services

//Response
case class Response(status:String, results:Array[Json] = Array[Json]())

//WebServices
trait WebService { def route: Route }

//Default service
object MainService extends WebService {
  override def route: Route = get {
    (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(StatusCodes.TemporaryRedirect)) {
      getFromFile("src/resources/www/index.html")
    } ~ {
      getFromDirectory("src/resources/www")
    }
  }
}

//Query service
object SocketService extends WebService {
  override def route: Route = path("ws") {
    get {
      handleWebSocketMessages(service)
    }
  }

  //This function handle Web Sockets messages
  val service: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(message) => {
      parse(message) match {
        case Left(failure) => TextMessage(Response("Bad request").asJson.noSpaces)
        case Right(json) => {
          //Retrieve query
          val query = json.hcursor

          //Read query parameters
          val name = query.get[String]("name").getOrElse("<MISSING NAME>")

          //Process query
          val view = Layers.batch("spells_names:spells_data")
          val search = view
            .filter{case (key, value) => key.contains(name)}
            .map{case (key, value) => parse(value.toList(0)).getOrElse(Json.Null)}

          //Send back response
          TextMessage(Response("", search.take(21)).asJson.noSpaces)
        }
      }
    }
    case _ => TextMessage(Response("Not supported").asJson.noSpaces)
  }
}