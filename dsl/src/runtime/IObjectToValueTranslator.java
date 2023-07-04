package runtime;

import semanticanalysis.IScope;

public interface IObjectToValueTranslator {
    Value translate(
            Object object,
            IScope globalScope,
            IMemorySpace parentMemorySpace,
            IEvironment environment);
}
