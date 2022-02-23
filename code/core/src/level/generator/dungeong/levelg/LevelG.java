package level.generator.dungeong.levelg;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import level.elements.Level;
import level.elements.graph.BFEdge;
import level.elements.graph.Graph;
import level.elements.graph.Node;
import level.elements.room.Room;
import level.generator.IGenerator;
import level.generator.dungeong.graphg.GraphG;
import level.generator.dungeong.graphg.NoSolutionException;
import level.generator.dungeong.roomg.Replacement;
import level.generator.dungeong.roomg.ReplacementLoader;
import level.generator.dungeong.roomg.RoomTemplate;
import level.generator.dungeong.roomg.RoomTemplateLoader;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import tools.Constants;

/**
 * Uses RoomG and GraphG to generate level.
 *
 * @author Andre Matutat
 */
public class LevelG implements IGenerator {
    private final GraphG graphg = new GraphG();
    private final RoomTemplateLoader roomLoader;
    private final ReplacementLoader replacementLoader;
    private final String pathToGraph;

    /**
     * Uses RoomG and GraphG to generate level.
     *
     * @param pathToRoomTemplates Path to roomTemplates.json
     * @param pathToReplacements Path to replacements.json
     * @param pathToGraph path to graphs/
     */
    public LevelG(String pathToRoomTemplates, String pathToReplacements, String pathToGraph) {
        roomLoader = new RoomTemplateLoader(pathToRoomTemplates);
        replacementLoader = new ReplacementLoader(pathToReplacements);
        this.pathToGraph = pathToGraph;
    }

    @Override
    public Level getLevel() throws NoSolutionException {
        return getLevel(DesignLabel.values()[new Random().nextInt(DesignLabel.values().length)]);
    }

    @Override
    public Level getLevel(int nodeCounter, int edgeCounter, DesignLabel design)
            throws NoSolutionException {
        Graph graph = graphg.getGraph(nodeCounter, edgeCounter, pathToGraph);
        if (graph == null) throw new NoSolutionException("No Graph found for this configuration");
        return getLevel(graph, design);
    }

    @Override
    public Level getLevel(DesignLabel designLabel) throws NoSolutionException {
        File dir = new File(Constants.getPathToGraph());
        File[] allGraphFiles = dir.listFiles();
        assert (allGraphFiles != null && allGraphFiles.length > 0);
        File graph = allGraphFiles[new Random().nextInt(allGraphFiles.length)];
        return getLevel(graphg.getGraph(graph.getPath()), designLabel);
    }

    @Override
    public Level getLevel(int nodeCounter, int edgeCounter) throws NoSolutionException {
        return getLevel(
                nodeCounter,
                edgeCounter,
                DesignLabel.values()[new Random().nextInt(DesignLabel.values().length)]);
    }

    @Override
    public Level getLevel(Graph graph) throws NoSolutionException {
        return getLevel(
                graph, DesignLabel.values()[new Random().nextInt(DesignLabel.values().length)]);
    }

    @Override
    public Level getLevel(Graph graph, DesignLabel design) throws NoSolutionException {
        List<Chain> chain = splitInChains(graph);
        return getLevel(graph, chain, design);
    }

    /**
     * Generate a Level from a graph that has already been split in chains.
     *
     * @param graph The Level-Graph.
     * @param chains The graph split into chains.
     * @param design The Design-Label the level should have.
     * @return The level.
     * @throws NoSolutionException If no solution can be found for the given configuration.
     */
    private Level getLevel(Graph graph, List<Chain> chains, DesignLabel design)
            throws NoSolutionException {
        return getLevel(getSolveSequence(chains, graph), graph, design);
    }

