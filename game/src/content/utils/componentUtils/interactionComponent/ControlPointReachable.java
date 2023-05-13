package content.utils.componentUtils.interactionComponent;

import api.Game;
import api.level.utils.Coordinate;
import api.utils.Point;

public class ControlPointReachable implements IReachable {

    @Override
    public boolean checkReachable(InteractionData interactionData) {
        boolean reachable = false;
        boolean pathBlocked = false;
        if ((interactionData.ic().getRadius() - interactionData.dist()) > 0) {
            reachable = true;
            // check path
            Point dirvec = interactionData.unitDir();
            for (int i = 1; i < interactionData.dist(); i++) {
                if (!Game.currentLevel
                        .getTileAt(
                                new Coordinate(
                                        (int) (dirvec.x * i + interactionData.pc().getPosition().x),
                                        (int)
                                                (dirvec.y * i
                                                        + interactionData.pc().getPosition().y)))
                        .isAccessible()) {
                    pathBlocked = true;
                    break;
                }
            }
        }
        return reachable && !pathBlocked;
    }
}
