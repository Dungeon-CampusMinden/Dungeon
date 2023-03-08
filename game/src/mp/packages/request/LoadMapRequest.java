package mp.packages.request;

import level.elements.ILevel;

public class LoadMapRequest {
    ILevel currentLevel;

    public LoadMapRequest(){}

    public LoadMapRequest(ILevel currentLevel){
        this.currentLevel = currentLevel;
    }

    public ILevel getCurrentLevel(){
        return currentLevel;
    }
}
