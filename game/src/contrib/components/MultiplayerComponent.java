package contrib.components;


import core.Component;
import core.Entity;

/** Used as flag for components, which has to be synchronized for MultiplayerSession. */
public class MultiplayerComponent extends Component {
    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public MultiplayerComponent(final Entity entity) {
        super(entity);
    }
}
