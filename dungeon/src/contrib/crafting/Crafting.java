package contrib.crafting;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import contrib.item.Item;
import core.Game;
import core.utils.logging.CustomLogLevel;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Handles the crafting system.
 *
 * <p>It contains a list of recipes and methods to {@link #addRecipe(Recipe) add} recipes. It also
 * provides the method {@link #recipeByIngredients(CraftingIngredient[])} to get a recipe based on
 * the provided ingredients.
 *
 * <p>It will load the recipes from the files via {@link #loadRecipes()}. Recipes have to be in the
 * 'assets/recipes' directory. This will autmaticly happen an program start. Call this in your
 * {@link core.game.PreRunConfiguration#userOnSetup onSetup callback}.
 */
public final class Crafting {
  private static final HashSet<Recipe> RECIPES = new HashSet<>();
  private static final Logger LOGGER = Logger.getLogger(Crafting.class.getSimpleName());

  /**
   * Get a recipe based on the provided items.
   *
   * <p>If there are multiple recipes that can be made with the provided items, recipes where items
   * must be in a specific order will be prioritized.
   *
   * @param inputs Ingredients used.
   * @return The recipe that can be crafted with the provided ingredients. If none can be crafted,
   *     the returned optional will be empty.
   */
  public static Optional<Recipe> recipeByIngredients(final CraftingIngredient[] inputs) {
    List<Recipe> possibleRecipes = new ArrayList<>();
    for (Recipe recipe : RECIPES) {
      if (recipe.canCraft(inputs)) {
        possibleRecipes.add(recipe);
      }
    }

    if (possibleRecipes.isEmpty()) {
      return Optional.empty();
    }
    if (possibleRecipes.size() == 1) {
      return Optional.of(possibleRecipes.get(0));
    }
    possibleRecipes.sort(Comparator.comparing(c -> !c.ordered()));

    return Optional.of(possibleRecipes.get(0));
  }

  /**
   * Add A recipe to the list of recipes programmatically.
   *
   * @param recipe The recipe to add.
   */
  public static void addRecipe(final Recipe recipe) {
    if (recipe.ingredients().length == 0) {
      throw new InvalidRecipeException("Recipes with no ingredients are not allowed!");
    }
    RECIPES.add(recipe);
  }

  /** Remove all recipes. */
  public static void clearRecipes() {
    RECIPES.clear();
  }

  /**
   * Load recipes from the recipes' folder.
   *
   * <p>If the program is compiled to a jar file, recipes will be loaded from within the jar file.
   */
  public static void loadRecipes() {
    if (Objects.requireNonNull(Crafting.class.getResource("/recipes"))
        .toString()
        .startsWith("jar:")) {
      loadFromJar();
    } else {
      loadFromFile();
    }
  }

  /** Load recipes if the program was started from a jar file. */
  private static void loadFromJar() {
    try {
      String path =
          new File(Objects.requireNonNull(Game.class.getResource("")).getPath())
              .getParent()
              // for windows
              .replaceAll("(!|file:\\\\)", "")
              // for unix/macos
              .replaceAll("(!|file:)", "");
      JarFile jar = new JarFile(path);
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.getName().startsWith("recipes") && entry.getName().endsWith(".recipe")) {
          LOGGER.info("Load recipe: " + entry.getName());
          Recipe r =
              parseRecipe(Game.class.getResourceAsStream("/" + entry.getName()), entry.getName());
          if (r != null) Crafting.RECIPES.add(r);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Load recipes if the program was started from a folder. */
  private static void loadFromFile() {
    File folder =
        new File(Objects.requireNonNull(Crafting.class.getResource("/recipes")).getPath());
    File[] files = folder.listFiles();
    if (files == null) {
      return;
    }
    for (File file : files) {
      if (file.getName().endsWith(".recipe")) {
        LOGGER.info("Load recipe: " + file.getName());
        Recipe r =
            parseRecipe(
                Crafting.class.getResourceAsStream("/recipes/" + file.getName()), file.getName());
        if (r != null) Crafting.RECIPES.add(r);
      }
    }
  }

  /**
   * Parse a recipe from a file.
   *
   * @param stream The stream to read from.
   * @param name The name of the file. Used for error logging only.
   * @return The parsed recipe.
   */
  private static Recipe parseRecipe(final InputStream stream, final String name) {
    try {
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder();
      reader.lines().forEach(builder::append);

      JsonReader jsonReader = new JsonReader();
      JsonValue root = jsonReader.parse(builder.toString());

      boolean orderedRecipe = root.getBoolean("ordered");
      JsonValue ingredients = root.get("ingredients"); // Array of items
      JsonValue results = root.get("results"); // Array of results

      if (ingredients.size <= 0) {
        throw new InvalidRecipeException("Recipes with no ingredients are not allowed!");
      }

      // Load Ingredients
      CraftingIngredient[] ingredientsArray = new CraftingIngredient[ingredients.size];
      for (int i = 0; i < ingredients.size; i++) {

        JsonValue ingredient = ingredients.get(i);
        JsonValue item = ingredient.get("item");
        String id = item.getString("id");

        String type = ingredient.getString("type");
        if (type.equals("item")) {
          if (item.has("param")) {
            @SuppressWarnings("unchecked")
            Constructor<? extends Item>[] itemConstructor =
                (Constructor<? extends Item>[]) Item.getItem(id).getDeclaredConstructors();
            Object[] params = parseParams(item.get("param"));
            Constructor<?> fittingCons = findFittingConstructor(itemConstructor, params);
            if (fittingCons == null) {
              throw new RuntimeException("No fitting constructor found for item: " + id);
            }
            ingredientsArray[i] = (CraftingIngredient) fittingCons.newInstance(params);
          } else {
            CraftingIngredient ci = Item.getItem(id).getDeclaredConstructor().newInstance();
            ingredientsArray[i] = ci;
          }
        } else {
          throw new RuntimeException("Unknown ingredient type: " + type);
        }
      }

      // Load Results
      CraftingResult[] resultsArray = new CraftingResult[results.size];
      for (int i = 0; i < results.size; i++) {
        JsonValue result = results.get(i);
        JsonValue item = result.get("item");
        String id = item.getString("id");

        String type = result.getString("type");
        if (type.equals("item")) {
          if (item.has("param")) {
            @SuppressWarnings("unchecked")
            Constructor<? extends Item>[] itemConstructor =
                (Constructor<? extends Item>[]) Item.getItem(id).getDeclaredConstructors();
            Object[] params = parseParams(item.get("param"));
            Constructor<?> fittingCons = findFittingConstructor(itemConstructor, params);
            if (fittingCons == null) {
              throw new RuntimeException("No fitting constructor found for item: " + id);
            }
            resultsArray[i] = (CraftingResult) fittingCons.newInstance(params);
          } else {
            CraftingResult cr = Item.getItem(id).getDeclaredConstructor().newInstance();
            resultsArray[i] = cr;
          }

        } else {
          throw new RuntimeException("Unknown result type: " + type);
        }
      }
      Recipe recipe = new Recipe(orderedRecipe, ingredientsArray, resultsArray);
      RECIPES.add(recipe);

      reader.close();

      return recipe;
    } catch (IOException
        | InvocationTargetException
        | InstantiationException
        | IllegalAccessException
        | NoSuchMethodException ex) {
      LOGGER.log(
          CustomLogLevel.ERROR, "Error parsing recipe (" + name + "): " + ex.getMessage()); // Error
    } catch (IllegalArgumentException ex) {
      ex.printStackTrace();
      LOGGER.log(
          CustomLogLevel.WARNING,
          "Error parsing recipe (" + name + "): " + ex.getMessage()); // Warning
    }

    return null;
  }

  private static Object[] parseParams(JsonValue param) {
    Object[] params = new Object[param.size];
    for (int i = 0; i < params.length; i++) {
      JsonValue p = param.get(i);
      String[] split = p.asString().split(":");
      String type = split[0];
      String value = split[1];

      // check if type is native if not check is it a class
      switch (type) {
        case "int":
          params[i] = Integer.parseInt(value);
          break;
        case "float":
          params[i] = Float.parseFloat(value);
          break;
        case "double":
          params[i] = Double.parseDouble(value);
          break;
        case "long":
          params[i] = Long.parseLong(value);
          break;
        case "short":
          params[i] = Short.parseShort(value);
          break;
        case "byte":
          params[i] = Byte.parseByte(value);
          break;
        case "boolean":
          params[i] = Boolean.parseBoolean(value);
          break;
        case "char":
          params[i] = value.charAt(0);
          break;
        default:
          try {
            Class<?> clazz = Class.forName(type);
            Object[] constants = clazz.getEnumConstants();
            boolean found = false;
            for (Object constant : constants) {
              if (constant.toString().equalsIgnoreCase(value)) {
                params[i] = constant;
                found = true;
                break;
              }
            }
            if (!found) {
              throw new RuntimeException("No Enum constant found for: " + value);
            }
          } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Error parsing param: " + p.asString());
          }
      }
    }
    return params;
  }

  private static Constructor<? extends Item> findFittingConstructor(
      Constructor<? extends Item>[] constructors, Object... params) {
    for (Constructor<? extends Item> constructor : constructors) {
      if (constructor.getParameterCount() == params.length) {
        boolean valid = true;
        for (int i = 0; i < params.length; i++) {
          Parameter parameter = constructor.getParameters()[i];
          if (!parameter.getType().isAssignableFrom(params[i].getClass())) {
            valid = false;
          }
        }
        if (valid) {
          return constructor;
        }
      }
    }
    return null;
  }
}
