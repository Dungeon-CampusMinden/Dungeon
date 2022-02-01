package level.generator.dungeong.levelg;

import level.elements.graph.Node;
import level.generator.dungeong.roomg.RoomTemplate;
import level.tools.Coordinate;
import level.tools.LevelElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        this.template = template;
        this.node = node;
        this.globalPosition = globalPosition;
    }

    /**
     * @param otherTemplate Template to check for.
     * @param otherGlobal Global-position of the template.
     * @return If the given template overlapped with this configuration-space.
     */
    public boolean overlap(RoomTemplate otherTemplate, Coordinate otherGlobal) {
        List<Coordinate> thisPoints = converInCoordinates(template, globalPosition);
        List<Coordinate> otherPoints = converInCoordinates(otherTemplate, otherGlobal);
        for (Coordinate p1 : thisPoints)
            for (Coordinate p2 : otherPoints)
                if (p1.equals(p2))
                    if (!isOuterWall(p1, globalPosition, template)
                            || !isOuterWall(p2, otherGlobal, otherTemplate)) return true;
        return false;
    }

    /**
     * @param otherTemplate Template to check for.
     * @param otherGlobal Position of the template.
     * @param attachSize Size of door.
     * @return If Template is attached.
     */
    public boolean attached(RoomTemplate otherTemplate, Coordinate otherGlobal, int attachSize) {
        List<Coordinate> thisPoints = converInCoordinates(template, globalPosition);
        List<Coordinate> otherPoints = converInCoordinates(otherTemplate, otherGlobal);
        List<Coordinate> attachingPoints = new ArrayList<>();
        for (Coordinate p1 : thisPoints)
            for (Coordinate p2 : otherPoints)
                if (p1.equals(p2))
                    if (isOuterWall(p1, globalPosition, template)
                            && isOuterWall(p2, otherGlobal, otherTemplate))
                        attachingPoints.add(p1); // maybe do something for door?
                    else return false;

        // check for door space
        if (attachingPoints.size() < attachSize) return false;

        List<Integer> xPoints = new ArrayList<>();
        List<Integer> yPoints = new ArrayList<>();
        for (Coordinate p : attachingPoints) {
            xPoints.add((int) p.x);
            yPoints.add((int) p.y);
        }

        Collections.sort(xPoints);
        int counter = 1;
        for (int i = 1; i < xPoints.size(); i++) {
            if (xPoints.get(i) - 1 == xPoints.get(i - 1)) counter++;
            else counter = 1;
            if (counter == attachSize) return true;
        }

        Collections.sort(yPoints);
        counter = 1;
        for (int i = 1; i < yPoints.size(); i++) {
            if (yPoints.get(i) - 1 == yPoints.get(i - 1)) counter++;
            else counter = 1;
            if (counter == attachSize) return true;
        }

        return false;
    }

    private List<Coordinate> converInCoordinates(RoomTemplate template, Coordinate globalPosition) {
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
            if (layout[(int) localP.y][(int) localP.x] != LevelElement.WALL) return false;
            // outer points
            if (localP.y == 0
                    || localP.y == layout.length - 1
                    || localP.x == 0
                    || localP.x == layout[0].length - 1) return true;

            // check all the way up
            boolean ok = true;
            for (int y = (int) localP.y; y < layout.length; y++)
                if (layout[y][(int) localP.x] != LevelElement.SKIP) {
                    ok = false;
                    break;
                }

            if (ok) return true;

            // check all the way down
            ok = true;
            for (int y = (int) localP.y; y <= 0; y--)
                if (layout[y][(int) localP.x] != LevelElement.SKIP) {
                    ok = false;
                    break;
                }

            if (ok) return true;

            // check all the way right
            ok = true;
            for (int x = (int) localP.x; x < layout[0].length; x++)
                if (layout[(int) localP.y][x] != LevelElement.SKIP) {
                    ok = false;
                    break;
                }

            if (ok) return true;

            // check all the way left
            ok = true;
            for (int x = (int) localP.x; x <= 0; x--)
                if (layout[(int) localP.y][x] != LevelElement.SKIP) {
                    ok = false;
                    break;
                }

            return ok;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }
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

    public List<Coordinate> getOuterWalls() {
        List<Coordinate> outerWalls = new ArrayList<>();
        int difx = globalPosition.x - template.getLocalRef().x;
        int dify = globalPosition.y - template.getLocalRef().y;
        LevelElement[][] layout = template.getLayout();

        for (int y = 0; y < layout.length; y++)
            for (int x = 0; x < layout[0].length; x++) {
                if (isOuterWall(new Coordinate(x, y), template))
                    outerWalls.add(new Coordinate(x + difx, y + dify));
            }
        return outerWalls;
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

    /**
     * @param other Door to?
     * @return Local positions of possible doors.
     */
    public List<Coordinate> getAttachingPoints(ConfigurationSpace other) {
        List<Coordinate> thisPoints = converInCoordinates(template, globalPosition);
        List<Coordinate> otherPoints =
                converInCoordinates(other.getTemplate(), other.getGlobalPosition());
        int difx = globalPosition.x - template.getLocalRef().x;
        int dify = globalPosition.y - template.getLocalRef().y;
        int odifx = other.getGlobalPosition().x - other.template.getLocalRef().x;
        int odify = other.getGlobalPosition().y - other.template.getLocalRef().y;
        List<Coordinate> doors = new ArrayList<>();
        for (Coordinate tp : thisPoints)
            for (Coordinate op : otherPoints) {
                if (tp.equals(op)) {

                    doors.add(new Coordinate(tp.x - difx, tp.y - dify));
                    doors.add(new Coordinate(op.x - odifx, op.y - odify));
                }
            }

        return doors;
    }
}
