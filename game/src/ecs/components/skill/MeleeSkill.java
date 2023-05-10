package ecs.components.skill;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.collision.ICollide;
import ecs.damage.Damage;
import ecs.entities.Entity;
import graphic.Animation;
import level.elements.tile.Tile;
import starter.Game;
import tools.Point;

import static starter.Game.currentLevel;

public class MeleeSkill implements ISkillFunction {

    private String pathToTexturesOfProjectile;
    private float projectileSpeed;

    private float projectileRange;
    private Damage projectileDamage;
    private Point projectileHitboxSize;

    private ITargetSelection selectionFunction;
    private float knockbackDistance;


    /**
     * Konstruiert ein neues MeleeSkill-Objekt mit dem angegebenen Projektil-Texturpfad, der Geschwindigkeit, dem Schaden, der Hitbox-Größe, dem Rückstoßabstand, der Ziel-Auswahl-Funktion und der Reichweite.
     *
     * @param pathToTexturesOfProjectile Der Pfad zu den Texturdateien für das Nahkampf-Angriffsprojektil.
     * @param projectileSpeed            Die Geschwindigkeit des Nahkampf-Angriffsprojektils.
     * @param projectileDamage           Der Schaden, der durch das Nahkampf-Angriffsprojektil verursacht wird.
     * @param projectileHitboxSize       Die Hitbox-Größe des Nahkampf-Angriffsprojektils.
     * @param knockbackDistance          Der Rückstoßabstand, der bei jeder Kollision mit dem Nahkampf-Angriffsprojektil angewendet wird.
     * @param selectionFunction          Die Ziel-Auswahl-Funktion für das Nahkampf-Angriffsprojektil.
     * @param projectileRange            Die Reichweite des Nahkampf-Angriffsprojektils.
     */

    public MeleeSkill(
        String pathToTexturesOfProjectile,
        float projectileSpeed,
        Damage projectileDamage,
        Point projectileHitboxSize,
        float knockbackDistance,
        ITargetSelection selectionFunction,
        float projectileRange) {
        this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
        this.projectileDamage = projectileDamage;
        this.projectileSpeed = projectileSpeed;
        this.projectileRange = projectileRange;
        this.projectileHitboxSize = projectileHitboxSize;
        this.selectionFunction = selectionFunction;
        this.knockbackDistance = knockbackDistance;
    }

    /**
     * Führt die Nahkampf-Angriffs-Fähigkeit aus, indem eine Projektil-Entität erstellt und Schaden sowie Rückstoß auf jede Kollision mit einer anderen Entität angewendet wird.
     *
     * @param entity Die Entität, die die Fähigkeit ausführt.
     */

    @Override
    public void execute(Entity entity) {
        Entity meleeAttack = new Entity();
        PositionComponent epc =
            (PositionComponent)
                entity.getComponent(PositionComponent.class)
                    .orElseThrow(
                        () -> new MissingComponentException("PositionComponent"));
        new PositionComponent(meleeAttack, epc.getPosition());

        Animation animation = AnimationBuilder.buildAnimation(pathToTexturesOfProjectile);
        new AnimationComponent(meleeAttack, animation);

        Point aimedOn = selectionFunction.selectTargetPoint();
        Point targetPoint =
            SkillTools.calculateLastPositionInRange(
                epc.getPosition(), aimedOn, projectileRange);
        Point velocity =
            SkillTools.calculateVelocity(epc.getPosition(), targetPoint, projectileSpeed);
        VelocityComponent vc =
            new VelocityComponent(meleeAttack, velocity.x, velocity.y, animation, animation);
        new ProjectileComponent(meleeAttack, epc.getPosition(), targetPoint);

        ICollide collide =
            (a, b, from) -> {
                if (b != entity) {
                    b.getComponent(HealthComponent.class)
                        .ifPresent(
                            hc -> {
                                ((HealthComponent) hc).receiveHit(projectileDamage);
                                applyKnockback(b, entity, knockbackDistance);
                                Game.removeEntity(meleeAttack);
                            });
                }
            };

        new HitboxComponent(
            meleeAttack, new Point(0.25f, 0.25f), projectileHitboxSize, collide, null);
    }

    /**
     * Wendet Rückstoß auf die spezifizierte Ziel-Entität basierend auf der Entfernung und Richtung vom Ziel auf die Entität an.
     *
     * @param target            Die Entität, auf die Rückstoß angewendet wird.
     * @param entity            Die Entität, die den Rückstoß verursacht.
     * @param knockbackDistance Der Abstand, um den Rückstoß anzuwenden.
     */
    public void applyKnockback(Entity target, Entity entity, float knockbackDistance) {
        PositionComponent targetPositionComponent =
            (PositionComponent) target.getComponent(PositionComponent.class)
                .orElseThrow(
                    () -> new MissingComponentException("PositionComponent for target"));
        PositionComponent entityPositionComponent =
            (PositionComponent) entity.getComponent(PositionComponent.class)
                .orElseThrow(
                    () -> new MissingComponentException("PositionComponent for entity"));

        Point direction = Point.getUnitDirectionalVector(targetPositionComponent.getPosition(), entityPositionComponent.getPosition());

        Point newPosition = new Point(

            targetPositionComponent.getPosition().x + direction.x * knockbackDistance,
            targetPositionComponent.getPosition().y + direction.y * knockbackDistance
        );

        Tile newTile = currentLevel.getTileAt(newPosition.toCoordinate());
        if (newTile.isAccessible()) {
            targetPositionComponent.setPosition(newPosition);
        }

    }
}
