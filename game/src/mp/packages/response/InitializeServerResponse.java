package mp.packages.response;

import tools.Point;

import static java.util.Objects.requireNonNull;

public class InitializeServerResponse {

    private final boolean isSucceed;
    private final Point initialHeroPosition;

    public InitializeServerResponse(final boolean isSucceed, final Point initialHeroPosition) {
        requireNonNull(initialHeroPosition);
        this.isSucceed = isSucceed;
        this.initialHeroPosition = initialHeroPosition;
    }

    public boolean getIsSucceed() {
        return isSucceed;
    }

    public Point getInitialHeroPosition() {
        return initialHeroPosition;
    }
}
