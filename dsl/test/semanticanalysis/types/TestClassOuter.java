package semanticanalysis.types;

import taskDependencyGraph.TaskDependencyGraph;

@DSLType
public class TestClassOuter {
    @DSLTypeMember private String member1;
    @DSLTypeMember private int member2;
    @DSLTypeMember private TaskDependencyGraph member3;
    private Object member4;
}
