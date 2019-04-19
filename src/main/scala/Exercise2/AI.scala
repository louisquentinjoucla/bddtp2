package com.exercise2.ai

import com.exercise2.monsters.Monster

package object AI {

  

  def compute(monster:Monster, edges:Seq[(Int, Int, Int, Int, Int)]):Seq[(Int, String)] = {

    val allies = edges.filter{case (ida, idb, team, kind, hpb) => team == 0 }
    val ennemies = edges.filter{case (ida, idb, team, kind, hpb) => team == 1 }

    //Melee attack
    val melee = ennemies.filter{case (ida, idb, team, kind, hpb) => (kind == 1) }
    if ((melee.size > 0)&&(monster.skills.contains("melee"))) {
      val target = melee.minBy(x => x._5)
      return Seq((target._2, monster.skills("melee")))
    }

    //Ranged attack
    val ranged = ennemies.filter{case (ida, idb, team, kind, hpb) => (kind == 2) }
    if ((ranged.size > 0)&&(monster.skills.contains("ranged"))) {
      val target = ranged.minBy(x => x._5)
      return Seq((target._2, monster.skills("ranged")))
    }
      
    //Move towards the weakest ennemy
    val move = ennemies.filter{case (ida, idb, team, kind, hpb) => (kind == 0) }
    if ((move.size > 0)&&(monster.skills.contains("move"))) {
      val target = move.minBy(x => x._5)
      return Seq((target._2, monster.skills("move")))
    }

    return Seq()

  }



/*
return Seq((Int, String))
monsters.actions 
*/

}




//Intelligence artificielle pour les actions des créatures. 
//Une créature doit-elle attaquer, soigner un allié, utiliser un sort, cracher du feu, faire un mouvement?
//Gérer les distances entre les créatures, à chaque itération
//Optimisation des structures de données, checkpointing.
