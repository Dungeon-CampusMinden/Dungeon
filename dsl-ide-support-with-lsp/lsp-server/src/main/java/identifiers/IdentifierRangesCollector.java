package identifiers;

import java.util.ArrayList;
import java.util.Hashtable;
import org.eclipse.lsp4j.Range;

/** Collector that collects id usages and their range. */
public class IdentifierRangesCollector {
  private final Hashtable<String, ArrayList<Range>> identifierRanges = new Hashtable<>();

  /**
   * Collects the name and range of an identifier.
   *
   * @param name the name of the identifier.
   * @param range the range where the identifier occurs.
   */
  public void collect(String name, Range range) {
    identifierRanges.putIfAbsent(name, new ArrayList<>());
    identifierRanges.get(name).add(range);
  }

  /**
   * Returns the collected identifiers with the ranges where they occur.
   *
   * @return the collected identifiers with the ranges where they occur.
   */
  public Hashtable<String, ArrayList<Range>> getCollectedIdentifiersRanges() {
    return identifierRanges;
  }

  /** Clears the collected definitions. */
  public void clear() {
    identifierRanges.clear();
  }
}
