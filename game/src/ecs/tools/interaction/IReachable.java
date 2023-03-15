package ecs.tools.interaction;

public interface IReachable {
    /**
     * Implementation of a Check if the Interaction is valid and could trigger the interaction
     *
     * @param interactionData the prepared Data of an Interaction between two Entities
     * @return true if the interaction is valid, otherwise false
     */
    boolean checkReachable(InteractionData interactionData);
}
