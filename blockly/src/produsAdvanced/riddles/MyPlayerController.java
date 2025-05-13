package produsAdvanced.riddles;

import produsAdvanced.abstraction.Hero;
import produsAdvanced.abstraction.PlayerController;

public class MyPlayerController extends PlayerController {

  private Hero hero;

  public MyPlayerController(Hero hero) {
    super(hero);
    this.hero = hero;
  }

  @Override
  protected void processKey(String key) {
    if (key.equals("W")) move(0, 5);
    if (key.equals("S")) move(0, -5);
    if (key.equals("A")) move(-5, 0);
    if (key.equals("D")) move(5, 0);
    if (key.equals("Q")) shoot();
  }

  private void move(float x, float y) {
    hero.setYSpeed(y);
    hero.setXSpeed(x);
  }

  private void shoot() {
    hero.shootFireball(hero.getMousePosition());
  }
}
