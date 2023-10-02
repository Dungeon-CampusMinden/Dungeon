package interpreter;

import runtime.IEvironment;
import runtime.Value;
import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.IType;
import task.Task;

import java.util.*;

/**
 * This class stores {@link FunctionSymbol}s, which refer to scenario builder
 * methods (which take a `task` definition as an argument and return `entity<><>`)
 * in a Map, in which the concrete task type is the key (as {@link IType}).
 */
public class ScenarioBuilderStorage {
    HashMap<IType, List<FunctionSymbol>> storedScenarioBuilders;

    /**
     * Constructor.
     */
    public ScenarioBuilderStorage() {
        storedScenarioBuilders = new HashMap<>();
    }

    /**
     * Initialize this {@link ScenarioBuilderStorage} from an existing {@link IEvironment}.
     * The environment's global symbols will be scanned for {@link AggregateType} instances,
     * which map to a {@link Task} implementation. For each such {@link AggregateType} a
     * new key is inserted into the internal HashMap.
     * @param environment The {@link IEvironment} to scan for {@link Task} related types.
     */
    public void initializeScenarioBuilderStorage(IEvironment environment) {
        var symbols = environment.getGlobalScope().getSymbols();

        // filter all global symbols for Task-types and initialize the
        // scenario builder storage for each of those types
        symbols.stream()
            .filter(
                symbol -> {
                    if (symbol instanceof AggregateType type) {
                        Class<?> originType = type.getOriginType();
                        return originType != null
                            && Task.class.isAssignableFrom(originType);
                    }
                    return false;
                })
            .map(symbol -> (IType) symbol)
            .forEach(this::initializeStorageForType);
    }

    protected void initializeStorageForType(IType type) {
        if (storedScenarioBuilders.containsKey(type)) {
            throw new RuntimeException("Storage for type " + type + " is already initialized");
        }
        storedScenarioBuilders.put(type, new ArrayList<>());
    }

    /**
     * Get all {@link IType}s, for which this {@link ScenarioBuilderStorage} currently
     * can store scenario builder methods as {@link FunctionSymbol}s.
     * @return a {@link Set} of {@link IType}s.
     */
    public Set<IType> getTypesWithStorage() {
        return storedScenarioBuilders.keySet();
    }

    /**
     * Store a {@link FunctionSymbol} as a scenario builder method. The
     * task type will be determined from the {@link semanticanalysis.types.FunctionType}
     * of the passed functionSymbol. The passed {@link FunctionSymbol} should have
     * a {@link semanticanalysis.types.FunctionType} which accepts a single parameter
     * of a {@link IType}, which maps to {@link Task} and return an `entity<><>` {@link Value}.
     * The client code is responsible to ensure this!
     *
     * @param functionSymbol the {@link FunctionSymbol} to store as a scenario builder method.
     */
    public void storeScenarioBuilder(FunctionSymbol functionSymbol) {
        // retrieve list for task type
        // the first parametertype denotes the task type
        IType taskType = functionSymbol.getFunctionType().getParameterTypes().get(0);

        if (storedScenarioBuilders.containsKey(taskType)) {
            var list = storedScenarioBuilders.get(taskType);
            list.add(functionSymbol);
        }
    }

    /**
     * Get a random {@link Optional} of a {@link FunctionSymbol} for a scenario builder method for a given
     * {@link IType}, which maps to {@link Task}.
     *
     * @param type The {@link IType}, to retrieve a random scenario builder method for.
     * @return An {@link Optional} containing a scenario builder method as {@link FunctionSymbol}
     * for the given {@link IType}. If this {@link ScenarioBuilderStorage} does not store
     * such a scenario builder, an empty {@link Optional} is returned.
     */
    public Optional<FunctionSymbol> retrieveRandomScenarioBuilderForType(IType type) {
        Optional<FunctionSymbol> returnSymbol = Optional.empty();
        if (!storedScenarioBuilders.containsKey(type)) {
            return returnSymbol;
        }

        List<FunctionSymbol> list = storedScenarioBuilders.get(type);
        if (list.size() == 0) {
            return returnSymbol;
        }

        Random random = new Random();
        int idx = random.nextInt(list.size());
        FunctionSymbol symbol = list.get(idx);
        return Optional.of(symbol);
    }
}
