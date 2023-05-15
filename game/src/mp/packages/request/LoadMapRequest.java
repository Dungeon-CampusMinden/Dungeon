package mp.packages.request;


import core.level.elements.ILevel;
import core.utils.Point;

public class LoadMapRequest {
    private final ILevel level;
    private final Point heroInitialPosition;

    public LoadMapRequest(final ILevel currentLevel){
        this.level = currentLevel;
        this.heroInitialPosition = null;
    }

    public LoadMapRequest(final ILevel currentLevel, final Point heroInitialPosition){
        this.level = currentLevel;
        this.heroInitialPosition = heroInitialPosition;
    }

    public ILevel getLevel() {
        return this.level;
    }

    public Point getHeroInitialPosition() {
        return heroInitialPosition;
    }
}
