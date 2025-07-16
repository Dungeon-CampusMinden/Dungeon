title: "Blockly Lösung: Level 16
---
​
## Blöcke
![solution](doc/produs_unterlagen/solution/blockly/img/loesung_level_18.png)

Code:
while(true){
    if(hero.isNearComponent(AIComponent.class, Direction.UP)){
        if(hero.isNearTile(LevelElement.WALL, Direction.RIGHT) && hero.isNearTile(LevelElement.WALL, Direction.LEFT)){
            hero.shootFireball();
        }else{
            if(hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
                hero.rotate(Direction.LEFT);
                hero.move();
            }else{
                hero.rotate(Direction.RIGHT);
                hero.move();
            }
        }
    }
    if(hero.isNearTile(LevelElement.WALL, Direction.UP)){
        if(hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
            hero.rotate(Direction.LEFT);
            hero.move();
        }else{
            hero.rotate(Direction.RIGHT);
            hero.move();
        }
    }else{
        if(!hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
            hero.rotate(Direction.RIGHT);
        }
        hero.move();
    }
}