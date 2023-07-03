package dslToGame;

import runtime.IMemorySpace;
import runtime.Value;

import semanticanalysis.IScope;

public interface IRuntimeObjectTranslator {
    Value translate(Object object, IScope globalScope, IMemorySpace parentMemorySpace);
}
