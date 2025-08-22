---
title: "Blockly Lösung: Level 22"
---

## Blöcke
![solution](.img/loesung_level_022.png)

## Code 

```java
hero.move();
hero.move();
hero.move();
hero.rotate(Direction.LEFT);
while(true){
    if(hero.isNearTile(LevelElement.WALL, Direction.UP)){
        if(hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
            hero.rotate(Direction.LEFT);
            hero.move();
        }else{
            hero.rotate(Direction.RIGHT);
            hero.move();
        }
    }else{
        if(!hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
            hero.rotate(Direction.RIGHT);
        }
        hero.move();
    }
}
```

