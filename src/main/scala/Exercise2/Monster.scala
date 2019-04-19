package com.exercise2.monsters

//-------------------------------------------------------------------------------------------
//Monsters and bestiary

case class Monster(name:String, var data:Map[String, Int] = Map.empty[String, Int], var skills:Seq[String] = Seq[String](), var actions:Seq[(Int, String)] = Seq[(Int, String)]()) {

    //Retrieve value associated to a key in data string
    def get(key:String):Int = {
      if (data.contains(key))
        return data(key)
      else
        return 0
    }

    //Set value associated to a key in data string
    def set(key:String, value:Int):Unit = {
      data = data + (key -> value)
    }

}

//Bestiary
package object Bestiary {
  def Solar():Monster = {
    return new Monster("Solar", Map("hpm" -> 363, "regen" -> 15, "armor" -> 44, "speed" -> 50), Seq("move"))
  }
  def OrcBarbarian():Monster = {
    return new Monster("Double Axe Fury", Map("hpm" -> 142, "regen" -> 0, "armor" -> 17, "speed" -> 40), Seq("move"))
  }
  def WorgRider():Monster = {
    return new Monster("Orc Worg Rider", Map("hpm" -> 13, "regen" -> 0, "armor" -> 18, "speed" -> 20), Seq("move"))
  }
}
