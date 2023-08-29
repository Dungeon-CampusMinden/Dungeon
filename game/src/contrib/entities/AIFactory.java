package contrib.entities;

import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.fight.RangeAI;
import contrib.utils.components.ai.idle.PatrolWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.idle.StaticRadiusWalk;
import contrib.utils.components.ai.transition.ProtectOnApproach;
import contrib.utils.components.ai.transition.ProtectOnAttack;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.ai.transition.SelfDefendTransition;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;

import core.Entity;
import core.Game;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A Class solely dedicated to randomly generating all types of AIs.
 *
 * <p>NOTE: Random ints get a +1 inside the random function due to the exclusionary nature of
 * random.nextInt() NOTE: If the first Monster rolls a ProtectOn AI, it will Protect itself.
 */
public class AIFactory {

    private static final Random RANDOM = new Random();

    // FightAI Parameters:
    // CollideAI
    private static final float RUSH_RANGE_LOW = 0.5f;
    private static final float RUSH_RANGE_HIGH = 2.0f;

    // RangeAI
    private static final float ATTACK_RANGE_LOW = 2.1f;
    private static final float ATTACK_RANGE_HIGH = 8f;
    private static final float DISTANCE_LOW = 1f;
    private static final float DISTANCE_HIGH = 2f;

    // IdleAi Parameters:
    // PatrouilleWalk
    private static final float PATROUILLE_RADIUS_LOW = 2f;
    private static final float PATROUILLE_RADIUS_HIGH = 6f;
    private static final int CHECKPOINTS_LOW = 2;
    private static final int CHECKPOINTS_HIGH = 6;
    private static final int PAUSE_TIME_LOW = 1;
    private static final int PAUSE_TIME_HIGH = 5;

    // RadiusWalk
    private static final float RADIUS_WALK_LOW = 2f;
    private static final float RADIUS_WALK_HIGH = 8f;
    private static final int BREAK_TIME_LOW = 1;
    private static final int BREAK_TIME_HIGH = 5;

    // StaticRadiusWalk
    private static final float STATIC_RADIUS_WALK_LOW = 2f;
    private static final float STATIC_RADIUS_WALK_HIGH = 8f;
    private static final int STATIC_BREAK_TIME_LOW = 1;
    private static final int STATIC_BREAK_TIME_HIGH = 5;

    // TransitionAI Parameters:
    // RangeTransition
    private static final float RANGE_TRANSITION_LOW = 2f;
    private static final float RANGE_TRANSITION_HIGH = 10f;

    // ProtectOnApproach
    private static final float PROTECT_RANGE_LOW = 2f;
    private static final float PROTECT_RANGE_HIGH = 8f;

    /**
     * Constructs a random FightAI with random parameters. Needs to be Updated whenever a new
     * FightAI is added.
     *
     * @return the generated FightAI NOTE: MeleeAI currently gets excluded from generation due to a
     *     bug that crashes the Game. The MeleeAI seems to have trouble with Pathing as it often has
     *     null as its path, causing the crash.
     */
    public static Consumer<Entity> generateRandomFightAI() {
        int index = RANDOM.nextInt(0, 2);

        return switch (index) {
            case 0 -> new CollideAI(RANDOM.nextFloat(RUSH_RANGE_LOW, RUSH_RANGE_HIGH));
            default -> new RangeAI(
                    RANDOM.nextFloat(ATTACK_RANGE_LOW, ATTACK_RANGE_HIGH),
                    RANDOM.nextFloat(DISTANCE_LOW, DISTANCE_HIGH),
                    new Skill(new FireballSkill(SkillTools::heroPositionAsPoint), 1));
                // siehe Bug https://github.com/Programmiermethoden/Dungeon/issues/812
                /*
                case 2 -> new MeleeAI(
                    1f,
                    new Skill(
                        new FireballSkill(SkillTools::getHeroPositionAsPoint),
                        1)
                );*/
        };
    }

