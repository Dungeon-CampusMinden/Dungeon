package contrib.utils.multiplayer.network.packages.event;

/**
 * Used to inform client that he is authenticated successfully. that server can synchronize over all
 */
public class OnAuthenticatedEvent {
    private final int assignedClientID;

    /**
     * create new instance.
     *
     * @param clientID From server assigned client ID.
     */
    public OnAuthenticatedEvent(final int clientID) {
        assignedClientID = clientID;
    }

    /**
     * @return From server assigned client ID.
     */
    public int assignedClientID() {
        return assignedClientID;
    }
}