    /**
     * Generate a Level from a graph that has already been split in chains.
     *
     * @param graph The Level-Graph.
     * @param solveSeq Sequence to solve.
     * @param design The Design-Label the level should have.
     * @return The level.
     * @throws NoSolutionException If no solution can be found for the given configuration.
     */
    private Level getLevel(List<Node> solveSeq, Graph graph, DesignLabel design)
            throws NoSolutionException {
        List<ConfigurationSpace> configurationSpaces = makeLevel(graph, solveSeq, design);
        List<Room> rooms = new ArrayList<>();
        List<Replacement> replacements;
        if (Constants.DISABLE_REPLACEMENTS) replacements = new ArrayList<>();
        else replacements = replacementLoader.getReplacements(design);
        placeDoors(configurationSpaces);
        // replace templates
        for (ConfigurationSpace cs : configurationSpaces) {
            RoomTemplate template = cs.getTemplate();
            rooms.add(template.replace(replacements, cs.getGlobalPosition(), design));
        }
        Level level = new Level(graph.getNodes(), rooms);
        if (checkIfCompletable(level)) return level;
        // in rare cases, the path to the target may be blocked.
        else return getLevel(solveSeq, graph, design);
    }
    /**
     * Split a graph in chains.
     *
     * @param graph Graph to split
     * @return List with Chains.
     */
    private List<Chain> splitInChains(Graph graph) {
        List<Chain> chains = new ArrayList<>();
        List<Node> notInChain = new ArrayList<>(graph.getNodes());
        for (BFEdge edge : graph.getBfEdges()) {
            List<Node> circle =
                    getShortestCirclePaths(
                            graph.getNodes().get(edge.getNode1()),
                            graph.getNodes().get(edge.getNode2()),
                            graph);
            // transform circle in chain
            Chain chain = new Chain();
            chain.setCircle(true);
            chain.setNodes(circle);
            chains.add(chain);
            notInChain.removeAll(chain.getNodes());
        }

        while (!notInChain.isEmpty()) {
            Chain chain = new Chain();
            chain.setCircle(false);
            Node start = notInChain.get(0);
            notInChain.remove(start);
            List<Node> firstWay = followChain(start, notInChain, graph);
            Collections.reverse(firstWay);
            List<Node> secondWay = followChain(start, notInChain, graph);
            firstWay.forEach(chain::add);
            chain.add(start);
            secondWay.forEach(chain::add);
            chains.add(chain);
        }
        return chains;
    }

    private List<Node> getShortestCirclePaths(Node start, Node goal, Graph graph) {
        List<List<Node>> allPath = getAllPath(start, goal, graph);
        List<Node> shortest = new ArrayList<>();
        for (List<Node> path : allPath)
            if (path.size() > 2 && (shortest.size() > path.size() || shortest.isEmpty()))
                shortest = path;
        return shortest;
    }

    private List<List<Node>> getAllPath(Node start, Node goal, Graph graph) {
        List<List<Node>> paths = new ArrayList<>();
        graph_search(start, new ArrayList<>(), goal, paths, graph);
        return paths;
    }

    private void graph_search(
            Node currentNode, List<Node> marked, Node goal, List<List<Node>> paths, Graph graph) {
        List<Node> myMarked = new ArrayList<>(marked);
        myMarked.add(currentNode);
        if (currentNode == goal) paths.add(myMarked);
        else
            for (int child : currentNode.getNeighbours()) {
                Node childNode = graph.getNodes().get(child);
                if (!myMarked.contains(childNode))
                    graph_search(childNode, new ArrayList<>(myMarked), goal, paths, graph);
            }
    }

    /**
     * follows a path of a node in one direction
     *
     * @param start start node
     * @param notInChain nodes not in chain
     * @param graph level graph
     * @return chain
     */
    private List<Node> followChain(Node start, List<Node> notInChain, Graph graph) {
        List<Node> chain = new ArrayList<>();
        List<Node> neighbour = new ArrayList<>();
        for (Integer index : start.getNeighbours()) neighbour.add(graph.getNodes().get(index));
        neighbour.retainAll(notInChain);
        if (neighbour.isEmpty()) return chain;
        chain.add(neighbour.get(0));
        notInChain.remove(neighbour.get(0));
        List<Node> nn = followChain(neighbour.get(0), notInChain, graph);
        chain.addAll(nn);
        return chain;
    }

