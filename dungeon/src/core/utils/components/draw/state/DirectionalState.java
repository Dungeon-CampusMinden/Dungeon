package core.utils.components.draw.state;

import core.utils.Direction;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import core.utils.components.path.IPath;

import java.util.Map;

public class DirectionalState extends State {

//  private Animation down; //Default animation
  private Animation left;
  private Animation up;
  private Animation right;

  public DirectionalState(String name, Map<String, Animation> map, String prefix) {
    super(name, map.get(prefix+"_down"));
    this.left = map.get(prefix+"_left");
    this.up = map.get(prefix+"_up");
    this.right = map.get(prefix+"_right");
  }
  public DirectionalState(String name, Map<String, Animation> map) {
    this(name, map, name);
  }
  public DirectionalState(String name, Animation down, Animation left, Animation up, Animation right) {
    super(name, down);
    this.left = left;
    this.up = up;
    this.right = right;
  }
  public DirectionalState(String name, IPath down, IPath left, IPath up, IPath right, AnimationConfig configDown, AnimationConfig configLeft, AnimationConfig configUp, AnimationConfig configRight) {
    this(name, new Animation(down, configDown), new Animation(left, configLeft), new Animation(up, configUp), new Animation(right, configRight));
  }
  public DirectionalState(String name, IPath down, IPath left, IPath up, IPath right, AnimationConfig config) {
    this(name, down, left, up, right, config, config, config, config);
  }
  public DirectionalState(String name, IPath path, SpritesheetConfig config) {
    super(name, path, config);
  }
  public DirectionalState(String name, IPath down, IPath left, IPath up, IPath right) {
    this(name, down, left, up, right, null);
  }

  @Override
  public Animation getAnimation() {
    Direction direction = (Direction) getData();
    if(direction == null) return super.getAnimation();
    return switch(direction){
      case DOWN, NONE -> super.getAnimation();
      case LEFT -> left;
      case UP -> up;
      case RIGHT -> right;
    };
  }
}
