//Imports and packages
package com.exercise1
import com.exercise1.Global
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD
import scala.collection.mutable
import scala.util.parsing.json.JSONObject
import java.io._
import java.nio.file.Paths
import com.exercise1.Global
import com.exercise1.BatchLayer
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser._
import io.circe.Json


//-------------------------------------------------------------------------------------------
//Batch layer
package object ServingLayer {

  def process(query:HCursor): Unit = {
    //Retrieve query
    

    //Read query parameters
    //val name = query.get[String]("name").getOrElse("<MISSING NAME>")
    val components = query.get[Seq[String]]("components").getOrElse(List())
    val schools = query.get[Seq[String]]("schools").getOrElse(List())

    //val init = query.get[Boolean]("init").getOrElse(false)

    //if (init) {
      
    //}
    init_filters()

    //Process query
    //if(name == "<MISSING NAME>"){

   // //} else {
      // Retreiving the batch views
      val spell_view = BatchLayer.views("spells_${name.charAt(0).toLowerCase}")
      val components_views = components.foreach(v=> BatchLayer.views("spells_${v}"))
      val schools_views = schools.foreach(v=> BatchLayer.views("spells_${v}"))
      // Inner join

    //}

    /**val search = view
  .filter{case (key, value) => key.contains(name)}
  .map{case (key, value) => parse(value.toList(0)).getOrElse(Json.Null)}
      */
    //search.take(21)).asJson.noSpaces
  }

  def init_filters():Unit = {
     val components_filter = Global.directory("src/resources/batchviews/spells/components")
     val schools_filter = Global.directory("src/resources/batchviews/spells/schools")
     val levels_filter = Global.directory("src/resources/batchviews/spells/levels")
     val classes_filter = Global.directory("src/resources/batchviews/spells/classes")

     println(classes_filter)
  }

}

 