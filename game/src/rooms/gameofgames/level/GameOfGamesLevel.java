package rooms.gameofgames.level;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.List;
import java.util.Map;

/** Server-side level setup for the Game of Games escape room. */
public class GameOfGamesLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "game-of-games-1";
  private static final String BOOKSHELF_POINT = "bookshelf";
  private static final String BOOKSHELF_DIALOG =
      "Between unfinished board-game manuals and dusty rulebooks, one note stands out:[p]"
          + "Every game has rules. The interesting part is finding out who wrote them.";

  /**
   * Creates the first Game of Games level.
   *
   * @param layout the tile layout loaded from the level asset
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level asset
   * @param decorations static decorations loaded from the level asset
   */
  public GameOfGamesLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }

  /**
   * Creates the first Game of Games level.
   *
   * @param layout the tile layout loaded from the level asset
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level asset
   */
  public GameOfGamesLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, LEVEL_NAME);
  }

  @Override
  protected void onFirstTick() {
    setupBookshelf();
  }

  private void setupBookshelf() {
    Entity bookshelf = DecoFactory.createDeco(getPoint(BOOKSHELF_POINT), Deco.BookshelfLarge);
    bookshelf.remove(DecoComponent.class);
    bookshelf.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (interacted, who) ->
                        DialogFactory.showDialogDialog(BOOKSHELF_DIALOG, () -> {}, who.id()),
                    "Read")));
    Game.add(bookshelf);
  }
}
