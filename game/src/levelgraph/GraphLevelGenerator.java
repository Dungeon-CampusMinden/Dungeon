package levelgraph;

import level.elements.ILevel;
import level.generator.IGenerator;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.LevelSize;

/**
 * Generates a space-based level whose structure is defined by the given graph
 *
 * @quthor Andre Matutat
 */
public class GraphLevelGenerator implements IGenerator {

    private LevelNode root; // todo convert from dotGraph

    /**
     * The Root-Node defines the graph
     *
     * @param graphRoot
     */
    public void setRoot(LevelNode graphRoot) {
        root = graphRoot;
    }

    @Override
    public ILevel getLevel(DesignLabel designLabel, LevelSize size) {
        if (root == null)
            throw new NullPointerException("Root is null. Please add a graph to this generator");
        else return (ILevel) new GraphLevel(root, size, designLabel).getRootRoom();
    }

    @Override
    public LevelElement[][] getLayout(LevelSize size) {
        return new LevelElement[0][];
    }
}
