package produsAdvanced.level;

import contrib.components.AIComponent;
import contrib.components.LeverComponent;
import contrib.components.SignComponent;
import contrib.entities.EntityFactory;
import contrib.entities.LeverFactory;
import contrib.entities.SignFactory;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import level.AdvancedLevel;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Write your own interaction and combat controls.
 *
 * @see produsAdvanced.riddles.MyPlayerController
 */
public class AdvancedControlLevel3 extends AdvancedLevel {
  private static boolean showMsg = true;
  private static final String msg =
      "Ein Schalter, vielleicht einfach mal ziehen? Und sind das Monster?! Ich konnte doch mal zaubern...";
  private static final String task = "Füge deiner Steuerung eine Funktion zum Interagieren hinzu.";
  private static final String title = "Level 3";
  private static final List<String> messages =
      Arrays.asList(
          "Pass im nächsten Raum auf die Monster auf! Wenn du weitere Tips brauchst, um sie zu besiegen, interagiere nochmal mit diesem Schild.",
          "Du musst einen Feuerball implementieren, um die Monster zu besiegen, schau mal ob du die passende Funktion findest.",
          "Die Funktion die du suchst heißt 'shootFireball'. Es ist eine Methode des Heros.");
  private static final List<String> titles =
      Arrays.asList("noch zwei Hinweise", "noch ein Hinweis", "letzter Hinweis");
  private LeverComponent l1, l2, l3;
  private ExitTile exit;
  private DoorTile door1, door2;
  AtomicInteger currentIndex = new AtomicInteger(-1);

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public AdvancedControlLevel3(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Control");
  }

  @Override
  protected void onFirstTick() {
    if (showMsg)
      DialogUtils.showTextPopup(
          msg,
          title,
          () -> {
            showMsg = false;
            DialogUtils.showTextPopup(task, title);
          });
    addSign();

    exit = (ExitTile) Game.endTile().orElseThrow();
    exit.close();
    Entity lever1 = LeverFactory.createLever(customPoints().get(0).toPoint());
    Entity lever2 = LeverFactory.createLever(customPoints().get(1).toPoint());
    Entity lever3 = LeverFactory.createLever(customPoints().get(2).toPoint());
    l1 = lever1.fetch(LeverComponent.class).get();
    l2 = lever2.fetch(LeverComponent.class).get();
    l3 = lever3.fetch(LeverComponent.class).get();
    Game.add(lever1);
    Game.add(lever2);
    Game.add(lever3);
    door1 = (DoorTile) Game.tileAt(customPoints().get(3)).orElse(null);
    door2 = (DoorTile) Game.tileAt(customPoints().get(4)).orElse(null);
    door1.close();
    door2.close();
    customPoints().remove(0);
    customPoints().remove(0);
    customPoints().remove(0);
    customPoints().remove(0);
    customPoints().remove(0);

    customPoints()
        .forEach(
            coordinate -> {
              try {
                Entity m = EntityFactory.randomMonster();
                m.fetch(PositionComponent.class).get().position(coordinate.toPoint());
                Game.add(m);

              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  protected void onTick() {
    if (l1.isOn()) door1.open();
    else door1.close();
    if (l2.isOn() && l3.isOn()) door2.open();
    else door2.close();
    if (Game.levelEntities(Set.of(AIComponent.class)).findAny().isEmpty()) exit.open();
  }

  private void addSign() {
    Entity sign =
        SignFactory.createSign(
            "", // Der Text, der angezeigt werden soll
            "", // Titel
            new Point(19.5F, 6.5F), // Position des Schildes
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
