---
title: "Blockly Lösung: Level 5
---
​
## Blöcke
![solution](doc/produs_unterlagen/solution/blockly/img/loesung_level_5.png)

Code:
hero.move();
for(int i = 1; i < 3; i++){
    hero.push();
}
hero.rotate(Direction.RIGHT);
hero.move();
for(int i = 1; i < 4; i++){
    hero.push();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i < 6; i++){
    hero.pull();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i < 3; i++){
    hero.push();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i < 3; i++){
    hero.move();
    hero.rotate(Direction.RIGHT);
}
for(int i = 1; i < 3; i++){
    hero.push();
}
for(int i = 1; i < 3; i++){
    hero.rotate(Direction.LEFT);
    hero.move();
    hero.rotate(Direction.RIGHT);
    hero.move();
}
hero.move();