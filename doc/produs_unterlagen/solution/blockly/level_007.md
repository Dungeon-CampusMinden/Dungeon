---
title: "Blockly Lösung: Level 7"
---

## Blöcke
![solution](./img/loesung_level_007.png)

## Code 

```java
hero.move();
hero.move();
hero.rotate(Direction.RIGHT);
hero.move();
hero.move();
hero.interact(Direction.HERE);
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 7; i++){
    hero.move();
}
hero.interact(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 13; i++){
    hero.move();
}
hero.interact(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 6; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.interact(Direction.LEFT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 11; i++){
    hero.move();
}

Ohne Direction.HERE:
hero.move();
hero.move();
hero.rotate(Direction.RIGHT);
hero.move();
hero.interact(Direction.UP);
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 7; i++){
    hero.move();
}
hero.interact(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 13; i++){
    hero.move();
}
hero.interact(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 6; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.interact(Direction.LEFT);
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i <= 11; i++){
    hero.move();
}
```

