package gameOfGames.level;

import contrib.components.DecoComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.dialogs.DialogFactory;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.Game;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
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
