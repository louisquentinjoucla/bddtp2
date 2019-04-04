//Imports
import org.apache.spark.sql.SparkSession

//Source
object Application extends App {

  //Apache Spark
  val spark = SparkSession.builder
    .master("local")
    .appName("BDD TP2")
    .getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  //Retrieve data from JSON file
  //val json = spark.sqlContext.read
    //.option("multiLine", true)
    //.json("*.json")

  println("Hello world !")
}
