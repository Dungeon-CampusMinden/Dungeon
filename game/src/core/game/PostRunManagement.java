package core.game;

import core.utils.IVoidFunction;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the functions to be executed after the game loop has finished or the window
 * is disposed.
 */
public class PostRunManagement {
  private static final List<IVoidFunction> userOnWindowDisposeList = new ArrayList<>();

  /**
   * Add a function to be executed when the window is disposed.
   *
   * @param function The function to be executed, cannot be null.
   */
  public static void addUserOnWindowDisposeFunction(IVoidFunction function) {
    if (function == null) {
      throw new IllegalArgumentException("Function cannot be null");
    }
    userOnWindowDisposeList.add(function);
  }

  /**
   * Get the list of functions to be executed when the window is disposed.
   *
   * @return The list of functions to be executed when the window is disposed.
   */
  public static List<IVoidFunction> userOnWindowDisposeList() {
    return userOnWindowDisposeList;
  }
}
