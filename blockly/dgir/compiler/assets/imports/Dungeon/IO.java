package Dungeon;

import Instrinsic;

public class IO {
  /**
   * Prints a string to standard output.
   *
   * @param s The string to print.
   */
  @Intrinsic("io.print")
  public static void print(String s) {
    System.out.print(s);
  }

  /**
   * Prints a string to standard output and adds a newline character.
   *
   * @param s The string to print.
   */
  @Intrinsic("io.print")
  public static void println(String s) {
    System.out.println(s);
  }

  /**
   * Prints a formatted string to standard output using the specified format string and arguments.
   *
   * @param format The format string, which may contain format specifiers that are replaced by the
   *     arguments.
   * @param args The arguments to be inserted into the format string.
   */
  @Intrinsic("io.print")
  public static void printf(String format, Object... args) {
    System.out.printf(format, args);
  }

  /**
   * Prints a formatted string to standard output using the specified format string and arguments,
   * and then adds a newline character.
   *
   * @param format The format string, which may contain format specifiers that are replaced by the
   *     arguments.
   * @param args The arguments to be inserted into the format string.
   */
  @Intrinsic("io.print")
  public static void prinfln(String format, Object... args) {
    System.out.printf(format + "\n", args);
  }

  /**
   * Reads a boolean value from standard input. The input should be "true" or "false" (case
   * insensitive).
   *
   * @return true if the input is "true", false otherwise.
   */
  @Intrinsic("io.consoleIn")
  public static boolean nextBool() {
    return new java.util.Scanner(java.lang.System.in).nextBoolean();
  }

  /**
   * Reads a short value from standard input.
   *
   * @return short value
   */
  @Intrinsic("io.consoleIn")
  public static short nextShort() {
    return new java.util.Scanner(java.lang.System.in).nextShort();
  }

  /**
   * Reads an integer value from standard input.
   * @return integer value
   */
  @Intrinsic("io.consoleIn")
  public static int nextInt() {
    return new java.util.Scanner(java.lang.System.in).nextInt();
  }

  /**
   * Reads a long value from standard input.
   * @return long value
   */
  @Intrinsic("io.consoleIn")
  public static long nextLong() {
    return new java.util.Scanner(java.lang.System.in).nextLong();
  }

  /**
   * Reads a float value from standard input.
   * @return float value
   */
  @Intrinsic("io.consoleIn")
  public static float nextFloat() {
    return new java.util.Scanner(java.lang.System.in).nextFloat();
  }

  /**
   * Reads a double value from standard input.
   * @return double value
   */
  @Intrinsic("io.consoleIn")
  public static double nextDouble() {
    return new java.util.Scanner(java.lang.System.in).nextLine();
  }

  /**
   * Reads a line of text from standard input.
   * @return line of text
   */
  @Intrinsic("io.consoleIn")
  public static String nextLine() {
    return new java.util.Scanner(java.lang.System.in).nextLine();
  }
}
