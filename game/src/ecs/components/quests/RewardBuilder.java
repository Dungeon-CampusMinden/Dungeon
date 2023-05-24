package ecs.components.quests;

import java.util.logging.Logger;

import ecs.components.HealthComponent;
import ecs.entities.Entity;

/**
 * Creates new instances of IReward
 */
public class RewardBuilder {
    private static final Logger RB_LOGGER = Logger.getLogger(RewardBuilder.class.getSimpleName());
    /**
     * Chooses a random reward and builds a new instance
     *
     * @return new instance of IReward
     */
    public static IReward buildRandomReward() {
        final int METHOD_COUNT = 2;
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

    // TODO: Implement items
    public static IReward buildItemReward() {
        throw new UnsupportedOperationException("The method buildItemReward is not yet implemented");
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
