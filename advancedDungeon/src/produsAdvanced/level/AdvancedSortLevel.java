package produsAdvanced.level;

import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.components.SignComponent;
import contrib.entities.DungeonMonster;
import contrib.entities.LeverFactory;
import contrib.entities.SignFactory;
import contrib.hud.DialogUtils;
import contrib.systems.EventScheduler;
import contrib.utils.DynamicCompiler;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import level.AdvancedLevel;
import produsAdvanced.abstraction.Monster;
import produsAdvanced.abstraction.MonsterSort;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Sort the monster in the Array.
 *
 * @see produsAdvanced.riddles.MyMonsterSort
 */
public class AdvancedSortLevel extends AdvancedLevel {

  /** Delay for the {@link EventScheduler} to draw position swap. */
  public static final int DELAY = 1000;

  /** Delay for the {@link EventScheduler} to schedule tint remove. */
  public static final int DELAY_UNTINT = DELAY + 200;

  /** Delaymultiplicator for the {@link EventScheduler} to draw position swap. */
  public static int delay_multiplication = 0;

  private static boolean showMsg = true;
  private final List<Monster> mobs = new ArrayList<>();
  private final AtomicInteger counter = new AtomicInteger(0);
  private final int[] monsterHPArray = {5, 7, 3, 6, 9, 2, 1};
  private DoorTile door;
  private boolean isLeverActivated = false;

  private static final SimpleIPath MONSTER_SORT_PATH =
      new SimpleIPath("advancedDungeon/src/produsAdvanced/riddles/MyMonsterSort.java");
  private static final String MONSTER_SORT_CLASSNAME = "produsAdvanced.riddles.MyMonsterSort";

  private static final String msg =
      "Da herrscht ja ein heilloses Durcheinander! Vielleicht öffnet sich ja die Tür, wenn ich hier für Ordnung sorge.";
  private static final String title = "Level 9";
  private static final List<String> messages =
      Arrays.asList(
          "Betätige den Schalter und schau was passiert.",
          "Du musst das Array MyMonsterArray in der Klasse MyMonsterSort sortieren, damit die Tür aufgeht. Wie das wohl geht?",
          "Sortiere die Monster im Array nach ihren Lebenspunkten. Das Kleinste soll dir am nächsten stehen.",
          "Die Lebenspunkte der Monster erhältst du mit '.getHealthPoints()' für ein Monster aus dem Array.",
          "Immer wenn du zwei Monster im Array tauschst, musst du auch ihre visuellen Positionen im Spiel tauschen! Nutze dafür die swapPosition() Methode.",
          "Wichtig: Rufe swapPosition() auf dem ERSTEN Monster auf und übergib das ZWEITE als Parameter.",
          "Denke daran: Nach jedem Tausch im Array auch swapPosition() aufrufen!");

  // Dynamische Titelgenerierung (statt statischer Liste)
  private String titleFor(int idx) {
    int lastIndex = messages.size() - 1;
    int remaining = lastIndex - idx;
    if (remaining <= 0) return "letzter Hinweis";
    if (remaining == 1) return "noch ein Hinweis";
    return "noch " + numberToGerman(remaining) + " Hinweise";
  }

  private String numberToGerman(int n) {
    switch (n) {
      case 2:
        return "zwei";
      case 3:
        return "drei";
      case 4:
        return "vier";
      case 5:
        return "fünf";
      case 6:
        return "sechs";
      case 7:
        return "sieben";
      case 8:
        return "acht";
      case 9:
        return "neun";
      case 10:
        return "zehn";
      default:
        return String.valueOf(n);
    }
  }

  // todo build dynamically
  // Entferne statische titles-Liste und behalte nur den Index
  AtomicInteger currentIndex = new AtomicInteger(-1);

  // Neu: Merker, ob der Schalter mindestens einmal betätigt wurde
  private boolean hasUsedLeverOnce = false;

