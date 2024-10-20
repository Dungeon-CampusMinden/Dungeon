package de.fwatermann.dungine.utils;

import static org.lwjgl.util.nfd.NativeFileDialog.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NFDPathSetEnum;

/**
 * Provides functionality for displaying file dialog windows using the native file dialog library (NFD).
 * Allows for opening single or multiple files with filter options.
 */
public class FileDialog {

  private FileDialog() {}

  /**
   * Represents a filter for file dialog operations, specifying display name and file extensions.
   */
  public static class Filter {

    /**
     * The display name of the filter.
     */
    public final String displayName;

    /**
     * The file extensions included in the filter.
     */
    public final String[] extensions;

    /**
     * Constructs a Filter with a display name and multiple extensions.
     *
     * @param displayName The display name of the filter.
     * @param extensions The file extensions included in the filter.
     */
    public Filter(String displayName, String ... extensions) {
      this.displayName = displayName;
      this.extensions = extensions;
    }

    /**
     * Constructs a Filter with a display name and a single extension.
     *
     * @param displayName The display name of the filter.
     * @param extension The single file extension included in the filter.
     */
    public Filter(String displayName, String extension) {
      this(displayName, new String[] {extension});
    }
  }

  static {
    NFD_Init();
  }

  /**
   * Opens a file dialog to select a single file, with optional file filters.
   *
   * @param pFilters Filters to apply to the file dialog.
   * @return The path to the selected file, or null if no file was selected or the operation was cancelled.
   */
  public static String openFile(Filter ... pFilters) {
    try(MemoryStack stack = MemoryStack.stackPush()) {
      NFDFilterItem.Buffer filters = null;
      if(pFilters.length > 0) {
        filters = NFDFilterItem.malloc(pFilters.length);
        for(int i = 0; i < pFilters.length; i ++) {
          filters.get(i)
            .name(stack.UTF8(pFilters[i].displayName))
            .spec(stack.UTF8(String.join(",", pFilters[i].extensions)));
        }
      }
      PointerBuffer outPath = stack.callocPointer(1);
      NFD_OpenDialog(outPath, filters, (ByteBuffer) null);
      return outPath.get(0) == 0 ? null : outPath.getStringUTF8(0);
    }
  }

  /**
   * Opens a file dialog to select multiple files, with optional file filters.
   *
   * @param pFilters Filters to apply to the file dialog.
   * @return An array of paths to the selected files, or null if no files were selected or the operation was cancelled.
   */
  public static String[] openFiles(Filter ... pFilters) {
    try(MemoryStack stack = MemoryStack.stackPush()) {
      PointerBuffer pp = stack.callocPointer(1);

      NFDFilterItem.Buffer filters = null;
      if(pFilters.length > 0) {
        filters = NFDFilterItem.calloc(pFilters.length);
        for(int i = 0; i < pFilters.length; i ++) {
          filters.get(i)
            .name(stack.UTF8(pFilters[i].displayName))
            .spec(stack.UTF8(String.join(",", pFilters[i].extensions)));
        }
      }

      int result = NFD_OpenDialogMultiple(pp, filters, (ByteBuffer) null);
      switch(result) {
        case NFD_OKAY:
          long pathSet = pp.get(0);

          NFDPathSetEnum psEnum = NFDPathSetEnum.calloc(stack);
          NFD_PathSet_GetEnum(pathSet, psEnum);

          List<String> paths = new ArrayList<String>();

          int i = 0;
          while(NFD_PathSet_EnumNext(psEnum, pp) == NFD_OKAY && pp.get(0) != 0) {
            paths.add(pp.getStringUTF8(0));
            i++;
            NFD_PathSet_FreePath(pp.get(0));
          }

          NFD_PathSet_FreeEnum(psEnum);
          NFD_PathSet_Free(pathSet);

          return paths.toArray(new String[0]);
        case NFD_CANCEL:
          return null;
        case NFD_ERROR:
          throw new RuntimeException("NFD-Error: " + NFD_GetError());
      }
    }
    return null;
  }


}
