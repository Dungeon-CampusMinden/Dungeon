package dungeonFiles;

import core.Entity;

import graph.Graph;

import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

import task.Task;

import java.util.List;

// TODO: add more fields (entry-point for interpreter, QuestType, etc.)
@DSLType
public record QuestConfig(
        @DSLTypeMember Graph<String> levelGraph,
        @DSLTypeMember String questDesc,
        @DSLTypeMember int questPoints,
        @DSLTypeMember String password,
        @DSLTypeMember Entity entity,
        @DSLTypeMember(name = "name") String displayName,
        @DSLTypeMember List<Task> tasks) {}
