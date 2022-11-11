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
    private int openingAnimationTime = 15;
    protected Hitbox hitbox;
    private List<Item> inventory;

    public TreasureChest(Point position) {
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

        this.currentPosition = position;
    }

    @Override
    public void update() {
        if (isOpen) {
            openingAnimationTime--;
            if (openingAnimationTime <= 0) {
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

    /** Action to do a collision
     *
     * @param other Object you colide with
     * @param from Direction from where you colide
     */
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

    /** Adds items into the treasure chest
     *
     * @param item Item to add into the treasure chest
     */
    public void addItem(Item item) {
        inventory.add(item);
    }
}
