package com.exercise2.skills
import com.exercise2.monsters.Monster

//-------------------------------------------------------------------------------------------
//Skills

package object Skill {

  //Execute skill effect
  def execute(a:Monster, skill:String, b:Monster):Seq[(String, Int)] = {

    //Skill selection
    if (skill.equals("move"))
      return Skill.move(a, b)

    return Seq()
  }

  //Move skill
  def move(a:Monster, b:Monster):Seq[(String, Int)] = {
    return Seq(("x", a.get("x")+1))
  }

}