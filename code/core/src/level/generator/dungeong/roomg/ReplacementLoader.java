package level.generator.dungeong.roomg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import level.tools.DesignLabel;
import level.tools.LevelElement;

import java.util.ArrayList;
import java.util.List;

/**
 * loads and stores replacements from a jsons
 *
 * @author Andre Matutat
 */
public class ReplacementLoader {
    private List<Replacement> replacements = new ArrayList<>();
    ;

    /**
     * Creates a ReplacementLoader and loads the replacements from the json. if the json is empty,
     * the list is empty
     *
     * @param path path to json
     */
    public ReplacementLoader(String path) {
        readFromJson(path);
    }

    /**
     * Returns a list of Replacements that have the corresponding DesignLabel
     *
     * @param l the DesignLabel, use ALL if you don't care
     * @return the list
     */
    public List<Replacement> getReplacements(DesignLabel l) {
        List<Replacement> results = new ArrayList<>(replacements);
        if (l != DesignLabel.ALL) results.removeIf(r -> r.getDesign() != l);
        return results;
    }

    /**
     * rotate the layout of the given replacement in 90 degree and create a new replacement with the
     * rotated layout
     *
     * @param r the Replacement that holds the layout to rotate
     * @return new Replacement with rotated layout
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
     * adds a replacement to the list
     *
     * @param r the replacement to add
     */
    public void addReplacement(Replacement r) {
        if (!replacements.contains(r)) replacements.add(r);
    }

    private void readFromJson(String path) {
        Json json = new Json();
        List<JsonValue> list = json.fromJson(List.class, Gdx.files.internal(path));
        for (JsonValue v : list) replacements.add(json.readValue(Replacement.class, v));
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
    }

    /**
     * Writes down the list to a json
     *
     * @param rep the list of replacements to save
     * @param path where to save
     */
    public void writeToJSON(List<Replacement> rep, String path) {
        Json json = new Json();
        String listInJson = json.toJson(rep);
        FileHandle file = Gdx.files.local(path);
        file.writeString(listInJson, false);
    }
}
