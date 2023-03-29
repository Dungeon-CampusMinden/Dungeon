package mp.packages.request;

public class DataChunk {
    private byte[] chunk;
    private boolean isLastChunk;

    public DataChunk() {
    }

    public DataChunk(byte[] chunk, boolean isLastChunk) {
        this.chunk = chunk;
        this.isLastChunk = isLastChunk;
    }

    public byte[] getChunk() {
        return this.chunk;
    }
}
