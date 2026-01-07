package blockly.compiler.java;

import blockly.compiler.api.CompilationException;
import blockly.compiler.api.CompilerPlugin;
import blockly.compiler.api.CompilerRequest;
import blockly.compiler.api.CompilerResponse;

public class JavaCompilerPlugin implements CompilerPlugin {
  @Override
  public String getId() {
    return "java";
  }

  @Override
  public String getDescription() {
    return "Java-to-IR compiler backend (dummy implementation)";
  }

  @Override
  public String compileToIr(String source, String compilationUnit) throws CompilationException {
    // In a real implementation, source would be parsed/compiled into IR here.
    if (source == null) {
      throw new CompilationException("Source code must not be null");
    }
    return "// IR for unit: " + (compilationUnit == null ? "<unknown>" : compilationUnit) + "\n" + "// length: " + source.length();
  }

  @Override
  public CompilerResponse compile(CompilerRequest request) throws CompilationException {
    if (request == null || request.source() == null) {
      throw new CompilationException("Source code must not be null");
    }
    String unit = request.unitName() == null ? "<unknown>" : request.unitName();
    String ir = "// IR for unit: " + unit + "\n" + "// length: " + request.source().length();
    return new CompilerResponse(getId(), ir);
  }
}

