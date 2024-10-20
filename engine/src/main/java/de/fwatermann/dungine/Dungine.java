package de.fwatermann.dungine;

/**
 * The `Dungine` class serves as the entry point for the Dungine library.
 * It contains the main method which provides a message indicating that this jar is intended to be used as a library for creating games.
 */
public class Dungine {

  private Dungine() {}

  /**
   * The main method which prints a message indicating the intended use of the Dungine library.
   *
   * @param args command-line arguments (not used)
   */
  public static void main(String[] args) {
    System.out.println("This is the DUNGINE! It is not made for direct execution. Use this jar as library to create your own games.");
  }

}
