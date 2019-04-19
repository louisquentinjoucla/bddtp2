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
    return new Monster("Solar", Map("hpm" -> 363, "hp" -> 363, "regen" -> 15, "armor" -> 44, "speed" -> 50, "fly" -> 150, "flying" -> 0), Seq("move", "dancing_greatsword", "ranged"))
  }

  def OrcBarbarian():Monster = {
    return new Monster("Double Axe Fury", Map("hpm" -> 142, "hp" -> 142, "regen" -> 0, "armor" -> 17, "speed" -> 40), Seq("move", "orc_double_axe", "mwk_composite_longbow"))
  }

  def WorgRider():Monster = {
    return new Monster("Orc Worg Rider", Map("hpm" -> 13, "hp" -> 13, "regen" -> 0, "armor" -> 18, "speed" -> 20), Seq("move", "mwk_battleaxe", "shortbow"))
  }

  def WarLord():Monster = {
    return new Monster("War Lord", Map("hpm" -> 141, "hp" -> 144, "regen" -> 0, "armor" -> 27, "speed" -> 30), Seq("move", "vicious_flail", "mwk_throwing_axe"))
  }
}
