package contrib.utils.multiplayer.network.packages.response;

/** Just to demonstrate communication. Not relevant for multiplayer session. */
public class PingResponse {

    private final long time = System.currentTimeMillis();

    /**
     * @return Time in milliseconds that indicates when instance was created.
     */
    public long getTime() {
        return time;
    }
}
