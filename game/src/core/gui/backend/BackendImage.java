package core.gui.backend;

import core.Assets;

public abstract class BackendImage {

    private final Assets.Images image;
    protected int width, height, channels;

    public BackendImage(Assets.Images image, int width, int height, int channels) {
        this.image = image;
        this.width = width;
        this.height = height;
        this.channels = channels;
    }

    public Assets.Images image() {
        return image;
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
}
