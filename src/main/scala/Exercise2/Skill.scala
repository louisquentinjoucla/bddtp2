package com.exercise2.skills
import com.exercise2.monsters.{Monster}

class Skill() extends Serializable {

  //apply(source:Monster, target:Monster)
  def test(source:Monster, target:Monster):List[(String, Int)] = {
    println(s"Missing skill")
    return List()
  }
}


object Move extends Skill() {

  override def test(source:Monster, target:Monster):List[(String, Int)] = {
    //println(s"${source.parameters("name")} moves towards ${target.parameters("name")}")
    return List()
  }

}