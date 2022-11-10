package character.objects;

import basiselements.AnimatableElement;
import character.player.Hero;
import collision.CharacterDirection;
import collision.Collidable;
import collision.Hitbox;
import graphic.Animation;
import java.util.ArrayList;
import java.util.List;
import level.elements.ILevel;
import textures.TextureHandler;
import tools.Point;

public class TreasureChest extends AnimatableElement implements Collidable {
    private Animation closed;
    private Animation opening;
    private Animation opened;
    private Animation currentAnimation;
    private Point currentPosition;
    private ILevel currentLevel;
    private boolean isOpen = false;
    private int counter = 15;
    protected Hitbox hitbox;
    private List<Item> inventory;

    public TreasureChest() {
        List<String> texturePaths = TextureHandler.getInstance().getTexturePaths("ui_heart_full");
        closed = new Animation(texturePaths, 1);
        texturePaths = TextureHandler.getInstance().getTexturePaths("ui_heart");
        opening = new Animation(texturePaths, 5);
        texturePaths = TextureHandler.getInstance().getTexturePaths("ui_heart_empty");
        opened = new Animation(texturePaths, 1);

        currentAnimation = closed;

        hitbox = new Hitbox(6, 6);
        hitbox.setCollidable(this);
        inventory = new ArrayList<>();
    }

    public void setLevel(ILevel level) {
        currentLevel = level;
        currentPosition = level.getStartTile().getCoordinate().toPoint();
    }

    @Override
    public void update() {
        if (isOpen) {
            counter--;
            if (counter <= 0) {
                currentAnimation = opened;
            }
        }
    }

    @Override
    public Hitbox getHitbox() {
        return hitbox;
    }

    @Override
    public Point getPosition() {
        return currentPosition;
    }

    @Override
    public void colide(Collidable other, CharacterDirection from) {
        if (other instanceof Hero && !isOpen) {
            currentAnimation = opening;
            inventory.forEach(i -> i.collect());
            inventory.clear();
            isOpen = true;
        }
    }

    @Override
    public Animation getActiveAnimation() {
        return currentAnimation;
    }

    public void addItem(Item item) {
        inventory.add(item);
    }
}
