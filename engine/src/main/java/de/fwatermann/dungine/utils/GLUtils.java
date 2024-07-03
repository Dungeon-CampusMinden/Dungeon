package de.fwatermann.dungine.utils;

import org.lwjgl.opengl.GL30;

public class GLUtils {

  /** Checks for OpenGL errors and throws a RuntimeException if an error occurred. */
  public static void checkGLError() {
    int error = GL30.glGetError();
    if (error != GL30.GL_NO_ERROR) {
      throw new RuntimeException("OpenGL error: " + error);
    }
  }

  /**
   * Checks if the OpenGL version is at least the specified version.
   *
   * @param minMajor the minimum major version
   * @param minMinor the minimum minor version
   * @return true if the OpenGL version is at least the specified version, false otherwise
   */
  public static boolean checkVersion(int minMajor, int minMinor) {
    int major = GL30.glGetInteger(GL30.GL_MAJOR_VERSION);
    int minor = GL30.glGetInteger(GL30.GL_MINOR_VERSION);
    return major > minMajor || (major == minMajor && minor >= minMinor);
  }
}
