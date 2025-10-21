package contrib.entities.deco;

import core.Entity;
import core.utils.Point;
import core.utils.components.draw.animation.SpritesheetConfig;
import java.util.ArrayList;
import java.util.List;

/** A factory class for creating composite decorative entities. */
public class CompositeDecoFactory {

  /**
   * Creates a chain of big trees with the specified length.
   *
   * @param pos the position where to spawn the chain
   * @param length the length of the tree chain. At least 3
   * @return the list of tree segment entities
   */
  public static List<Entity> createTreeChain(Point pos, int length) {
    if (length <= 2) throw new IllegalArgumentException("Length must be greater than 2");

    List<Entity> entities = new ArrayList<>();
    SpritesheetConfig treeL = Deco.TreeBigChainL.config().config().get();
    SpritesheetConfig treeM = Deco.TreeBigChainM.config().config().get();
    float lTreeOffset = treeL.spriteWidth() / 16f;

    entities.add(DecoFactory.createDeco(pos, Deco.TreeBigChainL));
    for (int i = 1; i < length - 1; i++) {
      entities.add(
          DecoFactory.createDeco(
              pos.translate(lTreeOffset + (i - 1) * (treeM.spriteWidth() / 16f), 0),
              Deco.TreeBigChainM));
    }
    entities.add(
        DecoFactory.createDeco(
            pos.translate(lTreeOffset + (length - 2) * (treeM.spriteWidth() / 16f), 0),
            Deco.TreeBigChainR));

    return entities;
  }
}
