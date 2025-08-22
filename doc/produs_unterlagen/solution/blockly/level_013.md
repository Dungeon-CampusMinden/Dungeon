---
title: "Blockly Lösung: Level 13"
---

## Blöcke
![solution](.img/loesung_level_013.png)

## Code 

```java
while(hero.isNearTile(LevelElement.FLOOR, Direction.HERE)){
    if(hero.isNearTile(LevelElement.WALL, Direction.UP)){
        hero.rotate(Direction.RIGHT);
    }else{
        hero.move();
    }
}

Ohne Direction.HERE:
while(true){
    if(hero.isNearTile(LevelElement.WALL, Direction.UP)){
        hero.rotate(Direction.RIGHT);
    }else{
        hero.move();
    }
}
```

