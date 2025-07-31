---
title: "Blockly Lösung: Level 4
---
​
## Blöcke
![solution](doc/produs_unterlagen/solution/blockly/img/loesung_level_4.png)

Code:
for(int i = 1; i < 7; i++){
    hero.move();
}
hero.interact(Direction.HERE);
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 4; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 4; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i < 4; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
hero.move();
hero.rotate(Direction.LEFT);
for(int i = 1; i < 6; i++){
    hero.move();
}

Ohne Direction.HERE:
for(int i = 1; i < 6; i++){
    hero.move();
}
hero.interact(Direction.UP);
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 5; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i < 4; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i < 4; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
hero.move();
hero.rotate(Direction.LEFT);
for(int i = 1; i < 6; i++){
    hero.move();
}