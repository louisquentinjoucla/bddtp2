import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD

class Graph[Vertex, Relationship] {

  var vertices:RDD[Vertex] = spark.sparkContext.emptyRDD[Vertex]
  var edges:RDD[(Vertex, Relationship, Vertex)] = spark.sparkContext.emptyRDD[(Vertex, Relationship, Vertex)]

}



