package dsl.semanticanalysis.types;

import dsl.semanticanalysis.types.annotation.DSLType;
import dsl.semanticanalysis.types.annotation.DSLTypeMember;
import graph.taskdependencygraph.TaskDependencyGraph;

@DSLType
public class TestClassOuter {
    @DSLTypeMember
    private String member1;
    @DSLTypeMember private int member2;
    @DSLTypeMember private TaskDependencyGraph member3;
    private Object member4;
}
