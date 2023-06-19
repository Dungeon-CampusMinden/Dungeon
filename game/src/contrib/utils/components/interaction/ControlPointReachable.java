package contrib.utils.components.interaction;

import core.Game;
import core.level.utils.Coordinate;
import core.utils.Point;

import java.util.function.Function;

public class ControlPointReachable implements Function<InteractionData, Boolean> {

    @Override
    public Boolean apply(final InteractionData interactionData) {
        boolean reachable = false;
        boolean pathBlocked = false;
        if ((interactionData.ic().radius() - interactionData.dist()) > 0) {
            reachable = true;
            // check path
            Point dirvec = interactionData.unitDir();
            for (int i = 1; i < interactionData.dist(); i++) {
                if (!Game.tileAT(
                                new Coordinate(
                                        (int) (dirvec.x * i + interactionData.pc().position().x),
                                        (int) (dirvec.y * i + interactionData.pc().position().y)))
                        .isAccessible()) {
                    pathBlocked = true;
                    break;
                }
            }
        }
        return reachable && !pathBlocked;
    }
}
