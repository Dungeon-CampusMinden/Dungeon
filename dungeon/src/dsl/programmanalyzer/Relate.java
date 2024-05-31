package dsl.programmanalyzer;

import org.neo4j.ogm.annotation.ValueFor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface Relate {
  String TYPE = "type";
  String DIRECTION = "direction";
  Direction INCOMING = Direction.INCOMING;
  Direction OUTGOING = Direction.OUTGOING;

  String type() default "";

  boolean persistObject() default true;

  @ValueFor("type")
  String value() default "";

  Direction direction() default Direction.OUTGOING;

  public static enum Direction {
    OUTGOING,
    INCOMING;

    private Direction() {
    }
  }

}
