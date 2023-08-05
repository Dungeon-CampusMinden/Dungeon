package core;

public class Filter {

    /**
     * Check if the given entity has all the components needed to be processed by this system.
     *
     * <p>If one or more additionally components are missing, this system will create a log entry
     * with information about the missing components.
     *
     * @param entity the entity to check
     * @return true if the entity is accepted, false if not.
     */
    protected boolean accept(Entity entity) {
        if (entity.isPresent(keyComponent)) {
            for (Class<? extends Component> klass : additionalComponents)
                if (!entity.isPresent(klass)) {
                    // will log also other missing components, so we can stop here
                    logMissingComponent(entity);
                    return false;
                }
            return true;
        }
        return false;
    }
}
