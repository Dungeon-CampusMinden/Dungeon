package newdsl.entrypoint;

import dsl.parser.ast.ObjectDefNode;
import newdsl.common.SourceLocation;

public record DSLEntryPoint(
    SourceLocation sourceLocation, String displayName, ObjectDefNode configDefinitionNode) {
}
