package dungine;

import dungine.state.hero.HeroState;

public class Main {

  public static void main(String[] args) {
    Dungine dungine = new Dungine(true);
    dungine.setState(new HeroState(dungine));
    //dungine.setStateTransition(new StartupTransition(dungine));
    //dungine.setState(new MainMenuState(dungine));
    dungine.start();
  }

}
