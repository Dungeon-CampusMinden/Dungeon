package Dungeon;

// Marker annotation your compiler looks for
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Intrinsic {
  String value(); // the IR opcode name
}
