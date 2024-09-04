package newdsl.entrypoint;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import newdsl.graph.TaskDependencyGraph;

@DSLType
public record DungeonConfig(
    @DSLTypeMember TaskDependencyGraph dependencyGraph,
    @DSLTypeMember(name = "name") String displayName) {
}
