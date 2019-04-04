//Imports
import org.apache.spark.sql.SparkSession

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

  //DEBUG
    bv_spells_monsters
      .take(30)
      .foreach(println)
}
