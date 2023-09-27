package interpreter;

import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.IType;

import java.util.*;

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

    public FunctionSymbol retrieveRandomScenarioBuilderForType(IType type) {
        Random random = new Random();

        if (!storedScenarioBuilders.containsKey(type)) {
            throw new RuntimeException("No scenario builders for given type " + type + " stored!");
        }

        List<FunctionSymbol> list = storedScenarioBuilders.get(type);
        if (list.size() == 0) {
            throw new RuntimeException("No scenario builders for given type " + type + " stored!");
        }

        int idx = random.nextInt(list.size());
        return list.get(idx);
    }
}
