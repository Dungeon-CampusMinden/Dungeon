while(true) {
    if(hero.checkBossViewDirection(Direction.RIGHT)){
        if(hero.isNearTile(LevelElement.WALL, Direction.UP)){
            if(!hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
                hero.rotate(Direction.RIGHT);
                hero.move();
            }else{
                hero.rotate(Direction.LEFT);
                hero.move();
            }
        }else{
            if(!hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
                hero.rotate(Direction.RIGHT);
            }
            hero.move();
        }
    }else{
        hero.rest();
    }
}