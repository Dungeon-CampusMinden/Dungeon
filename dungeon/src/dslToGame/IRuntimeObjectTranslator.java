package dslToGame;

import runtime.IMemorySpace;

public interface IRuntimeObjectTranslator<FromTy, ToTy> {
    ToTy translate(FromTy object, IMemorySpace ms);
}
