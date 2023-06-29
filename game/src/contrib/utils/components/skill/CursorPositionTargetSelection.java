package contrib.utils.components.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import core.Game;
import core.utils.Point;

public class CursorPositionTargetSelection implements ITargetSelection{
    @Override
    public Point selectTargetPoint() {
        Vector3 mousePosition =
            Game.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        return new Point(mousePosition.x, mousePosition.y);
    }
}
