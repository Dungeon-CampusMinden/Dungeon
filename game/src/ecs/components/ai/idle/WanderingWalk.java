package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import tools.Constants;
import tools.Point;

import java.util.Random;

public class WanderingWalk implements IIdleAI {

    private static final Random random = new Random();

    private final float radius;
    private final int pauseFrames;
    private final int wanderDistance;

    private GraphPath<Tile> currentPath;
    private int frameCounter = -1;

    /**
     * Konstruktor, der drei Argumente annimmt: radius, wanderDistance und pauseTime. Diese Variablen werden verwendet, um einen zufälligen Wanderpunkt für die Entität zu berechnen, zu dem sie sich bewegen soll.
     * @param radius Die maximale Entfernung vom aktuellen Standort der Entität, die der Wanderpunkt haben kann.
     * @param wanderDistance Die maximale Entfernung, die die Entität von ihrem aktuellen Standort entfernt wandern kann.
     * @param pauseTime Die Zeit (in Millisekunden), die die Entität pausieren soll, bevor sie wieder zu wandern beginnt.
     */
    public WanderingWalk(float radius, int wanderDistance, int pauseTime) {
        this.radius = radius;
        this.wanderDistance = wanderDistance;
        this.pauseFrames = pauseTime / (1000 / Constants.FRAME_RATE);
    }

    /**
     * Eine Methode, die einen zufälligen Wanderpunkt für die Entität berechnet.
     * @param currentPosition Die aktuelle Position der Entität.
     * @return Ein Point-Objekt, das den zufälligen Wanderpunkt darstellt.
     */
    private Point getRandomWanderPoint(Point currentPosition) {
        float angle = random.nextFloat() * 2 * (float) Math.PI;
        float distance = random.nextFloat() * wanderDistance;

        float newX = currentPosition.x + (float) Math.cos(angle) * distance;
        float newY = currentPosition.y + (float) Math.sin(angle) * distance;

        return new Point(newX, newY);
    }

    /**
     * Eine Methode, die aufgerufen wird, wenn die Entität keine anderen Aktionen ausführt. Sie enthält die Hauptlogik für die Wandering Walk-KI-Komponente.
     * @param entity Die Entität, die sich bewegt.
     */
    @Override
    public void idle(Entity entity) {
        PositionComponent position =
            (PositionComponent)
                entity.getComponent(PositionComponent.class)
                    .orElseThrow(
                        () -> new MissingComponentException("PositionComponent"));

        if (currentPath != null && !AITools.pathFinished(entity, currentPath)) {
            if (AITools.pathLeft(entity, currentPath)) {
                currentPath = AITools.calculatePath(position.getPosition(), getRandomWanderPoint(position.getPosition()));
            }
            if (currentPath.getCount() == 0) {
                return;
            }
            AITools.move(entity, currentPath);
            return;
        }

        if (currentPath != null && AITools.pathFinished(entity, currentPath)) {
            frameCounter = 0;
            currentPath = null;
            return;
        }

        if (frameCounter++ < pauseFrames && frameCounter != -1) {
            return;
        }

        frameCounter = -1;
        currentPath = AITools.calculatePath(position.getPosition(), getRandomWanderPoint(position.getPosition()));
    }
}
