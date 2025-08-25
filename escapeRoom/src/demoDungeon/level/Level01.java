package demoDungeon.level;

import com.badlogic.gdx.Input;
import contrib.components.*;
import contrib.entities.LeverFactory;
import contrib.entities.MiscFactory;
import contrib.entities.MonsterFactory;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.OkDialog;
import contrib.hud.dialogs.YesNoDialog;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.item.concreteItem.ItemResourceMushroomRed;
import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.InputComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import hint.Hint;
import hint.HintComponent;
import hint.HintSystem;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import petriNet.PetriNetSystem;
import petriNet.PlaceComponent;
import petriNet.TransitionComponent;

/**
 * The Demolevel.
 *
 * <p>The player has to craft a heal potion.
 */
public class Level01 extends DungeonLevel {

  private final String monsterRiddleTitle = "Hilfe wird gesucht";
  private final String rezeptRiddleTitle = "Rezept";
  private final String potionRiddleTitle = "Heiltrank";
  private Set<Entity> monster;
  private LeverComponent p;
  private boolean spawnMushroom = true;

  private HintSystem hintSystem;
  private PetriNetSystem petriNetSystem;

  private ExitTile exit;

  // hint system
  Entity craftPotionRiddle;
  Entity findRecipeRiddle;
  Entity talkToNPCRiddle;
  PlaceComponent talkToNPCRiddlePlace;
  PlaceComponent findRecipeRiddlePlace;
  PlaceComponent craftPotionRiddlePlace;

  /**
   * Creates a new Level03.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param customPoints The custom points of the level.
   */
  public Level01(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Demo");
  }

  @Override
  protected void onFirstTick() {
    DialogUtils.showTextPopup("HALLO? IST DA WER? ICH BRAUCHE HILFE?", "HILFE!");
    setupHints();

    npc();
    crafting();
    books();
    monster();
    chest();

    exit = (ExitTile) Game.randomTile(LevelElement.EXIT).get();
    exit.close();
  }

  private void setupHints() {
    hintSystem = new HintSystem();
    Game.add(hintSystem);

    petriNetSystem = new PetriNetSystem();
    Game.add(petriNetSystem);

    Game.hero()
        .ifPresent(
            hero ->
                hero.fetch(InputComponent.class)
                    .ifPresent(
                        inputComponent ->
                            inputComponent.registerCallback(
                                Input.Keys.T,
                                entity ->
                                    hintSystem
                                        .nextHint()
                                        .ifPresent(
                                            hint ->
                                                OkDialog.showOkDialog(
                                                    hint.text(), hint.titel(), () -> {})),
                                false,
                                true)));

    // Talk to NPC riddle
    talkToNPCRiddle = new Entity("Talk to monster riddle");
    talkToNPCRiddle.add(
        new HintComponent(
            new Hint(monsterRiddleTitle, "Du solltest schauen, woher die Geräusche kamen."),
            new Hint(monsterRiddleTitle, "Nicht jedes Monster ist böse."),
            new Hint(
                monsterRiddleTitle,
                "Rede mit dem roten Monster auf der anderen Seite des Abgrunds.")));
    talkToNPCRiddlePlace = new PlaceComponent();
    talkToNPCRiddle.add(talkToNPCRiddlePlace);
    Game.add(talkToNPCRiddle);

    // This is the first quest, so activate it
    talkToNPCRiddlePlace.produce();

    // Find recipe riddle
    findRecipeRiddle = new Entity("Find recipe riddle");
    findRecipeRiddlePlace = new PlaceComponent();
    findRecipeRiddle.add(
        new HintComponent(
            new Hint(rezeptRiddleTitle, "Vielleicht kann ich hier irgendwo ein Rezept finden."),
            new Hint(rezeptRiddleTitle, "Ich sollte in die Bibliothek."),
            new Hint(rezeptRiddleTitle, "Ich kann die Bücher lesen.")));
    findRecipeRiddle.add(findRecipeRiddlePlace);
    Game.add(findRecipeRiddle);

    TransitionComponent t1 = new TransitionComponent();
    petriNetSystem.addInputArc(t1, talkToNPCRiddlePlace, 2);
    petriNetSystem.addOutputArc(t1, findRecipeRiddlePlace);

    // Craft potion riddle
    craftPotionRiddle = new Entity("Craft potion riddle");
    craftPotionRiddlePlace = new PlaceComponent();
    craftPotionRiddle.add(
        new HintComponent(
            new Hint(potionRiddleTitle, "Die Zutaten kann ich im Level suchen."),
            new Hint(potionRiddleTitle, "Ich sollte die Monster besiegen."),
            new Hint(potionRiddleTitle, "In der Schatzkiste ist bestimmt auch was."),
            new Hint(potionRiddleTitle, "Am Crafting-Tisch kann ich Zutaten mischen."),
            new Hint(potionRiddleTitle, "Das Gegengift muss zum NPC.")));
    craftPotionRiddle.add(craftPotionRiddlePlace);
    Game.add(craftPotionRiddle);

    TransitionComponent t2 = new TransitionComponent();
    petriNetSystem.addInputArc(t2, findRecipeRiddlePlace, 2);
    petriNetSystem.addOutputArc(t2, craftPotionRiddlePlace);

    // For removing the last hints if potion was given to the entity
    TransitionComponent t3 = new TransitionComponent();
    petriNetSystem.addInputArc(t3, craftPotionRiddlePlace, 2);
  }

