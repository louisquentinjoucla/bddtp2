//Imports
import org.apache.spark.sql.SparkSession
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
import java.io._
import org.apache.spark

//Source
object Exercise1 extends App {

  //Apache Spark
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 1")
    .getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  //BatchLayer.compute(spark)
  BatchLayer.load(spark)

  //Setup WebSockets server with akka
  implicit val system = ActorSystem("akka-system")
  implicit val materializer = ActorMaterializer()
  val route = MainService.route ~ SocketService.route

  //Start server
  val binding = Http().bindAndHandle(route, "localhost", 8080)
  println("Server is online")
}


//-------------------------------------------------------------------------------------------
//Global name space
object Global {
  //List all files in a directories
  def directory(path:String): List[String] = (new File(path)).listFiles.filter(_.isDirectory).map(_.getName).toList

  //Clean directories of its content
  def clean(path:String): Unit = clean(new File(path))
  def clean(file:File, delete:Boolean = false):Unit = {
    if (file.isDirectory)
      file.listFiles.foreach(clean(_, true))
    if (delete && file.exists && !file.delete)
      throw new Exception(s"Unable to delete ${file.getAbsolutePath}")
  }

}

//-------------------------------------------------------------------------------------------
//Lambda architecture

object BatchLayer {
  val views = mutable.Map.empty[String, RDD[(String, Iterable[String])]]

  def compute(spark:SparkSession): Unit = {
    //Retrieve data from JSON file (spells)
    val spells = spark.sqlContext.read
      .option("multiLine", true)
      .json("src/resources/JSON/spells.json")

    //Retrieve data from JSON file (monsters)
    val monsters = spark.sqlContext.read
      .option("multiLine", true)
      .json("src/resources/JSON/monsters.json")

    //Clean batchviews folder
    Global.clean("src/resources/batchviews")

    //Create spells batch view (spell_name, [spell_data])
    //This view store in its values spells raw json data
    for(i <-97 until 123) {
      spells.rdd
        .map(row => (row.getAs[String]("name"), row.getValuesMap[Any](row.schema.fieldNames)))
        .filter{case (key,value) => key.charAt(0) == i.asInstanceOf[Char]}
        .groupByKey()
        .map{case (key, values) => (key, values.map(value => value.map{case (k, v) => (k, if (v.isInstanceOf[mutable.WrappedArray[String]]) v.asInstanceOf[mutable.WrappedArray[String]].toArray.mkString("[", ",", "]") else v) }))}
        .map{case (key, values) => (key, values.map(value => JSONObject(value.filter(_._2 != null)).toString()))}
        .map{case (key, values) => (key, values.mkString("[", ";;", "]"))}
        .coalesce(1)
        .saveAsTextFile(s"src/resources/batchviews/spells/name/${i.asInstanceOf[Char]}")
    }

    //Create a batch view (spell_name, [...monsters])
    monsters.rdd
      .map(row => (row.getAs[String]("name"), row.getAs[Seq[String]]("spells")))
      .flatMap{case (monster, spells) => spells.map(spell => (spell, monster))}
      .groupByKey()
      .map{case (key, values) => (key, values.mkString("[", ";;", "]"))}
      .saveAsTextFile(s"src/resources/batchviews/spells/monsters")

    //Prepare components batch views (component, [...spell_name])
    val spells_components = spells.rdd
      .map(row => (row.getAs[String]("name"), row.getAs[Seq[String]]("components")))
      .flatMap{case (spell, components) => components.map(component => (component, spell))}
      .groupByKey()

    //Create components batch views (spell_name, [component])
    spells_components.collectAsMap().keys.foreach{case component => {
      spells_components
        .filter{case (key, values) => key == component}
        .flatMap{case (key, spells) => spells.map(spell => (spell, component))}
        .map{case (spell, component) => (spell, s"[${component}]")}
        .saveAsTextFile(s"src/resources/batchviews/spells/components/${component}")
    }}

    //Prepare schools batch views (school, [...spell_name])
    val spells_schools = spells.rdd
      .map(row => (row.getAs[String]("School").split(" ")(0).toLowerCase, row.getAs[String]("name")))
      .groupByKey()

    //Create schools batch views (spell_name, [school])
    spells_schools.collectAsMap().keys.foreach{case school => {
      spells_schools
        .filter{case (key, values) => key == school}
        .flatMap{case (key, spells) => spells.map(spell => (spell, school))}
        .map{case (spell, school) => (spell, s"[${school}]")}
        .saveAsTextFile(s"src/resources/batchviews/spells/schools/${school}")
    }}

    //Prepare classes and levels batch views (spell_name, [...spell_class_lvl]")
    val spells_cl_lvl = spells.rdd
      .map(row => (row.getAs[String]("name"), row.getAs[String]("Level").toLowerCase.replaceAll("\\/", "-")))
      .groupByKey()
      .map{case (key, value) => (key, value.flatMap((v: String) => v.split(",").map(w => w.trim)))}

    //Prepare classes batch views (spell_class, [...(spell_name, spell_class_lvl]")
    val spells_classes = spells_cl_lvl
      .flatMap{case (spell, classes_lvl) => classes_lvl.map(class_lvl => (class_lvl, spell))}
      .filter{case (key, value) => key.matches("^[a-z-]+ [0-9]+")}
      .map{case (class_lvl, spell) => (class_lvl.split(" ")(0).toLowerCase, (spell, class_lvl.split(" ")(1)))}
      .groupByKey()

    //Create classes batch views
    spells_classes.collectAsMap().keys.foreach{case kind => {

      //Filter by spells by class
      val spells_classes_all = spells_classes
        .filter { case (key, values) => key == kind }

      //Create batch view (spell_name, spell_class) (all levels)
      spells_classes_all
        .flatMap { case (key, spells) => spells.map(spell_lvl => (spell_lvl._1, kind)) }
        .map { case (spell, kind) => (spell, s"[${kind}]") }
        .saveAsTextFile(s"src/resources/batchviews/spells/classes/${kind}/all")

      //Prepare batch view (spell_level, [...spells])
      val spells_levels = spells_classes_all
        .flatMap { case (key, spells) => spells.map(spell_lvl => (spell_lvl._2, spell_lvl._1)) }
        .groupByKey()

      //Create batch view (spell_name, spell_level)
      spells_levels.collectAsMap().keys.foreach { case level => {
        spells_levels
          .filter { case (key, values) => key == level }
          .flatMap { case (key, spells) => spells.map(spell => (spell, key)) }
          .saveAsTextFile(s"src/resources/batchviews/spells/classes/${kind}/${level}")
      }}

    }}

    //Prepare classes batch views (spell_class, [...(spell_name, spell_class_lvl]")
      val spells_levels = spells_classes
          .flatMap{case (kind, spells_lvl) => spells_lvl.map(spell_lvl => (spell_lvl._2, spell_lvl._1)) }
          .groupByKey()

      //Create classes batch views
      spells_levels.collectAsMap().keys.foreach{case level => {
        spells_levels
          .filter { case (key, values) => key == level }
          .flatMap { case (key, spells) => spells.map(spell => (spell, key)) }
          .saveAsTextFile(s"src/resources/batchviews/spells/levels/${level}")
      }}
    
    }



