package produsAdvanced.level;

import contrib.components.HealthComponent;
import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import contrib.utils.DynamicCompiler;
import contrib.utils.ICommand;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import entities.BlocklyMonster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import level.BlocklyLevel;
import produsAdvanced.abstraction.Monster;
import produsAdvanced.abstraction.MonsterSort;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Sort the monster in the Array.
 *
 * @see produsAdvanced.riddles.MyMonsterSort
 */
public class AdvancedSortLevel extends BlocklyLevel {

  private static boolean showText = true;
  private final List<Monster> mobs = new ArrayList<>();
  private final AtomicInteger counter = new AtomicInteger(0);
  private final int[] monsterHPArray = {5, 7, 3, 6, 9, 2, 1};
  private DoorTile door;
  private boolean isLeverActivated = false;

  private static final SimpleIPath MONSTER_SORT_PATH =
      new SimpleIPath("src/produsAdvanced/riddles/MyMonsterSort.java");
  private static final String MONSTER_SORT_CLASSNAME = "produsAdvanced.riddles.MyMonsterSort";

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
    if (showText) {
      DialogUtils.showTextPopup(
          "Sortiere die Monster nach ihren Lebenspunkten. Das Kleinste sollte an erster Stelle stehen. Wenn du es geschafft hast betätige den Hebel!",
          "Ziel");
      showText = false;
    }

    ICommand leverAction =
        new ICommand() {
          @Override
          public void execute() {
            if (!isLeverActivated) {
              isLeverActivated = true;
              checkPlayerSolution();
            }
          }

          @Override
          public void undo() {
            isLeverActivated = false;
          }
        };

    Entity lever =
        LeverFactory.createLever(new Point(customPoints().get(0).toCenteredPoint()), leverAction);
    customPoints().remove(0);

    BlocklyMonster.BlocklyMonsterBuilder hedgehogBuilder = BlocklyMonster.HEDGEHOG.builder();
    hedgehogBuilder.range(0);
    hedgehogBuilder.addToGame();

    customPoints()
        .forEach(
            coordinate -> {
              hedgehogBuilder.spawnPoint(coordinate.toCenteredPoint());
              Entity mob = hedgehogBuilder.build().orElseThrow();
              mobs.add(new Monster(mob));
              mob.fetch(HealthComponent.class).orElseThrow().maximalHealthpoints(10);
              int index = counter.getAndIncrement();
              mob.fetch(HealthComponent.class)
                  .orElseThrow()
                  .currentHealthpoints(monsterHPArray[index]);
            });
    door = (DoorTile) Game.randomTile(LevelElement.DOOR).orElseThrow();
    door.close();

    Game.add(lever);
  }

  @Override
  protected void onTick() {}

  Monster[] solution;

  private void checkPlayerSolution() {
    try {
      solution =
          ((MonsterSort)
                  DynamicCompiler.loadUserInstance(
                      MONSTER_SORT_PATH,
                      MONSTER_SORT_CLASSNAME,
                      new Tuple<>(Monster[].class, mobs.toArray(new Monster[0]))))
              .sortMonsters();
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

    if (arrayIsSorted(solution)) {
      door.open();
      DialogUtils.showTextPopup(
          "Sehr gut! Die Monster sind korrekt sortiert. Nun kämpfe dich durch!", "Erfolg");
    } else {
      DialogUtils.showTextPopup(
          "Die Monster sind noch nicht korrekt sortiert. Versuche es nochmal!", "Fehler");
    }
  }

  private boolean arrayIsSorted(Monster[] sortedArray) {
    if (sortedArray == null) {
      return false;
    }
    Monster[] checkArray = mobs.toArray(new Monster[0]);
    Arrays.sort(checkArray, Comparator.comparingInt(Monster::getHealthPoints));

    if (Arrays.equals(sortedArray, checkArray)) {
      for (int i = 0; i < sortedArray.length - 1; i++) {
        if (sortedArray[i].getPosition().x >= sortedArray[i + 1].getPosition().x) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
