package contrib.entities;

import contrib.item.Item;
import contrib.item.concreteItem.ItemKey;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemWoodenArrow;
import contrib.item.concreteItem.ItemWoodenBow;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.BowSkill;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import contrib.utils.components.skill.selfSkill.DashSkill;
import contrib.utils.components.skill.selfSkill.SelfHealSkill;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Set;

public enum CharacterClass {
  WIZARD(
      "character/wizard",
      Vector2.of(5, 5),
      1.3f,
      15,
      Set.of(
          new FireballSkill(SkillTools::cursorPositionAsPoint, new Tuple<>(Resource.MANA, 30)),
          new SelfHealSkill(300, 5, new Tuple<>(Resource.MANA, 80))),
      Set.of(new ItemPotionHealth()),
      6,
      100,
      10,
      50,
      5),
  KNIGHT(
      "character/knight",
      Vector2.of(4, 4),
      3f,
      35,
      Set.of(
          new BowSkill(SkillTools::cursorPositionAsPoint),
          new DashSkill(5, 180, 120, new Tuple<>(Resource.ENERGY, 20))),
      Set.of(
          new ItemWoodenBow(),
          new ItemWoodenArrow(ItemWoodenArrow.MAX_ARROW_STACK_SIZE),
          new ItemWoodenArrow(ItemWoodenArrow.MAX_ARROW_STACK_SIZE),
          new ItemWoodenArrow(ItemWoodenArrow.MAX_ARROW_STACK_SIZE)),
      10,
      0,
      0,
      120,
      5),

  GOBLIN (
          "character/monster/goblin",
          Vector2.of(20,20),
          1,
          100,
          Set.of(),
          Set.of(new ItemKey()),
          10,
          0,
          0,
          0,
          0
  );

  private final IPath textures;
  private final Vector2 speed;
  private final float mass;
  private final int hp;
  private final Set<Skill> startSkills;
  private final Set<Item> startItems;
  private final int inventorySize;
  private final int mana;
  private final float manaRestore;
  private final int energy;
  private final float energyRestore;

  CharacterClass(
      String textures,
      Vector2 speed,
      float mass,
      int hp,
      Set<Skill> startSkills,
      Set<Item> startItems,
      int inventorySize,
      int mana,
      float manaRestore,
      int energy,
      float energyRestore) {
    this.textures = new SimpleIPath(textures);
    this.speed = speed;
    this.mass = mass;
    this.hp = hp;
    this.startSkills = startSkills;
    this.startItems = startItems;
    this.inventorySize = inventorySize;
    this.mana = mana;
    this.manaRestore = manaRestore;
    this.energy = energy;
    this.energyRestore = energyRestore;
  }

  public IPath textures() {
    return textures;
  }

  public Vector2 speed() {
    return speed;
  }

  public float mass() {
    return mass;
  }

  public int hp() {
    return hp;
  }

  public Set<Skill> startSkills() {
    return startSkills;
  }

  public Set<Item> startItems() {
    return startItems;
  }

  public int inventorySize() {
    return inventorySize;
  }

  public int mana() {
    return mana;
  }

  public float manaRestore() {
    return manaRestore;
  }

  public int energy() {
    return energy;
  }

  public float energyRestore() {
    return energyRestore;
  }
}
