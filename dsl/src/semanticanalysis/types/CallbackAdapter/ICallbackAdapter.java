package semanticanalysis.types.CallbackAdapter;

@FunctionalInterface
public interface ICallbackAdapter {

    Object call(Object... params);
}
