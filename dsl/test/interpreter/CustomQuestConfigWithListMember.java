package interpreter;

import interpreter.mockecs.Entity;

import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

import java.util.List;

@DSLType(name = "quest_config")
public record CustomQuestConfigWithListMember(
        @DSLTypeMember Entity entity,
        @DSLTypeMember Entity secondEntity,
        @DSLTypeMember List<Integer> intList,
        @DSLTypeMember List<Float> floatList) {}
