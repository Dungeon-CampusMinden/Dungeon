package contrib.configuration;

import contrib.inventory.InventoryDialogState;
import core.configuration.ConfigKey;
import core.configuration.ConfigMap;
import core.configuration.values.ConfigIntValue;
import core.input.Keys;
import core.input.MouseButtons;

/**
 * Configuration class for mapping keyboard inputs to specific actions within the application.
 *
 * <p>The class is annotated with {@code @ConfigMap} to define a JSON-Path prefix for fields
 * used within it.
 *
 * <p>Each field in this class represents a specific gameplay or UI keybinding used by contrib
 * systems.
 */
@ConfigMap(path = {"keyboard"})
public class KeyboardConfig {
  /** This key is used to open the player's inventory. */
  public static final ConfigKey<Integer> INVENTORY_OPEN =
      new ConfigKey<>(new String[] {"inventory", "open"}, new ConfigIntValue(Keys.I));

  /** This key is used to close the player's inventory. */
  public static final ConfigKey<Integer> CLOSE_UI =
      new ConfigKey<>(new String[] {"ui", "close"}, new ConfigIntValue(Keys.ESCAPE));

  /** This key is used to interact with the world. */
  public static final ConfigKey<Integer> INTERACT_WORLD =
      new ConfigKey<>(new String[] {"interact", "world"}, new ConfigIntValue(Keys.E));

  /** This key is used to interact with the world. */
  public static final ConfigKey<Integer> MOUSE_INTERACT_WORLD =
      new ConfigKey<>(new String[] {"interact", "mouse"}, new ConfigIntValue(MouseButtons.RIGHT));

  /** This key is used to use an item. Only works if the player's inventory dialog is open. */
  public static final ConfigKey<Integer> USE_ITEM =
      new ConfigKey<>(new String[] {"item", "use"}, new ConfigIntValue(Keys.E));

  /**
   * This key is used to use an item. Only works if the player's inventory dialog is open.
   *
   * @see InventoryDialogState#isOpen(core.Entity)
   */
  public static final ConfigKey<Integer> MOUSE_USE_ITEM =
    new ConfigKey<>(new String[] {"item", "mouse"}, new ConfigIntValue(MouseButtons.RIGHT));

  /**
   * Quickly transfers an item from one inventory to another. E.g. chest to player or player to
   * crafting cauldron.
   */
  public static final ConfigKey<Integer> TRANSFER_ITEM =
      new ConfigKey<>(
          new String[] {"inventory", "transfer"}, new ConfigIntValue(MouseButtons.RIGHT));

  /** This key is used to use the active skill. */
  public static final ConfigKey<Integer> USE_SKILL =
      new ConfigKey<>(new String[] {"skill", "fireball"}, new ConfigIntValue(Keys.Q));

  /** Select the next skill as active Skill in the {@link contrib.components.SkillComponent}. */
  public static final ConfigKey<Integer> NEXT_SKILL =
      new ConfigKey<>(
          new String[] {"skill", "select next skill"}, new ConfigIntValue(Keys.PERIOD));

  /** Select the previous skill as active Skill in the {@link contrib.components.SkillComponent}. */
  public static final ConfigKey<Integer> PREV_SKILL =
      new ConfigKey<>(
          new String[] {"skill", "select prev skill"}, new ConfigIntValue(Keys.COMMA));

  /** This key is used shoot the active skill. */
  public static final ConfigKey<Integer> MOUSE_USE_SKILL =
      new ConfigKey<>(
          new String[] {"skill", "mouse_fireball"}, new ConfigIntValue(MouseButtons.LEFT));
}
