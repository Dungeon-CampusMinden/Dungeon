package entrypoint;

import dsl.parser.ast.Node;
import java.nio.file.Path;

/** Represents a parsed file. */
public record ParsedFile(Path filePath, Node rootASTNode) {}
