package contrib.entities;

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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;


/*
 * A Class solely dedicated to randomly generating all types of AIs.
 *
 * NOTE: Random ints get a +1 inside the random function due to the exclusionary nature of random.nextInt()
 * NOTE: If the first Monster rolls a ProtectOn AI, it will Protect itself.
 */
public class AIFactory {
    //FightAI Parameters:
    //CollideAI
    private static final float rushRangeLow = 0.5f;
    private static final float rushRangeHigh = 2.0f;

    //RangeAI
    private static final float attackRangeLow = 2.1f;
    private static final float attackRangeHigh = 8f;
    private static final float distanceLow = 1f;
    private static final float distanceHigh = 2f;


    //IdleAi Parameters:
    //PatroullieWalk
    private static final float patrouilleRadiusLow = 2f;
    private static final float patrouilleRadiusHigh = 6f;
    private static final int checkpointsLow = 2;
    private static final int checkpointsHigh = 6;
    private static final int pauseTimeLow = 1;
    private static final int pauseTimeHigh = 5;

    //RadiusWalk
    private static final float radiusWalkLow = 2f;
    private static final float radiusWalkHigh = 8f;
    private static final int breakTimeLow = 1;
    private static final int breakTimeHigh = 5;

    //StaticRadiusWalk
    private static final float staticRadiusWalkLow = 2f;
    private static final float staticRadiusWalkHigh = 8f;
    private static final int staticBreakTimeLow = 1;
    private static final int staticBreakTimeHigh = 5;


    //TransitionAI Parameters:
    //RangeTransition
    private static final float rangeTransitionLow = 2f;
    private static final float rangeTransitionHigh = 10f;

    //ProtectOnApproach
    private static final float protectRangeLow = 2f;
    private static final float protectRangeHigh = 8f;





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
                random.nextFloat(rushRangeLow, rushRangeHigh)
            );
            case 1 -> new RangeAI(
                random.nextFloat(attackRangeLow, attackRangeHigh),
                random.nextFloat(distanceLow,distanceHigh),
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
            default -> throw new IndexOutOfBoundsException("This IdleAI does not exist!");
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
                    random.nextFloat(patrouilleRadiusLow, patrouilleRadiusHigh),
                    random.nextInt(checkpointsLow, checkpointsHigh + 1),
                    random.nextInt(pauseTimeLow, pauseTimeHigh + 1),
                    modes[random.nextInt(0, modes.length)]
                );
            }
            case 1 -> {
                return new RadiusWalk(
                    random.nextFloat(radiusWalkLow, radiusWalkHigh),
                    random.nextInt(breakTimeLow, breakTimeHigh + 1)
                );
            }
            case 2 -> {
                return new StaticRadiusWalk(
                    random.nextFloat(staticRadiusWalkLow, staticRadiusWalkHigh),
                    random.nextInt(staticBreakTimeLow, staticBreakTimeHigh + 1)
                );
            }
            default -> throw new IndexOutOfBoundsException("This IdleAI does not exist!");
        }
    }

    /**
     * Constructs a random TransitionAI with random parameters.
     * Needs to be Updated whenever a new TransitionAI is added.
     * @return the generated TransitionAI
     */
    public static Function<Entity, Boolean> generateRandomTransitionAI(Entity entity){
        Random random = new Random();
        int index = random.nextInt(0,4);

        switch (index) {
            case 0 -> {
                return new RangeTransition(
                    random.nextFloat(rangeTransitionLow, rangeTransitionHigh)
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
                        random.nextFloat(protectRangeLow, protectRangeHigh),
                        randomMonster.get());
                }
                else{
                    transition = new ProtectOnApproach(
                        random.nextFloat(protectRangeLow, protectRangeHigh),
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
            default -> throw new IndexOutOfBoundsException("This IdleAI does not exist!");
        }
    }

    private static Optional<Entity> getRandomMonster(){
        Random random = new Random();
        Stream<Entity> monsterStream = Game.getEntitiesStream()
            .filter(m -> Objects.equals(m.toString(), "monster"));
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
