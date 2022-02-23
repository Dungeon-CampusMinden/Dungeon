package level.generator.dungeong.levelg;

import java.util.ArrayList;
import java.util.List;
import level.elements.graph.Node;
import level.generator.dungeong.roomg.RoomTemplate;
import level.tools.Coordinate;
import level.tools.LevelElement;

/** @author Andre Matutat */
public class ConfigurationSpace {
    private final RoomTemplate template;
    private final Node node;
    private final Coordinate globalPosition;

    /**
     * @param template The used RoomTemplate
     * @param node Node in the graph this configuration-space belongs to.
     * @param globalPosition Position of th localReferencePoint of the template in the global system
     */
    public ConfigurationSpace(RoomTemplate template, Node node, Coordinate globalPosition) {
        this.template = new RoomTemplate(template);
        this.node = node;
        this.globalPosition = globalPosition;
    }

    /**
     * @param otherTemplate Template to check for.
     * @param otherGlobal Global-position of the template.
     * @return If the given template overlapped with this configuration-space.
     */
    public boolean overlap(RoomTemplate otherTemplate, Coordinate otherGlobal) {
        List<Coordinate> thisPoints = convertInCoordinates(template, globalPosition);
        List<Coordinate> otherPoints = convertInCoordinates(otherTemplate, otherGlobal);
        for (Coordinate p1 : thisPoints)
            for (Coordinate p2 : otherPoints)
                if (p1.equals(p2))
                    if (!isOuterWall(p1, globalPosition, template)
                            || !isOuterWall(p2, otherGlobal, otherTemplate)) return true;
        return false;
    }

    public boolean overlap(ConfigurationSpace other) {
        return overlap(other.getTemplate(), other.getGlobalPosition());
    }

    private List<Coordinate> convertInCoordinates(
            RoomTemplate template, Coordinate globalPosition) {
        List<Coordinate> coordinate = new ArrayList<>();
        LevelElement[][] layout = template.getLayout();
        int difx = globalPosition.x - template.getLocalRef().x;
        int dify = globalPosition.y - template.getLocalRef().y;
        for (int y = 0; y < layout.length; y++)
            for (int x = 0; x < layout[0].length; x++)
                coordinate.add(new Coordinate(x + difx, y + dify));
        return coordinate;
    }

    private boolean isOuterWall(
            Coordinate globalPoint, Coordinate globalReferencePoint, RoomTemplate template) {

        int difx = globalReferencePoint.x - template.getLocalRef().x;
        int dify = globalReferencePoint.y - template.getLocalRef().y;
        Coordinate localP = new Coordinate(globalPoint.x - difx, globalPoint.y - dify);
        return isOuterWall(localP, template);
    }

    private boolean isOuterWall(Coordinate localP, RoomTemplate template) {

        try {

            LevelElement[][] layout = template.getLayout();
            if (layout[localP.y][localP.x] != LevelElement.WALL
                    && layout[localP.y][localP.x] != LevelElement.DOOR) return false;

            // outer points
            if (localP.y == 0
                    || localP.y == layout.length - 1
                    || localP.x == 0
                    || localP.x == layout[0].length - 1) return true;

            // check all the way up
            boolean ok = true;
            for (int y = localP.y; y < layout.length; y++)
                if (layout[y][localP.x] != LevelElement.SKIP) {
                    ok = false;
                    break;
                }

            if (ok) return true;

            // check all the way down
            ok = true;
            for (int y = localP.y; y <= 0; y--)
                if (layout[y][localP.x] != LevelElement.SKIP) {
                    ok = false;
                    break;
                }

            if (ok) return true;

            // check all the way right
            ok = true;
            for (int x = localP.x; x < layout[0].length; x++)
                if (layout[localP.y][x] != LevelElement.SKIP) {
                    ok = false;
                    break;
                }

            if (ok) return true;

            // check all the way left
            ok = true;
            for (int x = localP.x; x <= 0; x--)
                if (layout[localP.y][x] != LevelElement.SKIP) {
                    ok = false;
                    break;
                }

            return ok;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean layoutEquals(ConfigurationSpace other) {
        LevelElement[][] otherLayout = other.getTemplate().getLayout();
        LevelElement[][] thisLayout = template.getLayout();
        if (otherLayout.length != thisLayout.length) return false;
        if (otherLayout[0].length != thisLayout[0].length) return false;

        for (int y = 0; y < otherLayout.length; y++)
            for (int x = 0; x < otherLayout[0].length; x++)
                if (otherLayout[y][x] != thisLayout[y][x]) return false;
        return true;
    }

    public RoomTemplate getTemplate() {
        return template;
    }

    public Node getNode() {
        return node;
    }

    public Coordinate getGlobalPosition() {
        return globalPosition;
    }
}
