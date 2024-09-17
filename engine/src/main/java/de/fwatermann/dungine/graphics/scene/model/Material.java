package de.fwatermann.dungine.graphics.scene.model;

import de.fwatermann.dungine.graphics.mesh.Mesh;
import de.fwatermann.dungine.graphics.texture.Texture;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class Material {

  public static final int MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE = 0x01;
  public static final int MATERIAL_FLAG_HAS_NORMAL_TEXTURE = 0x02;
  public static final int MATERIAL_FLAG_HAS_SPECULAR_TEXTURE = 0x04;
  public static final int MATERIAL_FLAG_HAS_AMBIENT_TEXTURE = 0x08;

  public static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f);

  List<Mesh<?>> meshes = new ArrayList<>();

  boolean transparent = false;

  Vector4f diffuseColor = new Vector4f(DEFAULT_COLOR);
  Vector4f ambientColor = new Vector4f(DEFAULT_COLOR);
  Vector4f specularColor = new Vector4f(DEFAULT_COLOR);

  Texture diffuseTexture = null;
  Texture ambientTexture = null;
  Texture normalTexture = null;
  Texture specularTexture = null;

  float reflectance = 0.0f;

  int flags = 0;

}
