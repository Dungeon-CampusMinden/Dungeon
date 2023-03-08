package mp.packages.request;

import level.elements.ILevel;

public class LoadMapRequest {
    static ILevel currentLevel;

    public LoadMapRequest(){}

    public LoadMapRequest(ILevel currentLevel){
        this.currentLevel = currentLevel;
    }

    static public ILevel getCurrentLevel(){
        return currentLevel;
    }
}
