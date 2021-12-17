package level;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.Painter;
import level.elements.Level;
import level.elements.Room;
import level.elements.Tile;
import level.generator.IGenerator;

public class LevelAPI {
    private Level currentLevel;
    private SpriteBatch batch;
    private Painter painter;
    private IGenerator gen;

    public LevelAPI(SpriteBatch batch, Painter painter, IGenerator gen) {
        this.gen = gen;
        this.batch = batch;
        this.painter = painter;
    }

    public void loadLevel() {
        currentLevel = gen.getLevel();
        // currentLevel = createDummyLevel();
    }

    public void update() {
        drawLevel();
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    private void drawLevel() {
        for (Room r : getCurrentLevel().getRooms())
            for (int y = 0; y < r.getLayout().length; y++)
                for (int x = 0; x < r.getLayout()[0].length; x++) {
                    Tile t = r.getLayout()[y][x];
                    painter.draw(t.getTexture(), t.getGlobalPosition(), batch);
                }
    }
}
