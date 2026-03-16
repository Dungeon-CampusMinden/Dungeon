package dgir.vm.dap;

import dgir.core.debug.Location;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.jetbrains.annotations.Contract;

public interface DebugUtils {
  @Contract(pure = true)
  static boolean breakpointMatches(Breakpoint bp, Location location) {
    if (bp == null || location == null) return false;
    Integer line = bp.getLine();
    if (line == null || line != location.line()) return false;

    var src = bp.getSource();
    String path = src != null ? src.getPath() : null;
    if (path == null || !path.equals(location.file())) return false;

    Integer column = bp.getColumn();
    return column == null || column == location.column();
  }
}
