package level.produs.chapter1;

import contrib.hud.DialogUtils;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import level.BlocklyLevel;

import java.util.List;

public class Chapter11Level extends BlocklyLevel {


    /**
     * Call the parent constructor of a tile level with the given layout and design label. Set the
     * start tile of the hero to the given heroPos.
     *
     * @param layout       2D array containing the tile layout.
     * @param designLabel  The design label for the level.
     * @param customPoints The custom points of the level.
     */
    public Chapter11Level(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
        super(layout, designLabel, customPoints, "Chapter11");
    }

    @Override
    protected void onFirstTick() {
        DialogUtils.showTextPopup("Schau! Die Wache hat vergessen die Tür zu verriegeln Zeit für die Flucht. Lauf!", "Kapitel 1: Ausbruch");

    }

    @Override
    protected void onTick() {

    }
}
