package levelgraph;

import level.elements.ILevel;
import level.generator.IGenerator;
import level.tools.DesignLabel;
import level.tools.LevelSize;

public class GraphLevelGenerator implements IGenerator {

    private LevelNode root; // todo convert from dotGraph

    public void setRoot(LevelNode graphRoot) {
        root = graphRoot;
    }

    @Override
    public ILevel getLevel(DesignLabel designLabel, LevelSize size) {
        if (root == null)
            throw new NullPointerException("Root is null. Please add a graph to this generator");
        else return (ILevel) new GraphLevel(root, size, designLabel).getRootRoom();
    }
}
