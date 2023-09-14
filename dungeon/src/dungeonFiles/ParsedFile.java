package dungeonFiles;

import parser.ast.Node;

import java.nio.file.Path;

public record ParsedFile(Path filePath, Node rootASTNode) {}
