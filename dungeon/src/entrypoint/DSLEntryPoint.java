package entrypoint;

import dsl.parser.ast.ObjectDefNode;

/** Represents an entry point. */
public record DSLEntryPoint(
    ParsedFile file, String displayName, ObjectDefNode configDefinitionNode) {}
