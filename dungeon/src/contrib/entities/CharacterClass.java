package contrib.entities;

import contrib.item.Item;
import contrib.item.concreteItem.ItemPotionHealth;
import contrib.item.concreteItem.ItemWoodenArrow;
import contrib.item.concreteItem.ItemWoodenBow;
import contrib.utils.components.skill.Resource;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.projectileSkill.BowSkill;
import contrib.utils.components.skill.projectileSkill.FireWallSkill;
import contrib.utils.components.skill.projectileSkill.FireballSkill;
import contrib.utils.components.skill.selfSkill.DashSkill;
import contrib.utils.components.skill.selfSkill.SelfHealSkill;
import core.utils.Tuple;
import core.utils.Vector2;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Set;

/** Defines the Classes a Hero can be. */
public enum CharacterClass {
  /**
   * Wizard character class.
   *
   * <p>A magic-focused class specializing in ranged spell attacks and self-healing. Ideal for
   * players who prefer high damage from a distance and utility spells.
   */
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

  /**
   * Hunter character class.
   *
   * <p>A bow-focused class with high durability and stamina, starts with bow and arrows and can do
   * a short bursts of speed.
   */
  HUNTER(
      "character/knight",
      Vector2.of(4, 4),
      3f,
      35,
      Set.of(
          new BowSkill(SkillTools::cursorPositionAsPoint),
          new DashSkill(5, 180, 120, new Tuple<>(Resource.STAMINA, 20))),
      Set.of(
          new ItemWoodenBow(),
          new ItemWoodenArrow(ItemWoodenArrow.MAX_ARROW_STACK_SIZE),
          new ItemWoodenArrow(ItemWoodenArrow.MAX_ARROW_STACK_SIZE),
          new ItemWoodenArrow(ItemWoodenArrow.MAX_ARROW_STACK_SIZE)),
      10,
      0,
      0,
      120,
      5);

  private final IPath textures;
  private final Vector2 speed;
  private final float mass;
  private final int hp;
  private final Set<Skill> startSkills;
  private final Set<Item> startItems;
  private final int inventorySize;
  private final int mana;
  private final float manaRestore;
  private final int stamina;
  private final float staminaRestore;

  /**
   * Constructs a new {@code CharacterClass} with the specified attributes.
   *
   * <p>This constructor initializes all core properties of a character class, including movement,
   * health, skills, items, and resource pools.
   *
   * @param textures the path to the character's texture or sprite
   * @param speed the base movement speed of the character
   * @param mass the mass of the character, used in physics calculations
   * @param hp the starting health points of the character
   * @param startSkills the set of skills the character starts with
   * @param startItems the set of items the character starts with
   * @param inventorySize the maximum number of items the character can carry
   * @param mana the starting mana points
   * @param manaRestore the rate at which mana regenerates over time
   * @param energy the starting stamina (or energy) points
   * @param energyRestore the rate at which stamina regenerates over time
   */
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
    this.stamina = energy;
    this.staminaRestore = energyRestore;
  }

  /**
   * Returns the texture path or sprite representation for this character class.
   *
   * @return the {@link IPath} representing the character's textures
   */
  public IPath textures() {
    return textures;
  }

  /**
   * Returns the base movement speed of the character class.
   *
   * @return the {@link Vector2} representing the speed
   */
  public Vector2 speed() {
    return speed;
  }

  /**
   * Returns the mass of the character class, used for physics calculations.
   *
   * @return the mass as a float
   */
  public float mass() {
    return mass;
  }

  /**
   * Returns the starting health points of the character class.
   *
   * @return the initial HP
   */
  public int hp() {
    return hp;
  }

  /**
   * Returns the set of skills the character starts with.
   *
   * @return a {@link Set} of {@link Skill} objects
   */
  public Set<Skill> startSkills() {
    return startSkills;
  }

  /**
   * Returns the set of items the character starts with.
   *
   * @return a {@link Set} of {@link Item} objects
   */
  public Set<Item> startItems() {
    return startItems;
  }

  /**
   * Returns the maximum number of items the character can carry.
   *
   * @return the inventory size
   */
  public int inventorySize() {
    return inventorySize;
  }

  /**
   * Returns the starting mana points of the character class.
   *
   * @return the initial mana
   */
  public int mana() {
    return mana;
  }

  /**
   * Returns the mana regeneration rate for the character class.
   *
   * @return the mana restore value per tick or unit of time
   */
  public float manaRestore() {
    return manaRestore;
  }

  /**
   * Returns the starting stamina points of the character class.
   *
   * @return the initial energy
   */
  public int stamina() {
    return stamina;
  }

  /**
   * Returns the stamina regeneration rate for the character class.
   *
   * @return the stamina restore value per tick or unit of time
   */
  public float staminaRestore() {
    return staminaRestore;
  }
}
