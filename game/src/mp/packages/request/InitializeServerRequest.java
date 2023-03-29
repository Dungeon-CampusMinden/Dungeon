package mp.packages.request;

import level.elements.ILevel;

import java.io.*;

public class InitializeServerRequest implements Serializable {
    private final ILevel level;

    public InitializeServerRequest(ILevel currentLevel){
        this.level = currentLevel;
    }

    public ILevel getLevel() {
        return this.level;
    }
}
