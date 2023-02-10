package basiselements;

import tools.Point;

/**
 * A dungeon element that is used to visualize a <code>ThreadedDungeonElement</code>
 *
 * @author Maxim Fruendt
 */
public class ThreadedFakeDungeonElement extends DungeonElement {

    /** Visual information of this fake element. Also used as sync lock */
    private final VisualInformation visualInformation;

    /** Create a new fake element for a threaded element */
    public ThreadedFakeDungeonElement(String defaultTexture, Point spawnPosition) {
        visualInformation = new VisualInformation();
        visualInformation.texturePath = defaultTexture;
        visualInformation.position = spawnPosition;
    }

    /** Update this element */
    @Override
    public void update() {
        // Do nothing, logic is handled in ThreadedDungeonElement
    }

    /**
     * Update the texture of this fake element
     *
     * @param texturePath New texture
     */
    public void updateTexture(String texturePath) {
        synchronized (visualInformation) {
            visualInformation.texturePath = texturePath;
        }
    }

    /**
     * Get the position of this fake element
     *
     * @return Position
     */
    @Override
    public Point getPosition() {
        synchronized (visualInformation) {
            return visualInformation.position;
        }
    }

    /**
     * Update the position of this fake element
     *
     * @param position New position
     */
    public void setPosition(Point position) {
        synchronized (visualInformation) {
            visualInformation.position = position;
        }
    }

    /**
     * Get the texture of this fake element
     *
     * @return Texture
     */
    @Override
    public String getTexturePath() {
        synchronized (visualInformation) {
            return visualInformation.texturePath;
        }
    }

    /** Object holding the information that is needed to display this fake element */
    public static class VisualInformation {
        /** Path of the active texture */
        String texturePath;
        /** Current position in the level */
        Point position;
    }
}
