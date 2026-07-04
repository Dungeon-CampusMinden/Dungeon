package contrib.item.concreteItem;

import static org.junit.jupiter.api.Assertions.*;

import contrib.item.HealthPotionType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ItemPotionHealthTest {

  @Test
  void type_ShouldReturnPotionType() {
    ItemPotionHealth potion = new ItemPotionHealth(HealthPotionType.WEAK);

    assertEquals(HealthPotionType.WEAK, potion.type());
  }

  @Test
  void healAmount_ShouldReturnHealAmountOfPotionType() {
    ItemPotionHealth potion = new ItemPotionHealth(HealthPotionType.WEAK);

    assertEquals(HealthPotionType.WEAK.getHealAmount(), potion.healAmount());
  }

  @Test
  void itemData_ShouldContainPotionTypeAndHealAmount() {
    ItemPotionHealth potion = new ItemPotionHealth(HealthPotionType.WEAK);

    Map<String, String> data = potion.itemData();

    assertTrue(data.containsValue(HealthPotionType.WEAK.name()));
    assertTrue(data.containsValue(Integer.toString(HealthPotionType.WEAK.getHealAmount())));
  }

  @Test
  void equals_ShouldReturnTrueForSameHealAmount() {
    ItemPotionHealth potion1 = new ItemPotionHealth(HealthPotionType.WEAK);
    ItemPotionHealth potion2 = new ItemPotionHealth(HealthPotionType.WEAK);

    assertEquals(potion1, potion2);
  }

  @Test
  void hashCode_ShouldBeSameForSameHealAmount() {
    ItemPotionHealth potion1 = new ItemPotionHealth(HealthPotionType.WEAK);
    ItemPotionHealth potion2 = new ItemPotionHealth(HealthPotionType.WEAK);

    assertEquals(potion1.hashCode(), potion2.hashCode());
  }
}
