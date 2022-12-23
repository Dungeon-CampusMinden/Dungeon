package quest;

import basiselements.hud.ScreenText;
import character.objects.Letter;
import character.objects.PasswordChest;
import character.objects.TreasureChest;
import controller.EntityController;
import controller.ScreenController;
import dslToGame.QuestConfig;
import graph.Node;
import java.util.Iterator;
import level.elements.ILevel;
import level.tools.Coordinate;
import level.tools.LevelElement;
import levelgraph.GraphLevelGenerator;
import minimap.TextMap;
import room.IRoom;
import room.Room;
import tools.Point;

public class GraphSearchQuest extends Quest implements Evaluateable {

    PasswordChest passwordChest;
    ScreenText questInfo;

    TextMap textMap;

    public GraphSearchQuest(QuestConfig questConfig, ScreenController sc) {
        super(questConfig, sc);
        generator = new GraphLevelGenerator(questConfig.levelGraph());
        textMap = new TextMap(questConfig.levelGraph());
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

        passwordChest =
                new PasswordChest(
                        passwordChestCoordinate.toPoint(), questConfig.password(), sc, this);
        rootRoom.addElement(passwordChest);

        TreasureChest letterChest = new TreasureChest(letterChestCoordinate.toPoint());

        char c = getNodeToRoom(rootRoom).getValue().toCharArray()[0];
        letterChest.addItem(new Letter(c, textMap, rootRoom));
        rootRoom.addElement(letterChest);

        Iterator<Node<String>> graphIterator = questConfig.levelGraph().getNodeIterator();
        graphIterator.next();
        while (graphIterator.hasNext()) {
            Node<String> graphNode = graphIterator.next();
            IRoom room = getRoomToNode(graphNode);
            spawnTreasureChestWithLetter((ILevel) room, graphNode.getValue().toCharArray()[0]);
        }
    }

    @Override
    public void addQuestUIElements() {
        questInfo = new ScreenText(questText, new Point(450, 420), 1f);
        sc.add(questInfo);
    }

    @Override
    public int evaluateUserPerformance() {
        int score = maxscore - passwordChest.getFalseAttempts();
        System.out.println("YOUR SCORE IS: " + score);
        return score;
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
        char c = getNodeToRoom((Room) level).getValue().toCharArray()[0];
        t.addItem(new Letter(c, textMap, (Room) level));
        ((Room) level).addElement(t);
    }

    @Override
    public void onLevelLoad(ILevel currentLevel, EntityController entityController) {
        textMap.drawMap();
    }

    private Node<String> getNodeToRoom(Room r) {
        return GraphLevelGenerator.levelNodeToNode.get(r.getLevelNode());
    }

    private IRoom getRoomToNode(Node<String> n) {
        return GraphLevelGenerator.nodeToLevelNode.get(n).getRoom();
    }

    @Override
    public void evaluate() {
        evaluateUserPerformance();
    }
}
