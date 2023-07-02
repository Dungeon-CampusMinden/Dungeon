package interpreter.mockecs;

import semanticanalysis.types.DSLContextPush;
import semanticanalysis.types.DSLType;

// TODO: how to link this class to the type definitions in semantic
//  analysis (e.g. the syntax-concept of 'entity_type')?
@DSLType(name = "entity")
@DSLContextPush(name = "entity")
public class Entity {}
