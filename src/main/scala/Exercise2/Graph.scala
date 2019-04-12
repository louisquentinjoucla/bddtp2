package com.exercise2
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag
import com.exercise2.skills.{Skill}
import com.exercise2.monsters.{Monster}


import scala.collection.mutable

class Link(var a:Monster, var skills:Seq[Skill], var b:Monster) extends Serializable {}

class BattleGraph() extends Serializable {

  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 2")
    .getOrCreate()

  var vertices:RDD[Monster] = spark.sparkContext.emptyRDD[Monster]
  var edges:RDD[Link] = spark.sparkContext.emptyRDD[Link]

  var turn:Long = 0
  var cid:Long = 0

  def add(v:Monster):Unit = {
    v.parameters("id") = cid
    cid += 1
    vertices = vertices.union(spark.sparkContext.parallelize(Seq(v)))
  }

  def connect(a:Monster, skills:Seq[Skill], b:Monster):Unit = {
    edges = edges.union(spark.sparkContext.parallelize(Seq(new Link(a, skills, b))))
  }

  def next():Unit = {
    turn += 1
    /*
      vertices
        => target
        => skill

      solar
        => target : orc 1
        => skill : "move"

    */
    println("-----------------")
    val e = edges
      .filter(edge => edge.a.targets.contains(edge.b.parameters("id")))
      .map(edge => edge.skills(edge.a.skill).test(edge.a, edge.b, 0))
      .map(m => (m.id, m))
      .foreach{case (id,m) => println(m.toString())}
    

    //e1.join(e2).collect().foreach(println)



  }

    
  def print():Unit = {
    println("==========================================")
    println(s"Turn ${turn}")
    vertices.map(m => m.toString()).foreach(println)
  }

}


