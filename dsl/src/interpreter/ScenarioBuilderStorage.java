package interpreter;

import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.IType;

import java.util.*;

// TODO: javadoc
public class ScenarioBuilderStorage {
    HashMap<IType, List<FunctionSymbol>> storedScenarioBuilders;

    public ScenarioBuilderStorage() {
        storedScenarioBuilders = new HashMap<>();
    }

    public void initializeStorageForType(IType type) {
        if (storedScenarioBuilders.containsKey(type)) {
            throw new RuntimeException("Storage for type " + type + " is already initialized");
        }
        storedScenarioBuilders.put(type, new ArrayList<>());
    }

    public Set<IType> getTypesWithStorage() {
        return storedScenarioBuilders.keySet();
    }

    public void storeScenarioBuilder(FunctionSymbol functionSymbol) {
        // retrieve list for task type
        // the first parametertype denotes the task type
        IType taskType = functionSymbol.getFunctionType().getParameterTypes().get(0);

        if (storedScenarioBuilders.containsKey(taskType)) {
            var list = storedScenarioBuilders.get(taskType);
            list.add(functionSymbol);
        }
    }

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
