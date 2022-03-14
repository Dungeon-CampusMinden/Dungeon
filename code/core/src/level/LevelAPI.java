package level;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.Painter;
import level.elements.Level;
import level.elements.room.Room;
import level.elements.room.Tile;
import level.generator.IGenerator;
import level.generator.dungeong.graphg.NoSolutionException;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import tools.Point;

public class LevelAPI {
    private final SpriteBatch batch;
    private final Painter painter;
    private final IOnLevelLoader onLevelLoader;
    private IGenerator gen;
    private Level currentLevel;

    public LevelAPI(
            SpriteBatch batch, Painter painter, IGenerator gen, IOnLevelLoader onLevelLoader) {
        this.gen = gen;
        this.batch = batch;
        this.painter = painter;
        this.onLevelLoader = onLevelLoader;
    }

    public void loadLevel() throws NoSolutionException {
        currentLevel = gen.getLevel();
        onLevelLoader.onLevelLoad();
    }

    public void loadLevel(int nodes, int edges, DesignLabel designLabel)
            throws NoSolutionException {
        currentLevel = gen.getLevel(nodes, edges, designLabel);
        onLevelLoader.onLevelLoad();
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
                    if (t.getLevelElement() != LevelElement.SKIP)
                        painter.draw(
                                t.getTexture(),
                                new Point(t.getCoordinate().x, t.getCoordinate().y),
                                batch);
                }
    }

    public void setGenerator(IGenerator generator) {
        gen = generator;
    }
}
