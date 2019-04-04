//Imports
import org.apache.spark.sql.SparkSession
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.server.Route

//Source
object Exercise1 extends App {

  //Apache Spark
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 1")
    .getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  //Retrieve data from JSON file
  val monsters = spark.sqlContext.read
    .option("multiLine", true)
    .json("JSON/monster.json")

  //Create a batch view (Spells -> Monsters)
  val bv_spells_monsters = monsters.rdd
    .map(row => (row.getAs[String]("name"), row.getAs[Seq[String]]("spells")))
    .flatMap{case (monster, spells) => spells.map(spell => (spell, monster))}
    .groupByKey()

  //Setup WebSockets server with akka
  implicit val system = ActorSystem("akka-system")
  implicit val materializer = ActorMaterializer()
  implicit val execution = system.dispatcher
  val route = MainService.route ~ QueryService.route

  //Start server
  val binding = Http().bindAndHandle(route, "localhost", 8080)
  println("Server is online")
  //binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
  //println("Server is offline")
}

//WebServices
trait WebService { def route: Route }
//Default service
object MainService extends WebService { override def route: Route = pathEndOrSingleSlash { complete("Welcome to websocket server") } }
//Query service
object QueryService extends WebService { override def route: Route = path("q") { get { handleWebSocketMessages(service) } }
  val service: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
    case _ => TextMessage("Message type unsupported")
  }
}