    /**
     * Convert chains into a list of nodes, ordered by the sequence to be solved.
     *
     * @param chains chains
     * @param graph Level graph
     * @return Solve sequence
     */
    private List<Node> getSolveSequence(List<Chain> chains, Graph graph) {
        List<Chain> chainInOrder = new ArrayList<>();

        Chain shortestCircle = null;
        for (Chain c : chains) {
            if (c.isCircle()
                    && (shortestCircle == null
                            || c.getNodes().size() < shortestCircle.getNodes().size()))
                shortestCircle = c;
        }
        if (shortestCircle != null) chainInOrder.add(shortestCircle);
        else chainInOrder.add(chains.get(0));
        chainInOrder = orderChains(chainInOrder, chains, chainInOrder, graph);
        List<Node> solveSequence = new ArrayList<>();
        for (Chain chain : chainInOrder)
            for (Node node : chain.getNodes())
                if (!solveSequence.contains(node)) solveSequence.add(node);
        return solveSequence;
    }

    private List<Chain> orderChains(
            List<Chain> checkFor, List<Chain> allChains, List<Chain> chainInOrder, Graph graph) {
        if (checkFor.isEmpty()) return chainInOrder;
        List<Chain> chainInOrderCopy = new ArrayList<>(chainInOrder);

        List<Chain> neighbourChains = new ArrayList<>();
        for (Chain chain : checkFor) {
            for (Node node : chain.getNodes()) {
                List<Node> neighbours = new ArrayList<>();
                node.getNeighbours().forEach(index -> neighbours.add(graph.getNodes().get(index)));
                for (Node n : neighbours) {
                    Chain c = getChainToNode(n, allChains);
                    if (!chainInOrderCopy.contains(c) && !neighbourChains.contains(c))
                        neighbourChains.add(c);
                }
            }
        }
        Collections.sort(neighbourChains);
        chainInOrderCopy.addAll(neighbourChains);
        return orderChains(neighbourChains, allChains, chainInOrderCopy, graph);
    }

    private Chain getChainToNode(Node node, List<Chain> chains) {
        for (Chain c : chains) for (Node n : c.getNodes()) if (n == node) return c;
        return null;
    }

    /**
     * @param graph The Graph of the level.
     * @param solveSeq Sequence to solve.
     * @param design The Design-Label the level should have.
     * @return The level.
     * @throws NoSolutionException If no solution can be found for the given configuration.
     */
    private List<ConfigurationSpace> makeLevel(Graph graph, List<Node> solveSeq, DesignLabel design)
            throws NoSolutionException {
        List<RoomTemplate> templates = roomLoader.getRoomTemplates(design);
        List<RoomTemplate> allTemplates = new ArrayList<>();
        for (RoomTemplate template : templates) allTemplates.addAll(template.getAllRotations());
        List<ConfigurationSpace> solution =
                getLevelCS(graph, solveSeq, new ArrayList<>(), allTemplates);

        if (solution == null || solution.isEmpty())
            throw new NoSolutionException(
                    "No way to convert the given graph into a level using the given templates.");

        return solution;
    }

