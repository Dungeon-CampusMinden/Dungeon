package produsAdvanced.level;

import contrib.components.LeverComponent;
import contrib.components.SignComponent;
import contrib.entities.LeverFactory;
import contrib.entities.SignFactory;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import level.AdvancedLevel;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Modify your own interaction and combat controls.
 *
 * @see produsAdvanced.riddles.MyPlayerController
 */
public class AdvancedControlLevel4 extends AdvancedLevel {
  private static boolean showMsg = true;
  private static final String msg = "Wenn ich hier zu langsam bin, fall ich runter.";
  private static final String title = "Level 4";
  private static final List<String> messages =
      Arrays.asList(
          "Erhöhe deine Geschwindigkeit in deiner Steuerung, damit du nicht runter fällst.",
          "Du musst alle fünf Schalter betätigen, damit die Tür aufgeht.",
          "Du kommst einfacher über die Pits, wenn du dich auch diagonal bewegen kannst.");
  private static final List<String> titles =
      Arrays.asList("noch zwei Hinweise", "noch ein Hinweis", "letzter Hinweis");

  private ExitTile exit;
  Set<LeverComponent> leverComponentSet;
  AtomicInteger currentIndex = new AtomicInteger(-1);

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public AdvancedControlLevel4(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Control");
  }

  @Override
  protected void onFirstTick() {
    if (showMsg) DialogUtils.showTextPopup(msg, title, () -> showMsg = false);
    leverComponentSet = new HashSet<>();
    customPoints()
        .forEach(
            coordinate -> {
              Entity lever = LeverFactory.createLever(coordinate.toPoint());
              leverComponentSet.add(lever.fetch(LeverComponent.class).get());
              Game.add(lever);
            });

    exit = (ExitTile) Game.endTile().orElseThrow();
    exit.close();

    Game.allTiles(LevelElement.PIT)
        .forEach(
            tile -> {
              ((PitTile) tile).timeToOpen(120);
              ((PitTile) tile).close();
            });
    addSign();
  }

  @Override
  protected void onTick() {
    if (leverComponentSet.stream().allMatch(LeverComponent::isOn)) exit.open();
  }

  private void addSign() {
    Entity sign =
        SignFactory.createSign(
            "", // Der Text, der angezeigt werden soll
            "", // Titel
            new Point(1.5F, 2.5F), // Position des Schildes
            (entity, hero) -> {

              // Falls noch weitere Nachrichten vorhanden sind, zum nächsten Text wechseln
              if (currentIndex.get() < messages.size() - 1) {
                currentIndex.incrementAndGet();
                Game.levelEntities(Set.of(SignComponent.class))
                    .filter(signEntity -> signEntity.equals(entity))
                    .findFirst()
                    .ifPresent(
                        signEntity -> {
                          // Aktualisiere den Text des Schildes
                          SignComponent signComponent =
                              signEntity
                                  .fetch(SignComponent.class)
                                  .orElseThrow(
                                      () -> new RuntimeException("SignComponent not found"));
                          signComponent.text(messages.get(currentIndex.get()));
                          signComponent.title(titles.get(currentIndex.get()));
                        });
              }
            });
    Game.add(sign);
  }
}
