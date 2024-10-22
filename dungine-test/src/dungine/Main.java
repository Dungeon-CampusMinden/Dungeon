package dungine;

import dungine.state.StateMainMenu;

public class Main {

  public static void main(String[] args) {
    Dungine dungine = new Dungine(true);
    dungine.setState(new StateMainMenu(dungine));
    dungine.start();
  }

}
