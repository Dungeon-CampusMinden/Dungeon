package level.devlevel;

import com.badlogic.gdx.Input;
import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.configuration.KeyboardConfig;
import contrib.entities.DialogFactory;
import contrib.entities.MiscFactory;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemPotionWater;
import contrib.item.concreteItem.ItemResourceMushroomRed;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import entities.MonsterType;
import java.util.List;
import level.DevDungeonLevel;
import level.utils.ITickable;
import starter.DevDungeon;
import utils.EntityUtils;

/** The tutorial level */
public class TutorialLevel extends DevDungeonLevel implements ITickable {

  // Entity spawn points
  private final Coordinate mobSpawn;
  private final Point chestSpawn;
  private final Point cauldronSpawn;
  private Coordinate lastHeroCoords = new Coordinate(0, 0);

  public TutorialLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints);
    this.mobSpawn = customPoints.get(0);

    this.chestSpawn = customPoints.get(1).toCenteredPoint();
    this.cauldronSpawn = customPoints.get(2).toCenteredPoint();
  }

  @Override
  public void onTick(boolean isFirstTick) {
    if (isFirstTick) {
      DialogFactory.showTextPopup(
          "Willkommen im Tutorial! Hier lernst Du die Grundlagen des Spiels kennen. ",
          "Level " + DevDungeon.DUNGEON_LOADER.currentLevelIndex() + ": Tutorial",
          () -> {
            this.handleFirstTick();
            this.doorTiles().forEach(DoorTile::close);
            this.buildBridge();
          });
    }
    if (this.lastHeroCoords != null && !this.lastHeroCoords.equals(EntityUtils.getHeroCoords())) {
      // Only handle text popups if the hero has moved
      this.handleTextPopups();
    }
    this.handleDoors();
    this.lastHeroCoords = EntityUtils.getHeroCoords();
  }

  /**
   * Handles the spawning of the tutorial entities e.g. monsters It also fills the chest with the
   * necessary items for the tutorial
   *
   * @throws RuntimeException if any of the entities could not be created
   */
  private void handleFirstTick() {
    Entity mob = EntityUtils.spawnMonster(MonsterType.TUTORIAL, this.mobSpawn);
    if (mob == null) {
      throw new RuntimeException("Failed to create tutorial monster");
    }
    DoorTile mobDoor = (DoorTile) this.tileAt(this.customPoints().get(5));
    mob.fetch(HealthComponent.class).ifPresent(hc -> hc.onDeath((e) -> mobDoor.open()));
    Entity chest;
    Entity chest2;
    try {
      chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
      chest2 = MiscFactory.newChest(MiscFactory.FILL_CHEST.EMPTY);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create tutorial chest");
    }
    this.setupChest(chest, chest2);
    Entity cauldron;
    try {
      cauldron = MiscFactory.newCraftingCauldron();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create tutorial cauldron");
    }
    this.setupCauldron(cauldron);

    String movementKeys =
        Input.Keys.toString(core.configuration.KeyboardConfig.MOVEMENT_UP.value())
            + Input.Keys.toString(core.configuration.KeyboardConfig.MOVEMENT_LEFT.value())
            + Input.Keys.toString(core.configuration.KeyboardConfig.MOVEMENT_DOWN.value())
            + Input.Keys.toString(core.configuration.KeyboardConfig.MOVEMENT_RIGHT.value());
    DialogFactory.showTextPopup(
        "Verwende " + movementKeys + " (oder RMB), um dich zu bewegen.", "Bewegung");
  }

  private void handleTextPopups() {
    DoorTile frontDoor = (DoorTile) this.tileAt(this.customPoints().get(4));
    DoorTile mobDoor = (DoorTile) this.tileAt(this.customPoints().get(5));
    DoorTile CraftingDoor = (DoorTile) this.tileAt(this.customPoints().get(6));
    if (EntityUtils.getHeroCoords() == null) return;
    Tile heroTile = this.tileAt(EntityUtils.getHeroCoords());
    if (heroTile == null) return;

    if (frontDoor.coordinate().equals(heroTile.coordinate())) {
      DialogFactory.showTextPopup(
          "Mit "
              + Input.Keys.toString(KeyboardConfig.FIRST_SKILL.value())
              + " (oder LMB) kannst du angreifen.",
          "Kampf");
    } else if (mobDoor.coordinate().equals(heroTile.coordinate())) {
      DialogFactory.showTextPopup(
          "Kommen wir zum Craften. Du findest im Verlauf des Spiels verschiedene Ressourcen,"
              + " die du in Tränke und andere nützliche Gegenstände verwandeln kannst.",
          "Looting & Crafting",
          () -> {
            DialogFactory.showTextPopup(
                "\nTruhe/Kessel öffnen/schließen: ESC oder "
                    + Input.Keys.toString(KeyboardConfig.INTERACT_WORLD.value())
                    + "/"
                    + "RMB" // TODO: KeyboardConfig.MOUSE_INTERACT_WORLD.value() -> 'Unknown'
                    + "; Inventar öffnen/schließen: "
                    + Input.Keys.toString(KeyboardConfig.INVENTORY_OPEN.value())
                    + "; Items verwenden: "
                    + Input.Keys.toString(KeyboardConfig.USE_ITEM.value())
                    + "; Items verschieben: Drag & Drop oder RMB",
                "Looting & Crafting: Steuerung",
                () -> {
                  DialogFactory.showTextPopup(
                      "Du kannst Tränke und andere Gegenstände herstellen, indem du die Ressourcen in den Kessel legst und auf den Kessel klickst.",
                      "Looting & Crafting");
                });
          });
    } else if (CraftingDoor.coordinate().equals(heroTile.coordinate())) {
      DialogFactory.showTextPopup(
          "Im Dungeon findest immer wieder Hindernisse, Fallen und Rätsel."
              + "Versuche sie zu umgehen oder zu lösen. Löcher kannst du anhand rissiger Bodenplatten erkennen oder anhand von schwarzen Löchern.",
          "Rätsel & Fallen");
    }
  }

  /**
   * Handles the opening of the doors. Except for the mob door, this is handle in the onDeath of the
   * mob
   */
  private void handleDoors() {
    DoorTile frontDoor = (DoorTile) this.tileAt(this.customPoints().get(4));
    DoorTile CraftingDoor = (DoorTile) this.tileAt(this.customPoints().get(6));
    Point heroPos;
    try {
      heroPos = SkillTools.heroPositionAsPoint();
    } catch (MissingHeroException e) {
      return;
    }
    if (!frontDoor.isOpen() && frontDoor.position().distance(heroPos) < 2) {
      frontDoor.open();
    }

    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    InventoryComponent ic =
        hero.fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, InventoryComponent.class));
    if (!CraftingDoor.isOpen() && ic.hasItem(ItemPotionHealth.class)) {
      CraftingDoor.open();
    }
  }

  /**
   * Sets up the chest with the necessary items for the tutorial
   *
   * @param chest The chest to set up
   */
  private void setupChest(Entity chest, Entity b) {
    PositionComponent pc =
        chest
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, PositionComponent.class));
    InventoryComponent ic =
        chest
            .fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(chest, InventoryComponent.class));

    pc.position(this.chestSpawn);
    ic.add(
        new ItemPotionWater() {
          @Override
          public String description() {
            return super.description() + " (For Tutorial purposes usage is disabled)";
          }

          @Override
          public void use(final Entity e) {} // Disable usage of the potion to prevent soft locking
        });
    ic.add(
        new ItemResourceMushroomRed() {
          @Override
          public String description() {
            return super.description() + " (For Tutorial purposes usage is disabled)";
          }

          @Override
          public void use(final Entity e) {} // Disable usage of the potion to prevent soft locking
        });

    Game.add(chest);

    pc =
        b.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(b, PositionComponent.class));
    ic =
        b.fetch(InventoryComponent.class)
            .orElseThrow(() -> MissingComponentException.build(b, InventoryComponent.class));
    pc.position(this.customPoints().get(3).toCenteredPoint());
    ic.add(new ItemPotionHealth(HealthPotionType.NORMAL));
    Game.add(b);
  }

  /**
   * Sets up the crafting cauldron
   *
   * @param cauldron The cauldron to set up
   */
  private void setupCauldron(Entity cauldron) {
    PositionComponent pc =
        cauldron
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(cauldron, PositionComponent.class));
    pc.position(this.cauldronSpawn);
    Game.add(cauldron);
  }

  /** Builds a bridge to a special chest */
  private void buildBridge() {
    // The bridge should extend from x = 39 to x = 52 and y = 11
    // TODO: Implement bridge
  }
}
