//Imports and packages
import org.apache.spark.sql.SparkSession
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.exercise1.BatchLayer
import com.exercise1.MainService
import com.exercise1.SocketService
import org.apache.spark.sql.SparkSession
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

//-------------------------------------------------------------------------------------------
//Main
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
