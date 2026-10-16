package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import feature.entities.CharacterClass;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.Arrays;
import java.util.List;

/** Authored marker that makes one player character class available for a level. */
public final class AvailableCharacterPrefab extends Prefab {

  private static final String TYPE = "available-character";
  private static final List<String> CHARACTER_CLASS_CHOICES =
      Arrays.stream(CharacterClass.values()).map(CharacterClass::name).toList();

  public static final PrefabProperty<String> CHARACTER_CLASS =
      PrefabProperty.selection(
          "characterClass",
          "Character Class",
          CharacterClass.WIZARD.name(),
          CHARACTER_CLASS_CHOICES);

  /** Maximum number of players using this character; {@code -1} allows unlimited players. */
  public static final PrefabProperty<Integer> MAX_COUNT =
      PrefabProperty.integer("maxCount", "Max Count", -1, -1, Integer.MAX_VALUE);

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(CHARACTER_CLASS, MAX_COUNT);

  /** Creates the available-character marker definition. */
  public AvailableCharacterPrefab() {
    super(TYPE, "Available Character", PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored available-character marker.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public AvailableCharacterPrefab(ILevel level, String name) {
    super(TYPE, "Available Character", PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Returns the character class selected by this marker.
   *
   * @return selected player character class
   * @throws IllegalStateException if the marker is no longer active
   */
  public CharacterClass characterClass() {
    return CharacterClass.valueOf(CHARACTER_CLASS.get(activeInstance()));
  }

  /**
   * Returns the maximum number of players allowed to select this character.
   *
   * @return maximum count, or {@code -1} for unlimited players
   * @throws IllegalStateException if the marker is no longer active
   */
  public int maxCount() {
    return MAX_COUNT.get(activeInstance());
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    return List.of();
  }

  @Override
  protected boolean isLegacyProperty(String key) {
    return "position".equals(key);
  }

  private PrefabInstance activeInstance() {
    return currentInstance()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Available character marker '" + name() + "' is not active"));
  }
}
