package demoDungeon.level;

import com.badlogic.gdx.Input;
import contrib.components.*;
import contrib.entities.LeverFactory;
import contrib.entities.MiscFactory;
import contrib.entities.MonsterFactory;
import contrib.hud.DialogUtils;
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
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import hint.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import petriNet.PetriNetSystem;
import petriNet.PlaceComponent;
import petriNet.TransitionComponent;

/**
 * The Demolevel.
 *
 * <p>The player has to craft a Healpotion.
 */
public class Level01 extends DungeonLevel {

  Entity riddle1;
  PlaceComponent riddle1Place;
  private final String riddle1Title = "Hilfe wird gesucht";
  private final Hint[] riddle1Hints = {
    new Hint(riddle1Title + " 1", "Du solltest schauen, woher die Geräusche kamen."),
    new Hint(riddle1Title + " 2", "Nicht jedes Monster ist böse."),
    new Hint(riddle1Title + " 3", "Rede mit dem roten Monster auf der anderen Seite des Abgrunds.")
  };

  Entity riddle2;

  PlaceComponent riddle2Place;
  private final String riddle2Title = "Rezept";

  private final Hint[] riddle2Hints = {
    new Hint(riddle2Title + " 1", "Vielleicht kann ich hier irgendwo ein Rezept finden."),
    new Hint(riddle2Title + " 2", "Ich sollte in die Bibliothek."),
    new Hint(riddle2Title + " 3", "Ich kann die Bücher lesen.")
  };

  Entity riddle3;

  PlaceComponent riddle3Place;
  private final String riddle3Title = "Heiltrank";
  private final Hint[] riddle3Hints = {
    new Hint(riddle3Title + " 1", "Die Zutaten kann ich im Level suchen."),
    new Hint(riddle3Title + " 2", "Ich sollte die Monster besiegen."),
    new Hint(riddle3Title + " 3", "In der Schatzkiste ist bestimmt auch was."),
    new Hint(riddle3Title + " 4", "Am Crafting-Tisch kann ich Zutaten mischen."),
    new Hint(riddle3Title + " 5", "Das Gegengift muss zum NPC.")
  };

  private HintSystem hintSystem;

  private Set<Entity> monster;
  private LeverComponent preasurePlate;
  private boolean spawnMushroom = true;

  private ExitTile exit;

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

    PetriNetSystem petriNetSystem = new PetriNetSystem();
    Game.add(petriNetSystem);
    Game.hero()
        .ifPresent(
            hero ->
                hero.fetch(InputComponent.class)
                    .ifPresent(
                        inputComponent ->
                            inputComponent.registerCallback(
                                Input.Keys.Z,
                                entity ->
                                    hero.fetch(HintStorageComponent.class)
                                        .ifPresent(
                                            hs -> hs.addHint(hintSystem.nextHint().orElse(null))),
                                false,
                                true)));
    Game.hero()
        .ifPresent(
            hero ->
                hero.fetch(InputComponent.class)
                    .ifPresent(
                        inputComponent ->
                            inputComponent.registerCallback(
                                Input.Keys.T,
                                entity ->
                                    hero.fetch(HintStorageComponent.class)
                                        .ifPresent(HintLog::showHintLog),
                                false,
                                true)));

    // Talk to NPC riddle
    riddle1 = new Entity("Talk to monster riddle");
    riddle1.add(new HintComponent(riddle1Hints));
    riddle1Place = new PlaceComponent();
    riddle1.add(riddle1Place);
    Game.add(riddle1);

    // This is the first quest, so activate it
    riddle1Place.produce();

    // Find recipe riddle
    riddle2 = new Entity("Find recipe riddle");
    riddle2Place = new PlaceComponent();
    riddle2.add(new HintComponent(riddle2Hints));
    riddle2.add(riddle2Place);
    Game.add(riddle2);

    TransitionComponent t1 = new TransitionComponent();
    petriNetSystem.addInputArc(t1, riddle1Place, 2);
    petriNetSystem.addOutputArc(t1, riddle2Place);

    // Craft potion riddle
    riddle3 = new Entity("Craft potion riddle");
    riddle3Place = new PlaceComponent();
    riddle3.add(new HintComponent(riddle3Hints));

    riddle3.add(riddle3Place);
    Game.add(riddle3);

    TransitionComponent t2 = new TransitionComponent();
    petriNetSystem.addInputArc(t2, riddle2Place, 2);
    petriNetSystem.addOutputArc(t2, riddle3Place);

    // For removing the last hints if potion was given to the entity
    TransitionComponent t3 = new TransitionComponent();
    petriNetSystem.addInputArc(t3, riddle3Place, 2);
  }

  private void npc() {
    Entity plate = LeverFactory.pressurePlate(customPoints.get(6).toCenteredPoint());
    preasurePlate = plate.fetch(LeverComponent.class).get();
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
                      riddle1Place.produce();
                      removeTalkToMonsterRiddle(hero);

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
                                          riddle3Place.produce();
                                          removeCraftPotionRiddle(hero);
                                          removeFindRecipeRiddle(hero);
                                          moveNpc(entity);
                                          npc.remove(InteractionComponent.class);
                                        }
                                      },
                                      () -> DialogUtils.showTextPopup("Beeile dich.", "Hilfe."))));
                    })));

    Game.add(npc);
  }

  private void removeTalkToMonsterRiddle(Entity hero) {
    Game.remove(riddle1);
    hero.fetch(HintStorageComponent.class).ifPresent(hs -> hs.removeHint(riddle1Hints));
  }

  private void removeFindRecipeRiddle(Entity hero) {
    Game.remove(riddle2);
    hero.fetch(HintStorageComponent.class).ifPresent(hs -> hs.removeHint(riddle2Hints));
  }

  private void removeCraftPotionRiddle(Entity hero) {
    Game.remove(riddle3);
    hero.fetch(HintStorageComponent.class).ifPresent(hs -> hs.removeHint(riddle3Hints));
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
            entity -> {
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
              riddle2Place.produce();
              removeFindRecipeRiddle(Game.hero().orElseThrow());
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
    if (preasurePlate.isOn()) exit.open();

    if (spawnMushroom && monster.isEmpty()) {
      spawnMushroom = false;
      ItemResourceMushroomRed mushroom = new ItemResourceMushroomRed();
      mushroom.drop(customPoints.get(7).toCenteredPoint());
    }
  }
}
