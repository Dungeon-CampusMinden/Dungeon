hero.rotate(Direction.RIGHT);
while(!hero.isNearTile(LevelElement.WALL, Direction.UP)){
    if(hero.checkBossViewDirection(Direction.RIGHT)){
        hero.rest();
    }else{
        hero.move();
    }
}
hero.rotate(Direction.LEFT);
while(!hero.isNearTile(LevelElement.WALL, Direction.UP)){
    if(hero.checkBossViewDirection(Direction.RIGHT)){
        hero.rest();
    }else{
        hero.move();
    }
}
hero.rotate(Direction.LEFT);
while(hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
    if(hero.checkBossViewDirection(Direction.RIGHT)){
        hero.rest();
    }else{
        hero.move();
    }
}
if(hero.checkBossViewDirection(Direction.RIGHT)){
        hero.rest();
    }else{
        hero.move();
    }
hero.rotate(Direction.RIGHT);
while(!hero.isNearTile(LevelElement.PIT, Direction.RIGHT)){
    if(hero.checkBossViewDirection(Direction.RIGHT)){
        hero.rest();
    }else{
        hero.move();
    }
}
hero.rotate(Direction.RIGHT);