title: "Blockly Lösung: Level 14
---
​
## Blöcke
![solution](doc/produs_unterlagen/solution/blockly/img/loesung_level_14.png)

Code:
while(hero.isNearTile(LevelElement.FLOOR, Direction.HERE)){
    if(hero.isNearTile(LevelElement.WALL, Direction.UP)){
        if(hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
            hero.rotate(Direction.LEFT);
        }
        if(hero.isNearTile(LevelElement.WALL, Direction.LEFT)){
            hero.rotate(Direction.RIGHT);
        }
    }else{
        hero.move();
    }
}

Ohne Direction.HERE:
while(true){
    if(hero.isNearTile(LevelElement.WALL, Direction.UP)){
        if(hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
            hero.rotate(Direction.LEFT);
        }
        if(hero.isNearTile(LevelElement.WALL, Direction.LEFT)){
            hero.rotate(Direction.RIGHT);
        }
    }else{
        hero.move();
    }
}