package dslToGame.dsltypeproperties;

import core.Entity;
import core.components.PositionComponent;
import core.components.VelocityComponent;

import semanticanalysis.types.DSLTypeProperty;
import semanticanalysis.types.IDSLTypeProperty;

public class EntityExtension {
    @DSLTypeProperty(name = "velocity_component", extendedType = Entity.class)
    public static class VelocityComponentProperty
            implements IDSLTypeProperty<Entity, VelocityComponent> {
        public static EntityExtension.VelocityComponentProperty instance =
                new EntityExtension.VelocityComponentProperty();

        private VelocityComponentProperty() {}

        @Override
        public void set(Entity instance, VelocityComponent valueToSet) {
            instance.removeComponent(VelocityComponent.class);
            instance.addComponent(valueToSet);
        }

        @Override
        public VelocityComponent get(Entity instance) {
            var optionalComponent = instance.fetch(VelocityComponent.class);
            return optionalComponent.orElse(null);
        }

        @Override
        public boolean isSettable() {
            return true;
        }

        @Override
        public boolean isGettable() {
            return true;
        }
    }

    @DSLTypeProperty(name = "position_component", extendedType = Entity.class)
    public static class PositionComponentProperty
            implements IDSLTypeProperty<Entity, PositionComponent> {
        public static EntityExtension.PositionComponentProperty instance =
                new EntityExtension.PositionComponentProperty();

        private PositionComponentProperty() {}

        @Override
        public void set(Entity instance, PositionComponent valueToSet) {
            instance.removeComponent(PositionComponent.class);
            instance.addComponent(valueToSet);
        }

        @Override
        public PositionComponent get(Entity instance) {
            var optionalComponent = instance.fetch(PositionComponent.class);
            return optionalComponent.orElse(null);
        }

        @Override
        public boolean isSettable() {
            return true;
        }

        @Override
        public boolean isGettable() {
            return true;
        }
    }
}
