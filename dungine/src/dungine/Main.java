package dungine;


import dungine.state.mainmenu.MainMenuState;
import dungine.transitions.StartupTransition;

public class Main {

  public static void main(String[] args) {
    Dungine dungine = new Dungine(true);
    dungine.setStateTransition(new StartupTransition(dungine));
    dungine.setState(new MainMenuState(dungine));
    dungine.start();
  }

}
