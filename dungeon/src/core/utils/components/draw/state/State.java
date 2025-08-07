package core.utils.components.draw.state;

import com.badlogic.gdx.graphics.g2d.Sprite;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;

import java.util.List;
import java.util.Map;

public class State {

  public final String name;
  protected Animation animation;
  protected Object data;

  public State(String name, Animation animation){
    if(name == null) throw new IllegalArgumentException("name can't be empty");
    if(animation == null) throw new IllegalArgumentException("animation can't be null");
    this.name = name;
    this.animation = animation;
  }
  public State(String name, IPath path, AnimationConfig config){
    this(name, new Animation(path, config));
  }
  public State(String name, IPath path, SpritesheetConfig config){
    this(name, path, new AnimationConfig(config));
  }
  public State(String name, IPath path){
    this(name, path, new AnimationConfig());
  }
  public State(String name, IPath... paths){
    this(name, new Animation(paths));
  }
  public State(String name, List<IPath> paths){
    this(name, new Animation(paths));
  }

  public void update(){
    getAnimation().update();
  }
  public void frameCount(int frameCount){
    getAnimation().frameCount(frameCount);
  }

  public Sprite getSprite(){
    return getAnimation().getSprite();
  }
  public float getSpriteWidth(){
    return getAnimation().getSpriteWidth();
  }
  public float getSpriteHeight(){
    return getAnimation().getSpriteHeight();
  }

  public boolean isAnimationFinished(){
    return getAnimation().isFinished();
  }

  public Animation getAnimation(){
    return animation;
  }

  public Object getData(){ return data; }
  public void setData(Object data){
    this.data = data;
  }

  public static State fromMap(Map<String, Animation> map, String name){
    if(name == null) throw new IllegalArgumentException("name can't be null");
    if(map == null) throw new IllegalArgumentException("map can't be null");
    return new State(name, map.get(name));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
      "name='" + name + '\'' +
      ", animation=" + animation +
      ", data=" + data +
      '}';
  }
}
