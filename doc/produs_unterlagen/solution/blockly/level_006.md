---
title: "Blockly Lösung: Level 6"
---

## Blöcke
![solution](./img/loesung_level_006.png)

## Code 

```java
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.push();
}
for(int i = 1; i <= 3; i++){
    hero.rotate(Direction.RIGHT);
    hero.move();
    hero.move();
}
for(int i = 1; i <= 3; i++){
    hero.push();
}
hero.rotate(Direction.RIGHT);
hero.move();
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    for(int x = 1; x <= 9; x++){
    hero.move();
    }
    hero.rotate(Direction.RIGHT);
}
```

