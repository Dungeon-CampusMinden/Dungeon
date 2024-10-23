package dungine;

import dungine.state.StateMainMenu;
import dungine.state.StateTransition;

public class Main {

  public static void main(String[] args) {
    Dungine dungine = new Dungine(false);
    dungine.setState(new StateMainMenu(dungine));
    dungine.setStateTransition(new StateTransition(dungine));
    dungine.start();
  }
}
