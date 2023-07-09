package contrib.utils.multiplayer.packages.response;

/**
 * Just to demonstrate communication.
 * Not relevant for multiplayer session.
 */
public class PingResponse {

    long time = System.currentTimeMillis();

    public long getTime() {
        return time;
    }
}