    /**
     * Incremental-backtracking-process to find the configuration-space for all rooms.
     *
     * @param graph The Graph of the level.
     * @param notPlaced Nodes left to place, ordered by the sequence to be solved.
     * @param partSolution The (partial) solution found so far.
     * @param templates RoomTemplates to place.
     * @return A solution in form of a list of ConfigurationSpaces that can be converted into a
     *     level. null if no solution could be found.
     */
    private List<ConfigurationSpace> getLevelCS(
            Graph graph,
            List<Node> notPlaced,
            List<ConfigurationSpace> partSolution,
            List<RoomTemplate> templates) {
        // todo switch from bfs to dfs

        if (notPlaced.isEmpty()) return partSolution; // end solution found

        // take next node
        Node thisNode = notPlaced.get(0);
        List<Node> notPlacedAfterThis = new ArrayList<>(notPlaced);
        notPlacedAfterThis.remove(thisNode);

        // todo bfs starts

        // calculate configuration spaces for thisNode
        List<ConfigurationSpace> spaces;
        List<ConfigurationSpace> neighbourSpaces = getNeighbourCS(graph, thisNode, partSolution);
        if (neighbourSpaces.isEmpty()) {
            // this is the first node ever
            if (partSolution.isEmpty()) spaces = calCS(null, thisNode, templates, partSolution);
            // wrong node order
            else {
                notPlacedAfterThis.add(thisNode);
                return getLevelCS(graph, notPlacedAfterThis, partSolution, templates);
            }

        } else spaces = getCS(neighbourSpaces, thisNode, templates, partSolution);
        if (spaces.isEmpty()) {
            return null; // No solution. Backtrack if possible
        }

        // add some random factor
        Collections.shuffle(spaces);

        // todo bfs ends

        // go one step deeper
        for (ConfigurationSpace cs : spaces) {
            List<ConfigurationSpace> thisPartSolution = new ArrayList<>(partSolution);
            thisPartSolution.add(cs);
            List<ConfigurationSpace> solution =
                    getLevelCS(graph, notPlacedAfterThis, thisPartSolution, templates);
            if (solution != null) return solution; // end solution found
        }
        return null; // No solution. Backtrack if possible
    }

    private List<ConfigurationSpace> getNeighbourCS(
            Graph graph, Node node, List<ConfigurationSpace> spaces) {
        List<ConfigurationSpace> neighbourSpaces = new ArrayList<>();
        List<Node> neighbours = new ArrayList<>();
        for (Integer i : node.getNeighbours()) neighbours.add(graph.getNodes().get(i));
        for (Node n : neighbours) {
            ConfigurationSpace nodeSpace = getSpaceToNode(spaces, n);
            if (nodeSpace != null) neighbourSpaces.add(nodeSpace);
        }
        return neighbourSpaces;
    }

    private ConfigurationSpace getSpaceToNode(List<ConfigurationSpace> spaces, Node find) {
        for (ConfigurationSpace cs : spaces) if (cs.getNode() == find) return cs;
        return null;
    }

    /**
     * Finds all possible configuration-spaces for the given setup.
     *
     * @param neighbourSpaces Configuration-spaces of the neighbours.
     * @param node Node to check for.
     * @param templates List of templates to use.
     * @param partSolution All already placed rooms.
     * @return All possible configuration-spaces for node.
     */
    private List<ConfigurationSpace> getCS(
            List<ConfigurationSpace> neighbourSpaces,
            Node node,
            List<RoomTemplate> templates,
            List<ConfigurationSpace> partSolution) {
        List<ConfigurationSpace> possibleSpaces = new ArrayList<>();
        for (ConfigurationSpace neighbourSpace : neighbourSpaces)
            if (possibleSpaces.isEmpty())
                possibleSpaces = calCS(neighbourSpace, node, templates, partSolution);
            else {
                List<ConfigurationSpace> newSpaces =
                        calCS(neighbourSpace, node, templates, partSolution);
                List<ConfigurationSpace> drop = new ArrayList<>();

                for (ConfigurationSpace newSpace : newSpaces) {
                    boolean equal = false;
                    for (ConfigurationSpace possibleSpace : possibleSpaces) {
                        if (newSpace.getGlobalPosition().equals(possibleSpace.getGlobalPosition())
                                && newSpace.layoutEquals(possibleSpace)) equal = true;
                    }
                    if (!equal) drop.add(newSpace);
                }
                possibleSpaces = newSpaces;
                possibleSpaces.removeAll(drop);
            }
        return possibleSpaces;
    }

