package de.fwatermann.dungine.graphics.scene.model;

import static org.lwjgl.assimp.Assimp.*;

import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.IndexDataType;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.Mesh;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.graphics.texture.TextureMagFilter;
import de.fwatermann.dungine.graphics.texture.TextureManager;
import de.fwatermann.dungine.graphics.texture.TextureMinFilter;
import de.fwatermann.dungine.graphics.texture.TextureWrapMode;
import de.fwatermann.dungine.graphics.texture.animation.ArrayAnimation;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.pair.Pair;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIFile;
import org.lwjgl.assimp.AIFileIO;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIString;
import org.lwjgl.assimp.AITexture;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class ModelLoader {

  private static final Logger LOGGER = LogManager.getLogger(ModelLoader.class);
  private static final String MODEL_ROOT_FILE_KEY = "###---###ROOTFILE###---###";


  private static final Map<Resource, Model> modelCache = new HashMap<>();

  public static Model loadModel(Resource resource) {
    if (modelCache.containsKey(resource)) {
      return modelCache.get(resource);
    }
    AIScene aiScene = loadScene(resource);
    if (aiScene == null) {
      throw new RuntimeException("Failed to load model: " + aiGetErrorString());
    }

    int numMaterials = aiScene.mNumMaterials();
    List<Material> materials = new ArrayList<>();
    for (int i = 0; i < numMaterials; i++) {
      AIMaterial aiMaterial = AIMaterial.create(aiScene.mMaterials().get(i));
      Material material = loadMaterial(resource, aiScene, aiMaterial);
      materials.add(material);
      LOGGER.debug("Material: {}", material.transparent);
    }

    int numMeshes = aiScene.mNumMeshes();
    PointerBuffer aiMeshes = aiScene.mMeshes();
    Material defaultMaterial = new Material();
    for (int i = 0; i < numMeshes; i++) {
      AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
      Mesh<?> mesh = loadMesh(aiMesh);
      int materialIndex = aiMesh.mMaterialIndex();
      if (materialIndex >= 0 && materialIndex < materials.size()) {
        materials.get(materialIndex).meshes.add(new Material.MeshEntry(mesh, 0, 0));
      } else {
        defaultMaterial.meshes.add(new Material.MeshEntry(mesh, 0, 0));
      }
    }
    if (!defaultMaterial.meshes.isEmpty()) {
      materials.add(defaultMaterial);
    }
    materials.removeIf(m -> m.meshes.isEmpty());

    materials.sort((Material a, Material b) -> {
      if (a.transparent && !b.transparent) {
        return 1;
      } else if (!a.transparent && b.transparent) {
        return -1;
      }
      return 0;
    });

    Model model = new Model(materials);
    modelCache.put(resource, model);
    return model;
  }

  private static AIScene loadScene(Resource resource) {
    AIFileIO fileIO = AIFileIO.create().OpenProc((pFileIO, pFileName, pOpenMode) -> {
      String fileName = MemoryUtil.memUTF8(pFileName);
      try {
        ByteBuffer data;
        LOGGER.debug("Loading File: {}", fileName);
        if(fileName.equals(MODEL_ROOT_FILE_KEY)) {
          data = resource.readBytes();
        } else {
          data = resource.resolveRelative(fileName).readBytes();
        }
        return AIFile.create()
          .ReadProc((pFile, pBuffer, pSize, pCount) -> {
            long max = Math.min(data.remaining() / pSize, pCount);
            MemoryUtil.memCopy(MemoryUtil.memAddress(data), pBuffer, max * pSize);
            data.position(data.position() + (int) (max * pSize));
            return max;
          })
          .SeekProc((pFile, offset, origin) -> {
            switch(origin) {
              case aiOrigin_CUR -> data.position(data.position() + (int) offset);
              case aiOrigin_END -> data.position(data.limit() + (int) offset);
              case aiOrigin_SET -> data.position((int) offset);
            }
            return aiReturn_SUCCESS;
          })
          .TellProc((pFile) -> data.position())
          .FileSizeProc((pFile) -> data.limit())
          .address();
      } catch (IOException e) {
        throw new RuntimeException("Could not load relative resource!", e);
      }
    }).CloseProc((pFileIO, pFile) -> {
      AIFile aiFile = AIFile.create(pFile);
      aiFile.ReadProc().free();
      aiFile.SeekProc().free();
      aiFile.FileSizeProc().free();
    });

    int flags = aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_CalcTangentSpace | aiProcess_OptimizeMeshes | aiProcess_GenSmoothNormals;

    AIScene aiScene = aiImportFileEx(MODEL_ROOT_FILE_KEY, flags, fileIO);
    fileIO.OpenProc().free();
    fileIO.CloseProc().free();

    return aiScene;
  }

  private static Material loadMaterial(Resource modelFile, AIScene aiScene, AIMaterial aiMaterial) {
    Material material = new Material();
    material.diffuseColor.set(loadColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, 0));
    material.ambientColor.set(loadColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, 0));
    material.specularColor.set(loadColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, 0));

    Pair<Texture, Boolean> diffuseTexture = loadTexture(modelFile, aiScene, aiMaterial, aiTextureType_DIFFUSE, 0, true);
    material.diffuseTexture = diffuseTexture.a() != null ? ArrayAnimation.of(diffuseTexture.a()) : null;
    Pair<Texture, Boolean> ambientTexture = loadTexture(modelFile, aiScene, aiMaterial, aiTextureType_AMBIENT, 0, false);
    material.ambientTexture = ambientTexture.a() != null ? ArrayAnimation.of(ambientTexture.a()) : null;
    Pair<Texture, Boolean> specularTexture = loadTexture(modelFile, aiScene, aiMaterial, aiTextureType_SPECULAR, 0, false);
    material.specularTexture = specularTexture.a() != null ? ArrayAnimation.of(specularTexture.a()) : null;
    Pair<Texture, Boolean> normalTexture = loadTexture(modelFile, aiScene, aiMaterial, aiTextureType_NORMALS, 0, false);
    material.normalTexture = normalTexture.a() != null ? ArrayAnimation.of(normalTexture.a()) : null;

    material.transparent = diffuseTexture.b();

    //Reflectance
    {
      float[] shinFac = new float[1];
      int[] pMax = new int[1];
      int result = aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_SHININESS_STRENGTH, aiTextureType_NONE, 0, shinFac, pMax);
      if(result == aiReturn_SUCCESS) {
        material.reflectance = shinFac[0];
      } else {
        material.reflectance = 0.0f;
      }
    }

    material.flags |= material.diffuseTexture != null ? Material.MATERIAL_FLAG_HAS_DIFFUSE_TEXTURE : 0;
    material.flags |= material.ambientTexture != null ? Material.MATERIAL_FLAG_HAS_AMBIENT_TEXTURE : 0;
    material.flags |= material.specularTexture != null ? Material.MATERIAL_FLAG_HAS_SPECULAR_TEXTURE : 0;
    material.flags |= material.normalTexture != null ? Material.MATERIAL_FLAG_HAS_NORMAL_TEXTURE : 0;

    return material;
  }

  private static Pair<Texture, Boolean> loadTexture(
    Resource modelFile, AIScene aiScene, AIMaterial aiMaterial, int textureType, int index, boolean checkTransparent) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      AIString texturePath = AIString.calloc(stack);

      IntBuffer mapping = stack.callocInt(1);
      IntBuffer uvIndex = stack.callocInt(1);
      FloatBuffer blend = stack.callocFloat(1);
      IntBuffer op = stack.callocInt(1);
      IntBuffer mapMode = stack.callocInt(3);
      IntBuffer flags = stack.callocInt(1);

      int error =
        aiGetMaterialTexture(
          aiMaterial,
          textureType,
          index,
          texturePath,
          mapping, uvIndex, blend, op, mapMode, flags);
      if(error != aiReturn_SUCCESS) return new Pair<>(null, false);
      String path = texturePath.dataString();
      if(path.isEmpty()) return new Pair<>(null, false);

      //Wrap mode
      TextureWrapMode uWrapMode = switch(mapMode.get(0)) {
        case aiTextureMapMode_Wrap -> TextureWrapMode.REPEAT;
        case aiTextureMapMode_Clamp -> TextureWrapMode.CLAMP_TO_EDGE;
        case aiTextureMapMode_Mirror -> TextureWrapMode.MIRRORED_REPEAT;
        case aiTextureMapMode_Decal -> TextureWrapMode.CLAMP_TO_BORDER;
        default -> TextureWrapMode.CLAMP_TO_EDGE;
      };
      TextureWrapMode vWrapMode = switch(mapMode.get(1)) {
        case aiTextureMapMode_Wrap -> TextureWrapMode.REPEAT;
        case aiTextureMapMode_Clamp -> TextureWrapMode.CLAMP_TO_EDGE;
        case aiTextureMapMode_Mirror -> TextureWrapMode.MIRRORED_REPEAT;
        case aiTextureMapMode_Decal -> TextureWrapMode.CLAMP_TO_BORDER;
        default -> TextureWrapMode.CLAMP_TO_EDGE;
      };
      TextureMagFilter magFilter = TextureMagFilter.NEAREST;
      TextureMinFilter minFilter = TextureMinFilter.LINEAR;


      Texture texture;
      boolean transparent = false;
      if (path.startsWith("*")) { // Embedded texture
        int embeddedTextureIndex = Integer.parseInt(path.substring(1));
        AITexture aiTexture = AITexture.create(aiScene.mTextures().get(embeddedTextureIndex));
        if (aiTexture.mHeight() != 0) { // Texture is not compressed
          ByteBuffer buffer = aiTexture.pcDataCompressed(); // RGBA8888
          texture = new Texture(aiTexture.mWidth(), aiTexture.mHeight(), GL33.GL_RGBA, minFilter, magFilter, uWrapMode, vWrapMode, buffer);
          if (checkTransparent) {
            transparent = isTransparent(buffer);
          }
        } else {
          ByteBuffer buffer = aiTexture.pcDataCompressed();
          IntBuffer channels = BufferUtils.createIntBuffer(1);
          IntBuffer width = BufferUtils.createIntBuffer(1);
          IntBuffer height = BufferUtils.createIntBuffer(1);
          ByteBuffer pixels =
            STBImage.stbi_load_from_memory(buffer, width, height, channels, 4);
          if (pixels == null) {
            throw new RuntimeException(
              "Failed to load embedded texture: " + STBImage.stbi_failure_reason());
          }
          texture = new Texture(width.get(0), height.get(0), GL33.GL_RGBA, minFilter, magFilter, uWrapMode, vWrapMode, pixels);
          if(checkTransparent) {
            transparent = isTransparent(pixels);
          }
          STBImage.stbi_image_free(pixels);
        }
      } else {
        Resource textureResource = modelFile.resolveRelative(path);
        if (textureResource == null) {
          throw new RuntimeException("Failed to load texture: " + path);
        }
        texture = TextureManager.load(textureResource);
        texture.bind(GL33.GL_TEXTURE0);
        texture.wrapS(uWrapMode);
        texture.wrapT(vWrapMode);
        texture.unbind();
        if(checkTransparent) {
          transparent = isTransparent(texture.readPixels());
        }
      }

      return new Pair<>(texture, transparent);
    }
  }

  private static boolean isTransparent(ByteBuffer pixels) {
    ByteBuffer buffer = pixels.asReadOnlyBuffer();
    for(int i = 3; i < buffer.limit(); i += 4) {
      if((buffer.get(i) & 0xFF) < 255) {
        return true;
      }
    }
    return false;
  }

  private static Vector4f loadColor(AIMaterial aiMaterial, String matKey, int index) {
    AIColor4D color = AIColor4D.create();
    int error = aiGetMaterialColor(aiMaterial, matKey, aiTextureType_NONE, 0, color);
    if (error == aiReturn_SUCCESS) {
      return new Vector4f(color.r(), color.g(), color.b(), color.a());
    }
    return new Vector4f(Material.DEFAULT_COLOR);
  }

  private static Mesh<?> loadMesh(AIMesh mesh) {
    int numVertices = mesh.mNumVertices();
    float[] vertices = new float[numVertices * 14]; // 3 for position, 2 for texture coordinates
    for (int i = 0; i < numVertices; i++) {
      AIVector3D position = mesh.mVertices().get(i);
      AIVector3D normal = mesh.mNormals().get(i);
      AIVector3D textureCoords = mesh.mTextureCoords(0).get(i);
      AIVector3D tanget = mesh.mTangents().get(i);
      AIVector3D bitangent = mesh.mBitangents().get(i);
      vertices[i * 14] = position.x();
      vertices[i * 14 + 1] = position.y();
      vertices[i * 14 + 2] = position.z();
      vertices[i * 14 + 3] = textureCoords.x();
      vertices[i * 14 + 4] = 1.0f - textureCoords.y();
      vertices[i * 14 + 5] = normal.x();
      vertices[i * 14 + 6] = normal.y();
      vertices[i * 14 + 7] = normal.z();
      vertices[i * 14 + 8] = tanget.x();
      vertices[i * 14 + 9] = tanget.y();
      vertices[i * 14 + 10] = tanget.z();
      vertices[i * 14 + 11] = bitangent.x();
      vertices[i * 14 + 12] = bitangent.y();
      vertices[i * 14 + 13] = bitangent.z();
    }

    int numFaces = mesh.mNumFaces();
    List<Integer> indicesList = new ArrayList<>();
    AIFace.Buffer aiFaces = mesh.mFaces();
    for (int i = 0; i < numFaces; i++) {
      AIFace aiFace = aiFaces.get(i);
      IntBuffer inBuf = aiFace.mIndices();
      while (inBuf.remaining() > 0) {
        indicesList.add(inBuf.get());
      }
    }
    int[] indices = indicesList.stream().mapToInt(Integer::intValue).toArray();

    ByteBuffer verticesBuffer = BufferUtils.createByteBuffer(vertices.length * Float.BYTES);
    verticesBuffer.asFloatBuffer().put(vertices);

    ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indices.length * Integer.BYTES);
    indicesBuffer.asIntBuffer().put(indices);

    return new IndexedMesh(
      verticesBuffer,
      PrimitiveType.TRIANGLES,
      indicesBuffer,
      IndexDataType.UNSIGNED_INT,
      GLUsageHint.DRAW_STATIC,
      new VertexAttribute(3, DataType.FLOAT, "aPosition"),
      new VertexAttribute(2, DataType.FLOAT, "aTexCoord"),
      new VertexAttribute(3, DataType.FLOAT, "aNormal"),
      new VertexAttribute(3, DataType.FLOAT, "aTangent"),
      new VertexAttribute(3, DataType.FLOAT, "aBitangent"));
  }
}
