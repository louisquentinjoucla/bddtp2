package com.exercise2.monsters
import scala.collection.mutable
import com.exercise2.skills.{Skill, Move}


case class Monster(var data:String) {
    def get(key:String):String = {
      val values = (s"${key}=(.*?);").r.findFirstMatchIn(data)
      values match {
        case Some(name) =>
          return values.get.subgroups(0)
        case None =>
          return "<undefined>"
      }
      return "<undefined>"
    }

    def getAsInt(key:String):Int = {
      val value = get(key)
      if (value.equals("<undefined>"))
        return 0
      else 
        return get(key).toInt
    }


    def set(key:String, svalue:Any):Unit = {
      val value = svalue.toString()
      if (get(key).equals("<undefined>"))
        data = s"${data}${key}=${value};"
      else 
        data = data.replaceAll(s"${key}=.*?;", s"${key}=${value};")
    }
    
}

package object Bestiary {
  val Solar:String = "name=Solar;hpm=363;regen=15;armor=44;speed=50;flying=false;"
}


/*

abstract class Monster(var parameters:mutable.Map[String, Any] = mutable.Map.empty[String, Any]) extends Serializable {

    var skills:Seq[Skill] = Seq[Skill](Move)

    val targets:Array[Int] = Array[Int]()
    val skill:Int = 0

    def id:Int = return parameters("id").asInstanceOf[Int]

    def setPosition(x:Int, y:Int, z:Int): Unit = {
      parameters("x") = x
      parameters("y") = y
      parameters("z") = z
    }

    override def toString():String = {
      return parameters.map{case (k, v) => k + ":" + v}.mkString("|")
    }

}

class Solar() 
    extends Monster(mutable.Map[String, Any]("name" -> "Solar", "hpm" -> 363, "regen" -> 15, "armor" -> 44, "speed" -> 50, "flying" -> false)) {
    
    override val targets:Array[Int] = Array[Int](3, 4)
}


class OrcBarbarian() 
    extends Monster(mutable.Map[String, Any]("name" -> "Double Axe Fury", "hpm" -> 142, "regen" -> 0, "armor" -> 17, "speed" -> 40, "flying" -> false)) {
    
}


class WorgRider() 
    extends Monster(mutable.Map[String, Any]("name" -> "Orc Worg Rider", "hpm" -> 13, "regen" -> 0, "armor" -> 18, "speed" -> 20, "flying" -> false)) {
    
}

class WarLord() 
    extends Monster(mutable.Map[String, Any]("name" -> "Brutal Warlord","hpm" -> 141, "regen" -> 0, "armor" -> 27, "speed" -> 30, "flying" -> false)) {
    
}


*/

/**

HP
Regen
Armure
Vitesse

Array[Attack]



Noeud a  Noeud b



Attaquer/Déplacement x2

Solar
  arc : 110ft
  épée : 5ft/10ft
  déplacement en volant
  régénération : 15hp/tour
  armure : 44
  hp 

  détacher son épée pdt 4 tours, elle attaque tout seul
  heal tous les alliés de 200hp (1 seul fois)
  summon monster 7 (1 seul fois)
  summon monster, autant de fois
  wish : copie le sort polar ray (efficace contre les dragons)

Orc
  Hp
  armure
  attaques

Dragon
  4x attaques ailes/queue/morsure/griffe



CC 20/20

Solar -> edges avec tout le monde

Pour toucher une créature, il faut égaliser ou battre son armure (AC).

*/