package interpreter.mockECS;

import semanticanalysis.types.DSLContextPush;
import semanticanalysis.types.DSLType;

// TODO: how to link this class to the type definitions in semantic
//  analysis (e.g. the syntax-concept of 'game_object')?
@DSLType(name = "game_object")
@DSLContextPush(name = "entity")
public class Entity {}
