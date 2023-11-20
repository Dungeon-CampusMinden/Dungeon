package dslinterop.dsltypeadapters;

import core.components.DrawComponent;

import dsl.semanticanalysis.types.typebuilding.annotation.DSLTypeAdapter;
import dsl.semanticanalysis.types.typebuilding.annotation.DSLTypeMember;

import java.io.IOException;

/** Typeadapter for creation of {@link DrawComponent}s. */
public class DrawComponentAdapter {
    /**
     * Buildermethod for creating a new {@link DrawComponent} from a path, pointing to animations.
     *
     * @param path A String containing a path to a directory containing animations.
     * @return the created {@link DrawComponent}
     */
    @DSLTypeAdapter(name = "draw_component")
    public static DrawComponent buildDrawComponentFromPath(
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
