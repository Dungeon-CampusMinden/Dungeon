package blockly.compiler.api;

/**
 * Standard response from any compiler backend.
 */
public class CompilerResponse {
  private final String backendId;
  private final String ir;

  public CompilerResponse(String backendId, String ir) {
    this.backendId = backendId;
    this.ir = ir;
  }

  public String backendId() {
    return backendId;
  }

  public String ir() {
    return ir;
  }
}

