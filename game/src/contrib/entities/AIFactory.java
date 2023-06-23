package contrib.entities;

import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.fight.RangeAI;
import contrib.utils.components.ai.idle.PatrouilleWalk;
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
import core.components.DrawComponent;
import core.components.VelocityComponent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * A Class solely dedicated to randomly generating all types of AIs.
 *
 * NOTE: Random ints get a +1 inside the random function due to the exclusionary nature of random.nextInt()
 * NOTE: If the first Monster rolls a ProtectOn AI, it will Protect itself.
 */
public class AIFactory {
    //FightAI Parameters:
    //CollideAI
    private static final float RUSH_RANGE_LOW = 0.5f;
    private static final float RUSH_RANGE_HIGH = 2.0f;

    //RangeAI
    private static final float ATTACK_RANGE_LOW = 2.1f;
    private static final float ATTACK_RANGE_HIGH = 8f;
    private static final float DISTANCE_LOW = 1f;
    private static final float DISTANCE_HIGH = 2f;


    //IdleAi Parameters:
    //PatroullieWalk
    private static final float PATROUILLE_RADIUS_LOW = 2f;
    private static final float PATROUILLE_RADIUS_HIGH = 6f;
    private static final int CHECKPOINTS_LOW = 2;
    private static final int CHECKPOINTS_HIGH = 6;
    private static final int PAUSE_TIME_LOW = 1;
    private static final int PAUSE_TIME_HIGH = 5;

    //RadiusWalk
    private static final float RADIUS_WALK_LOW = 2f;
    private static final float RADIUS_WALK_HIGH = 8f;
    private static final int BREAK_TIME_LOW = 1;
    private static final int BREAK_TIME_HIGH = 5;

    //StaticRadiusWalk
    private static final float STATIC_RADIUS_WALK_LOW = 2f;
    private static final float STATIC_RADIUS_WALK_HIGH = 8f;
    private static final int STATIC_BREAK_TIME_LOW = 1;
    private static final int STATIC_BREAK_TIME_HIGH = 5;


    //TransitionAI Parameters:
    //RangeTransition
    private static final float RANGE_TRANSITION_LOW = 2f;
    private static final float RANGE_TRANSITION_HIGH = 10f;

    //ProtectOnApproach
    private static final float PROTECT_RANGE_LOW = 2f;
    private static final float PROTECT_RANGE_HIGH = 8f;





    /**
     * Constructs a random FightAI with random parameters.
     * Needs to be Updated whenever a new FightAI is added.
     * @return the generated FightAI
     * NOTE: MeleeAI currently gets excluded from generation due to a bug that crashes the Game.
     * The MeleeAI seems to have trouble with Pathing as it often has null as its path, causing the crash.
     */
    public static Consumer<Entity> generateRandomFightAI(){
        Random random = new Random();
        int index = random.nextInt(0,2);

        return switch (index) {
            case 0 -> new CollideAI(
                random.nextFloat(RUSH_RANGE_LOW, RUSH_RANGE_HIGH)
            );
            case 1 -> new RangeAI(
                random.nextFloat(ATTACK_RANGE_LOW, ATTACK_RANGE_HIGH),
                random.nextFloat(DISTANCE_LOW, DISTANCE_HIGH),
                new Skill(
                    new FireballSkill(SkillTools::getHeroPositionAsPoint),
                    1)
            );
            /*
            case 2 -> new MeleeAI(
                1f,
                new Skill(
                    new FireballSkill(SkillTools::getHeroPositionAsPoint),
                    1)
            );*/
            default -> throw new IndexOutOfBoundsException("This FightAI does not exist");
        };
    }

    /**
     * Constructs a random IdleAI with random parameters.
     * Needs to be Updated whenever a new IdleAI is added.
     * @return the generated IdleAI
     */
    public static Consumer<Entity> generateRandomIdleAI(){
        Random random = new Random();
        int index = random.nextInt(0,3);

        switch (index) {
            case 0 -> {
                PatrouilleWalk.MODE[] modes = PatrouilleWalk.MODE.values();
                return new PatrouilleWalk(
                    random.nextFloat(PATROUILLE_RADIUS_LOW, PATROUILLE_RADIUS_HIGH),
                    random.nextInt(CHECKPOINTS_LOW, CHECKPOINTS_HIGH + 1),
                    random.nextInt(PAUSE_TIME_LOW, PAUSE_TIME_HIGH + 1),
                    modes[random.nextInt(0, modes.length)]
                );
            }
            case 1 -> {
                return new RadiusWalk(
                    random.nextFloat(RADIUS_WALK_LOW, RADIUS_WALK_HIGH),
                    random.nextInt(BREAK_TIME_LOW, BREAK_TIME_HIGH + 1)
                );
            }
            case 2 -> {
                return new StaticRadiusWalk(
                    random.nextFloat(STATIC_RADIUS_WALK_LOW, STATIC_RADIUS_WALK_HIGH),
                    random.nextInt(STATIC_BREAK_TIME_LOW, STATIC_BREAK_TIME_HIGH + 1)
                );
            }
            default -> throw new IndexOutOfBoundsException("This IdleAI does not exist");
        }
    }

    /**
     * Constructs a random TransitionAI with random parameters.
     * Needs to be Updated whenever a new TransitionAI is added.
     * @return the generated TransitionAI
     */
    public static Function<Entity, Boolean> generateRandomTransitionAI(Entity entity){
        Random random = new Random();
        int index = random.nextInt(0, 4);

        switch (index) {
            case 0 -> {
                return new RangeTransition(
                    random.nextFloat(RANGE_TRANSITION_LOW, RANGE_TRANSITION_HIGH)
                );
            }
            case 1 -> {
                return new SelfDefendTransition();
            }
            case 2 -> {
                ProtectOnApproach transition;
                Optional<Entity> randomMonster = getRandomMonster();
                if(getRandomMonster().isPresent()){
                    transition = new ProtectOnApproach(
                        random.nextFloat(PROTECT_RANGE_LOW, PROTECT_RANGE_HIGH),
                        randomMonster.get());
                }
                else{
                    transition = new ProtectOnApproach(
                        random.nextFloat(PROTECT_RANGE_LOW, PROTECT_RANGE_HIGH),
                        entity);
                }
                return transition;
            }
            case 3 -> {
                ProtectOnAttack transition;
                Optional<Entity> randomMonster = getRandomMonster();
                if(getRandomMonster().isPresent()){
                    transition = new ProtectOnAttack(randomMonster.get());
                }
                else{
                    transition = new ProtectOnAttack(entity);
                }
                return transition;
            }
            default -> throw new IndexOutOfBoundsException("This TransitionAI does not exist");
        }
    }

    /**
     * Goes filters all Entities for Monsters and returns a random one.
     * @return returns either a random Monster or null if it cant find one
     */
    private static Optional<Entity> getRandomMonster(){
        Random random = new Random();
        Stream<Entity> monsterStream = Game.entityStream()
            .filter(m -> m.fetch(HealthComponent.class).isPresent())
            .filter(m -> m.fetch(AIComponent.class).isPresent())
            .filter(m -> m.fetch(VelocityComponent.class).isPresent());

        List<Entity> monsterList = monsterStream.toList();

        if(monsterList.size() > 0){
            Entity monster = monsterList.get(random.nextInt(monsterList.size()));
            return Optional.ofNullable(monster);
        }
        else{
            return Optional.ofNullable(null);
        }
    }
}
