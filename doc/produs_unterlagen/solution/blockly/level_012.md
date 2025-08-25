---
title: "Blockly Lösung: Level 12"
---

## Blöcke
![solution](./img/loesung_level_012.png)

## Code 

```java
hero.rotate(Direction.LEFT);
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 5; i++){
    if(hero.active(Direction.LEFT)){
        for(int x = 1; x <= 2; x++){
            hero.move();
        }
    }else{
        hero.interact(Direction.LEFT);
        for(int x = 1; x <= 2; x++){
            hero.move();
        }
    }
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 5; i++){
    if(hero.active(Direction.LEFT)){
        for(int x = 1; x <= 2; x++){
            hero.move();
        }
    }else{
        hero.interact(Direction.LEFT);
        for(int x = 1; x <= 2; x++){
            hero.move();
        }
    }
}
hero.rotate(Direction.RIGHT);
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 11; i++){
    hero.move();
}
```

