package dslToGame.typeadapters;

import core.components.DrawComponent;

import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;

import java.io.IOException;

public class DrawComponentAdapter {
    @DSLTypeAdapter(name = "draw_component")
    public static DrawComponent buildQuizFromSingleChoiceTask(
            @DSLTypeMember(name = "path") String path) {
        DrawComponent comp = null;
        try {
            comp = new DrawComponent(path);
        } catch (IOException e) {
            return null;
        }
        return comp;
    }
}
