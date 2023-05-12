package mp.packages.response;

public class InitServerResponse {
    private final boolean isSucceed;

    public InitServerResponse(final boolean isSucceed){
        this.isSucceed = isSucceed;
    }

    public boolean isSucceed() {
        return isSucceed;
    }
}
