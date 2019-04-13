package com.exercise2
import org.apache.spark.sql.SparkSession
import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag
import com.exercise2.skills.{Skill}
import com.exercise2.monsters.{Monster}
import scala.collection.mutable
import org.apache.spark.rdd.RDD


import scala.collection.mutable

class BattleGraph() extends Serializable {

  val spark = SparkSession.builder
    .master("local")
    .appName("Exercise 2")
    .getOrCreate()
  
  val sqlContext = spark.sqlContext
  import sqlContext.implicits._


  var vertices = Seq[(Long, Monster, List[(Long, Int)])]().toDS()

  var turn:Long = 0L
  var cid:Long = 0L

  def add(v:Monster):Unit = {
    v.set("id", cid)
    cid += 1
    val vertex = Seq[(Long, Monster, List[(Long, Int)])]((cid, v, List[(Long, Int)]())).toDS()
    vertices = vertices.union(vertex)
  }

  
  def connect():Unit = {

    val x = vertices.map(v => (v._1, v._2.getAsInt("team"))).collect()

    vertices = vertices.map(va =>
      (va._1, va._2, x.map(vb => (vb._1, if (vb._2 == va._2.getAsInt("team")) 0 else 1)).toList)
    )

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

