package runtime;

public interface IObjectToValueTranslator {
    Value translate(Object object, IMemorySpace parentMemorySpace, IEvironment environment);
}
