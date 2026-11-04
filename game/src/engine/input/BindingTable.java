package engine.input;

import engine.utils.JsonHandler;
import engine.utils.logging.DungeonLogger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * All bindings of the running process: defaults of the known actions plus the player's overrides.
 *
 * <p>Actions bring their defaults; the table learns them through {@link #register(ContextSpec)}. A
 * player override wins over the default. An empty override means deliberately unbound; a missing
 * one means default. The override file keeps entries of actions this process does not know, so one
 * starter never drops the bindings of another room.
 *
 * <p>Bindings are parsed and indexed when they change, never per frame.
 */
public final class BindingTable {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(BindingTable.class);

  // A binding with the action it drives.
  record Bound(ActionId action, Binding binding) {}

  /**
   * Two actions that would share an input.
   *
   * @param binding the shared input
   * @param first one action
   * @param second the other action
   * @param context name of the context or context group they collide in
   */
  public record Conflict(Binding binding, ActionId first, ActionId second, String context) {}

  private final Map<ActionId, List<Binding>> defaults = new LinkedHashMap<>();
  private final Map<String, List<String>> overrides = new TreeMap<>();
  private final Set<ContextSpec> contexts = new LinkedHashSet<>();
  private final Map<Binding, List<Bound>> index = new HashMap<>();
  private Path file;

  /**
   * Makes a context and its actions known: their defaults, the conflict check and the labels of
   * texts. New conflicts go to the log. Registering a context again changes nothing.
   *
   * @param spec the context declaration
   */
  public void register(ContextSpec spec) {
    if (!contexts.add(spec)) return;
    spec.actions().forEach(action -> defaults.putIfAbsent(action, action.defaults()));
    rebuildIndex();
    logConflicts(spec.actions());
  }

  private void logConflicts(Set<ActionId> actions) {
    for (Conflict conflict : conflicts()) {
      if (!actions.contains(conflict.first()) && !actions.contains(conflict.second())) continue;
      LOGGER.warn(
          "Input conflict in {}: {} drives {} and {}",
          conflict.context(),
          conflict.binding().format(),
          conflict.first(),
          conflict.second());
    }
  }

  /**
   * Gets the current bindings of an action.
   *
   * @param action the action
   * @return override if present, otherwise the defaults
   */
  public List<Binding> bindings(ActionId action) {
    List<String> override = overrides.get(action.toString());
    if (override != null) return override.stream().map(Binding::parse).toList();
    return defaults.getOrDefault(action, action.defaults());
  }

  /**
   * Replaces the player's bindings of an action and saves them.
   *
   * @param action the action
   * @param bindings the new bindings; empty means deliberately unbound
   * @return the conflict that prevented the change, or empty if the change was applied
   */
  public Optional<Conflict> rebind(ActionId action, List<Binding> bindings) {
    List<String> previous = overrides.get(action.toString());
    overrides.put(action.toString(), bindings.stream().map(Binding::format).toList());
    Optional<Conflict> conflict =
        conflicts().stream()
            .filter(c -> c.first().equals(action) || c.second().equals(action))
            .findFirst();
    if (conflict.isPresent()) {
      if (previous == null) overrides.remove(action.toString());
      else overrides.put(action.toString(), previous);
      return conflict;
    }
    rebuildIndex();
    save();
    return Optional.empty();
  }

  /**
   * Removes the player's override of an action.
   *
   * @param action the action
   */
  public void reset(ActionId action) {
    if (overrides.remove(action.toString()) == null) return;
    rebuildIndex();
    save();
  }

  /**
   * Finds colliding bindings: inside every context, and among all non-blocking contexts together.
   * Blocking contexts may cover keys below them on purpose. A combo never collides with its plain
   * key because specificity decides between them.
   *
   * @return every conflict
   */
  public List<Conflict> conflicts() {
    List<Conflict> found = new ArrayList<>();
    for (ContextSpec spec : contexts) collectConflicts(List.of(spec), spec.name(), found);
    collectConflicts(
        contexts.stream().filter(spec -> !spec.blocksBelow()).toList(), "non-blocking", found);
    return found;
  }

  private void collectConflicts(List<ContextSpec> group, String name, List<Conflict> found) {
    Map<Binding, ActionId> seen = new HashMap<>();
    for (ContextSpec spec : group) {
      for (ActionId action : spec.actions()) {
        for (Binding binding : bindings(action)) {
          if (!spec.accepts(action, binding)) continue;
          ActionId other = seen.putIfAbsent(binding, action);
          if (other == null || other.equals(action)) continue;
          boolean bothClose = spec.closing().contains(action) && spec.closing().contains(other);
          if (!bothClose) found.add(new Conflict(binding, other, action, name));
        }
      }
    }
  }

  /**
   * Reads the player's overrides. A missing file means no overrides. An invalid file is moved aside
   * and reported, so it is never overwritten silently; the game then starts with defaults.
   *
   * @param path location of the binding file; saves go there as well
   */
  public void load(Path path) {
    file = path;
    overrides.clear();
    if (Files.exists(path)) {
      try {
        overrides.putAll(parse(Files.readString(path, StandardCharsets.UTF_8)));
      } catch (IOException | RuntimeException e) {
        moveAside(path, e);
      }
    }
    rebuildIndex();
    logConflicts(defaults.keySet());
  }

  private static Map<String, List<String>> parse(String json) {
    Map<String, List<String>> parsed = new TreeMap<>();
    for (Map.Entry<String, Object> entry : JsonHandler.readJson(json).entrySet()) {
      ActionId.of(entry.getKey());
      if (!(entry.getValue() instanceof List<?> values)) {
        throw new IllegalArgumentException("No binding list for " + entry.getKey());
      }
      List<String> bindings = new ArrayList<>();
      for (Object value : values) {
        Binding.parse((String) value);
        bindings.add((String) value);
      }
      parsed.put(entry.getKey(), List.copyOf(bindings));
    }
    return parsed;
  }

  private static void moveAside(Path path, Exception cause) {
    Path aside = path.resolveSibling(path.getFileName() + ".invalid");
    try {
      Files.move(path, aside, StandardCopyOption.REPLACE_EXISTING);
      LOGGER.error("Invalid input bindings moved to {}: {}", aside, cause.getMessage());
    } catch (IOException e) {
      throw new IllegalStateException("Cannot move invalid binding file " + path, e);
    }
  }

  private void save() {
    if (file == null) return;
    Map<String, Object> data = new TreeMap<>(overrides);
    try {
      Path temp = file.resolveSibling(file.getFileName() + ".tmp");
      Files.writeString(temp, JsonHandler.writeJson(data, true), StandardCharsets.UTF_8);
      try {
        Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      LOGGER.error("Cannot save input bindings to {}: {}", file, e.getMessage());
    }
  }

  /**
   * Gets every binding that fires on an input.
   *
   * @param trigger the input, without prefixes
   * @return the matching bindings with their actions
   */
  List<Bound> bound(Binding trigger) {
    return index.getOrDefault(trigger, List.of());
  }

  private void rebuildIndex() {
    index.clear();
    for (ActionId action : defaults.keySet()) {
      for (Binding binding : bindings(action)) {
        index
            .computeIfAbsent(binding.trigger(), key -> new ArrayList<>())
            .add(new Bound(action, binding));
      }
    }
  }
}
