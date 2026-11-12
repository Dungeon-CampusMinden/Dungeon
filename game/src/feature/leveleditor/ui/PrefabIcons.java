package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import feature.leveleditor.PrefabCatalog;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabRegistry;
import java.util.Locale;
import java.util.Optional;

/**
 * Creates the icons of prefab types for the editor panels: the palette image of the prefab, or a
 * tile in the color of its category with the initials of its name.
 */
public final class PrefabIcons {

  private static final Color TILE_TEXT = new Color(0.08f, 0.08f, 0.1f, 1f);

  private PrefabIcons() {}

  /**
   * Creates the icon of a prefab type.
   *
   * @param type prefab type
   * @param size displayed size in pixels, decides the font size of tiles
   * @return a new actor
   */
  public static Actor create(String type, float size) {
    Optional<Prefab> prefab = PrefabRegistry.find(type);
    if (prefab.isPresent()) {
      PrefabCatalog.Icon icon = PrefabCatalog.icon(prefab.get());
      if (icon.image() != null) {
        Image image = new Image(icon.image());
        image.setScaling(Scaling.fit);
        image.setTouchable(Touchable.disabled);
        return image;
      }
    }
    String name = prefab.map(Prefab::displayName).orElse(type);
    return tile(initials(name), EditorTheme.categoryColor(PrefabCatalog.categoryOf(type)), size);
  }

  /**
   * Creates a colored tile with a short text.
   *
   * @param text tile text, one or two characters
   * @param color tile color
   * @param size displayed size in pixels
   * @return a new actor
   */
  public static Actor tile(String text, Color color, float size) {
    Table tile = new Table();
    tile.setTouchable(Touchable.disabled);
    tile.setBackground(EditorTheme.rounded(color, null, size >= 24 ? 5 : 3));
    Label label =
        EditorTheme.boldLabel(
            text, Math.max(9, Math.round(size * (text.length() > 1 ? 0.5f : 0.62f))), TILE_TEXT);
    label.setAlignment(Align.center);
    tile.add(label).center();
    return tile;
  }

  /**
   * Returns up to two initials of a name, e.g. "RN" for "RNG Number".
   *
   * @param name display name
   * @return initials in upper case
   */
  static String initials(String name) {
    String[] words = name.trim().split("[\\s_-]+");
    if (words.length == 0 || words[0].isEmpty()) return "?";
    if (words.length == 1) return words[0].substring(0, 1).toUpperCase(Locale.ROOT);
    return (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase(Locale.ROOT);
  }
}
