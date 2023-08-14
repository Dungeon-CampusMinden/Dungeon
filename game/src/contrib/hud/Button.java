package contrib.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import core.Game;
import core.utils.components.draw.TextureMap;

import java.util.function.Consumer;

public class Button {

    private static final Texture TEXTURE_BUTTON;
    private static final Texture TEXTURE_BUTTON_HOVER;
    private static final Texture TEXTURE_BUTTON_PRESS;

    static {
        TEXTURE_BUTTON = TextureMap.instance().textureAt("hud/button/button_idle.png");
        TEXTURE_BUTTON_HOVER = TextureMap.instance().textureAt("hud/button/button_hover.png");
        TEXTURE_BUTTON_PRESS = TextureMap.instance().textureAt("hud/button/button_press.png");
    }

    protected CombinableGUI parent;
    protected int x, y, width, height;
    private boolean pressed = false;
    private Consumer<Button> onClick;

    public Button(CombinableGUI parent, int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.parent = parent;
        this.init();
    }

    private void init() {
        this.parent
                .actor()
                .addListener(
                        new InputListener() {
                            @Override
                            public boolean touchDown(
                                    InputEvent event, float x, float y, int pointer, int button) {
                                if (Button.this.x() <= (x + Button.this.parent.x())
                                        && (Button.this.x() + Button.this.width())
                                                >= (x + Button.this.parent.x())
                                        && Button.this.y() <= (y + Button.this.parent.y())
                                        && (Button.this.y() + Button.this.height())
                                                >= (y + Button.this.parent.y())) {
                                    Button.this.pressed = true;
                                    Button.this.onClick.accept(Button.this);
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public void touchUp(
                                    InputEvent event, float x, float y, int pointer, int button) {
                                Button.this.pressed = false;
                            }
                        });
    }

    public void onClick(Consumer<Button> onClick) {
        this.onClick = onClick;
    }

    public void draw(Batch batch) {
        int mouseX = Gdx.input.getX();
        int mouseY = Math.round(Game.stage().orElseThrow().getHeight()) - Gdx.input.getY();
        if (mouseX >= this.x
                && mouseX <= this.x + this.width
                && mouseY >= this.y
                && mouseY <= this.y + this.height) {
            batch.draw(
                    this.pressed ? TEXTURE_BUTTON_PRESS : TEXTURE_BUTTON_HOVER,
                    this.x,
                    this.y,
                    this.width,
                    this.height);
        } else {
            batch.draw(TEXTURE_BUTTON, this.x, this.y, this.width, this.height);
        }
    }

    public int x() {
        return this.x;
    }

    public void x(int x) {
        this.x = x;
    }

    public int y() {
        return this.y;
    }

    public void y(int y) {
        this.y = y;
    }

    public int width() {
        return this.width;
    }

    public void width(int width) {
        this.width = width;
    }

    public int height() {
        return this.height;
    }

    public void height(int height) {
        this.height = height;
    }
}
