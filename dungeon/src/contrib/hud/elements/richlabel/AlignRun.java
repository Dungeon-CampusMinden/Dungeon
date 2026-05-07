package contrib.hud.elements.richlabel;

/**
 * A layout-only run that changes the horizontal alignment of all subsequent lines until the next
 * {@code AlignRun} overrides it. Does not produce any visible output itself.
 *
 * <p>The {@code align} value uses libGDX {@link com.badlogic.gdx.utils.Align} bits. Only the
 * horizontal bits ({@link com.badlogic.gdx.utils.Align#left}, {@link
 * com.badlogic.gdx.utils.Align#center}, {@link com.badlogic.gdx.utils.Align#right}) are inspected.
 * A value of {@code -1} resets to the alignment programmatically assigned to the {@link
 * contrib.hud.elements.RichLabel} (i.e. removes any previous tag-based override).
 *
 * @param align the libGDX align bits, or {@code -1} to reset to the programmatic default
 */
public record AlignRun(int align) implements Run {}
