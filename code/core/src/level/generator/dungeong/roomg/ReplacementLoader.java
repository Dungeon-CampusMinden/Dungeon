package level.generator.dungeong.roomg;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import level.tools.DesignLabel;
import level.tools.LevelElement;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and stores replacements from a .json.
 *
 * @author Andre Matutat
 */
public class ReplacementLoader {
    private List<Replacement> replacements = new ArrayList<>();

    /**
     * Creates a ReplacementLoader and loads the replacements from the json. if the .json is empty,
     * the list is empty
     *
     * @param path path to json
     */
    public ReplacementLoader(String path) {
        readFromJson(path);
    }

    /**
     * Writes down a list of replacments to a .json.
     *
     * @param rep The list of replacements to save.
     * @param path Where to save?
     */
    public static void writeToJSON(List<Replacement> rep, String path) {
        Gson gson = new Gson();
        String json = gson.toJson(rep);
        try {
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter(path, StandardCharsets.UTF_8));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            System.out.println("File" + path + " not found");
        }
    }

    /**
     * Returns a list of Replacements that have the corresponding DesignLabel.
     *
     * @param label The DesignLabel, use ALL if you don't care.
     * @return The list with replacments.
     */
    public List<Replacement> getReplacements(DesignLabel label) {
        List<Replacement> results = new ArrayList<>(replacements);
        if (label != DesignLabel.ALL)
            results.removeIf(r -> r.getDesign() != label && r.getDesign() != DesignLabel.ALL);
        return results;
    }

    /**
     * Rotate the layout of the given replacement in 90 degree and create a new replacement with the
     * rotated layout
     *
     * @param r The Replacement that holds the layout to rotate
     * @return New Replacement with rotated layout
     */
    private Replacement rotate90(final Replacement r) {
        LevelElement[][] originalLayout = r.getLayout();
        int mSize = originalLayout.length;
        int nSize = originalLayout[0].length;
        LevelElement[][] rotatedLayout = new LevelElement[nSize][mSize];
        for (int row = 0; row < mSize; row++)
            for (int col = 0; col < nSize; col++)
                rotatedLayout[col][mSize - 1 - row] = originalLayout[row][col];
        return new Replacement(rotatedLayout, r.canRotate(), r.getDesign());
    }

    /**
     * Adds a replacement to the list.
     *
     * @param r The replacement to add.
     */
    public void addReplacement(Replacement r) {
        if (!replacements.contains(r)) replacements.add(r);
    }

    /**
     * Read in replacments from a .json. Rotates them if necassary.
     *
     * @param path Path to .json.
     */
    private void readFromJson(String path) {
        Type replacementType = new TypeToken<ArrayList<Replacement>>() {}.getType();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(path, StandardCharsets.UTF_8));
            replacements = new Gson().fromJson(reader, replacementType);
            if (replacements == null) throw new NullPointerException("File is empty");
            // add all rotations to list
            List<Replacement> toRotate = new ArrayList<>(replacements);
            toRotate.removeIf(r -> !r.canRotate());

            for (Replacement r : toRotate) {
                Replacement tmp = r;
                // 90,180,270
                for (int i = 0; i < 3; i++) {
                    tmp = rotate90(tmp);
                    replacements.add(tmp);
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("File may be corrupted ");
            e.printStackTrace();
        }
    }
}