      def load(spark:SparkSession): Unit = {
        //Read batch views back from text file
        Global.directory("src/resources/batchviews").foreach(name => {
          BatchLayer.views(name) = spark.sparkContext.textFile(s"src/resources/batchviews/${name}")
            .map(row => {
              val matches = "^\\((.+),\\[(.+)\\]\\)$".r.findFirstMatchIn(row).get.subgroups
              (matches(0), matches(1).split(";;"))
            })
        })
      }

    }


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
              val components = query.get[Seq[String]]("components").getOrElse(List())
              val schools = query.get[Seq[String]]("schools").getOrElse(List())

              //Process query
              if(name == "<MISSING NAME>"){

              } else {
                // Retreiving the batch views
                val spell_view = Layers.batch("spells_${name.charAt(0).toLowerCase}")
                val components_views = components.foreach(v=> Layers.batch("spells_${v}"))
                val schools_views = schools.foreach(v=> Layers.batch("spells_${v}"))
                // Inner join

              }

              /**val search = view
            .filter{case (key, value) => key.contains(name)}
            .map{case (key, value) => parse(value.toList(0)).getOrElse(Json.Null)}
                */
              //Send back response
              //TextMessage(Response("", search.take(21)).asJson.noSpaces)
              TextMessage(Response("").asJson.noSpaces)
            }
          }
        }
        case _ => TextMessage(Response("Not supported").asJson.noSpaces)
      }
    }