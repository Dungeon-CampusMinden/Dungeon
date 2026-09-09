package rooms.systemRecovery.riddles;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.path.SimpleIPath;
import feature.entities.LeverFactory;
import feature.entities.WorldItemBuilder;
import feature.utils.ICommand;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.items.BatteryItem;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/**
 * Riddle 1: materialize energy containers and unlock the one-shot battery reward.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class EnergyRiddle {
  private final DungeonLevel level;

  private boolean energyPuzzleSolved = false;
  private boolean batterySpawned = false;

  /** Creates the riddle for the owning level. */
  public EnergyRiddle(DungeonLevel level) {
    this.level = level;
  }

  /** Spawns the battery lever and the door power socket. */
  public void setup() {
    Entity arrayLever =
        LeverFactory.createLever(
            level.getPoint("array_lever"),
            new ICommand() {
              @Override
              public void execute() {
                if (!energyPuzzleSolved || batterySpawned) {
                  return;
                }

                batterySpawned = true;

                Game.add(
                    WorldItemBuilder.buildWorldItem(
                        new BatteryItem(), level.getPoint("array_item_spawn")));
              }

              @Override
              public void undo() {}
            });
    Game.add(arrayLever);
    Game.add(
        EntityFactory.batteryBox(
            level.getPoint("batteriebox_modul"),
            () ->
                ((DoorTile) Game.tileAt(level.getPoint("door_modulspeicher")).orElseThrow())
                    .open()));
  }

  /** Enables the array lever after the energy puzzle has been solved. */
  public void completeEnergyPuzzle() {
    energyPuzzleSolved = true;
    markEnergyCratesCorrect();
  }

  /** Materializes one empty container per array element after terminal step 1. */
  public void spawnEnergyCrates() {
    for (int index = 0; index < 5; index++) {
      Game.add(EntityFactory.cryoBox(level.getPoint("a" + index), false));
    }
  }

  /** Applies local rendering feedback; headless servers never load shader textures. */
  private void markEnergyCratesCorrect() {
    if (Game.isHeadless()) return;
    String[] values = {
      TerminalInterpreterSetup.ENERGIE_VALUE_0,
      TerminalInterpreterSetup.ENERGIE_VALUE_1,
      TerminalInterpreterSetup.ENERGIE_VALUE_2,
      TerminalInterpreterSetup.ENERGIE_VALUE_3,
      TerminalInterpreterSetup.ENERGIE_VALUE_4
    };
    for (int index = 0; index < values.length; index++) {
      float fill = Integer.parseInt(values[index]) / 100f;
      Game.entityAtPoint(level.getPoint("a" + index))
          .findFirst()
          .flatMap(entity -> entity.fetch(DrawComponent.class))
          .ifPresent(
              draw ->
                  draw.shaders()
                      .add(
                          "energieShader",
                          new EnergyFillShader(
                                  fill,
                                  Color.BLUE,
                                  TextureMap.instance()
                                      .textureAt(new SimpleIPath("objects/tech/CryoBox.png")))
                              .animMagnitude(0)));
    }
  }
}
