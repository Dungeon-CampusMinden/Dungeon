---
title: "Blockly Lösung: Level 15"
---

## Blöcke
![solution](./img/loesung_level_015_1.png)
![solution](./img/loesung_level_015_2.png)

## Code 

```java
int anzahl_Monster = 0;
int anzahl_Wand_Vorne = 0;
while(anzahl_Wand_Vorne < 7){
    if(hero.isNearComponent(AIComponent.class, Direction.UP)){
        anzahl_Monster++;
        while(hero.isNearComponent(AIComponent.class, Direction.UP)){
            hero.shootFireball();
        }
    }
    if(hero.isNearTile(LevelElement.WALL, Direction.UP)){
        if(hero.isNearTile(LevelElement.WALL, Direction.UP) && hero.isNearTile(LevelElement.WALL, Direction.LEFT)){
            hero.rotate(Direction.RIGHT);
            anzahl_Wand_Vorne++;
        }
        if(hero.isNearTile(LevelElement.WALL, Direction.UP) && hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
            hero.rotate(Direction.LEFT);
            anzahl_Wand_Vorne++;
        }
    }else{
        hero.move();
    }
}
if(anzahl_Monster == 1){
    for(int i = 1; i <= 5; i++){
        hero.move();
    }
}
if(anzahl_Monster == 2){
    for(int i = 1; i <= 10; i++){
        hero.move();
    }
}
if(anzahl_Monster == 3){
    for(int i = 1; i <= 15; i++){
        hero.move();
    }
}
if(anzahl_Monster == 4){
    for(int i = 1; i <= 20; i++){
        hero.move();
    }
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 6; i++){
    hero.move();
}
```

