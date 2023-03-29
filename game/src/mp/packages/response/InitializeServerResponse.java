package mp.packages.response;

public class InitializeServerResponse {

    private final boolean isSucceed;

    public InitializeServerResponse(boolean isSucceed) {
        this.isSucceed = isSucceed;
    }

    public boolean isSucceed() {
        return isSucceed;
    }
}
