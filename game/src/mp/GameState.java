package mp;

import tools.Point;

import java.util.HashMap;

public class GameState {
    private static HashMap<Integer, Point> heroPositionByClientId = new HashMap<>();

    public GameState(){
    }

    public GameState(HashMap<Integer, Point> heroPositionByClientId){
        this.heroPositionByClientId = heroPositionByClientId;
    }

    public static HashMap<Integer, Point> getHeroPositionByClientId(){
        return heroPositionByClientId;
    }
    public void setHeroPositionByClientId(HashMap<Integer, Point> heroPositionByClientId){
        this.heroPositionByClientId = heroPositionByClientId;
    }
}
