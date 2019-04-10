//Imports and packages
package com.exercise1
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.exercise1.Global
import com.exercise1.BatchLayer
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import io.circe.Json

//-------------------------------------------------------------------------------------------
//Web services

package object WebServices {

  def start():Unit = {
    //Debug
    println("Starting server...")
    //Setup WebSockets server with akka
    implicit val system = ActorSystem("akka-system")
    implicit val materializer = ActorMaterializer()
    val route = MainService.route ~ SocketService.route

    //Start server
    val binding = Http().bindAndHandle(route, "localhost", 8080)
    println("Server is online")
  }

}

//Response
case class Response(status:String, results:Array[Json] = Array[Json](), n:Long = 0)

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
          TextMessage(ServingLayer.process(query).asJson.noSpaces)
        }
      }
    }
    case _ => TextMessage(Response("Not supported").asJson.noSpaces)
  }
}