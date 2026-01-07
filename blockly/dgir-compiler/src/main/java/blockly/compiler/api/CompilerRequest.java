package blockly.compiler.api;

/**
 * Describes a compile request so multiple backends can share a stable contract.
 */
public class CompilerRequest {
  private final String source;
  private final String unitName;

  public CompilerRequest(String source, String unitName) {
    this.source = source;
    this.unitName = unitName;
  }

  public String source() {
    return source;
  }

  public String unitName() {
    return unitName;
  }
}

