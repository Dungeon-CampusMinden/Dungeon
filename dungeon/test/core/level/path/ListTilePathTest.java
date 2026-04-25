package core.level.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import core.level.Tile;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link ListTilePath} class. */
public class ListTilePathTest {

  /** The constructor copies the input tiles so later source changes do not affect the path. */
  @Test
  public void constructorCopiesInputCollection() {
    Tile first = Mockito.mock(Tile.class);
    Tile second = Mockito.mock(Tile.class);
    List<Tile> source = new ArrayList<>();
    source.add(first);

    ListTilePath path = new ListTilePath(source);
    source.add(second);

    assertEquals(1, path.size());
    assertSame(first, path.get(0));
  }

  /** The list view of the path is immutable. */
  @Test
  public void asUnmodifiableListCannotBeModified() {
    ListTilePath path = new ListTilePath(List.of(Mockito.mock(Tile.class)));

    assertThrows(
        UnsupportedOperationException.class,
        () -> path.asUnmodifiableList().add(Mockito.mock(Tile.class)));
  }
}
