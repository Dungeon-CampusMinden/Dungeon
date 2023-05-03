package ecs.entities.objects;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.InteractionComponent;
import ecs.entities.Entity;
import ecs.entities.nps.Ghost;
import graphic.Animation;

/**
 * Die Java-Klasse stellt die Entität eines Grabsteins in einem ECS-Game-Engine dar.
 * Die Klasse hat zwei Methoden setupAnimationComponent und setupInteractionComponent um die Animation und Interaktionskomponenten der Entität einzurichten.
 */
public class Tombstone extends Entity {

    /**
     * Der Dateipfad zur Bilddatei des Grabsteins.
     */
    private final String pathToTombstone = "objects/treasurechest/Object/Tombstone.png";
    Ghost ghost = new Ghost();

    /**
     * Konstruiert ein Tombstone-Objekt.
     * Es wird eine neue Instanz der PositionComponent erstellt und dann setupAnimationComponent und setupInteractionComponent aufgerufen, um die Animation und Interaktionskomponenten einzurichten.
     */
    public Tombstone() {
        super();
        new PositionComponent(this);
        setupAnimationComponent();
        setupInteractionComponent();
    }

    /**
     * Richten Sie die Animationkomponente der Entität ein.
     * Diese Methode ruft die statische Methode buildAnimation der Klasse AnimationBuilder auf, die die Animation des Grabsteins aus der Bilddatei erstellt.
     * Dann wird eine neue Instanz der AnimationComponent-Komponente erstellt, die der Entität hinzugefügt wird.
     */
    private void setupAnimationComponent() {
        Animation stone = AnimationBuilder.buildAnimation(pathToTombstone);
        new AnimationComponent(this, stone);
    }

    /**
     * Richten Sie die Interaktionskomponente der Entität ein.
     * Diese Methode erstellt eine neue Instanz der InteractionComponent-Komponente, die der Entität hinzugefügt wird.
     * Diese Komponente bestimmt den Interaktionsradius der Entität sowie die Aktion, die ausgeführt werden soll, wenn ein Spieler mit der Entität interagiert.
     */
    private void setupInteractionComponent() {
        new InteractionComponent(this, InteractionComponent.DEFAULT_RADIUS, true, (playerEntity) -> {


        });
    }
}
