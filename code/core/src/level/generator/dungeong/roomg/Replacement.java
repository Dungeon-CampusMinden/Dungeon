package level.generator.dungeong.roomg;

import level.tools.DesignLabel;
import level.tools.LevelElement;

/**
 * can be used to replace placeholder in a room layout
 *
 * @author Andre Matutat
 */
public class Replacement {

    private LevelElement[][] layout;
    private DesignLabel label;
    private boolean rotate;

    /**
     * @param layout of the replacer, use placeholder in fields that should not be replaced
     * @param rotate can the layout be rotated?
     * @param label DesignLabel of the replacer
     */
    public Replacement(LevelElement[][] layout, boolean rotate, DesignLabel label) {
        setLayout(layout);
        this.rotate = rotate;
        this.label = label;
    }

    public LevelElement[][] getLayout() {
        // copy of the layout
        return copyLayout(layout);
    }

    public void setLayout(LevelElement[][] layout) {
        this.layout = copyLayout(layout);
    }

    private LevelElement[][] copyLayout(LevelElement[][] toCopy) {
        LevelElement[][] copy = new LevelElement[layout.length][layout[0].length];
        for (int y = 0; y < toCopy.length; y++)
            for (int x = 0; x < toCopy[0].length; x++) {
                copy[y][x] = toCopy[y][x];
            }
        return copy;
    }

    public DesignLabel getDesign() {
        return label;
    }

    public boolean canRotate() {
        return rotate;
    }
}
