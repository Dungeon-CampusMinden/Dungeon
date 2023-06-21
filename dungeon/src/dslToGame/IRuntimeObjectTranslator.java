package dslToGame;

import core.Entity;
import runtime.AggregateValue;
import runtime.IEvironment;
import runtime.IMemorySpace;

public interface IRuntimeObjectTranslator<FromTy, ToTy> {
    ToTy translate(FromTy object, IEvironment environment, IMemorySpace parentMemorySpace);
}
