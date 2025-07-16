package contrib.configuration;

import com.badlogic.gdx.Input;
import core.components.PlayerComponent;
import core.configuration.ConfigKey;
import core.configuration.ConfigMap;
import core.configuration.values.ConfigIntValue;
import core.utils.Point;

/** WTF? . */
@ConfigMap(path = {"keyboard"})
public class KeyboardConfig {
  /** WTF? . */
  public static final ConfigKey<Integer> INVENTORY_OPEN =
      new ConfigKey<>(new String[] {"inventory", "open"}, new ConfigIntValue(Input.Keys.I));

  /**
   * If Mouse Movement is enabled. This key is used to open and close the inventory.
   *
   * @see contrib.entities.HeroFactory#ENABLE_MOUSE_MOVEMENT
   */
  public static final ConfigKey<Integer> MOUSE_INVENTORY_TOGGLE =
      new ConfigKey<>(
          new String[] {"inventory", "mouse"}, new ConfigIntValue(Input.Buttons.MIDDLE));

  /** WTF? . */
  public static final ConfigKey<Integer> CLOSE_UI =
      new ConfigKey<>(new String[] {"ui", "close"}, new ConfigIntValue(Input.Keys.ESCAPE));

  /** WTF? . */
  public static final ConfigKey<Integer> INTERACT_WORLD =
      new ConfigKey<>(new String[] {"interact", "world"}, new ConfigIntValue(Input.Keys.E));

  /**
   * If Mouse Movement is enabled. This key is used to interact with the world.
   *
   * @see contrib.entities.HeroFactory#ENABLE_MOUSE_MOVEMENT
   */
  public static final ConfigKey<Integer> MOUSE_INTERACT_WORLD =
      new ConfigKey<>(new String[] {"interact", "mouse"}, new ConfigIntValue(Input.Buttons.LEFT));

  /** WTF? . */
  public static final ConfigKey<Integer> USE_ITEM =
      new ConfigKey<>(new String[] {"item", "use"}, new ConfigIntValue(Input.Keys.E));

  /**
   * If Mouse Movement is enabled. This key is used to use an item. Only works if in Hero's
   * inventory.
   *
   * @see contrib.entities.HeroFactory#ENABLE_MOUSE_MOVEMENT
   * @see contrib.hud.inventory.InventoryGUI#inHeroInventory
   */
  public static final ConfigKey<Integer> MOUSE_USE_ITEM =
      new ConfigKey<>(new String[] {"item", "mouse"}, new ConfigIntValue(Input.Buttons.RIGHT));

  /**
   * Quickly transfers an item from one inventory to another. E.g. chest to player or player to
   * crafting cauldron.
   */
  public static final ConfigKey<Integer> TRANSFER_ITEM =
      new ConfigKey<>(
          new String[] {"inventory", "transfer"}, new ConfigIntValue(Input.Buttons.RIGHT));

  /** WTF? . */
  public static final ConfigKey<Integer> FIRST_SKILL =
      new ConfigKey<>(new String[] {"skill", "fireball"}, new ConfigIntValue(Input.Keys.Q));

  /**
   * If Mouse Movement is enabled. This key is used shoot the first skill.
   *
   * <p>If {@link #MOUSE_INTERACT_WORLD} is set to the same value as this key, the hero will only
   * shoot if nothing interactable is under the cursor.
   *
   * @see contrib.entities.HeroFactory#ENABLE_MOUSE_MOVEMENT
   * @see contrib.entities.HeroFactory#checkIfClickOnInteractable(Point)
   * @see contrib.entities.HeroFactory#registerMouseLeftClick(PlayerComponent)
   */
  public static final ConfigKey<Integer> MOUSE_FIRST_SKILL =
      new ConfigKey<>(
          new String[] {"skill", "mouse_fireball"}, new ConfigIntValue(Input.Buttons.LEFT));

  /**
   * If Mouse Movement is enabled. This key is used to move the hero.
   *
   * @see contrib.entities.HeroFactory#ENABLE_MOUSE_MOVEMENT
   */
  public static final ConfigKey<Integer> MOUSE_MOVE =
      new ConfigKey<>(
          new String[] {"movement", "mouse_move"}, new ConfigIntValue(Input.Buttons.RIGHT));

  /** Keybinding to zoom in ,if the {@link contrib.utils.components.Debugger} is active. */
  public static final ConfigKey<Integer> DEBUG_ZOOM_IN =
      new ConfigKey<>(new String[] {"debug", "zoom_in"}, new ConfigIntValue(Input.Keys.K));

  /** Keybinding to zoom out ,if the {@link contrib.utils.components.Debugger} is active. */
  public static final ConfigKey<Integer> DEBUG_ZOOM_OUT =
      new ConfigKey<>(new String[] {"debug", "zoom_out"}, new ConfigIntValue(Input.Keys.L));

  /**
   * Keybinding to spawn a monster on courser location, if the {@link
   * contrib.utils.components.Debugger} is active.
   */
  public static final ConfigKey<Integer> DEBUG_SPAWN_MONSTER =
      new ConfigKey<>(new String[] {"debug", "spawn_monster"}, new ConfigIntValue(Input.Keys.X));

  /**
   * Keybinding to teleport the hero on the Start-Tile, if the {@link
   * contrib.utils.components.Debugger} is active.
   */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_START =
      new ConfigKey<>(new String[] {"debug", "teleport_Start"}, new ConfigIntValue(Input.Keys.J));

  /**
   * Keybinding to teleport the hero next to the {@link core.level.elements.tile.ExitTile}, if the
   * {@link contrib.utils.components.Debugger} is active.
   */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_END =
      new ConfigKey<>(new String[] {"debug", "teleport_end"}, new ConfigIntValue(Input.Keys.H));

  /**
   * Keybinding to teleport the hero on the {@link core.level.elements.tile.ExitTile}, if the {@link
   * contrib.utils.components.Debugger} is active.
   */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_ON_END =
      new ConfigKey<>(new String[] {"debug", "teleport_onEnd"}, new ConfigIntValue(Input.Keys.G));

  /**
   * Keybinding to teleport the hero to the cursor mouse location, if the {@link
   * contrib.utils.components.Debugger} is active.
   */
  public static final ConfigKey<Integer> DEBUG_TELEPORT_TO_CURSOR =
      new ConfigKey<>(new String[] {"debug", "teleport_cursor"}, new ConfigIntValue(Input.Keys.O));

  /** Keybinding to open all doors, if the {@link contrib.utils.components.Debugger} is active. */
  public static final ConfigKey<Integer> DEBUG_OPEN_DOORS =
      new ConfigKey<>(new String[] {"open", "open_doors"}, new ConfigIntValue(Input.Keys.C));
}
