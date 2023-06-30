package dslToGame;

import interpreter.DSLInterpreter;

import runtime.IEvironment;
import runtime.IMemorySpace;

public interface IRuntimeObjectTranslator<FromTy, ToTy> {
    ToTy translate(
            FromTy object,
            IEvironment environment,
            IMemorySpace parentMemorySpace,
            DSLInterpreter interpreter);
}
