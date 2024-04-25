package dsl.interpreter;

import dsl.annotation.DSLType;
import dsl.annotation.DSLTypeMember;
import dsl.interpreter.mockecs.Entity;
import java.util.List;
import java.util.Set;

/**
 * WTF? .
 *
 * @param entity foo
 * @param secondEntity foo
 * @param intList foo
 * @param floatSet foo
 * @param stringSet foo
 * @param stringList foo
 */
@DSLType(name = "quest_config")
public record CustomQuestConfig(
    @DSLTypeMember Entity entity,
    @DSLTypeMember Entity secondEntity,
    @DSLTypeMember List<Integer> intList,
    @DSLTypeMember Set<Float> floatSet,
    @DSLTypeMember Set<String> stringSet,
    @DSLTypeMember List<String> stringList) {}
