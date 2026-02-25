package core.ir;

import org.jetbrains.annotations.NotNull;

public record SourceLocation(@NotNull String file, int line, int column) {
  public static final SourceLocation UNKNOWN = new SourceLocation("<unknown>", -1, -1);

  public static @NotNull SourceLocation fromString(@NotNull String loc) {
    String[] parts = loc.split(":");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid source location format: " + loc);
    }
    String file = parts[0];
    int line = Integer.parseInt(parts[1]);
    int column = Integer.parseInt(parts[2]);
    return new SourceLocation(file, line, column);
  }

  @Override
  public @NotNull String toString() {
    return file + ":" + line + ":" + column;
  }
}
