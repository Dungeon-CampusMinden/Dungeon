package level.produs;

import components.BlockComponent;
import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.MissingComponentException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import level.BlocklyLevel;
import level.LevelManagementUtils;

public class Chapter21Level extends BlocklyLevel {
  private static boolean showText = true;

  private Set<LeverComponent> torches = new HashSet<>();
  private DoorTile door;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Chapter21Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 2: Level 1");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusHero();
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.RIGHT);
    LevelManagementUtils.zoomDefault();
    if (showText) {
      DialogUtils.showTextPopup(
          "Endlich raus da, aber wie geht es jetzt weiter?", "Kapitel 2: Flucht");
      showText = false;
    }

    // create torches and light every second one
    final boolean[] coin = {new Random().nextBoolean()};
    customPoints()
        .forEach(
            coordinate -> {
              Entity torch = LeverFactory.createTorch(coordinate.toCenteredPoint());
              torch.add(new BlockComponent());
              Game.add(torch);
              LeverComponent lc =
                  torch
                      .fetch(LeverComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(torch, LeverComponent.class));
              torches.add(lc);
              if (coin[0]) {
                lc.toggle();
                DrawComponent dc =
                    torch
                        .fetch(DrawComponent.class)
                        .orElseThrow(
                            () -> MissingComponentException.build(torch, DrawComponent.class));
                dc.currentAnimation("on");
                coin[0] = false;
              } else coin[0] = true;
            });

    door = (DoorTile) Game.randomTile(LevelElement.DOOR).orElseThrow();
    door.close();
  }

  @Override
  protected void onTick() {
    if (torches.stream().allMatch(t -> t.isOn())) door.open();
    else door.close();
  }
}
