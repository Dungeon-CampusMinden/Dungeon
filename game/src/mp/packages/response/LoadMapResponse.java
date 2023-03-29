package mp.packages.response;

import level.elements.ILevel;

public class LoadMapResponse {
    private ILevel level;

    public LoadMapResponse(ILevel currentLevel){
        this.level = currentLevel;
    }

    public ILevel getLevel() {
        return this.level;
    }
}
