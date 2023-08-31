package petriNet;

public class Place {
    private int tokenCount = 0;

    public void placeToken() {
        tokenCount++;
    }

    public int tokenCount() {
        return tokenCount;
    }
}
