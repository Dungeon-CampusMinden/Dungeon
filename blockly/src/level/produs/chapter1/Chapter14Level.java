package level.produs.chapter1;

import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import level.BlocklyLevel;

import java.util.List;

public class Chapter14Level extends BlocklyLevel {


    /**
     * Call the parent constructor of a tile level with the given layout and design label. Set the
     * start tile of the hero to the given heroPos.
     *
     * @param layout       2D array containing the tile layout.
     * @param designLabel  The design label for the level.
     * @param customPoints The custom points of the level.
     */
    public Chapter14Level(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
        super(layout, designLabel, customPoints, "Chapter14");
    }

    @Override
    protected void onFirstTick() {

    }

    @Override
    protected void onTick() {

    }
}
