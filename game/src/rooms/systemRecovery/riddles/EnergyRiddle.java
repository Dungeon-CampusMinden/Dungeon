package rooms.systemRecovery.riddles;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.utils.components.draw.shader.EnergyFillShader;
import feature.entities.LeverFactory;
import feature.entities.WorldItemBuilder;
import feature.shader.ShaderComponent;
import feature.utils.ICommand;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.items.BatteryItem;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.riddles.support.RiddleCallbacks;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/**
 * Riddle 1: materialize energy containers and unlock the one-shot battery reward.
 *
 * <p>Owned by one level instance. Gameplay and scheduled actions run on the authoritative server;
 * clients receive entity state through the existing snapshot protocol.
 */
public final class EnergyRiddle {
  private final DungeonLevel level;
  private final RiddleCallbacks callbacks;

  private boolean energyPuzzleSolved = false;
  private boolean batterySpawned = false;
  private boolean batteryInserted = false;
  private Entity energyDisplay;
  private String energyDisplayText;

  /** Creates the riddle for the owning level. */
  public EnergyRiddle(DungeonLevel level) {
    this(level, RiddleCallbacks.noop());
  }

  /** Creates the riddle with callbacks for physical success and failure events. */
  public EnergyRiddle(DungeonLevel level, RiddleCallbacks callbacks) {
    this.level = level;
    this.callbacks = callbacks;
    this.energyDisplayText = SystemRecoveryText.text("world.energy.display-values");
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
                  callbacks.failure("pull", -1);
                  return;
                }

                batterySpawned = true;
                callbacks.success("pull", -1);

                Game.add(
                    WorldItemBuilder.buildWorldItem(
                        new BatteryItem(), level.getPoint("array_item_spawn")));
              }

              @Override
              public void undo() {}
            });
    Game.add(arrayLever);
    energyDisplay =
        EntityFactory.hintDisplay(
            level.getPoint("display_energie"),
            () -> energyDisplayText,
            SystemRecoveryText.text("world.energy.title"));
    energyDisplay.name("energy_display");
    Game.add(energyDisplay);
    Game.add(
        EntityFactory.batteryBox(
            level.getPoint("batteriebox_modul"),
            () -> {
              if (batteryInserted) return;
              batteryInserted = true;
              callbacks.success("battery-inserted", -1);
              SystemRecoveryLevel.showModuleAssignments();
              ((DoorTile) Game.tileAt(level.getPoint("door_modulspeicher")).orElseThrow()).open();
              SystemRecoveryLevel.announceStoryToAllPlayers(
                  SystemRecoveryStoryDialogs.MODULE_ARRAY);
            }));
  }

  /** Enables the array lever after the energy puzzle has been solved. */
  public void completeEnergyPuzzle() {
    if (energyPuzzleSolved) return;
    energyPuzzleSolved = true;
    callbacks.solved();
    energyDisplayText = SystemRecoveryText.text("world.energy.display-complete");
    if (energyDisplay != null) {
      EntityFactory.updateDisplayText(energyDisplay, energyDisplayText);
    }
    markEnergyCratesCorrect();
  }

  /**
   * Returns whether both terminal steps of the energy puzzle were accepted.
   *
   * @return {@code true} after the energy values have been initialized and assigned
   */
  public boolean completed() {
    return energyPuzzleSolved;
  }

  /**
   * Returns whether the battery has actually been inserted into the module-storage socket.
   *
   * @return {@code true} after the battery-box callback has run
   */
  public boolean batteryInserted() {
    return batteryInserted;
  }

  /** Materializes one empty container per array element after terminal step 1. */
  public void spawnEnergyCrates() {
    for (int index = 0; index < 5; index++) {
      Game.add(EntityFactory.cryoBox(level.getPoint("a" + index), false));
    }
  }

  /**
   * Publishes the filled state of each energy crate through the authoritative shader component.
   *
   * <p>{@link feature.shader.ShaderSyncSystem} projects this declaration into the local draw list
   * on clients. Keeping the declaration on the server is important: it is included in snapshots,
   * so clients joining after the puzzle was solved see the same crate fill levels as existing
   * clients.
   */
  private void markEnergyCratesCorrect() {
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
          .ifPresent(
              entity ->
                  entity.add(
                      new ShaderComponent(
                          "energieShader",
                          0,
                          new EnergyFillShader(
                                  fill, Color.BLUE, "objects/tech/CryoBox.png")
                              .animMagnitude(0))));
    }
  }
}
