package dslinput;

import dsl.parser.ast.ObjectDefNode;

public record DSLEntryPoint(
        ParsedFile file, String displayName, ObjectDefNode configDefinitionNode) {}
