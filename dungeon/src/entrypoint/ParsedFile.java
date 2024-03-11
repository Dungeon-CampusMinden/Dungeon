package entrypoint;

import dsl.parser.ast.Node;
import java.nio.file.Path;

/**
 * Represents a parsed file.
 *
 * @param filePath
 * @param rootASTNode
 */
public record ParsedFile(Path filePath, Node rootASTNode) {}
