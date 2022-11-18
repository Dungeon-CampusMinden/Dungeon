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
    protected Animation opening;
    private Animation opened;
    protected Animation currentAnimation;
    private Point currentPosition;
    private ILevel currentLevel;
    protected boolean isOpen = false;
    private final int openingFrames = 3;
    private final int openingFrameTime = 30;
    private int openingAnimationTime = (int) ((openingFrameTime * openingFrames) / 2);
    protected Hitbox hitbox;
    protected List<Item> inventory;

    public TreasureChest(Point position) {
        List<String> texturePaths =
                TextureHandler.getInstance().getTexturePaths("chest_full_open_anim_f0");
        closed = new Animation(texturePaths, 1);
        texturePaths = TextureHandler.getInstance().getTexturePaths("chest_full_open_anim_");
        opening = new Animation(texturePaths, openingFrameTime);
        texturePaths = TextureHandler.getInstance().getTexturePaths("chest_empty_open_anim_f2.png");

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

    /**
     * Action to do a collision
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

    /**
     * Adds items into the treasure chest
     *
     * @param item Item to add into the treasure chest
     */
    public void addItem(Item item) {
        inventory.add(item);
    }
}
