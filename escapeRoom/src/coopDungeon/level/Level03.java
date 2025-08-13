package coopDungeon.level;

import contrib.components.AIComponent;
import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.entities.MiscFactory;
import contrib.hud.DialogUtils;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.YesNoDialog;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.utils.components.ai.AIUtils;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.DungeonLevel;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Level03 extends DungeonLevel {


  private ExitTile exit;
  /**
   * Creates a new Level03.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param customPoints The custom points of the level.
   */
  public Level03(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Coop 3");
  }

  @Override
  protected void onFirstTick() {
    DialogUtils.showTextPopup("HALLO? IST DA WER? ICH BRAUCHE HILFE?","HILFE!");
    npc();
    crafting();
    books();
    monster();
    chest();
    exit= (ExitTile) Game.randomTile(LevelElement.EXIT).get();
    exit.close();
  }

  private void npc() {
    Entity npc = new Entity();
    npc.add(new VelocityComponent(5));
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
                      entity.remove(InteractionComponent.class);
                      entity.add(
                          new InteractionComponent(
                              3,
                              true,
                              (entity1, entity2) ->
                                  YesNoDialog.showYesNoDialog(
                                      "Hast du das gegenmittel bei dir?",
                                      "Hilfe",
                                      () -> {
                                        boolean check = checkForHealItem(entity2);
                                        if (!check) {
                                          DialogUtils.showTextPopup(
                                              "Das ist nicht das richitge Mittel.", "Falsch.");
                                        } else {
                                          DialogUtils.showTextPopup("Danke", "Richtig");
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
    return player.fetch(InventoryComponent.class).filter(inventoryComponent -> inventoryComponent.hasItem(ItemPotionHealth.class)).isPresent();
  }

  private void moveNpc(Entity npc) {
    Point goal = customPoints.get(6).toCenteredPoint();
    npc.add(new AIComponent(entity -> {

    }, new Consumer<Entity>() {
      @Override
      public void accept(Entity entity) {
        if(!Game.tileAtEntity(entity).equals(Game.tileAT(goal))){
          AIUtils.move(entity, LevelUtils.calculatePath(entity.fetch(PositionComponent.class).get().position(),goal));
        }
      }
    }, entity -> false));
  }

  private void crafting() {
    // TODO add crafting recipes
    try {
      Game.add(MiscFactory.newCraftingCauldron(customPoints.get(1).toCenteredPoint()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void books() {}

  private void monster() {}

  private void chest() {
    try {
      Game.add(MiscFactory.newChest(Set.of(new ItemPotionHealth()),customPoints.get(2).toCenteredPoint()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void onTick() {}
}
