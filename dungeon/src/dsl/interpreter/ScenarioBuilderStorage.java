package dsl.interpreter;

import dsl.runtime.callable.ICallable;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.util.*;
import task.Task;

/**
 * WTF? (erster Satz ist kurz).
 *
 * <p>This class stores {@link FunctionSymbol}s, which refer to scenario builder methods (which take
 * a `task` definition as an argument and return `entity<><>`) in a Map, in which the concrete task
 * type is the key (as {@link IType}).
 */
public class ScenarioBuilderStorage {
  HashMap<IType, List<ICallable>> storedScenarioBuilders;
  HashMap<IType, ArrayDeque<Integer>> lastRetrievedBuilderIdxs;

  /** Constructor. ??? */
  public ScenarioBuilderStorage() {
    storedScenarioBuilders = new HashMap<>();
    lastRetrievedBuilderIdxs = new HashMap<>();
  }

  /**
   * WTF? (erster Satz ist kurz).
   *
   * <p>Initialize this {@link ScenarioBuilderStorage} from an existing {@link IEnvironment}. The
   * environment's global symbols will be scanned for {@link AggregateType} instances, which map to
   * a {@link Task} implementation. For each such {@link AggregateType} a new key is inserted into
   * the internal HashMap.
   *
   * @param environment The {@link IEnvironment} to scan for {@link Task} related types.
   */
  public void initializeScenarioBuilderStorage(IEnvironment environment) {
    var symbols = environment.getGlobalScope().getSymbols();

    // filter all global symbols for Task-types and initialize the
    // scenario builder storage for each of those types
    symbols.stream()
        .filter(
            symbol -> {
              if (symbol instanceof AggregateType type) {
                Class<?> originType = type.getOriginType();
                return originType != null && Task.class.isAssignableFrom(originType);
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
    lastRetrievedBuilderIdxs.put(type, new ArrayDeque<>());
  }

  /**
   * Get all {@link IType}s, for which this {@link ScenarioBuilderStorage} currently can store
   * scenario builder methods as {@link FunctionSymbol}s.
   *
   * @return a {@link Set} of {@link IType}s.
   */
  public Set<IType> getTypesWithStorage() {
    return storedScenarioBuilders.keySet();
  }

  /**
   * WTF? (erster Satz ist kurz).
   *
   * <p>Store a {@link FunctionSymbol} as a scenario builder method. The task type will be
   * determined from the {@link FunctionType} of the passed functionSymbol. The passed {@link
   * FunctionSymbol} should have a {@link FunctionType} which accepts a single parameter of a {@link
   * IType}, which maps to {@link Task} and return an `entity<><>` {@link Value}. The client code is
   * responsible to ensure this!
   *
   * @param callable the {@link FunctionSymbol} to store as a scenario builder method.
   */
  public void storeScenarioBuilder(ICallable callable) {
    // retrieve list for task type
    // the first parametertype denotes the task type
    IType taskType = callable.getFunctionType().getParameterTypes().get(0);

    if (storedScenarioBuilders.containsKey(taskType)) {
      var list = storedScenarioBuilders.get(taskType);
      list.add(callable);
    }

    // clear last retrieved builders for this type
    var dequeue = lastRetrievedBuilderIdxs.get(taskType);
    dequeue.clear();
  }

  /**
   * Get a random {@link Optional} of a {@link FunctionSymbol} for a scenario builder method for a
   * given {@link IType}, which maps to {@link Task}.
   *
   * @param type The {@link IType}, to retrieve a random scenario builder method for.
   * @return An {@link Optional} containing a scenario builder method as {@link FunctionSymbol} for
   *     the given {@link IType}. If this {@link ScenarioBuilderStorage} does not store such a
   *     scenario builder, an empty {@link Optional} is returned.
   */
  public Optional<ICallable> retrieveRandomScenarioBuilderForType(IType type) {
    Optional<ICallable> returnSymbol = Optional.empty();
    if (!storedScenarioBuilders.containsKey(type)) {
      return returnSymbol;
    }

    List<ICallable> list = storedScenarioBuilders.get(type);
    if (list.size() == 0) {
      return returnSymbol;
    }

    // we only consider the last 4 retrieved scenario builders
    // so pop the one which was retrieved the longest ago
    var lastRetrievedIdxs = lastRetrievedBuilderIdxs.get(type);
    if (lastRetrievedIdxs.size() == 5) {
      // remove last entry
      lastRetrievedIdxs.pop();
    }

    // store weighted counts for the last retrieved scenario builders
    // the latest will be weighted the highest

    // initialize count storage
    HashMap<Integer, Float> counts = new HashMap<>();
    for (int i = 0; i < list.size(); i++) {
      counts.put(i, 0.0f);
    }

    // do the counting
    float modifier = 3.0f;
    var iterator = lastRetrievedIdxs.descendingIterator();
    while (iterator.hasNext()) {
      var idx = iterator.next();
      var currentCount = counts.get(idx);
      // add 1 * modifier to the current count
      currentCount = currentCount + modifier;
      counts.put(idx, currentCount);

      // update modifier
      modifier = modifier * 0.65f;
    }

    // order by lowest count
    var sortedList = counts.entrySet().stream().sorted(Map.Entry.comparingByValue()).toList();
    List<Integer> idxsWithLowestCount = new ArrayList<>();
    float lowestCount = sortedList.get(0).getValue();

    // find lowest counts
    for (var sortedEntry : sortedList) {
      if (sortedEntry.getValue() == lowestCount) {
        idxsWithLowestCount.add(sortedEntry.getKey());
      } else {
        break;
      }
    }

    // select random idx from within the lowest counts
    Random random = new Random();
    int randomInt = random.nextInt(idxsWithLowestCount.size());
    int idx = idxsWithLowestCount.get(randomInt);

    // retrieve the function symbol by idx
    ICallable callable = list.get(idx);
    lastRetrievedIdxs.add(idx);
    return Optional.of(callable);
  }
}
