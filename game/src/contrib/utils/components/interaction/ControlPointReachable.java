package contrib.utils.components.interaction;

import core.Game;
import core.utils.position.Point;
import core.utils.position.Position;

import java.util.function.Function;

public class ControlPointReachable implements Function<InteractionData, Boolean> {

    @Override
    public Boolean apply(final InteractionData interactionData) {
        boolean reachable = false;
        boolean pathBlocked = false;
        if ((interactionData.ic().radius() - interactionData.dist()) > 0) {
            reachable = true;
            // check path
            Position dirvec = interactionData.unitDir();
            for (int i = 1; i < interactionData.dist(); i++) {
                if (!Game.tileAT(
                                new Point(
                                        (int)
                                                (dirvec.point().x * i
                                                        + interactionData
                                                                .pc()
                                                                .position()
                                                                .point()
                                                                .x),
                                        (int)
                                                (dirvec.point().y * i
                                                        + interactionData
                                                                .pc()
                                                                .position()
                                                                .point()
                                                                .y)))
                        .isAccessible()) {
                    pathBlocked = true;
                    break;
                }
            }
        }
        return reachable && !pathBlocked;
    }
}
