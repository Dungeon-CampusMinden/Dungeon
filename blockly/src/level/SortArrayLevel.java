package level;

import contrib.components.InteractionComponent;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import entities.MiscFactory;

import java.util.List;

public class SortArrayLevel extends BlocklyLevel {
  private static boolean showText = true;

  public SortArrayLevel(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Array-Sortierung");
  }

  @Override
  protected void onFirstTick() {
    if (showText) {
      DialogUtils.showTextPopup(
        "SortArrayLevel",
        "Array-Aufgabe"
      );
      showText = false;
    }


  }

  @Override
  protected void onTick() {

  }


}
