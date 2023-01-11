package level;

import basiselements.DungeonElement;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;
import controller.Game;
import graphic.DungeonCamera;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import level.elements.tile.Tile;
import level.tools.Coordinate;
import level.tools.LevelElement;
import tools.Constants;
import tools.Point;

/**
 * The level editor holds information about the tools, used to modify the level
 *
 * @author Maxim Fruendt
 */
public class LevelEditor implements InputProcessor {

    /** List of level elements that can be spawned in the editor menu */
    protected static final List<LevelElement> spawnableLevelElements = new ArrayList<>();
    /** List of the names of spawnable level elements */
    protected static final List<String> spawnableLevelElementNames = new ArrayList<>();
    /** List of objects that can be spawned in the editor menu */
    protected static final List<Class<? extends DungeonElement>> spawnableObjects =
            new ArrayList<>();
    /** List of the names of spawnable objects */
    protected static final List<String> spawnableObjectNames = new ArrayList<>();

    /** Elements which can be placed */
    public enum EditorElement {
        /** Elements that make up the level */
        LEVEL_ELEMENT,
        /** Spawnable objects, which will be added to the entity controller */
        OBJECT
    }

    /** The currently selected level element, which will be placed */
    protected EditorElement brush;
    /** Index of the level element that should be spawned */
    protected int levelElementIndex = 0;
    /** Index of the object that should be spawned */
    protected int objectIndex = 0;
    /** Flag if the free cam should be enabled */
    protected boolean enableFreeCam = false;
    /** API of the level which will be edited */
    protected LevelAPI levelAPI;
    /** Game in which this level editor is used */
    protected Game game;
    /** Current camera of the game, used to translate coordinates */
    protected DungeonCamera camera;
    /** Method that is used to add a new object to the dungeon */
    Function<DungeonElement, Boolean> addObjectToDungeon;

    /**
     * Create a new level editor
     *
     * @param levelAPI API used to modify the level
     * @param game Game in which the level editor is used
     * @param camera Camera of the dungeon, used to translate coordinates
     * @param addObjectToDungeon Adder that is used to spawn objects in the dungeon
     */
    public LevelEditor(
            LevelAPI levelAPI,
            Game game,
            DungeonCamera camera,
            Function<DungeonElement, Boolean> addObjectToDungeon) {
        this.levelAPI = levelAPI;
        this.game = game;
        this.camera = camera;
        this.addObjectToDungeon = addObjectToDungeon;
    }

    /**
     * Set the currently selected brush
     *
     * @param element New element for the brush
     */
    public void setBrush(EditorElement element) {
        brush = element;
    }

    /**
     * Set the index of the selected spawnable level element
     *
     * @param index New index
     */
    public void setLevelElementIndex(int index) {
        levelElementIndex = index;
    }

    /**
     * Set the index of the selected spawnable object
     *
     * @param index New index
     */
    public void setObjectIndex(int index) {
        objectIndex = index;
    }

    /**
     * Enable or disable the free cam
     *
     * @param enableFreeCam true if free cam should be enabled.
     */
    public void setFreeCam(boolean enableFreeCam) {
        this.enableFreeCam = enableFreeCam;
    }

    /**
     * Get the display names of spawnable level elements
     *
     * @return List of names of spawnable level elements
     */
    public List<String> getSpawnableLevelElementNames() {
        return spawnableLevelElementNames;
    }

    /**
     * Get the display names of spawnable objects
     *
     * @return List of names of spawnable objects
     */
    public List<String> getSpawnableObjectNames() {
        return spawnableObjectNames;
    }

    /** Called by the GUI when the game should be paused */
    public void pauseGame() {
        game.pause();
    }

    /** Called by the GUI when the game should be started */
    public void startGame() {
        game.resume();
    }

