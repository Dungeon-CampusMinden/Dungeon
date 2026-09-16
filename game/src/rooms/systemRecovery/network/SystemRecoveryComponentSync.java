package rooms.systemRecovery.network;

import engine.Entity;
import engine.components.DrawComponent;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.collision.CollideSync;
import feature.components.CollideComponent;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import rooms.systemRecovery.modules.display.DisplayTextComponent;
import rooms.systemRecovery.modules.display.DoorLabelComponent;

/**
 * Synchronizes generic System Recovery components shared by snapshots and initial entity spawns.
 */
public final class SystemRecoveryComponentSync {

  private static final CollideSync COLLIDE_SYNC =
      CollideSync.withPrefix(SystemRecoveryEntitySpawnStrategy.METADATA_COLLIDER_PREFIX);

  private SystemRecoveryComponentSync() {}

  /**
   * Applies all generic System Recovery metadata to one client-side entity.
   *
   * @param entity client-side entity receiving the metadata
   * @param metadata synchronized metadata
   */
  public static void applyEntityMetadata(Entity entity, Map<String, String> metadata) {
    applyInteractableMetadata(entity, metadata);
    keypadFromMetadata(metadata).ifPresent(keypad -> applyKeypadState(entity, keypad));
    applyDisplayMetadata(entity, metadata);
    collideComponentFromMetadata(metadata)
        .ifPresent(collideState -> COLLIDE_SYNC.apply(entity, collideState));
  }

  /**
   * Applies a synchronized keypad component during an initial spawn or a snapshot update.
   *
   * @param entity client-side keypad entity
   * @param metadata synchronized keypad metadata
   */
  public static void applyKeypadMetadata(Entity entity, Map<String, String> metadata) {
    keypadFromMetadata(metadata).ifPresent(keypad -> applyKeypadState(entity, keypad));
  }

  /**
   * Applies a synchronized display key on the client that renders it.
   *
   * @param entity client-side display entity
   * @param metadata synchronized display metadata
   */
  public static void applyDisplayMetadata(Entity entity, Map<String, String> metadata) {
    String text = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_DISPLAY_TEXT);
    if (text == null) return;
    entity
        .fetch(DisplayTextComponent.class)
        .ifPresentOrElse(
            display -> display.text(text), () -> entity.add(new DisplayTextComponent(text)));
  }

  /**
   * Applies interactability, door-label and active-module-socket metadata to an entity.
   *
   * @param entity client-side entity
   * @param metadata synchronized interaction metadata
   */
  public static void applyInteractableMetadata(Entity entity, Map<String, String> metadata) {
    DoorLabelComponent.applyMetadata(entity, metadata);
    String interactable = metadata.get(SystemRecoveryEntitySpawnStrategy.METADATA_INTERACTABLE);
    if (interactable == null) return;

    if (Boolean.parseBoolean(interactable)) {
      if (!entity.isPresent(InteractionComponent.class)) {
        entity.add(new InteractionComponent());
      }
    } else {
      entity.remove(InteractionComponent.class);
    }

    if (Boolean.parseBoolean(
        metadata.getOrDefault(
            SystemRecoveryEntitySpawnStrategy.METADATA_MODULE_SOCKET_ACTIVE, "false"))) {
      entity.add(
          new DrawComponent(new Animation(new SimpleIPath("objects/tech/Screen_info_1.png"))));
    }
  }

  /**
   * Creates a collider from synchronized metadata when the metadata is valid.
   *
   * @param metadata synchronized collider metadata
   * @return reconstructed collider, if the metadata is valid
   */
  public static Optional<CollideComponent> collideComponentFromMetadata(
      Map<String, String> metadata) {
    return COLLIDE_SYNC.fromMetadata(metadata);
  }

  /**
   * Appends keypad state to server metadata used by both spawns and regular snapshots.
   *
   * @param keypad keypad whose state should be encoded
   * @param metadata metadata map receiving the keypad state
   */
  public static void appendKeypadMetadata(KeypadComponent keypad, Map<String, String> metadata) {
    metadata.put(SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD, "true");
    metadata.put(
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_CORRECT_DIGITS,
        digitsToString(keypad.correctDigits()));
    metadata.put(
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_ENTERED_DIGITS,
        digitsToString(keypad.enteredDigits()));
    metadata.put(
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_UNLOCKED,
        String.valueOf(keypad.isUnlocked()));
    metadata.put(
        SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_SHOW_DIGIT_COUNT,
        String.valueOf(keypad.showDigitCount()));
  }

  private static Optional<KeypadComponent> keypadFromMetadata(Map<String, String> metadata) {
    if (!Boolean.parseBoolean(
        metadata.getOrDefault(SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD, "false"))) {
      return Optional.empty();
    }
    return Optional.of(
        new KeypadComponent(
            parseDigits(
                metadata.getOrDefault(
                    SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_CORRECT_DIGITS, "")),
            parseDigits(
                metadata.getOrDefault(
                    SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_ENTERED_DIGITS, "")),
            Boolean.parseBoolean(
                metadata.getOrDefault(
                    SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_UNLOCKED, "false")),
            Boolean.parseBoolean(
                metadata.getOrDefault(
                    SystemRecoveryEntitySpawnStrategy.METADATA_KEYPAD_SHOW_DIGIT_COUNT, "true"))));
  }

  private static void applyKeypadState(Entity entity, KeypadComponent state) {
    KeypadComponent keypad = entity.fetch(KeypadComponent.class).orElse(null);
    if (keypad == null) {
      entity.add(state);
      return;
    }
    keypad.enteredDigits().clear();
    keypad.enteredDigits().addAll(state.enteredDigits());
    keypad.isUnlocked(state.isUnlocked());
    keypad.showDigitCount(state.showDigitCount());
  }

  private static List<Integer> parseDigits(String value) {
    if (value.isBlank()) return new ArrayList<>();
    List<Integer> digits = new ArrayList<>();
    for (String digit : value.split(",")) {
      try {
        digits.add(Integer.parseInt(digit));
      } catch (NumberFormatException ignored) {
        return new ArrayList<>();
      }
    }
    return digits;
  }

  private static String digitsToString(List<Integer> digits) {
    return digits.stream()
        .map(String::valueOf)
        .reduce((left, right) -> left + "," + right)
        .orElse("");
  }
}
