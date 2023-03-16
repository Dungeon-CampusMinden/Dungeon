package mp.packages.request;

import level.elements.ILevel;
import java.io.Serializable;

public class LoadMapRequest implements Serializable {
    private ILevel level;

    public LoadMapRequest(){}

    public LoadMapRequest(ILevel currentLevel){
        this.level = currentLevel;
    }

    public ILevel getLevel() {
        return this.level;
    }
}
