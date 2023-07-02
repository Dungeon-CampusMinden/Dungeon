package dslToGame;

import core.Entity;

import dslToGame.graph.Graph;

import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;


// TODO: add more fields (entry-point for interpreter, QuestType, etc.)
@DSLType
public record QuestConfig(
        @DSLTypeMember Graph<String> levelGraph,
        @DSLTypeMember String questDesc,
        @DSLTypeMember int questPoints,
        @DSLTypeMember String password,
        @DSLTypeMember Entity entity) {}
