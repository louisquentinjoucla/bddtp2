package com.exercise2
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag
import com.exercise2.skills.{Skill}
import com.exercise2.monsters.{Monster}
import scala.collection.mutable
import org.apache.spark.rdd.RDD


import scala.collection.mutable

class Link(var a:Monster, var skills:Seq[Skill], var b:Monster) extends Serializable {}

class BattleGraph() extends Serializable {

  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 2")
    .getOrCreate()
  
  val sqlContext = spark.sqlContext
  import sqlContext.implicits._


  var vertices = Seq[(Long, Monster)]().toDS()

  var turn:Long = 0L
  var cid:Long = 0L

  def add(v:Monster):Unit = {
    v.set("id", cid)
    cid += 1

    val vertex = Seq[(Long, Monster)]((cid, v)).toDS()
    vertices = vertices.union(vertex)
  }

  def next():Unit = {
    turn += 1
  }
    
  def print():Unit = {
    println("==========================================")
    println(s"Turn ${turn}")
    vertices.show(false)
  }

}


