package entities.levercommands;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import contrib.components.LeverComponent;
import contrib.systems.LeverSystem;
import contrib.utils.ICommand;
import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import systems.EventScheduler;
import systems.FogOfWarSystem;

/**
 * A {@link LeverSystem Lever}-Command that opens a passage in the dungeon.
 *
 * <p>The OpenPassageCommand is a command that can be executed by a lever entity. When executed, it
 * will open a passage in the dungeon by changing the tiles in the specified area to floor tiles.
 *
 * <p>The command can be undone, which will revert the changes made to the tiles. The command also
 * plays a sound effect when executed.
 *
 * @see LeverSystem LeverSystem
 * @see LeverComponent LeverComponent
 */
public class OpenPassageCommand implements ICommand {
  private static final IPath OPEN_PASSAGE =
      new SimpleIPath("sounds/dragging_a_cinderblock_across_concrete.wav");
  private final Coordinate topLeft;
  private final Coordinate bottomRight;
  private boolean isOpen = false;

  private LevelElement[][] originalTiles;

  /**
   * Constructs a new OpenPassageCommand with the given parameters.
   *
   * @param topLeft The top-left corner of the area to open.
   * @param bottomRight The bottom-right corner of the area to open.
   * @see level.devlevel.IllusionRiddleLevel IllusionRiddleLevel
   */
  public OpenPassageCommand(Coordinate topLeft, Coordinate bottomRight) {
    this.topLeft = topLeft;
    this.bottomRight = bottomRight;
  }

  @Override
  public void execute() {
    if (!isOpen) {
      this.originalTiles =
          new LevelElement[bottomRight.x - topLeft.x + 1][topLeft.y - bottomRight.y + 1];
      for (int x = topLeft.x; x <= bottomRight.x; x++) {
        for (int y = bottomRight.y; y <= topLeft.y; y++) {
          Tile tile = Game.currentLevel().tileAt(new Coordinate(x, y));
          if (tile == null) return;
          originalTiles[x - topLeft.x][topLeft.y - y] = tile.levelElement();
          Game.currentLevel().changeTileElementType(tile, LevelElement.FLOOR);
          Tile newTile = Game.currentLevel().tileAt(new Coordinate(x, y));
          ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).updateTile(tile, newTile);
        }
      }
      this.isOpen = true;
    }
    playSound();
  }

  @Override
  public void undo() {
    if (isOpen) {
      for (int x = topLeft.x; x <= bottomRight.x; x++) {
        for (int y = bottomRight.y; y <= topLeft.y; y++) {
          Tile tile = Game.currentLevel().tileAt(new Coordinate(x, y));
          if (tile == null) return;
          LevelElement oldElement = originalTiles[x - topLeft.x][topLeft.y - y];
          Game.currentLevel().changeTileElementType(tile, oldElement);
          Tile newTile = Game.currentLevel().tileAt(new Coordinate(x, y));
          ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).updateTile(tile, newTile);
        }
      }
      // Workaround to fix wall textures
      for (int x = topLeft.x; x <= bottomRight.x; x++) {
        for (int y = bottomRight.y; y <= topLeft.y; y++) {
          Tile tile = Game.currentLevel().tileAt(new Coordinate(x, y));
          if (tile == null) return;
          Game.currentLevel().changeTileElementType(tile, tile.levelElement());
        }
      }
      this.isOpen = false;
    }
  }

  private void playSound() {
    Sound soundEffect = Gdx.audio.newSound(Gdx.files.internal(OPEN_PASSAGE.pathString()));

    // Play the sound with the adjusted pitch
    long soundId = soundEffect.play();
    soundEffect.setPitch(soundId, 0.57f);

    // Set the volume
    soundEffect.setVolume(soundId, 0.1f);

    EventScheduler.getInstance().scheduleAction(soundEffect::dispose, 10000L);
  }
}
