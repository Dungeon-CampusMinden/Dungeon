package contrib.configuration;

import core.configuration.ConfigKey;
import core.configuration.ConfigMap;
import core.configuration.values.ConfigIntValue;
import core.input.Keys;
import core.input.MouseButtons;

/** WTF? . */
@ConfigMap(path = {"keyboard"})
public class KeyboardConfig {
  /** WTF? . */
  public static final ConfigKey<Integer> INVENTORY_OPEN =
      new ConfigKey<>(new String[] {"inventory", "open"}, new ConfigIntValue(Keys.I));

  /** WTF? . */
  public static final ConfigKey<Integer> CLOSE_UI =
      new ConfigKey<>(new String[] {"ui", "close"}, new ConfigIntValue(Keys.ESCAPE));

  /** WTF? . */
  public static final ConfigKey<Integer> INTERACT_WORLD =
      new ConfigKey<>(new String[] {"interact", "world"}, new ConfigIntValue(Keys.E));

  /** This key is used to interact with the world. */
  public static final ConfigKey<Integer> MOUSE_INTERACT_WORLD =
      new ConfigKey<>(new String[] {"interact", "mouse"}, new ConfigIntValue(MouseButtons.RIGHT));

  /** WTF? . */
  public static final ConfigKey<Integer> USE_ITEM =
      new ConfigKey<>(new String[] {"item", "use"}, new ConfigIntValue(Keys.E));

  /**
   * This key is used to use an item. Only works if in Hero's inventory.
   *
   * @see contrib.hud.inventory.InventoryGUI#inPlayerInventory
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

  /** Keybinding to zoom in if debug controls are active. */
  public static final ConfigKey<Integer> DEBUG_ZOOM_IN =
      new ConfigKey<>(new String[] {"debug", "zoom_in"}, new ConfigIntValue(Keys.K));

  /** Keybinding to zoom out if debug controls are active. */
  public static final ConfigKey<Integer> DEBUG_ZOOM_OUT =
      new ConfigKey<>(new String[] {"debug", "zoom_out"}, new ConfigIntValue(Keys.L));

  /**
   * Keybinding to spawn a monster at the cursor location if debug controls are active.
   */
  public static final ConfigKey<Integer> DEBUG_SPAWN_MONSTER =
      new ConfigKey<>(new String[] {"debug", "spawn_monster"}, new ConfigIntValue(Keys.X));

  /**
   * Keybinding to teleport the player to the start tile if debug controls are active.
   */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_START =
      new ConfigKey<>(new String[] {"debug", "teleport_Start"}, new ConfigIntValue(Keys.J));

  /**
   * Keybinding to teleport the player next to the {@link core.level.elements.tile.ExitTile} if
   * debug controls are active.
   */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_END =
      new ConfigKey<>(new String[] {"debug", "teleport_end"}, new ConfigIntValue(Keys.H));

  /**
   * Keybinding to teleport the player onto the {@link core.level.elements.tile.ExitTile} if debug
   * controls are active.
   */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_ON_END =
      new ConfigKey<>(new String[] {"debug", "teleport_onEnd"}, new ConfigIntValue(Keys.G));

  /**
   * Keybinding to teleport the player to the cursor location if debug controls are active.
   */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_CURSOR =
      new ConfigKey<>(new String[] {"debug", "teleport_cursor"}, new ConfigIntValue(Keys.O));

  /** Keybinding to open all doors if debug controls are active. */
  public static final ConfigKey<Integer> DEBUG_OPEN_DOORS =
      new ConfigKey<>(new String[] {"open", "open_doors"}, new ConfigIntValue(Keys.C));

  /**
   * Keybinding to toggle the debug HUD if the active runtime provides debug rendering.
   */
  public static final ConfigKey<Integer> DEBUG_TOGGLE_HUD =
      new ConfigKey<>(new String[] {"debug", "toggle_hud"}, new ConfigIntValue(Keys.F3));
}
