package Exercise2

import org.apache.spark.sql.SparkSession

//-------------------------------------------------------------------------------------------
//Main
object Exercise2 extends App {

  //Apache Spark
  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 2")
    .config("spark.testing.memory", "2147480000")
    .getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  //Application
  println("hello world (ex2)")

}
