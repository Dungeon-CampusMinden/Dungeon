package core.ir;

import core.debug.Location;
import core.traits.ITerminator;
import java.lang.reflect.Constructor;

/**
 * Utility interface that allows creating default terminators for every block that currently has
 * none. The type of terminator is specified by the implementing class
 */
public interface ImplicitTerminator {
  /**
   * Get the type of the implicit terminator to add to blocks without terminators. The constructor
   * must take a single Location argument, which will be the location of the block to which the
   * terminator is being added.
   *
   * @return the type of the implicit terminator.
   */
  Constructor<? extends ITerminator> getImplicitTerminatorType() throws NoSuchMethodException;

  default void addImplicitTerminators() {
    Op op = (Op) this;
    for (Region region : op.getRegions())
      for (Block block : region.getBlocks()) {
        if (block.getTerminator().isEmpty()) {
          try {
            Location trueLocation;

            if (block.getOperations().isEmpty())
              trueLocation = block.getParentOperation().orElseThrow().getLocation();
            else trueLocation = block.getOperations().getLast().getLocation();
            Location debugLocation =
                new Location(trueLocation.file(), trueLocation.line() + 1, trueLocation.column());
            Op terminator = (Op) getImplicitTerminatorType().newInstance(debugLocation);
            block.addOperation(terminator);
          } catch (Exception e) {
            throw new RuntimeException(
                "Failed to create implicit terminator for block: " + block, e);
          }
        }
      }
  }
}
