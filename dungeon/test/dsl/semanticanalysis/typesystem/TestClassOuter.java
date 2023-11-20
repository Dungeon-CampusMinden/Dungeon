package dsl.semanticanalysis.typesystem;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLType;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeMember;

import graph.taskdependencygraph.TaskDependencyGraph;

@DSLType
public class TestClassOuter {
    @DSLTypeMember private String member1;
    @DSLTypeMember private int member2;
    @DSLTypeMember private TaskDependencyGraph member3;
    private Object member4;
}
