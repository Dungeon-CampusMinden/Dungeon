package mp.packages.request;

import level.elements.ILevel;
import tools.Point;

import java.util.Optional;

public class InitializeServerRequest {
    private final ILevel level;
    private final Point heroInitialPosition;

    public InitializeServerRequest(final ILevel currentLevel){
        this.level = currentLevel;
        this.heroInitialPosition = null;
    }

    public InitializeServerRequest(final ILevel currentLevel, final Point heroInitialPosition){
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
