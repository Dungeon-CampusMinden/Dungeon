while(true) {
    if(hero.checkBossViewDirection(Direction.RIGHT)){
        if(hero.isNearTile(LevelElement.WALL, Direction.UP)){
            while(!hero.isNearTile(LevelElement.WALL, Direction.RIGHT)){
                if(hero.checkBossViewDirection(Direction.RIGHT)){
                    hero.rotate(Direction.RIGHT);
                }else{
                    hero.rest();
                }
                if(hero.checkBossViewDirection(Direction.RIGHT)){
                    hero.move();
                }else{
                    hero.rest();
                }
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