package chamberofsecrets;

import contrib.utils.DynamicCompiler;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;

public class MyMain {

  private static final SimpleIPath PATH = new SimpleIPath("src/chamberofsecrets/ToCode2.java");
  private static final String CLASSNAME = "chamberofsecrets.ToCode2";

  public static void main(String[] args) {
    try {
      Object o = DynamicCompiler.loadUserInstance(PATH, CLASSNAME, new Tuple<>(Integer.class, 2));
      System.out.println(((ToCode) o).getInteger());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
