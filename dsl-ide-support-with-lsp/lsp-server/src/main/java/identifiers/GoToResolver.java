package identifiers;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.util.Ranges;

/** Resolver that resolves the target of goto definition and goto references invocations. */
public class GoToResolver {

  /**
   * Returns the definition for the id at the given position or an empty list if no definition is
   * known for the position.
   *
   * @param uri the uri of the document.
   * @param position the position the definition is asked for.
   * @param definitions the identifier definitions inside the document.
   * @param usages the identifier usages inside the document.
   * @return the definition for the id at the given position or an empty list if no definition is
   *     known for the position.
   */
  public static List<Location> resolveDefinition(
      String uri,
      Position position,
      Hashtable<String, ArrayList<Range>> definitions,
      Hashtable<String, ArrayList<Range>> usages) {
    ArrayList<Location> locations = new ArrayList<>();
    for (Map.Entry<String, ArrayList<Range>> usage : usages.entrySet()) {
      if (usage.getValue().stream().anyMatch(r -> Ranges.containsPosition(r, position))
          && definitions.containsKey(usage.getKey())) {
        locations.add(new Location(uri, definitions.get(usage.getKey()).getFirst()));
        break;
      }
    }

    return locations;
  }

  /**
   * Returns the usages for the definition at the given position or an empty list if no usages are
   * known for the position.
   *
   * @param uri the uri of the document.
   * @param position the position the usages are asked for.
   * @param definitions the identifier definitions inside the document.
   * @param usages the identifier usages inside the document.
   * @return the usages for the definition at the given position or an empty list if no usages are
   *     known for the position.
   */
  public static List<Location> resolveUsages(
      String uri,
      Position position,
      Hashtable<String, ArrayList<Range>> definitions,
      Hashtable<String, ArrayList<Range>> usages) {
    ArrayList<Location> locations = new ArrayList<>();
    for (Map.Entry<String, ArrayList<Range>> definition : definitions.entrySet()) {
      for (Range definitionRange : definition.getValue()) {
        if (Ranges.containsPosition(definitionRange, position)
            && usages.containsKey(definition.getKey())) {
          for (Range usageRange : usages.get(definition.getKey())) {
            locations.add(new Location(uri, usageRange));
          }
          break;
        }
      }
    }

    return locations;
  }
}
