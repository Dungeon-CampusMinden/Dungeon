---
title: "Blockly Lösung: Level 17"
---

## Blöcke
![solution](.img/loesung_level_017.png)

## Code 

```java
hero.move();
hero.move();
hero.move();
while(true){
    if(hero.isNearTile(LevelElement.FLOOR, Direction.RIGHT)){
        hero.rotate(Direction.RIGHT);
        hero.move();
    }else if(hero.isNearTile(LevelElement.FLOOR, Direction.UP)){
        hero.move();
    }else if(hero.isNearTile(LevelElement.FLOOR, Direction.LEFT)){
        hero.rotate(Direction.LEFT);
        hero.move();
    }else if(hero.isNearTile(LevelElement.EXIT, Direction.UP)){
        hero.move();
    }else{
        hero.rotate(Direction.LEFT);
        hero.rotate(Direction.LEFT);
        hero.move();
    }
}
```