    /**
     * Constructs a random IdleAI with random parameters. Needs to be Updated whenever a new IdleAI
     * is added.
     *
     * @return the generated IdleAI
     */
    public static Consumer<Entity> generateRandomIdleAI() {
        int index = RANDOM.nextInt(0, 3);

        switch (index) {
            case 0 -> {
                PatrolWalk.MODE[] modes = PatrolWalk.MODE.values();
                return new PatrolWalk(
                        RANDOM.nextFloat(PATROUILLE_RADIUS_LOW, PATROUILLE_RADIUS_HIGH),
                        RANDOM.nextInt(CHECKPOINTS_LOW, CHECKPOINTS_HIGH + 1),
                        RANDOM.nextInt(PAUSE_TIME_LOW, PAUSE_TIME_HIGH + 1),
                        modes[RANDOM.nextInt(0, modes.length)]);
            }
            case 1 -> {
                return new RadiusWalk(
                        RANDOM.nextFloat(RADIUS_WALK_LOW, RADIUS_WALK_HIGH),
                        RANDOM.nextInt(BREAK_TIME_LOW, BREAK_TIME_HIGH + 1));
            }
            default -> {
                return new StaticRadiusWalk(
                        RANDOM.nextFloat(STATIC_RADIUS_WALK_LOW, STATIC_RADIUS_WALK_HIGH),
                        RANDOM.nextInt(STATIC_BREAK_TIME_LOW, STATIC_BREAK_TIME_HIGH + 1));
            }
        }
    }

    /**
     * Constructs a random TransitionAI with random parameters. Needs to be Updated whenever a new
     * TransitionAI is added.
     *
     * @return the generated TransitionAI
     */
    public static Function<Entity, Boolean> generateRandomTransitionAI(Entity entity) {
        int index = RANDOM.nextInt(0, 4);

        switch (index) {
            case 0 -> {
                return new RangeTransition(
                        RANDOM.nextFloat(RANGE_TRANSITION_LOW, RANGE_TRANSITION_HIGH));
            }
            case 1 -> {
                return new SelfDefendTransition();
            }
            case 2 -> {
                return new ProtectOnApproach(
                        RANDOM.nextFloat(PROTECT_RANGE_LOW, PROTECT_RANGE_HIGH),
                        randomMonsterOrMe(entity));
            }
            default -> {
                return new ProtectOnAttack(randomMonsterOrMe(entity));
            }
        }
    }

    /**
     * Returns random entity from Game that is a monster.
     *
     * <p>A monster is an Entity with a {@link HealthComponent} and an {@link AIComponent}.
     *
     * <p>Use this method to get an entity as parameter for specific AI-Behaviors like {@link
     * ProtectOnAttack}
     *
     * @return a random monster from the game or null no monster exists in the game.
     */
    private static Optional<Entity> randomMonster() {
        Stream<Entity> monsterStream =
                Game.entityStream()
                        .filter(m -> m.fetch(HealthComponent.class).isPresent())
                        .filter(m -> m.fetch(AIComponent.class).isPresent());

        List<Entity> monsterList = monsterStream.toList();
        Entity monster = null;

        if (monsterList.size() > 0) {
            monster = monsterList.get(RANDOM.nextInt(monsterList.size()));
        }
        return Optional.ofNullable(monster);
    }

    /**
     * Returns random entity from Game that is a monster. If no monster exist, return the given
     * entity,
     *
     * <p>A monster is an Entity with a {@link HealthComponent} and an {@link AIComponent}.
     *
     * <p>Use this method to get an entity as parameter for specific AI-Behaviors like {@link
     * ProtectOnAttack}
     *
     * @param me entity to return if the no monster exist in the game
     * @return a random monster from the game or the given entity if no monster exists in the game.
     */
    private static Entity randomMonsterOrMe(Entity me) {
        AtomicReference<Entity> r = new AtomicReference<>(me);
        randomMonster().ifPresent(r::set);
        return r.get();
    }
}
