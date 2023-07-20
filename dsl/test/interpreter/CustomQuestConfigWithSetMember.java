package interpreter;

import interpreter.mockecs.Entity;
import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

import java.util.Set;

@DSLType(name = "quest_config")
public record CustomQuestConfigWithSetMember(@DSLTypeMember Entity entity, @DSLTypeMember Entity secondEntity,
                                             @DSLTypeMember Set<Integer> intSet,
                                             @DSLTypeMember Set<Float> floatSet) {
}
