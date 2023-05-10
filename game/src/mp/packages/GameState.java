package mp.packages;

import tools.Point;

import java.util.HashMap;

public class GameState {
    private static HashMap<Integer, Point> heroPositionByClientId = new HashMap<>();

    public GameState(){
    }

    public GameState(final HashMap<Integer, Point> heroPositionByClientId){
        this.heroPositionByClientId = heroPositionByClientId;
    }

    public HashMap<Integer, Point> getHeroPositionByClientId(){
        return heroPositionByClientId;
    }
    public void setHeroPositionByClientId(HashMap<Integer, Point> heroPositionByClientId){
        this.heroPositionByClientId = heroPositionByClientId;
    }
}
