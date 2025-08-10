title: "Blockly Lösung: Level 16
---
​
## Blöcke
![solution](doc/produs_unterlagen/solution/blockly/img/loesung_level_17.png)

Code:
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