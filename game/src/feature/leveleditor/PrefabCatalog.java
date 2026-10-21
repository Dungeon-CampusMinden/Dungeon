package feature.leveleditor;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import engine.components.DrawComponent;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.entities.deco.Deco;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Sorts the registered prefabs into the categories of the prefab palette and assigns their palette
 * icons.
 *
 * <p>Prefabs that are not listed in any category end up in an "Other" category, prefabs without an
 * icon are shown by their name. Every prefab is also listed in the "All" category.
 */
public final class PrefabCatalog {

  private static final List<CategoryDefinition> CATEGORIES =
      List.of(
          new CategoryDefinition(
              "Switches",
              "lever",
              "pressure-plate",
              "torch",
              "keypad",
              "text-keypad",
              "deco-interaction"),
          new CategoryDefinition(
              "Puzzles & Items", "image-puzzle", "item-receptacle", "pushable-stone"),
          new CategoryDefinition(
              "Effects & Logic", "door-control", "dialog-trigger", "show-image", "random-code"),
          new CategoryDefinition("Visuals", "water", "shader", "level-hider"),
          new CategoryDefinition(
              "Level Setup", "invisible-wall", "available-character", "design-label-region"));

  private static final Map<String, Supplier<Icon>> ICONS =
      Map.ofEntries(
          Map.entry("lever", () -> spritesheet("objects/lever", "on", null)),
          Map.entry("pressure-plate", () -> spritesheet("objects/pressureplate", "off", null)),
          Map.entry("torch", () -> spritesheet("objects/torch", "on", null)),
          Map.entry("keypad", () -> texture("objects/keypad/on.png", "123")),
          Map.entry("text-keypad", () -> texture("objects/keypad/off.png", "ABC")),
          Map.entry("deco-interaction", () -> deco(Deco.BookshelfLarge, "!")),
          Map.entry("image-puzzle", () -> texture("items/puzzle-piece.png", null)),
          Map.entry("item-receptacle", () -> deco(Deco.Cabinet, null)),
          Map.entry("pushable-stone", () -> texture("objects/push-stone.png", null)),
          Map.entry("door-control", () -> texture("dungeon/default/door/top_closed.png", null)),
          Map.entry("dialog-trigger", () -> texture("emotes/emote_dots3.png", null)),
          Map.entry("show-image", () -> texture("items/rpg/item_paper_map.png", null)),
          Map.entry("water", () -> texture("items/rpg/item_bucket_wide_water.png", null)),
          Map.entry(
              "available-character", () -> spritesheet("character/knight", "idle_down", null)),
          Map.entry("random-code", () -> text("RNG Number")),
          Map.entry("shader", () -> text("Shader")),
          Map.entry("level-hider", () -> text("Level Hider")),
          Map.entry("invisible-wall", () -> text("Invis Wall")),
          Map.entry("design-label-region", () -> text("Design Label")));

  private static final Map<String, Icon> LOADED_ICONS = new HashMap<>();

  private PrefabCatalog() {}

  /**
   * Returns the palette categories with the registered prefabs they contain.
   *
   * @return non-empty categories in palette order, ending with "All"
   */
  public static List<Category> categories() {
    Map<String, Prefab> byType = new HashMap<>();
    for (Prefab prefab : PrefabRegistry.all()) byType.put(prefab.type(), prefab);
    Set<String> categorized = new HashSet<>();
    List<Category> categories = new ArrayList<>();
    for (CategoryDefinition definition : CATEGORIES) {
      List<Prefab> prefabs = new ArrayList<>();
      for (String type : definition.types()) {
        Prefab prefab = byType.get(type);
        if (prefab == null) continue;
        prefabs.add(prefab);
        categorized.add(type);
      }
      if (!prefabs.isEmpty()) categories.add(new Category(definition.name(), prefabs));
    }
    List<Prefab> other = new ArrayList<>();
    for (Prefab prefab : PrefabRegistry.all()) {
      if (!categorized.contains(prefab.type())) other.add(prefab);
    }
    if (!other.isEmpty()) categories.add(new Category("Other", other));
    List<Prefab> all = new ArrayList<>();
    for (Category category : categories) all.addAll(category.prefabs());
    categories.add(new Category("All", all));
    return categories;
  }

  /**
   * Returns the palette icon of a prefab, loading its image on first use.
   *
   * @param prefab prefab definition
   * @return icon of the prefab, or its name if it has no icon
   */
  public static Icon icon(Prefab prefab) {
    return LOADED_ICONS.computeIfAbsent(
        prefab.type(),
        type -> {
          Supplier<Icon> icon = ICONS.get(type);
          return icon == null ? text(prefab.displayName()) : icon.get();
        });
  }

  private static Icon texture(String path, String badge) {
    return new Icon(
        new TextureRegion(TextureMap.instance().textureAt(new SimpleIPath(path))), badge);
  }

  private static Icon spritesheet(String path, String state, String badge) {
    Map<String, Animation> animations = Animation.loadAnimationSpritesheet(new SimpleIPath(path));
    Animation animation = animations.get(state);
    if (animation == null) animation = animations.values().iterator().next();
    return new Icon(new TextureRegion(animation.getSprite()), badge);
  }

  private static Icon deco(Deco deco, String badge) {
    return new Icon(
        new TextureRegion(new DrawComponent(deco.path(), deco.config()).getSprite()), badge);
  }

  private static Icon text(String name) {
    return new Icon(null, name);
  }

  /**
   * A named group of prefabs shown together in the palette.
   *
   * @param name category name
   * @param prefabs prefabs in palette order
   */
  public record Category(String name, List<Prefab> prefabs) {}

  /**
   * Visual representation of a prefab in the palette.
   *
   * @param image icon image, or {@code null} if the prefab is shown by {@code label} alone
   * @param label small badge drawn over the image, or the shown name if there is no image; may be
   *     {@code null} for images without a badge
   */
  public record Icon(TextureRegion image, String label) {}

  private record CategoryDefinition(String name, String... types) {}
}
