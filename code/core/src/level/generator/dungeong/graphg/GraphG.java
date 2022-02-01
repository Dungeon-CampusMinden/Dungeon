package level.generator.dungeong.graphg;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import level.elements.graph.Graph;
import level.elements.graph.Node;

/**
 * Can read in graphs from .json or can generate new graphs.
 *
 * @author Andre Matutat
 */
public class GraphG {
    private static final int MAX_SOLUTIONS = 1000;

    /**
     * Read in a graph from .json with the given configuration.
     *
     * @param nodes Number of nodes in the graph.
     * @param edges Numbers of extra edges in the graph.
     * @param path Path to the .json folder (not to the exact file).
     * @return A graph. Can be null if no .json for this configuration was found.
     */
    public Graph getGraph(int nodes, int edges, String path) {
        path += "/" + nodes + "_" + edges + ".json";
        List<Graph> sol = null;
        sol = readFromJson(path);
        return sol.get(new Random().nextInt(sol.size()));
    }
    /**
     * Read in a graph from a given .json.
     *
     * @param json Path to the exact json file.
     * @return A graph. Can be null if no .json for this configuration was found.
     */
    public Graph getGraph(String json) {
        List<Graph> sol = null;
        sol = readFromJson(json);
        return sol.get(new Random().nextInt(sol.size()));
    }

    /**
     * Calculate a list of planar graphs. Generate all possible trees and then draw extra edges.
     *
     * @param nodes number of nodes
     * @param edges number of extra edges that get drawn into the generated tree
     * @return a list of all solutions
     * @throws CantBePlanarException
     * @throws IllegalArgumentException
     * @throws NoSolutionException
     */
    public List<Graph> generateGraphs(int nodes, int edges)
            throws CantBePlanarException, IllegalArgumentException, NoSolutionException {
        if (nodes <= 1)
            throw new IllegalArgumentException("A graph must consist of at least two nodes");
        if (edges < 0)
            throw new IllegalArgumentException("Number of additional edges cannot be negative");

        // e≤3v-6 must hold
        int minimumEdges = nodes - 1 + edges;
        int leftTerm = 3 * nodes - 6;
        if (minimumEdges > leftTerm) throw new CantBePlanarException("e<=3V-6 does not hold");

        Graph tree = new Graph();
        List<Graph> trees = new ArrayList<>();
        trees.add(tree);
        trees = calculateTrees(trees, nodes - 2);
        List<Graph> solutions = calculateGraphs(trees, edges);
        if (solutions.isEmpty()) throw new NoSolutionException("No solution found"); // ??
        return solutions;
    }

    /**
     * Calculates all trees for the configuration.
     *
     * @param trees Already calculated trees.
     * @param nodesLeft Number of nodes that are left to add to the graph.
     * @return All trees.
     */
    private List<Graph> calculateTrees(List<Graph> trees, int nodesLeft) {
        if (nodesLeft <= 0) return trees;
        else {
            reduceGraph(trees);
            List<Graph> newTrees = new ArrayList<>();
            for (Graph t : trees)
                for (Node n : t.getNodes()) {
                    Graph newTree = new Graph(t);
                    // have to get the copy of 'n', the index of the copy is always the index of the
                    // original
                    if (newTree.connectNewNode(n.getIndex())) newTrees.add(newTree);
                }
            return calculateTrees(newTrees, nodesLeft - 1);
        }
    }

    /**
     * Calculate all graphs.
     *
     * @param graphs Already caluclated graphs. Start with trees.
     * @param edgesLeft Number of edges left to add to the graph.
     * @return All graphs.
     */
    private List<Graph> calculateGraphs(List<Graph> graphs, int edgesLeft) {
        if (edgesLeft <= 0) return graphs;
        else {
            reduceGraph(graphs);
            List<Graph> newGraphs = new ArrayList<>();
            for (Graph g : graphs)
                for (Node n1 : g.getNodes())
                    for (Node n2 : g.getNodes()) {
                        Graph newGraph = new Graph(g);
                        // same as in tree, have to get the copys of n and n2
                        if (n1.getIndex() != n2.getIndex()
                                && newGraph.connectNodes(n1.getIndex(), n2.getIndex())) {
                            newGraphs.add(newGraph);
                        }
                    }
            return calculateGraphs(newGraphs, edgesLeft - 1);
        }
    }

    /**
     * Read in a .json file with graphs
     *
     * @param path Path to json.
     * @return All graphs in the file.
     */
    private List<Graph> readFromJson(String path) {
        Type graphType = new TypeToken<ArrayList<Graph>>() {}.getType();
        try {
            JsonReader reader = new JsonReader(new FileReader(path, StandardCharsets.UTF_8));
            return new Gson().fromJson(reader, graphType);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
            return new ArrayList<>();
        } catch (IOException e) {
            System.out.println("File may be corrupted ");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Writes down a list of graphs to a .json.
     *
     * @param graphs The list of rooms to save.
     * @param path Where to save?
     */
    public void writeToJSON(List<Graph> graphs, String path) {
        Gson gson = new Gson();
        String json = gson.toJson(graphs);
        try {
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter(path, StandardCharsets.UTF_8));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            System.out.println("File" + path + " not found");
        }
    }

    private void reduceGraph(List<Graph> graphs) {
        Random r = new Random();
        while (graphs.size() > MAX_SOLUTIONS) {
            graphs.remove(r.nextInt(graphs.size()));
        }
    }
}
