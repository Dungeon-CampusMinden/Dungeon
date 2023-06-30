package dslToGame;

import interpreter.DSLInterpreter;

import runtime.IEvironment;
import runtime.IMemorySpace;
import runtime.Value;

public interface IRuntimeObjectTranslator {
    Value translate(
            Object object,
            IEvironment environment,
            IMemorySpace parentMemorySpace,
            DSLInterpreter interpreter);
}
