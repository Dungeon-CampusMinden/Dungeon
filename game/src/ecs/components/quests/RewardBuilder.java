package ecs.components.quests;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Cake;
import ecs.entities.Entity;
import ecs.entities.Item;
import ecs.entities.MonsterPotion;
import ecs.entities.SpeedPotion;

/**
 * Creates new instances of IReward
 */
public class RewardBuilder {

    private static final Logger RB_LOGGER = Logger.getLogger(RewardBuilder.class.getSimpleName());

    private static final Class[] ITEMS = {
            Cake.class,
            MonsterPotion.class,
            SpeedPotion.class
    };

    /**
     * Chooses a random reward and builds a new instance
     *
     * @return new instance of IReward
     */
    public static IReward buildRandomReward() {
        final int METHOD_COUNT = 3;
        int key = (int) (Math.random() * METHOD_COUNT);
        switch (key) {

            case 1:
                return buildIncreaseMaxHealthReward();

            case 2:
                return buildItemReward();

            default:
                return buildHealReward();
        }
    }

    /**
     * Builds a new instance of IReward that will heal the player to maximum health
     *
     * @return new instance of IReward
     */
    public static IReward buildHealReward() {
        RB_LOGGER.info("Building new IReward (healing)");
        return new IReward() {

            @Override
            public void reward(Entity entity) {
                if (entity.getComponent(HealthComponent.class).isPresent()) {
                    HealthComponent health = ((HealthComponent) entity.getComponent(HealthComponent.class).get());
                    health.setCurrentHealthpoints(health.getMaximalHealthpoints());
                }
            }

            @Override
            public String toString() {
                return "get fully healed";
            }

        };
    }

    /**
     * Builds a new instance of IReward that will increase the player's maximum
     * healthpoints by one percent
     *
     * @return new instance of IReward
     */
    public static IReward buildIncreaseMaxHealthReward() {
        RB_LOGGER.info("Building new IReward (health increase healing)");
        return new IReward() {

            @Override
            public void reward(Entity entity) {
                if (entity.getComponent(HealthComponent.class).isPresent()) {
                    HealthComponent health = ((HealthComponent) entity.getComponent(HealthComponent.class).get());
                    health.setMaximalHealthpoints((int) (health.getMaximalHealthpoints() * 1.01));
                }
            }

            @Override
            public String toString() {
                return "get 1% more health points";
            }

        };
    }

    /**
     * Builds a new IReward that will grant the hero an item
     * 
     * @return returns a new instance of IReward
     */
    public static IReward buildItemReward() {
        int rnd = (int) (Math.random() * ITEMS.length);
        return new IReward() {

            @Override
            public void reward(Entity entity) {
                if (!entity.getComponent(PositionComponent.class).isPresent())
                    throw new MissingComponentException("PositionComponent");
                PositionComponent pc = (PositionComponent) entity.getComponent(PositionComponent.class).get();
                try {
                    Constructor constructor = ITEMS[rnd].getConstructor();
                    Item item = (Item) constructor.newInstance();
                    item.getComponent(PositionComponent.class).map(PositionComponent.class::cast).get()
                            .setPosition(pc.getPosition());
                } catch (Exception e) {
                    RB_LOGGER.warning(e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public String toString() {
                return "get a " + ITEMS[rnd].getSimpleName();
            }

        };
    }

    /**
     * Builds a new reward that will double the quest holders Healthpoints and fully
     * heal the quest holder
     *
     * @return new Instance of IReward
     */
    public static IReward buildBossReward() {
        RB_LOGGER.info("Building new IReward (health increase and healing)");
        return new IReward() {

            @Override
            public void reward(Entity entity) {
                if (entity.getComponent(HealthComponent.class).isPresent()) {
                    HealthComponent health = ((HealthComponent) entity.getComponent(HealthComponent.class).get());
                    health.setMaximalHealthpoints(health.getMaximalHealthpoints() * 2);
                }
                if (entity.getComponent(HealthComponent.class).isPresent()) {
                    HealthComponent health = ((HealthComponent) entity.getComponent(HealthComponent.class).get());
                    health.setCurrentHealthpoints(health.getMaximalHealthpoints());
                }
            }

            @Override
            public String toString() {
                return "get 100% more health points and get fully healed";
            }

        };
    }

}
