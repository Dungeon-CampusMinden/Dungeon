package de.fwatermann.dungine.graphics.scene.model;

import de.fwatermann.dungine.graphics.mesh.Mesh;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import de.fwatermann.dungine.utils.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.joml.Vector4f;

/**
 * The `Material` class represents the material properties of a 3D model. It includes colors,
 * textures, and other material-specific attributes.
 */
public class Material {

  /** Flag indicating the material has a diffuse texture. */
  public static final int MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE = 0x01;

  /** Flag indicating the material has a normal texture. */
  public static final int MATERIAL_FLAG_HAS_NORMAL_TEXTURE = 0x02;

  /** Flag indicating the material has a specular texture. */
  public static final int MATERIAL_FLAG_HAS_SPECULAR_TEXTURE = 0x04;

  /** Flag indicating the material has an ambient texture. */
  public static final int MATERIAL_FLAG_HAS_AMBIENT_TEXTURE = 0x08;

  /** The default color for the material. */
  public static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f);

  /** The list of mesh entries associated with this material. */
  public final List<MeshEntry> meshes = new ArrayList<>();

  /** The diffuse color of the material. */
  public final Vector4f diffuseColor = new Vector4f(DEFAULT_COLOR);

  /** The ambient color of the material. */
  public final Vector4f ambientColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

  /** The specular color of the material. */
  public final Vector4f specularColor = new Vector4f(DEFAULT_COLOR);

  /** The diffuse texture animation of the material. */
  @Nullable public Animation diffuseTexture = null;

  /** The ambient texture animation of the material. */
  @Nullable public Animation ambientTexture = null;

  /** The normal texture animation of the material. */
  @Nullable public Animation normalTexture = null;

  /** The specular texture animation of the material. */
  @Nullable public Animation specularTexture = null;

  /** Indicates whether the material is transparent. */
  public boolean transparent = false;

  /** The reflectance value of the material. */
  public float reflectance = 0.0f;

  /** The flags indicating the presence of various textures. */
  public int flags = 0;

  /** Constructs a new `Material` with the default values. */
  public Material() {}

  /**
   * The `MeshEntry` record represents an entry in the list of meshes associated with the material.
   *
   * @param mesh the mesh associated with the material
   * @param offset the offset in the mesh
   * @param count the number of elements in the mesh
   */
  public record MeshEntry(Mesh<?> mesh, int offset, int count) {}
}
