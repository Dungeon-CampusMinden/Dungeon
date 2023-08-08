package contrib.hud.inventory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

import contrib.utils.components.item.ItemData;

import core.utils.components.draw.Animation;

public class ItemActor extends Actor {

    private ItemData itemData;

    private Animation inventoryAnimation;

    public ItemActor(ItemData itemData) {
        this.itemData = itemData;
        this.inventoryAnimation = itemData.item().inventoryAnimation();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.draw(
                new Texture(inventoryAnimation.nextAnimationTexturePath()),
                getX(),
                getY(),
                getWidth(),
                getHeight());
    }
}
