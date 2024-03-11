package entrypoint;

import dsl.parser.ast.ObjectDefNode;

/**
 * Represents an entry point.
 *
 * @param file
 * @param displayName
 * @param configDefinitionNode
 */
public record DSLEntryPoint(
    ParsedFile file, String displayName, ObjectDefNode configDefinitionNode) {}
