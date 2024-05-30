package dsl.programmanalyzer;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

// TODO: temporary test
public class RelationshipRecorder {

  public class Record {
    private List<Relationship> relationships = new ArrayList<>();
    private HashSet<Relatable> relatables = new HashSet<>();
    boolean processed = false;
    private HashSet<Relatable> objectsToPersist = new HashSet<>();
  }

  public static RelationshipRecorder instance = new RelationshipRecorder();

  // TODO: clean up, if it gets to big!!
  private Stack<Record> records = new Stack<>();
  private Record currentRecord;
  private HashMap<Class<?>, List<Field>> fieldMemo = new HashMap<>();

  public RelationshipRecorder() {
    this.currentRecord = new Record();
    this.records.push(currentRecord);
  }

  public Relationship add(
      Object startObject,
      Long startIdx,
      String relationshipName,
      List<Long> endIdxs,
      boolean forceIdxProperty) {
    var rel = new Relationship(startIdx, relationshipName, endIdxs, forceIdxProperty);
    this.currentRecord.relationships.add(rel);
    rel.startObject(startObject);
    return rel;
  }

  public Relationship add(Object startObject, Long startIdx, String relationshipName, Long endIdx) {
    var rel = new Relationship(startIdx, relationshipName, List.of(endIdx));
    this.currentRecord.relationships.add(rel);
    rel.startObject(startObject);
    return rel;
  }

  private String convertFieldName(String fieldName) {
    Pattern pattern = Pattern.compile("([a-z0-9_])([A-Z])");
    Matcher matcher = pattern.matcher(fieldName);
    var underscored = matcher.replaceAll(mr -> mr.group(1) + '_' + mr.group(2).toLowerCase());
    return underscored.toUpperCase();
  }

  // TODO: extend for properties
  public void translateRelationshipEntity(Object relationshipEntity) {
    var clazz = relationshipEntity.getClass();
    if (clazz.isAnnotationPresent(RelationshipEntity.class)) {
      var annotation = clazz.getAnnotation(RelationshipEntity.class);
      String relationName =
          annotation.type().isBlank() ? convertFieldName(clazz.getSimpleName()) : annotation.type();
      try {
        var startField =
            Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(StartNode.class))
                .findFirst()
                .get();
        startField.setAccessible(true);
        var startObject = startField.get(relationshipEntity);

        var endField =
            Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(EndNode.class))
                .findFirst()
                .get();
        endField.setAccessible(true);

        var endObject = endField.get(relationshipEntity);
        if (startObject instanceof Relatable startRelatable
            && endObject instanceof Relatable endRelatable) {
          var relationship = this.add(startObject, startRelatable.getId(), relationName, endRelatable.getId());

          var propertyFielsd = Arrays.stream(clazz.getFields()).filter(f -> f.isAnnotationPresent(Property.class));
          HashMap<String, Object> properties = new HashMap<>();
          propertyFielsd.forEach(f -> {
            try {
              f.setAccessible(true);
              String name = f.getName();
              var value = f.get(relationshipEntity);
              if (value instanceof String stringValue) {
                properties.put(name, "\""+stringValue+"\"");
              } else if (value.getClass().isEnum())  {
                properties.put(name, "\""+value+"\"");
              } else {
                properties.put(name, value);
              }
            } catch (IllegalAccessException ignored) { }
          });
          relationship.addProperties(properties);
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private List<Field> getAllFields(Class clazz) {
    if (fieldMemo.containsKey(clazz)) {
      return fieldMemo.get(clazz);
    }

    if (clazz == null) {
      return Collections.emptyList();
    }

    List<Field> result = new ArrayList<>(getAllFields(clazz.getSuperclass()));
    List<Field> filteredFields = Arrays.stream(clazz.getDeclaredFields()).toList();
    result.addAll(filteredFields);
    this.fieldMemo.put(clazz, result);
    return result;
  }


  public void processRelationships() {
    if (currentRecord.processed) {
      return;
    }
    for (var relatable : this.currentRecord.relatables) {
      var relationFields =
        getAllFields(relatable.getClass()).stream().filter(f -> f.isAnnotationPresent(Relate.class)).toList();
      relationFields.forEach(
          f -> {
            Relate annotation = f.getAnnotation(Relate.class);
            Long startId = relatable.getId();
            String relationName =
                annotation.type().isBlank() ? convertFieldName(f.getName()) : annotation.type();
            // get related objects
            f.setAccessible(true);
            try {
              var value = f.get(relatable);
              if (value != null) {
                if (value instanceof Iterable<?>) {
                  Iterable<Relatable> iterable = (Iterable<Relatable>) value;
                  ArrayList<Long> ids = new ArrayList<>();
                  iterable.forEach(i -> ids.add(i.getId()));
                  this.add(relatable, startId, relationName, ids, true);
                } else if (value instanceof Relatable relatableValue) {
                  this.add(relatable, startId, relationName, relatableValue.getId());
                } else {
                  throw new RuntimeException(
                      "Field marked with @Relate does not implement Relatable");
                }
              }
            } catch (IllegalAccessException ex) {

            } catch (ClassCastException otherEx) {
              throw new RuntimeException(
                  "Iterable field marked with @Relate does not have elements, which implement Relatable");
            }
          });
    }
  }

  public void processObjectsToPersist() {
    for (var relatable : this.currentRecord.relatables) {
      this.currentRecord.objectsToPersist.add(relatable);
      var relationFields =
          Arrays.stream(relatable.getClass().getDeclaredFields())
              .filter(f -> f.isAnnotationPresent(Relate.class));
      relationFields.forEach(
          f -> {
            Relate annotation = f.getAnnotation(Relate.class);
            // get related objects
            f.setAccessible(true);
            try {
              var value = f.get(relatable);
              if (value != null) {
                if (value instanceof Iterable<?>) {
                  Iterable<Relatable> iterable = (Iterable<Relatable>) value;
                  if (annotation.persistObject()) {
                    iterable.forEach(i -> currentRecord.objectsToPersist.add(i));
                  }
                } else if (value instanceof Relatable relatableValue) {
                  if (annotation.persistObject()) {
                    currentRecord.objectsToPersist.add(relatableValue);
                  }
                } else {
                  throw new RuntimeException(
                      "Field marked with @Relate does not implement Relatable");
                }
              }
            } catch (IllegalAccessException ex) {

            } catch (ClassCastException otherEx) {
              throw new RuntimeException(
                  "Iterable field marked with @Relate does not have elements, which implement Relatable");
            }
          });
    }
  }

  public void addRelatable(Relatable relatable) {
    this.currentRecord.relatables.add(relatable);
  }

  public void pushNewRecord() {
    if (!currentRecord.processed) {
      processRelationships();
    }

    this.currentRecord = new Record();
    this.records.push(this.currentRecord);
  }

  public List<Relationship> get() {
    return this.currentRecord.relationships;
  }

  public List<Relatable> getObjectsToPersist() {
    return this.currentRecord.objectsToPersist.stream().toList();
  }
}
