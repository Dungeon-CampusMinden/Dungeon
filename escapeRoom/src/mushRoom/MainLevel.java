package mushRoom;

import contrib.modules.levelHide.LevelHideFactory;
import core.Game;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.systems.DrawSystem;
import core.utils.Point;
import core.utils.Rectangle;
import core.utils.components.draw.shader.ColorGradeShader;
import java.util.*;

/** The MushRoom. */
public class MainLevel extends DungeonLevel {

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public MainLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Demo");
  }

  @Override
  protected void onFirstTick() {
    DrawSystem ds = (DrawSystem) Game.systems().get(DrawSystem.class);

    float width = 46, height = 33;
    ds.levelShaders()
        .add("a1", new ColorGradeShader(0.2f, 1, 1).region(new Rectangle(width, height, 0, 0)));
    ds.levelShaders()
        .add(
            "a2",
            new ColorGradeShader(0.1f, 1, 1).region(new Rectangle(width, height, width + 8, 0)));
    ds.levelShaders()
        .add(
            "a3",
            new ColorGradeShader(0.3f, 1, 1).region(new Rectangle(width, height, 0, height + 8)));
    ds.levelShaders()
        .add(
            "a4",
            new ColorGradeShader(0.5f, 0.1f, 0.6f)
                .region(new Rectangle(width, height, width + 8, height + 8)));

    Game.add(LevelHideFactory.createLevelHide(getPoint("cave-1-start"), getPoint("cave-1-end")));
  }

  @Override
  protected void onTick() {}
}
