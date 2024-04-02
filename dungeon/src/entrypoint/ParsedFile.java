package entrypoint;

import dsl.parser.ast.Node;
import java.nio.file.Path;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public record ParsedFile(Path filePath, Node rootASTNode) {}
