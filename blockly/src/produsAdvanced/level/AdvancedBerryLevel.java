package produsAdvanced.level;

import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.EntityFactory;
import contrib.entities.WorldItemBuilder;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.YesNoDialog;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import level.BlocklyLevel;
import produsAdvanced.abstraction.Berry;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>A custom level where the player must collect non-toxic berries and place them in a chest to
 * feed an NPC. Once enough safe berries are delivered, the NPC opens the exit door.
 *
 * @see produsAdvanced.riddles.MyPlayerController
 * @see Berry
 */
public class AdvancedBerryLevel extends BlocklyLevel {
  private static boolean showMsg = true;
  private static String msg = "Der Ork dort sieht verzweifelt aus. Mal schauen, ob ich ihm helfen kann.";
  private static String titel = "Level 5";
  private static final int BERRY_GOAL = 5;
  private static final String NPC_TEXTURE_PATH = "character/monster/orc_shaman";
  private static final String DIALOG_TITLE_HUNGER = "HUNGER!";
  private static final String DIALOG_TITLE_SATISFIED = "Satt!";
  private static final String DIALOG_TITLE_MINE = "Meins!";

  private static final String DIALOG_LOGIN = "Kann ich die Beeren in der Kiste essen?";
  private static final String DIALOG_MESSAGE_START =
      "Ich hab so einen Hunger. Bring mir %d Beeren, aber nicht die giftigen! Leg sie einfach in die Kiste.";
  private static final String DIALOG_MESSAGE_NOT_ENOUGH =
      "Was ist das denn? Davon werde ich ja nie satt. Ich hab gesagt, du sollst mir %d Beeren bringen, LOS!";
  private static final String DIALOG_MESSAGE_SUCCESS =
      "Ich danke dir, ich habe schon so lange nichts gegessen. Ich werde die Tür für dich öffnen.";
  private static final String DIALOG_MESSAGE_TOXIC =
      "Willst du mich umbringen? Da sind giftige dabei!";
  private static final String DIALOG_MESSAGE_LATER = "Mach schnell, ich habe echt Hunger.";
  private static final String DIALOG_MESSAGE_CHEST = "Ey, lass die Finger davon!";

  private Entity chest;
  private ExitTile door;

  /**
   * Constructor for the berry collection level.
   *
   * @param layout 2D array representing the level layout.
   * @param designLabel The design label for the level.
   * @param customPoints Custom positions used in this level (e.g., for NPC and chest).
   */
  public AdvancedBerryLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Berry");
  }

  @Override
  protected void onFirstTick() {
    if (showMsg) DialogUtils.showTextPopup(msg, titel, () -> showMsg = false);
    createNPC();
    createChest();
    spawnBerries();
    door = (ExitTile) Game.randomTile(LevelElement.EXIT).get();
    door.close();
  }

  /** Spawns both toxic and non-toxic berries at random floor tiles. */
  private void spawnBerries() {
    for (int i = 0; i < BERRY_GOAL; i++) {
      spawnBerry(true); // Toxic berry
      spawnBerry(false); // Non-toxic berry
    }
  }

  private void spawnBerry(boolean isToxic) {
    Entity berry =
        WorldItemBuilder.buildWorldItem(
            new Berry(isToxic),
            Game.randomTile(LevelElement.FLOOR).get().coordinate().toCenteredPoint());
    berry.name("Berry");
    HealthComponent health = new HealthComponent(999, (entity -> {}));
    berry.add(health);
    makeInvincible(berry);
    berry.add(new CollideComponent());
    Game.add(berry);
  }

  private static void makeInvincible(Entity entity) {
    entity
        .fetch(HealthComponent.class)
        .ifPresent(
            hc -> {
              hc.onHit(
                  (cause, damage) -> {
                    if (damage.damageType() != DamageType.HEAL
                        && damage.damageType() != DamageType.FALL) {
                      hc.receiveHit(new Damage(-damage.damageAmount(), DamageType.HEAL, entity));
                    }
                  });
            });
  }

  /** Creates a chest at the second custom point. */
  private void createChest() {
    try {
      chest = EntityFactory.newChest(Set.of(), customPoints().get(1).toCenteredPoint());
    } catch (IOException e) {
      throw new RuntimeException("Failed to create chest", e);
    }
    Game.add(chest);
  }

  /** Creates an NPC that gives the player a berry-fetching quest. */
  private void createNPC() {
    Entity npc = new Entity("NPC");
    npc.add(new PositionComponent(customPoints().get(0).toCenteredPoint()));

    try {
      npc.add(new DrawComponent(new SimpleIPath(NPC_TEXTURE_PATH)));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load NPC texture", e);
    }
    npc.add(new CollideComponent());
    npc.add(new HealthComponent(999, (entity -> {})));
    makeInvincible(npc);
    npc.add(
        new InteractionComponent(
            1,
            true,
            (entity, hero) ->
                DialogUtils.showTextPopup(
                    String.format(DIALOG_MESSAGE_START, BERRY_GOAL),
                    DIALOG_TITLE_HUNGER,
                    () -> {
                      entity.remove(InteractionComponent.class);
                      entity.add(
                          new InteractionComponent(
                              1,
                              true,
                              (entity1, entity2) ->
                                  YesNoDialog.showYesNoDialog(
                                      DIALOG_LOGIN,
                                      DIALOG_TITLE_HUNGER,
                                      () -> {
                                        int count = checkBerryCount();
                                        if (count < BERRY_GOAL) {
                                          DialogUtils.showTextPopup(
                                              String.format(DIALOG_MESSAGE_NOT_ENOUGH, BERRY_GOAL),
                                              DIALOG_TITLE_HUNGER);
                                        } else if (checkBerries()) {
                                          DialogUtils.showTextPopup(
                                              DIALOG_MESSAGE_SUCCESS, DIALOG_TITLE_SATISFIED);
                                          door.open();
                                          npc.remove(InteractionComponent.class);
                                          chest.remove(InteractionComponent.class);
                                          chest.add(
                                              new InteractionComponent(
                                                  1,
                                                  true,
                                                  (e1, e2) ->
                                                      DialogUtils.showTextPopup(
                                                          DIALOG_MESSAGE_CHEST,
                                                          DIALOG_TITLE_MINE)));
                                        } else {
                                          DialogUtils.showTextPopup(
                                              DIALOG_MESSAGE_TOXIC, DIALOG_TITLE_HUNGER);
                                        }
                                      },
                                      () ->
                                          DialogUtils.showTextPopup(
                                              DIALOG_MESSAGE_LATER, DIALOG_TITLE_HUNGER))));
                    })));
    Game.add(npc);
  }

  /**
   * Checks whether all berries in the chest are non-toxic.
   *
   * @return True if none of the berries are toxic; false otherwise.
   */
  private boolean checkBerries() {
    InventoryComponent ic = chest.fetch(InventoryComponent.class).get();
    long toxicCount =
        ic.items(Berry.class).stream().map(item -> (Berry) item).filter(Berry::isToxic).count();
    return toxicCount == 0;
  }

  /**
   * Counts the number of berries in the chest.
   *
   * @return The number of berry items.
   */
  private int checkBerryCount() {
    InventoryComponent ic = chest.fetch(InventoryComponent.class).get();
    return (int) ic.items(Berry.class).stream().count();
  }

  @Override
  protected void onTick() {
    // No per-tick logic needed for this level
  }
}
