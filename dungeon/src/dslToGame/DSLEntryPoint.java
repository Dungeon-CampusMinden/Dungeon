package dslToGame;

import parser.ast.ObjectDefNode;

public record DSLEntryPoint(ParsedFile file, String displayName, ObjectDefNode configDefinitionNode) {}
