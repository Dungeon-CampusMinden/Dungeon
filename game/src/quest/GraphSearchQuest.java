package quest;

import character.objects.TreasureChest;
import controller.EntityController;
import dslToGame.QuestConfig;
import graph.Node;
import java.util.Iterator;
import level.elements.ILevel;
import level.tools.Coordinate;
import level.tools.LevelElement;
import levelgraph.GraphLevelGenerator;
import room.Room;

public class GraphSearchQuest extends Quest {
    public GraphSearchQuest(QuestConfig questConfig) {
        super(questConfig);
        generator = new GraphLevelGenerator(questConfig.levelGenGraph());
    }

    @Override
    public void addQuestObjectsToLevels() {
        // Kisten in Level verteilen
        Room rootRoom = (Room) root;
        Coordinate passwordChestCoordinate = getRandomCoordinate(rootRoom);
        Coordinate letterChestCoordinate = null;
        while (letterChestCoordinate == null || letterChestCoordinate == passwordChestCoordinate) {
            letterChestCoordinate = getRandomCoordinate(rootRoom);
        }
        TreasureChest passwordChest = new TreasureChest(passwordChestCoordinate.toPoint());
        // TODO Lock chest with password
        rootRoom.addElement(passwordChest);

        TreasureChest letterChest = new TreasureChest(letterChestCoordinate.toPoint());
        // Todo add letter
        rootRoom.addElement(letterChest);

        Iterator<Node<String>> graphIterator = questConfig.levelGenGraph().getNodeIterator();
        // TODO insert iterator
        // Iterator<LevelNode> levelIterator = generator.getNodes().iterator();

        while (graphIterator.hasNext()) {
            //    if(!levelIterator.hasNext())  throw Exception
            Node<String> graphNode = graphIterator.next();
            // LevelNode levelNode = levelIterator.next();
            // Room r = levelNode.getRoom();
            // spawnTreasureChestWithLetter(r, graphNode.getValue().toCharArray()[0]);
        }
    }

    @Override
    public void addQuestUIElements() {
        // UI
    }

    @Override
    public void evaluateUserPerformance() {
        // Bewertung durchführen
    }

    private Coordinate getRandomCoordinate(ILevel level) {
        Coordinate c = level.getRandomTile(LevelElement.FLOOR).getCoordinate();
        while (c == level.getStartTile().getCoordinate()) {
            c = level.getRandomTile(LevelElement.FLOOR).getCoordinate();
        }
        return c;
    }

    private void spawnTreasureChestWithLetter(ILevel level, Character codeFragment) {
        TreasureChest t = new TreasureChest(getRandomCoordinate(level).toPoint());
        // TODO add Letter
        ((Room) level).getElements().add(t);
    }

    @Override
    public void onLevelLoad(ILevel currentLevel, EntityController entityController) {
        // TODO print new map with players position?
    }
}
