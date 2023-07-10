package contrib.utils.multiplayer.packages.response;

import contrib.utils.multiplayer.packages.request.InitializeServerRequest;

/** Response of {@link InitializeServerRequest} */
public class InitializeServerResponse {
    private final boolean isSucceed;

    /**
     * Create a new Instance.
     *
     * @param isSucceed State whether server has been initialized or not.
     */
    public InitializeServerResponse(final boolean isSucceed) {
        this.isSucceed = isSucceed;
    }

    /**
     * @return State whether server has been initialized or not.
     */
    public boolean isSucceed() {
        return isSucceed;
    }
}
