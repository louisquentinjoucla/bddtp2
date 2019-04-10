//Imports and packages
package com.exercise1
import org.apache.spark.sql.SparkSession
import com.exercise1.WebServices

//-------------------------------------------------------------------------------------------
//Main
object Exercise1 extends App {

  //Apache Spark
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 1")
    .getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  //Application
  BatchLayer.compute(spark)
  BatchLayer.load(spark)
  WebServices.start()

}
