title: "Blockly Lösung: Level 16
---
​
## Blöcke
![solution](doc/produs_unterlagen/solution/blockly/img/loesung_level_20.png)

Code:
while(true){
    if(hero.checkBossViewDirection(Direction.RIGHT)){
        if(hero.isNearTile(LevelElement.WALL, Direction.UP) || hero.isNearTile(LevelElement.HOLE, Direction.UP)){
            if((!hero.isNearTile(LevelElement.WALL, Direction.RIGHT)) && (!hero.isNearTile(LevelElement.HOLE, Direction.RIGHT))){
                hero.rotate(Direction.RIGHT);
                hero.move();
            }else{
                hero.rotate(Direction.LEFT);
                hero.move();
            }
        }else{
            if((!hero.isNearTile(LevelElement.WALL, Direction.RIGHT)) && (!hero.isNearTile(LevelElement.HOLE, Direction.RIGHT))){
                hero.rotate(Direction.RIGHT);
            }
            hero.move();
        }
    }else{
        hero.rest();
    }
}