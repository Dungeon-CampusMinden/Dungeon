package level.generator.dungeong.roomg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import level.tools.DesignLabel;

import java.util.ArrayList;
import java.util.List;

/**
 * loads and stores roomtemplates from a json
 *
 * @author Andre Matutat
 */
public class RoomTemplateLoader {
    private List<RoomTemplate> roomTemplates = new ArrayList<>();

    /**
     * Creates a RoomTemplateLoader and loads the template from the json. if the json is empty, the
     * list is empty
     *
     * @param path path to json
     */
    public RoomTemplateLoader(String path) {
        this.readFromJson(path);
    }

    /**
     * Returns a list of RoomTemplates that have the corresponding DesignLabel
     *
     * @param label the DesignLabel, use ALL if you don't care
     * @return the list
     */
    public List<RoomTemplate> getRoomTemplates(DesignLabel label) {
        List<RoomTemplate> results = new ArrayList<>(roomTemplates);
        if (label != DesignLabel.ALL) results.removeIf(r -> r.getDesign() != label);
        return results;
    }

    /**
     * adds a template to the list
     *
     * @param template the template to add
     */
    public void addRoomTemplate(RoomTemplate template) {
        if (!roomTemplates.contains(template)) roomTemplates.add(template);
    }

    private void readFromJson(String path) {
        Json json = new Json();
        List<JsonValue> list = json.fromJson(List.class, Gdx.files.internal(path));
        for (JsonValue v : list) roomTemplates.add(json.readValue(RoomTemplate.class, v));
    }

    /**
     * Writes down the list to a json
     *
     * @param templates the list of template to save
     * @param path where to save
     */
    public void writeToJSON(List<RoomTemplate> templates, String path) {
        Json json = new Json();
        String listInJson = json.toJson(templates);
        FileHandle file = Gdx.files.local(path);
        file.writeString(listInJson, false);
    }
}
