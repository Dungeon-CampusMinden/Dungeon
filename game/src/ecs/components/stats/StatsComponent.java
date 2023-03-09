package ecs.components.stats;

import ecs.components.Component;
import ecs.entities.Entity;

public class StatsComponent extends Component {

    private Stats stats = new Stats();

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public StatsComponent(Entity entity) {
        super(entity);
    }

    /**
     * Get the stats object of the entity
     *
     * @return stats object
     */
    public Stats getStats() {
        return this.stats;
    }

    /**
     * Overwrite the stats object of the entity
     *
     * @param stats new stats object
     */
    public void setStats(Stats stats) {
        this.stats = stats;
    }
}
