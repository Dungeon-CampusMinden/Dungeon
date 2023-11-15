package core.gui.backend;

public abstract class BackendImage {

    protected int width, height, channels;

    public BackendImage(int width, int height, int channels) {
        this.width = width;
        this.height = height;
        this.channels = channels;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int channels() {
        return channels;
    }

    /** Frees the image from (video-)memory. */
    public abstract void free();
}