    /**
     * Calculate the configuration-spaces to find possible positions for the room in the level.
     *
     * @param staticSpace The non-movable component.
     * @param dynamicNode The movable component.
     * @param template The templates to use.
     * @param level All other rooms that are already placed in the level and are not allowed to
     *     overlap.
     * @return all possible configuration-spaces.
     */
    private List<ConfigurationSpace> calCS(
            ConfigurationSpace staticSpace,
            Node dynamicNode,
            List<RoomTemplate> template,
            List<ConfigurationSpace> level) {

        List<ConfigurationSpace> spaces = new ArrayList<>();
        for (RoomTemplate layout : template) {
            List<ConfigurationSpace> possibleCS = new ArrayList<>();
            // this the first node placed in the level.
            if (level.isEmpty()) {
                possibleCS.add(new ConfigurationSpace(layout, dynamicNode, new Coordinate(0, 0)));
            } else {
                possibleCS = getCSDoors(staticSpace, layout, dynamicNode);
            }
            for (ConfigurationSpace possibleSpace : possibleCS) {
                boolean isValid = true;
                for (ConfigurationSpace levelSpace : level)
                    if (levelSpace.overlap(possibleSpace)) {
                        isValid = false;
                        break;
                    }
                if (isValid) spaces.add(possibleSpace);
            }
        }
        return spaces;
    }

    /**
     * Creates a ConfigurationSpace for each Door-Combination Doesn't check for overlapping.
     *
     * @param staticSpace Where to attach.
     * @param template What to attach.
     * @return All ConfigurationSpaces where doors are connected.
     */
    private List<ConfigurationSpace> getCSDoors(
            ConfigurationSpace staticSpace, RoomTemplate template, Node dynamicNode) {
        int difx = staticSpace.getGlobalPosition().x - staticSpace.getTemplate().getLocalRef().x;
        int dify = staticSpace.getGlobalPosition().y - staticSpace.getTemplate().getLocalRef().y;
        List<Coordinate> staticDoors = staticSpace.getTemplate().getDoors();
        List<Coordinate> dynamicDoors = template.getDoors();
        List<ConfigurationSpace> doorCS = new ArrayList<>();
        for (Coordinate staticDoor : staticDoors) {
            Coordinate staticDoorGlobal = new Coordinate(staticDoor.x + difx, staticDoor.y + dify);
            for (Coordinate door : dynamicDoors) {
                template.setLocalRef(door);
                doorCS.add(new ConfigurationSpace(template, dynamicNode, staticDoorGlobal));
            }
        }
        return doorCS;
    }

    /**
     * Checks each possible door and marks them as used if they are.
     *
     * @param level The Level in his ConfigurationSpace format
     */
    private void placeDoors(List<ConfigurationSpace> level) {
        for (ConfigurationSpace cS : level)
            for (Coordinate cD : cS.getTemplate().getDoors()) {
                // calculate global door coordinate
                int difx = cS.getGlobalPosition().x - cS.getTemplate().getLocalRef().x;
                int dify = cS.getGlobalPosition().y - cS.getTemplate().getLocalRef().y;
                Coordinate cdg = new Coordinate(cD.x + difx, cD.y + dify);
                // check all other rooms for doors at the same coordinate
                for (ConfigurationSpace oS : level)
                    if (!cS.equals(oS))
                        for (Coordinate oD : oS.getTemplate().getDoors()) {
                            // calculate global door coordinate
                            int odifx = oS.getGlobalPosition().x - oS.getTemplate().getLocalRef().x;
                            int odify = oS.getGlobalPosition().y - oS.getTemplate().getLocalRef().y;
                            Coordinate odg = new Coordinate(oD.x + odifx, oD.y + odify);

                            // if doors are on the same spot, mark them as used
                            if (cdg.equals(odg)) {
                                cS.getTemplate().useDoor(cD);
                                oS.getTemplate().useDoor(oD);
                            }
                        }
            }
    }

    /**
     * Checks if a level can be completed. If not, this level should not be used.
     *
     * @param level To check for.
     * @return Can you reach the End-Tile from the Start-Tile?
     */
    private boolean checkIfCompletable(Level level) {
        return level.isTileReachable(level.getStartTile(), level.getEndTile());
    }
}
