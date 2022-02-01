package level.generator.dungeong.roomg;

import level.tools.DesignLabel;
import level.tools.LevelElement;

/**
 * Will be used to replace placeholder in a room layout.
 *
 * @author Andre Matutat
 */
public class Replacement {

    private final DesignLabel label;
    private final boolean rotate;
    private LevelElement[][] layout;

    /**
     * @param layout Layout of the replacer, use placeholder in fields that should not be replaced
     * @param rotate Can the layout be rotated?
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
        LevelElement[][] copy = new LevelElement[toCopy.length][toCopy[0].length];
        for (int y = 0; y < toCopy.length; y++)
            System.arraycopy(toCopy[y], 0, copy[y], 0, toCopy[0].length);
        return copy;
    }

    public DesignLabel getDesign() {
        return label;
    }

    public boolean canRotate() {
        return rotate;
    }
}
