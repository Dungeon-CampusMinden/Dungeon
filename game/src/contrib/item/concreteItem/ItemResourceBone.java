package contrib.item.concreteItem;

import contrib.item.Item;

import core.utils.components.draw.Animation;

public class ItemResourceBone extends Item {
    public ItemResourceBone() {
        super("Bone", "A bone. Who knows what it's from?", Animation.of("items/resource/bone.png"));
    }
}
