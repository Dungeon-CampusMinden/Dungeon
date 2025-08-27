package lispy.values;

/**
 * Numeric value (actually int).
 *
 * @param value value (int)
 */
public record NumVal(int value) implements Value {}
