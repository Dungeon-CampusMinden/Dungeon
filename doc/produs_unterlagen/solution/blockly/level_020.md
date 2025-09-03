---
title: "Blockly Lösung: Level 20"
---

## Blöcke
![solution](./img/loesung_level_020.png)

## Code 

```java
while(true){
    if(hero.checkBossViewDirection(Direction.RIGHT)){
        if(hero.isNearTile(LevelElement.WALL, Direction.UP) || hero.isNearTile(LevelElement.HOLE, Direction.UP) || hero.isNearTile(LevelElement.PIT, Direction.UP)){
            if((!hero.isNearTile(LevelElement.WALL, Direction.RIGHT)) && (!hero.isNearTile(LevelElement.HOLE, Direction.RIGHT)) && (!hero.isNearTile(LevelElement.PIT, Direction.RIGHT))){
                hero.rotate(Direction.RIGHT);
            }else{
                hero.rotate(Direction.LEFT);
            }
        }else{
            if((!hero.isNearTile(LevelElement.WALL, Direction.RIGHT)) && (!hero.isNearTile(LevelElement.HOLE, Direction.RIGHT)) && (!hero.isNearTile(LevelElement.PIT, Direction.RIGHT))){
                hero.rotate(Direction.RIGHT);
            }
        }
        if(!hero.isNearTile(LevelElement.PIT, Direction.UP) && hero.checkBossViewDirection(Direction.RIGHT)){
            hero.move();
        }
    }else{
        hero.rest();
    }
}
```

