package level.generator;

import level.elements.Level;
import level.elements.graph.Graph;
import level.generator.dungeong.graphg.NoSolutionException;
import level.tools.DesignLabel;

public interface IGenerator {
    /**
     * Get a level with a random configuration.
     *
     * @return The level.
     */
    Level getLevel() throws NoSolutionException;

    /**
     * Get a leve with the given configuration.
     *
     * @param designLabel The design of the level.
     * @return The level.
     */
    default Level getLevel(DesignLabel designLabel) throws NoSolutionException {
        return getLevel();
    }

    /**
     * Get a leve with the given configuration.
     *
     * @param nodes Number of nodes in the level-graph.
     * @param edges Number of (extra) edges in the level-graph.
     * @return The level.
     * @throws NoSolutionException If no solution can be found for the given configuration.
     */
    default Level getLevel(int nodes, int edges) throws NoSolutionException {
        return getLevel();
    }

    /**
     * Get a leve with the given configuration.
     *
     * @param nodes Number of nodes in the level-graph.
     * @param edges Number of (extra) edges in the level-graph.
     * @param designLabel The design of the level.
     * @return The level.
     * @throws NoSolutionException If no solution can be found for the given configuration.
     */
    default Level getLevel(int nodes, int edges, DesignLabel designLabel)
            throws NoSolutionException {
        return getLevel();
    }

    /**
     * Generate a Level from a given graph.
     *
     * @param graph The Level-Graph.
     * @param designLabel The Design-Label the level should have.
     * @return The level.
     * @throws NoSolutionException If no solution can be found for the given configuration.
     */
    default Level getLevel(Graph graph, DesignLabel designLabel) throws NoSolutionException {
        return getLevel();
    }

    /**
     * Generate a Level from a given graph.
     *
     * @param graph The Level-Graph.
     * @return The level.
     * @throws NoSolutionException If no solution can be found for the given configuration.
     */
    default Level getLevel(Graph graph) throws NoSolutionException {
        return getLevel();
    }
}
