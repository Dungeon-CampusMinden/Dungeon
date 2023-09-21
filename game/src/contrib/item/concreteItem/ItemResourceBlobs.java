package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceBlobs extends Item {
    public ItemResourceBlobs() {
        super("Blobs", "Blobs.", Animation.of("items/resource/blobs.png"));
    }
}
