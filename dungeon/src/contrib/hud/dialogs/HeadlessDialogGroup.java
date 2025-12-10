package contrib.hud.dialogs;

import com.badlogic.gdx.scenes.scene2d.Group;
import java.util.HashMap;
import java.util.Map;

/**
 * A headless-compatible Group that can be created on the server without requiring graphics.
 *
 * <p>This class extends {@link Group} but doesn't initialize any GDX graphics resources. It stores
 * dialog metadata for server-side tracking without rendering.
 *
 * <p>Used by dialog builders (TextDialog, OkDialog, etc.) when running in headless mode to provide
 * a placeholder that the server can reference without crashing.
 */
public class HeadlessDialogGroup extends Group {

  private final String title;
  private final String message;
  private final String[] buttonLabels;
  private final Map<String, Object> metadata = new HashMap<>();

  /**
   * Creates a new HeadlessDialogGroup with the specified metadata.
   *
   * @param title the dialog title (can be null)
   * @param message the dialog message (can be null)
   * @param buttonLabels the button labels (can be null or empty)
   */
  public HeadlessDialogGroup(String title, String message, String... buttonLabels) {
    this.title = title;
    this.message = message;
    this.buttonLabels = buttonLabels != null ? buttonLabels : new String[0];
    // Ensure visibility is true by default
    setVisible(true);
  }

  /**
   * Creates a new HeadlessDialogGroup with no metadata.
   *
   * <p>Use this constructor when you don't need to store dialog-specific information.
   */
  public HeadlessDialogGroup() {
    this(null, null);
  }

  /**
   * Gets the dialog title.
   *
   * @return the title, or null if not set
   */
  public String getTitle() {
    return title;
  }

  /**
   * Gets the dialog message.
   *
   * @return the message, or null if not set
   */
  public String getMessage() {
    return message;
  }

  /**
   * Gets the button labels.
   *
   * @return array of button labels (never null, may be empty)
   */
  public String[] getButtonLabels() {
    return buttonLabels;
  }

  /**
   * Stores custom metadata in this group.
   *
   * @param key the metadata key
   * @param value the metadata value
   * @return this group for method chaining
   */
  public HeadlessDialogGroup putMetadata(String key, Object value) {
    metadata.put(key, value);
    return this;
  }

  /**
   * Gets custom metadata from this group.
   *
   * @param key the metadata key
   * @param <T> the expected type
   * @return the metadata value, or null if not found
   */
  @SuppressWarnings("unchecked")
  public <T> T getMetadata(String key) {
    return (T) metadata.get(key);
  }

  /**
   * Gets all metadata.
   *
   * @return the metadata map
   */
  public Map<String, Object> metadata() {
    return metadata;
  }

  // Override methods that would require graphics to be no-ops

  @Override
  public void act(float delta) {
    // No-op in headless mode
  }

  @Override
  public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
    // No-op in headless mode
  }
}
