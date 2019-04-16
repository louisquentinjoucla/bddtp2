package com.exercise2.monsters

case class Monster(var data:String) {
    //Retrieve value associated to a key in data string
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

    //Retrieve value associated to a key in data string (and return it as int)
    def getAsInt(key:String):Int = {
      val value = get(key)
      if (value.equals("<undefined>"))
        return 0
      else 
        return get(key).toInt
    }

    def getAsArray(key:String):Array[String] = {
      val value = get(key)
      if (value.equals("<undefined>"))
        return Array[String]()
      else
        return get(key).split(",")
    }

    def setActions(actions:Seq[(Int, String)]):Unit = {
      set("actions", actions.map{case (target, skill) => s"${target}->${skill}"}.mkString(","))
    }

    def getActions():Seq[(Int, String)] = {
      val value = get("actions")
      if (value.equals("<undefined>"))
        return Seq[(Int, String)]()

      return value.split(",").map(action => {
        val p = action.split("->")
        (p(0).toInt, p(1))
      })
    }

    //Set value associated to a key in data string
    def set(key:String, svalue:Any):Unit = {
      val value = svalue.toString()
      if (get(key).equals("<undefined>"))
        data = s"${data}${key}=${value};"
      else 
        data = data.replaceAll(s"${key}=.*?;", s"${key}=${value};")
    }
    
}

//Bestiary
package object Bestiary {
  val Solar:String = "name=Solar;hpm=363;regen=15;armor=44;speed=50;flying=false;skills=move;"
  val OrcBarbarian:String = "name=Double Axe Fury;hpm=142;regen=0;armor=17;speed=40;flying=false;skills=move;"
  val WorgRider:String = "name=Orc Worg Rider;hpm=13;regen=0;armor=18;speed=20;flying=false;skills=move;"
}

/*
Array[Attack]
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