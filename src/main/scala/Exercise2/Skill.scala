package com.exercise2.skills
import com.exercise2.monsters.{Monster}

class Skill() extends Serializable {

  //apply(source:Monster, target:Monster)
  def apply(source:Monster, target:Monster, get:Int):Monster = {
    println(s"Missing skill")
    if (get == 0) return source
    return target
  }
}


object Move extends Skill() {

  override def apply(source:Monster, target:Monster, get:Int):Monster = {
    println(s"${source.parameters("name")} moves towards ${target.parameters("name")}")
    source.parameters("x") = source.parameters("x").asInstanceOf[Float] + 1
    //source.parameters("x") = source.parameters("speed") target.parameters("x")
    if (get == 0) return source
    return target
  }

}