package demoDungeon.level;

import com.badlogic.gdx.Input;
import contrib.components.*;
import contrib.entities.DungeonMonster;
import contrib.entities.LeverFactory;
import contrib.entities.MiscFactory;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.YesNoDialog;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.item.concreteItem.ItemResourceMushroomRed;
import contrib.modules.interaction.InteractionComponent;
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
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import hint.*;
import java.util.*;
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
  private Set<Entity> monster;
  private LeverComponent preasurePlate;
  private boolean spawnMushroom = true;
  private ExitTile exit;

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public Level01(LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "Demo");
  }

  @Override
  protected void onFirstTick() {
    Game.player().flatMap(h -> h.fetch(HintLogComponent.class)).ifPresent(HintLogComponent::clear);
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
    HintSystem hintSystem = new HintSystem();
    Game.add(hintSystem);
    Game.add(HintGiverFactory.mailbox(new Point(1, 5)));
    PetriNetSystem petriNetSystem = new PetriNetSystem();
    Game.add(petriNetSystem);
    // register hint log
    Game.player()
        .ifPresent(
            player ->
                player
                    .fetch(InputComponent.class)
                    .ifPresent(
                        inputComponent ->
                            inputComponent.registerCallback(
                                Input.Keys.T,
                                entity ->
                                    player
                                        .fetch(HintLogComponent.class)
                                        .ifPresent(HintLogDialog::showHintLog),
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
    Entity plate = LeverFactory.pressurePlate(getPoint(6));
    preasurePlate = plate.fetch(LeverComponent.class).get();
    Game.add(plate);
    Entity npc = new Entity();
    npc.add(new VelocityComponent(5));
    npc.add(new CollideComponent());
    npc.add(new PositionComponent(getPoint(0)));
    npc.add(new DrawComponent(new SimpleIPath("character/monster/chort")));
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
    hero.fetch(HintLogComponent.class).ifPresent(log -> log.removeHint(riddle1Hints));
  }

  private void removeFindRecipeRiddle(Entity hero) {
    Game.remove(riddle2);
    hero.fetch(HintLogComponent.class).ifPresent(log -> log.removeHint(riddle2Hints));
  }

  private void removeCraftPotionRiddle(Entity hero) {
    Game.remove(riddle3);
    hero.fetch(HintLogComponent.class).ifPresent(log -> log.removeHint(riddle3Hints));
  }

  private boolean checkForHealItem(Entity player) {
    return player
        .fetch(InventoryComponent.class)
        .filter(inventoryComponent -> inventoryComponent.hasItem(ItemPotionHealth.class))
        .isPresent();
  }

  private void moveNpc(Entity npc) {
    Point goal = getPoint(6);
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
    Game.add(MiscFactory.newCraftingCauldron(getPoint(1)));
  }

  private void books() {
    List<Tile> points = LevelUtils.accessibleTilesInRange(getPoint(2), 5f);
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
              removeFindRecipeRiddle(Game.player().orElseThrow());
            }));
  }

  private void monster() {
    monster = new HashSet<>();
    List<Tile> points = LevelUtils.accessibleTilesInRange(getPoint(7), 5f);
    for (int i = 0; i < 5; i++) {
      Entity m =
          DungeonMonster.randomMonster()
              .builder()
              .build(points.get(RANDOM.nextInt(points.size())).position());
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
    }
  }

  private void chest() {
    Game.add(MiscFactory.catapult(getPoint(9), getPoint(10), 10f));
    Game.add(MiscFactory.catapult(getPoint(11), getPoint(12), 10f));
    Game.add(MiscFactory.catapult(getPoint(13), getPoint(14), 10f));
    Game.add(MiscFactory.marker(getPoint(10)));
    Game.add(MiscFactory.marker(getPoint(12)));
    Game.add(MiscFactory.marker(getPoint(14)));
    Game.add(MiscFactory.newChest(Set.of(new ItemPotionWater()), getPoint(8)));
  }

  @Override
  protected void onTick() {
    if (preasurePlate.isOn()) exit.open();
    if (spawnMushroom && monster.isEmpty()) {
      spawnMushroom = false;
      ItemResourceMushroomRed mushroom = new ItemResourceMushroomRed();
      mushroom.drop(getPoint(7));
    }
  }
}
