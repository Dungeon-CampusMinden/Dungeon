package dsl.semanticanalysis.typesystem;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import graph.taskdependencygraph.TaskDependencyGraph;

/** WTF? . */
@DSLType
public class TestClassOuter {
  @DSLTypeMember private String member1;
  @DSLTypeMember private int member2;
  @DSLTypeMember private TaskDependencyGraph member3;
  private Object member4;
}
