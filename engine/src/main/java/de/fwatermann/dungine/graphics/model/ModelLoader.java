package de.fwatermann.dungine.graphics.model;

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
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.utils.annotations.Nullable;
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
      materials.add(loadMaterial(resource, aiScene, aiMaterial));
    }

    int numMeshes = aiScene.mNumMeshes();
    PointerBuffer aiMeshes = aiScene.mMeshes();
    Material defaultMaterial = new Material();
    for (int i = 0; i < numMeshes; i++) {
      AIMesh aiMesh = AIMesh.create(aiMeshes.get(i));
      Mesh<?> mesh = loadMesh(aiMesh);
      int materialIndex = aiMesh.mMaterialIndex();
      if (materialIndex >= 0 && materialIndex < materials.size()) {
        materials.get(materialIndex).meshes.add(mesh);
      } else {
        defaultMaterial.meshes.add(mesh);
      }
    }
    if (!defaultMaterial.meshes.isEmpty()) {
      materials.add(defaultMaterial);
    }

    Model model = new Model(resource, materials);
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

    int flags = aiProcess_JoinIdenticalVertices | aiProcess_Triangulate | aiProcess_OptimizeMeshes;

    AIScene aiScene = aiImportFileEx(MODEL_ROOT_FILE_KEY, flags, fileIO);
    fileIO.OpenProc().free();
    fileIO.CloseProc().free();

    return aiScene;
  }

  private static Material loadMaterial(Resource modelFile, AIScene aiScene, AIMaterial aiMaterial) {
    Material material = new Material();
    material.diffuseColor.set(loadColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, 0));
    material.diffuseTexture = loadTexture(modelFile, aiScene, aiMaterial, aiTextureType_DIFFUSE, 0);
    material.normalTexture = loadTexture(modelFile, aiScene, aiMaterial, aiTextureType_NORMALS, 0);
    material.specularTexture =
        loadTexture(modelFile, aiScene, aiMaterial, aiTextureType_SPECULAR, 0);
    return material;
  }

  @Nullable
  private static Texture loadTexture(
      Resource modelFile, AIScene aiScene, AIMaterial aiMaterial, int textureType, int index) {
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
      if(error != aiReturn_SUCCESS) return null;
      String path = texturePath.dataString();
      if(path.isEmpty()) return null;

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
      if (path.startsWith("*")) { // Embedded texture
        int embeddedTextureIndex = Integer.parseInt(path.substring(1));
        AITexture aiTexture = AITexture.create(aiScene.mTextures().get(embeddedTextureIndex));
        if (aiTexture.mHeight() != 0) { // Texture is not compressed
          ByteBuffer buffer = aiTexture.pcDataCompressed(); // RGBA8888
          texture = new Texture(aiTexture.mWidth(), aiTexture.mHeight(), GL33.GL_RGBA, minFilter, magFilter, uWrapMode, vWrapMode, buffer);
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
      }

      return texture;
    }
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
    float[] vertices = new float[numVertices * 8]; // 3 for position, 2 for texture coordinates
    for (int i = 0; i < numVertices; i++) {
      AIVector3D position = mesh.mVertices().get(i);
      AIVector3D normal = mesh.mNormals().get(i);
      AIVector3D textureCoords = mesh.mTextureCoords(0).get(i);
      vertices[i * 8] = position.x();
      vertices[i * 8 + 1] = position.y();
      vertices[i * 8 + 2] = position.z();
      vertices[i * 8 + 3] = textureCoords.x();
      vertices[i * 8 + 4] = 1.0f - textureCoords.y();
      vertices[i * 8 + 5] = normal.x();
      vertices[i * 8 + 6] = normal.y();
      vertices[i * 8 + 7] = normal.z();
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
        new VertexAttribute(3, DataType.FLOAT, "aNormal"));
  }
}
