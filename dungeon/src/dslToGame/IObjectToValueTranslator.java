package dslToGame;

import runtime.IMemorySpace;
import runtime.Value;

import semanticanalysis.IScope;

public interface IObjectToValueTranslator {
    Value translate(Object object, IScope globalScope, IMemorySpace parentMemorySpace);
}
