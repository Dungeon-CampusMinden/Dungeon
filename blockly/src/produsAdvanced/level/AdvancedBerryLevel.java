package produsAdvanced.level;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.EntityFactory;
import contrib.entities.WorldItemBuilder;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.YesNoDialog;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.IVoidFunction;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import level.BlocklyLevel;
import produsAdvanced.abstraction.Berry;

public class AdvancedBerryLevel extends BlocklyLevel {
  int BERRY_GOAL = 5;
  Entity chest;
  ExitTile door;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public AdvancedBerryLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Berry");
  }

  @Override
  protected void onFirstTick() {
    createNPC();
    createChest();
    spawnBerrys();
    // gibt nur eine Tür
    door = (ExitTile) Game.randomTile(LevelElement.EXIT).get();
    door.close();
  }

  private void spawnBerrys() {
    for (int i = 0; i < BERRY_GOAL; i++) {
      Game.add(
          WorldItemBuilder.buildWorldItem(
              new Berry(true),
              Game.randomTile(LevelElement.FLOOR).get().coordinate().toCenteredPoint()));
      Game.add(
          WorldItemBuilder.buildWorldItem(
              new Berry(false),
              Game.randomTile(LevelElement.FLOOR).get().coordinate().toCenteredPoint()));
    }
  }

  private void createChest() {
    try {
      chest = EntityFactory.newChest(Set.of(), customPoints().get(1).toCenteredPoint());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(chest);
  }

  private void createNPC() {
    Entity npc = new Entity("NPC");
    npc.add(new PositionComponent(customPoints().get(0).toCenteredPoint()));
    try {
      npc.add(new DrawComponent(new SimpleIPath("character/monster/orc_shaman")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    npc.add(
        new InteractionComponent(
            1,
            true,
            (entity, hero) ->
                DialogUtils.showTextPopup(
                    "Ich hab so einen Hunger Bring mir "
                        + BERRY_GOAL
                        + " Beeren, aber nicht die giftigen! Leg sie einfach in dien Kiste.",
                    "HUNGER!",
                    new IVoidFunction() {
                      @Override
                      public void execute() {
                        entity.remove(InteractionComponent.class);
                        entity.add(
                            new InteractionComponent(
                                1,
                                true,
                                (entity1, entity2) ->
                                    YesNoDialog.showYesNoDialog(
                                        "Kann ich die Beeren in der Kiste essen?",
                                        "HUNGER!",
                                        new IVoidFunction() {
                                          @Override
                                          public void execute() {
                                            int count = checkBerryCount();
                                            if (count < BERRY_GOAL)
                                              DialogUtils.showTextPopup(
                                                  "Was ist das denn? Davon werde ich ja nie satt. Ich hab gesagt du sollst mir "
                                                      + BERRY_GOAL
                                                      + " Beeren bringen, LOS!",
                                                  "HUNGER!");
                                            else {
                                              boolean success = checkBerrys();

                                              if (success) {
                                                DialogUtils.showTextPopup(
                                                    "Ich danke dir, ich habe schon so lange nichts gegessen. Ich werde die Tür für dich öffnen,",
                                                    "Satt!");
                                                door.open();
                                              } else {
                                                DialogUtils.showTextPopup(
                                                    "Willst du mich umbringen? Da sind giftige dabei!",
                                                    "HUNGER!");
                                              }
                                            }
                                          }
                                        },
                                        () ->
                                            DialogUtils.showTextPopup(
                                                "Mach schnell, ich habe echt hunger,",
                                                "HUNGER!"))));
                      }
                    })));
    Game.add(npc);
  }

  private boolean checkBerrys() {
    InventoryComponent ic = chest.fetch(InventoryComponent.class).get();
    int toxicBerrys =
        (int)
            ic.items(Berry.class).stream().map(item -> (Berry) item).filter(Berry::isToxic).count();
    return toxicBerrys == 0;
  }

  private int checkBerryCount() {
    InventoryComponent ic = chest.fetch(InventoryComponent.class).get();
    return (int) ic.items(Berry.class).stream().count();
  }

  @Override
  protected void onTick() {}
}
