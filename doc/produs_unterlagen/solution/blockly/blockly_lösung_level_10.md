title: "Blockly Lösung: Level 10
---
​
## Blöcke
![solution](doc/produs_unterlagen/solution/blockly/img/loesung_level_10.1.png)
![solution](doc/produs_unterlagen/solution/blockly/img/loesung_level_10.2.png)

Code:
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.pickup();
    hero.move();
}
for(int i = 1; i <= 2; i++){
    hero.rotate(Direction.LEFT);
}
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.shootFireball();
hero.rotate(Direction.LEFT);
hero.shootFireball();
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.shootFireball();
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.move();
}
hero.pickup();
hero.rotate(Direction.RIGHT);
hero.shootFireball();
hero.rotate(Direction.LEFT);
hero.move();
hero.pickup();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 4; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 5; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
hero.shootFireball();
hero.rotate(Direction.LEFT);
hero.move();
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 12; i++){
    hero.move();
}
hero.rotate(Direction.RIGHT);
for(int i = 1; i <= 2; i++){
    hero.move();
}
hero.rotate(Direction.LEFT);
for(int i = 1; i <= 3; i++){
    hero.move();
}