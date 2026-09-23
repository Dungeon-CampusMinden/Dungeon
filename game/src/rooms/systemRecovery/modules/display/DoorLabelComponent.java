package rooms.systemRecovery.modules.display;

import engine.Component;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Links a door label to the authoritative completion condition of its prerequisite riddle.
 *
 * @param completed supplier for the current authoritative completion state
 */
public record DoorLabelComponent(BooleanSupplier completed) implements Component {
  /** Shared spawn/snapshot key, including for players joining after a riddle is solved. */
  public static final String METADATA_KEY = "systemRecovery.doorLabel.completed";

  /** Exports the current prerequisite state without loading graphical resources. */
  public static void appendMetadata(Entity entity, Map<String, String> metadata) {
    entity
        .fetch(DoorLabelComponent.class)
        .ifPresent(
            label ->
                metadata.put(METADATA_KEY, Boolean.toString(label.completed().getAsBoolean())));
  }

  /** Applies the server's label color on graphical clients only. */
  public static void applyMetadata(Entity entity, Map<String, String> metadata) {
    String completed = metadata.get(METADATA_KEY);
    if (completed != null) updateAppearance(entity, Boolean.parseBoolean(completed));
  }

  /** Recolors the display lettering without tinting the housing or adding an outline. */
  public static void updateAppearance(Entity entity, boolean completed) {
    if (Game.isHeadless()) return;
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            draw -> {
              draw.tintColor(-1);
              if (draw.shaders().get("doorLabelStatus") instanceof DisplayTextStatusShader shader) {
                shader.completed(completed);
              } else {
                draw.shaders().remove("doorLabelStatus");
                draw.shaders().add("doorLabelStatus", new DisplayTextStatusShader(completed));
              }
            });
  }
}
