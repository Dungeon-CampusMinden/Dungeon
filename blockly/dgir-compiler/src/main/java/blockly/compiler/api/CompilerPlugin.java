package blockly.compiler.api;

public interface CompilerPlugin {

  /**
   * @return A unique identifier for this compiler backend (e.g. "java").
   */
  String getId();

  /**
   * @return A short human-friendly description of the backend.
   */
  String getDescription();

/**
 * Compile source code into the Blockly intermediate representation.
 *
 * @param source           The source code to compile.
 * @param compilationUnit  Optional logical name (e.g. filename) for diagnostics.
 * @return The IR text produced by this backend.
 * @throws CompilationException if the source cannot be compiled.
 */
String compileToIr(String source, String compilationUnit) throws CompilationException;
/**
* Compile source code into the Blockly intermediate representation.
*
* @param request description of the compilation unit
* @return a response carrying backend id and IR payload
* @throws CompilationException if the source cannot be compiled.
*/
CompilerResponse compile(CompilerRequest request) throws CompilationException;
 }