    /**
     * Spawns an object at the desired coordinates
     *
     * @param element Element type that should be spawned
     * @param position at which the object will be spawned
     */
    protected void spawnObjectAt(EditorElement element, Point position) {
        try {
            if (element == EditorElement.OBJECT) {
                if (objectIndex >= 0 && objectIndex < spawnableObjects.size()) {
                    DungeonElement object =
                            spawnableObjects
                                    .get(objectIndex)
                                    .getDeclaredConstructor()
                                    .newInstance();
                    object.setPosition(position);
                    addObjectToDungeon.apply(object);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Callback that will be invoked when the user holds down a key
     *
     * @param keycode Code of the hold key
     * @return True if event was handled, else false
     */
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    /**
     * Callback that will be invoked when the user has released a key
     *
     * @param keycode Code of the released key
     * @return True if event was handled, else false
     */
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    /**
     * Callback that will be invoked when the user has pressed a key
     *
     * @param character Typed character
     * @return True if event was handled, else false
     */
    @Override
    public boolean keyTyped(char character) {
        if (enableFreeCam) {
            if (character == Constants.LEVEL_EDITOR_FREE_CAM_FORWARDS) {
                Point newPos = camera.getFocusPoint();
                newPos.y += Constants.LEVEL_EDITOR_FREE_CAM_SPEED;
                camera.setFocusPoint(newPos);
                return true;
            } else if (character == Constants.LEVEL_EDITOR_FREE_CAM_BACKWARDS) {
                Point newPos = camera.getFocusPoint();
                newPos.y -= Constants.LEVEL_EDITOR_FREE_CAM_SPEED;
                camera.setFocusPoint(newPos);
                return true;
            } else if (character == Constants.LEVEL_EDITOR_FREE_CAM_RIGHT) {
                Point newPos = camera.getFocusPoint();
                newPos.x += Constants.LEVEL_EDITOR_FREE_CAM_SPEED;
                camera.setFocusPoint(newPos);
                return true;
            } else if (character == Constants.LEVEL_EDITOR_FREE_CAM_LEFT) {
                Point newPos = camera.getFocusPoint();
                newPos.x -= Constants.LEVEL_EDITOR_FREE_CAM_SPEED;
                camera.setFocusPoint(newPos);
                return true;
            }
        }
        return false;
    }

    /**
     * Callback that will be invoked when the user has pressed their mouse button or finger
     *
     * @param screenX X coordinate of the clicked position
     * @param screenY Y coordinate of the clicked position
     * @param pointer ID of the used pointer
     * @param button ID of the used button
     * @return True if event was handled, else false
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /**
     * Callback that will be invoked when the user has released their mouse button or finger
     *
     * @param screenX X coordinate of the clicked position
     * @param screenY Y coordinate of the clicked position
     * @param pointer ID of the used pointer
     * @param button ID of the used button
     * @return True if event was handled, else false
     */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // If no brush is selected or we have no level to edit or the camera input is missing, leave
        if (brush == null || levelAPI == null || camera == null) {
            return false;
        }

        // Get the clicked tile
        Vector3 pos = camera.unproject(new Vector3(screenX, screenY, 0));
        Tile tile =
                levelAPI.getCurrentLevel()
                        .getTileAt(new Coordinate((int) (pos.x + 1f), (int) (pos.y + .5f)));

        // Check if the clicked tile actually exists
        if (tile == null) {
            return false;
        }

        switch (brush) {
            case OBJECT -> {
                // Only spawn object if a floor tile was clicked
                if (tile.getLevelElement() == LevelElement.FLOOR) {
                    spawnObjectAt(EditorElement.OBJECT, new Point(pos.x, pos.y));
                } else {
                    return false;
                }
            }
            case LEVEL_ELEMENT -> levelAPI.getCurrentLevel()
                    .changeTileElementType(tile, spawnableLevelElements.get(levelElementIndex));
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Callback that will be invoked when the user has dragged their fingers
     *
     * @param screenX New X coordinate of the finger
     * @param screenY New Y coordinate of the finger
     * @param pointer ID of the used pointer
     * @return True if event was handled, else false
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    /**
     * Callback that will be invoked when the user has moved the mouse
     *
     * @param screenX New X coordinate of the mouse
     * @param screenY New Y coordinate of the mouse
     * @return True if event was handled, else false
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    /**
     * Callback that will be invoked when the user scrolled
     *
     * @param amountX Scrolled amount in X direction
     * @param amountY Scrolled amount in Y direction
     * @return True if event was handled, else false
     */
    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    /**
     * Register a spawnable level element in the level editor menu
     *
     * @param object Spawnable level element that will be added to the spawn menu
     * @param name Display name of the level element
     */
    public static void addSpawnableLevelElement(LevelElement object, String name) {
        if (!spawnableLevelElements.contains(object)) {
            spawnableLevelElements.add(object);
            spawnableLevelElementNames.add(name);
        }
    }

    /**
     * Register a spawnable object in the level editor menu
     *
     * @param object Spawnable object that will be added to the spawn menu
     * @param name Display name of the object
     * @param <T> Type of the object
     */
    public static <T extends DungeonElement> void addSpawnableObject(Class<T> object, String name) {
        if (!spawnableObjects.contains(object)) {
            spawnableObjects.add(object);
            spawnableObjectNames.add(name);
        }
    }
}