  private void npc() {
    Entity plate = LeverFactory.pressurePlate(customPoints.get(6).toCenteredPoint());
    p = plate.fetch(LeverComponent.class).get();
    Game.add(plate);
    Entity npc = new Entity();
    npc.add(new VelocityComponent(5));
    npc.add(new CollideComponent());
    npc.add(new PositionComponent(customPoints.get(0).toCenteredPoint()));
    try {
      npc.add(new DrawComponent(new SimpleIPath("character/monster/chort")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    npc.add(
        new InteractionComponent(
            3,
            true,
            (entity, hero) ->
                DialogUtils.showTextPopup(
                    "Ich brauche dringend ein Heilmittel gegen meine Vergiftung.",
                    "Muschel esser.",
                    () -> {
                      talkToNPCRiddlePlace.produce();
                      Game.remove(talkToNPCRiddle);
                      entity.remove(InteractionComponent.class);
                      entity.add(
                          new InteractionComponent(
                              3,
                              true,
                              (entity1, entity2) ->
                                  YesNoDialog.showYesNoDialog(
                                      "Hast du das Gegenmittel bei dir?",
                                      "Hilfe",
                                      () -> {
                                        boolean check = checkForHealItem(entity2);
                                        if (!check) {
                                          DialogUtils.showTextPopup(
                                              "Das ist nicht das richitge Mittel.", "Falsch.");
                                        } else {
                                          DialogUtils.showTextPopup("Danke", "Richtig");
                                          craftPotionRiddlePlace.produce();
                                          Game.remove(craftPotionRiddle);
                                          Game.remove(findRecipeRiddle);
                                          moveNpc(entity);
                                          npc.remove(InteractionComponent.class);
                                        }
                                      },
                                      new IVoidFunction() {
                                        @Override
                                        public void execute() {
                                          DialogUtils.showTextPopup("Beeile dich.", "Hilfe.");
                                        }
                                      })));
                    })));

    Game.add(npc);
  }

  private boolean checkForHealItem(Entity player) {
    return player
        .fetch(InventoryComponent.class)
        .filter(inventoryComponent -> inventoryComponent.hasItem(ItemPotionHealth.class))
        .isPresent();
  }

  private void moveNpc(Entity npc) {
    Point goal = customPoints.get(6).toCenteredPoint();
    npc.add(
        new AIComponent(
            entity -> {},
            new Consumer<Entity>() {
              @Override
              public void accept(Entity entity) {
                Optional<Tile> entityTile = Game.tileAtEntity(entity);
                Optional<Tile> goalTile = Game.tileAt(goal);

                if (entityTile.isPresent()
                    && goalTile.isPresent()
                    && !entityTile.get().equals(goalTile.get())) {
                  AIUtils.followPath(
                      entity,
                      LevelUtils.calculatePath(
                          entity.fetch(PositionComponent.class).get().position(), goal));
                }
              }
            },
            entity -> false));
  }

  private void crafting() {
    try {
      Game.add(MiscFactory.newCraftingCauldron(customPoints.get(1).toCenteredPoint()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void books() {
    List<Tile> points =
        LevelUtils.accessibleTilesInRange(customPoints.get(2).toCenteredPoint(), 5f);
    Point p1 = points.get(RANDOM.nextInt(points.size())).position();
    Point p2 = points.get(RANDOM.nextInt(points.size())).position();
    while (p1.equals(p2)) p2 = points.get(RANDOM.nextInt(points.size())).position();
    Game.add(
        MiscFactory.book(p1, "Rote Pilze machen Bauchschmerzen.", "Das große Pilzbuch", () -> {}));
    Game.add(
        MiscFactory.book(
            p2,
            "Um ein Gegengift zu erstellen, muss man etwas Giftiges mit Wasser aufkochen.",
            "Gifte und Gegengifte",
            () -> {
              findRecipeRiddlePlace.produce();
              Game.remove(findRecipeRiddle);
            }));
  }

  private void monster() {
    monster = new HashSet<>();
    List<Tile> points =
        LevelUtils.accessibleTilesInRange(customPoints.get(7).toCenteredPoint(), 5f);

    for (int i = 0; i < 5; i++) {
      try {
        Entity m = MonsterFactory.randomMonster();
        m.fetch(PositionComponent.class)
            .map(pc -> pc.position(points.get(RANDOM.nextInt(points.size())).position()));
        m.fetch(HealthComponent.class)
            .ifPresent(
                healthComponent ->
                    healthComponent.onDeath(
                        entity -> {
                          monster.remove(entity);
                          Game.remove(entity);
                        }));
        monster.add(m);
        Game.add(m);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void chest() {
    Game.add(
        MiscFactory.catapult(
            customPoints.get(9).toCenteredPoint(), customPoints.get(10).toCenteredPoint(), 10f));
    Game.add(
        MiscFactory.catapult(
            customPoints.get(11).toCenteredPoint(), customPoints.get(12).toCenteredPoint(), 10f));
    Game.add(
        MiscFactory.catapult(
            customPoints.get(13).toCenteredPoint(), customPoints.get(14).toCenteredPoint(), 10f));
    Game.add(MiscFactory.marker(customPoints.get(10).toCenteredPoint()));
    Game.add(MiscFactory.marker(customPoints.get(12).toCenteredPoint()));
    Game.add(MiscFactory.marker(customPoints.get(14).toCenteredPoint()));

    try {
      Game.add(
          MiscFactory.newChest(
              Set.of(new ItemPotionWater()), customPoints.get(8).toCenteredPoint()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void onTick() {
    if (p.isOn()) exit.open();

    if (spawnMushroom && monster.isEmpty()) {
      spawnMushroom = false;
      ItemResourceMushroomRed mushroom = new ItemResourceMushroomRed();
      mushroom.drop(customPoints.get(7).toCenteredPoint());
    }
  }
}
