package starter;

import contrib.entities.MiscFactory;
import contrib.entities.WorldItemBuilder;
import contrib.item.HealthPotionType;
import contrib.item.concreteItem.ItemPotionHealth;
import core.Game;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.Map;

public class TestLevel extends DungeonLevel {
  public TestLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "playground");
  }

  @Override
  public void onFirstTick() {
    System.out.println("TestLevel started");
    var chest_p = getPoint("chest");
    var chest = MiscFactory.newChest(MiscFactory.FILL_CHEST.RANDOM);
    chest.fetch(PositionComponent.class).get().position(chest_p);
    Game.add(chest);
    var cauldron_p = getPoint("cauldron");
    var cauldron = MiscFactory.newCraftingCauldron(cauldron_p);
    Game.add(cauldron);
    var lose_item_p = getPoint("loseItem");
    var lose_item =
        WorldItemBuilder.buildWorldItemSimpleInteraction(
            new ItemPotionHealth(HealthPotionType.GREATER), lose_item_p);
    Game.add(lose_item);
  }
}