  private boolean isSorting = false;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public AdvancedSortLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "SortArray");
  }

  @Override
  protected void onFirstTick() {
    if (showMsg) DialogUtils.showTextPopup(msg, title, () -> showMsg = false);

    ICommand leverAction =
        new ICommand() {
          @Override
          public void execute() {
            if (!isLeverActivated && !isSorting) {
              isLeverActivated = true;
              isSorting = true;
              hasUsedLeverOnce = true; // ab jetzt erste Schild-Nachricht überspringen
              checkPlayerSolution();
            }
          }

          @Override
          public void undo() {
            isLeverActivated = false;
          }
        };

    Entity lever =
        LeverFactory.createLever(new Point(customPoints().get(0).toPoint()), leverAction);
    customPoints().remove(0);

    customPoints()
        .forEach(
            coordinate -> {
              Entity mob = DungeonMonster.randomMonster().builder().build(coordinate.toPoint());
              mob.remove(AIComponent.class);
              mobs.add(new Monster(mob));
              mob.fetch(HealthComponent.class).orElseThrow().maximalHealthpoints(10);
              int index = counter.getAndIncrement();
              mob.fetch(HealthComponent.class)
                  .orElseThrow()
                  .currentHealthpoints(monsterHPArray[index]);
              Game.add(mob);
            });
    door = (DoorTile) Game.randomTile(LevelElement.DOOR).orElseThrow();
    door.close();

    Game.add(lever);
    addSign();
  }

  @Override
  protected void onTick() {}

  Monster[] solution;

  private void checkPlayerSolution() {
    try {
      solution =
          ((MonsterSort)
                  DynamicCompiler.loadUserInstance(MONSTER_SORT_PATH, MONSTER_SORT_CLASSNAME))
              .sortMonsters(mobs.toArray(new Monster[0]));
      sortListLikeArray(mobs, solution);
    } catch (UnsupportedOperationException e) {
      // Spezifische Behandlung für nicht implementierte Methode
      DialogUtils.showTextPopup(
          "Die Methode 'sortMonsters' wurde noch nicht implementiert. "
              + "Bitte implementiere sie zuerst!",
          "Nicht implementiert");
      return; // Methode beenden, aber Spiel weiterlaufen lassen
    } catch (Exception e) {
      // Andere Fehler abfangen
      DialogUtils.showTextPopup("Ein Fehler ist aufgetreten: " + e.getMessage(), "Fehler");
      return;
    }
    if (solution == null) {
      DialogUtils.showTextPopup("Die Methode 'sortMonsters' gibt null zurück!", "Fehler");
      return;
    }

    EventScheduler.scheduleAction(
        () -> {
          if (arrayIsSorted(solution)) {
            door.open();
            DialogUtils.showTextPopup(
                "Sehr gut! Die Monster sind korrekt sortiert. Nun kämpfe dich durch!", "Erfolg");
          } else {
            DialogUtils.showTextPopup(
                "Die Monster sind noch nicht korrekt sortiert. Versuche es nochmal!", "Fehler");
          }
          EventScheduler.clear();
          delay_multiplication = 0;
          isSorting = false;
        },
        DELAY_UNTINT * (delay_multiplication - 1) + 100);
  }

  private boolean arrayIsSorted(Monster[] sortedArray) {
    if (sortedArray == null) {
      return false;
    }
    Monster[] checkArray = mobs.toArray(new Monster[0]);
    Arrays.sort(checkArray, Comparator.comparingInt(Monster::getHealthPoints));

    if (Arrays.equals(sortedArray, checkArray)) {
      for (int i = 0; i < sortedArray.length - 1; i++) {
        if (sortedArray[i].getPosition().x() >= sortedArray[i + 1].getPosition().x()) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private void sortListLikeArray(List<Monster> mobs, Monster[] solution) {
    // Build a map from Monster to its index in the solution array
    Map<Monster, Integer> indexMap = new HashMap<>();
    for (int i = 0; i < solution.length; i++) {
      indexMap.put(solution[i], i);
    }

    // Sort the list using the index map
    mobs.sort(Comparator.comparingInt(indexMap::get));
  }

  private void addSign() {
    Entity sign =
        SignFactory.createSign(
            "", // Der Text, der angezeigt werden soll
            "", // Titel
            new Point(3.5F, 7.5F), // Position des Schildes
            (entity, hero) -> {
              // Vor der ersten Hebel-Aktivierung: nur den ersten Hinweis anzeigen
              if (!hasUsedLeverOnce) {
                SignComponent sc =
                    entity
                        .fetch(SignComponent.class)
                        .orElseThrow(() -> new RuntimeException("SignComponent not found"));
                sc.text(messages.get(0));
                sc.title("");
                return;
              }

              // Danach: Hinweise weiterblättern, den ersten Hinweis überspringen
              if (currentIndex.get() < messages.size() - 1) {
                int nextIndex = currentIndex.incrementAndGet();
                if (hasUsedLeverOnce && nextIndex == 0) {
                  nextIndex = currentIndex.incrementAndGet();
                }
                final int idx = nextIndex;
                Game.levelEntities(Set.of(SignComponent.class))
                    .filter(signEntity -> signEntity.equals(entity))
                    .findFirst()
                    .ifPresent(
                        signEntity -> {
                          SignComponent signComponent =
                              signEntity
                                  .fetch(SignComponent.class)
                                  .orElseThrow(
                                      () -> new RuntimeException("SignComponent not found"));
                          signComponent.text(messages.get(idx));
                          signComponent.title(titleFor(idx));
                        });
              }
            });
    Game.add(sign);
  }
}
