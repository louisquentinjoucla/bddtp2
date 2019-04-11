import scala.collection.mutable

abstract class Monster(var parameters:mutable.Map[String, Any] = mutable.Map.empty[String, Any) {

}

class Solar() 
    extends Monster(mutable.Map[String, Any]("name" -> "Solar", "hpm" -> 363, "regen" -> 15, "armor" -> 44, "speed" -> 50, "flying" -> false)) {
    
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




/*

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