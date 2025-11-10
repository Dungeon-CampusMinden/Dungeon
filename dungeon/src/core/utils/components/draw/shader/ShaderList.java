package core.utils.components.draw.shader;

import java.util.*;

/**
 * A collection class for managing multiple AbstractShader instances with unique identifiers and
 * assigned priorities. Supports adding, removing, retrieving, and iterating over shaders sorted
 * first by priority and then by insertion order.
 */
public class ShaderList {

  // Unique counter to track insertion order
  private long insertionCounter = 0;

  // Map for quick lookup and management by identifier
  private final Map<String, AbstractShader> shaderMap = new HashMap<>();

  // Map to store the priority for each shader identifier
  private final Map<String, Integer> priorityMap = new HashMap<>();

  // Map to store the insertion index for fast removal from the TreeSet
  private final Map<String, Long> insertionIndexMap = new HashMap<>();

  // TreeMap for automatic primary sorting by priority
  // Key: Priority (Integer)
  // Value: TreeSet of ShaderEntry, which performs the secondary sort by insertionIndex
  private final TreeMap<Integer, Set<ShaderEntry>> sortedByPriority = new TreeMap<>();

  /**
   * Adds a new shader with an assigned priority and identifier.
   *
   * @param identifier The unique name given by the system
   * @param shader The AbstractShader object
   * @param priority The priority level (e.g., lower number is higher priority)
   * @return true if the shader was added, false if the identifier already exists
   */
  public boolean add(String identifier, AbstractShader shader, int priority) {
    if (shaderMap.containsKey(identifier)) {
      return false;
    }

    long newIndex = insertionCounter++;
    ShaderEntry entry = new ShaderEntry(shader, newIndex);

    shaderMap.put(identifier, shader);
    priorityMap.put(identifier, priority);
    insertionIndexMap.put(identifier, newIndex);

    sortedByPriority.computeIfAbsent(priority, k -> new TreeSet<>()).add(entry);

    return true;
  }

  /**
   * Adds a new shader with default priority (0).
   *
   * @param identifier The unique name given by the system
   * @param shader The AbstractShader object
   * @return true if the shader was added, false if the identifier already exists
   */
  public boolean add(String identifier, AbstractShader shader) {
    return add(identifier, shader, 0);
  }

  /**
   * Removes a shader by its unique identifier.
   *
   * @param identifier The unique name/identifier assigned by the creating system
   * @return true if the shader was found and removed, false otherwise
   */
  public boolean remove(String identifier) {
    if (!shaderMap.containsKey(identifier)) {
      return false;
    }

    AbstractShader shaderToRemove = shaderMap.remove(identifier);
    int priority = priorityMap.remove(identifier);
    long index = insertionIndexMap.remove(identifier);

    removeFromSortedMap(new ShaderEntry(shaderToRemove, index), priority);
    return true;
  }

  /**
   * Retrieves a shader by its unique identifier.
   *
   * @param identifier The unique name/identifier assigned by the creating system
   * @return The AbstractShader object, or null if not found
   */
  public AbstractShader get(String identifier) {
    return shaderMap.get(identifier);
  }

  /**
   * Changes the priority of an existing shader.
   *
   * @param identifier The shader's unique identifier
   * @param newPriority The new priority level
   * @return true if the priority was updated, false if the shader was not found
   */
  public boolean changePriority(String identifier, int newPriority) {
    AbstractShader shader = shaderMap.get(identifier);
    Integer oldPriority = priorityMap.get(identifier);
    Long index = insertionIndexMap.get(identifier);

    if (shader == null || oldPriority == null) {
      return false;
    }
    if (oldPriority == newPriority) {
      return true;
    }

    ShaderEntry entry = new ShaderEntry(shader, index);

    removeFromSortedMap(entry, oldPriority);
    priorityMap.put(identifier, newPriority);

    sortedByPriority.computeIfAbsent(newPriority, k -> new TreeSet<>()).add(entry);

    return true;
  }

  /**
   * Checks if at least one shader is currently enabled.
   *
   * @return true if any shader is enabled, false otherwise
   */
  public boolean hasEnabledShaders() {
    for (AbstractShader shader : shaderMap.values()) {
      if (shader.enabled()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Calls shader.enabled(enabled) on ALL shaders in the list.
   *
   * @param enabled The enabled state to set for all shaders
   */
  public void enableAll(boolean enabled) {
    for (AbstractShader shader : shaderMap.values()) {
      shader.enabled(enabled);
    }
  }

  /** Calls shader.enabled(true) on ALL shaders in the list. */
  public void enableAll() {
    enableAll(true);
  }

  /** Calls shader.enabled(false) on ALL shaders in the list. */
  public void disableAll() {
    enableAll(false);
  }

  /**
   * Helper method to remove a shader entry from the sorted map based on its priority.
   *
   * @param entry The ShaderEntry to remove
   * @param priority The priority level of the shader
   */
  private void removeFromSortedMap(ShaderEntry entry, int priority) {
    Set<ShaderEntry> set = sortedByPriority.get(priority);
    if (set != null) {
      set.remove(entry);
      if (set.isEmpty()) {
        sortedByPriority.remove(priority);
      }
    }
  }

  /**
   * Returns an iterable collection of all shaders, sorted first by priority, then by insertion
   * time.
   *
   * @param onlyEnabled If true, only enabled shaders will be included
   * @return An iterable of AbstractShader objects
   */
  public Iterable<AbstractShader> getSorted(boolean onlyEnabled) {
    return () ->
        new Iterator<>() {
          private final Iterator<Set<ShaderEntry>> mapIterator =
              sortedByPriority.values().iterator();
          private Iterator<ShaderEntry> currentSetIterator = Collections.emptyIterator();
          private AbstractShader nextShader = null;

          @Override
          public boolean hasNext() {
            if (nextShader != null) {
              return true;
            }

            while (true) {
              if (currentSetIterator.hasNext()) {
                ShaderEntry candidateEntry = currentSetIterator.next();
                AbstractShader candidate = candidateEntry.shader;

                if (!onlyEnabled || candidate.enabled()) {
                  nextShader = candidate;
                  return true;
                }
              } else if (mapIterator.hasNext()) {
                currentSetIterator = mapIterator.next().iterator();
              } else {
                return false;
              }
            }
          }

          @Override
          public AbstractShader next() {
            if (!hasNext()) {
              throw new NoSuchElementException();
            }
            AbstractShader result = nextShader;
            nextShader = null;
            return result;
          }
        };
  }

  /**
   * Returns an iterable collection of all enabled shaders, sorted first by priority, then by
   * insertion time.
   *
   * @return An iterable of enabled AbstractShader objects
   */
  public Iterable<AbstractShader> getEnabledSorted() {
    return getSorted(true);
  }

  /**
   * Internal record to hold shader and its insertion index for sorting.
   *
   * @param shader The AbstractShader instance
   * @param insertionIndex The unique insertion index
   */
  private record ShaderEntry(AbstractShader shader, long insertionIndex)
      implements Comparable<ShaderEntry> {
    @Override
    public int compareTo(ShaderEntry other) {
      // Secondary sort: Insertion time (Lower index = earlier insertion)
      return Long.compare(this.insertionIndex, other.insertionIndex);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ShaderEntry that = (ShaderEntry) o;
      // Equality based on the unique insertion index
      return insertionIndex == that.insertionIndex;
    }

    @Override
    public int hashCode() {
      return Long.hashCode(insertionIndex);
    }
  }
}
