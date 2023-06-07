package interpreter;

import interpreter.mockecs.Entity;

import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

@DSLType(name = "quest_config")
public record CustomQuestConfig(@DSLTypeMember Entity entity) {}
