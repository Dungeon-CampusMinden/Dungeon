---
title: "Camera: "I can see you!"
---


## Cameras in libGDX

## DungeonCamera



Aus dem alten Wiki
LibGDX bietet eine Orthogonalkamera an (`OrthographicCamera`). Diese funktioniert wie eine perspektivische Kamera, steht aber senkrecht auf der Bildebene.

Wir haben 16x16-Gridfelder (Pixel). Würde man die Orthogonalkamera mit einer virtuellen Weite und Höhe aufrufen, die der Weite und Höhe des Viewports entspricht, wäre ein Gridfeld genau ein Pixel groß. Dementsprechend müssen wir die virtuelle Weite und Höhe durch `16f` teilen, damit ein Gridfeld genau 16x16 Pixel ist. Anschließend "zoomen" wir noch um 50 % heran, damit ein Gridfeld etwas größer wird und 32x32 Pixel entspricht:

```java
/*
 * Virtual width and height ~~ one grid field size in pixel
 */
public static final float VIRTUAL_WIDTH = WIDTH / 16f;
public static final float VIRTUAL_HEIGHT = HEIGHT / 16f;
public static final float DEFAULT_ZOOM_FACTOR = 0.5f;
```

```java
/** Setting up the camera. */
private void setupCamera() {
    camera = new DungeonCamera(null, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT);
    camera.zoom = Constants.DEFAULT_ZOOM_FACTOR;

    // See also:
    // https://stackoverflow.com/questions/52011592/libgdx-set-ortho-camera
}
```

Die virtuelle Weite und Höhe kann mit den Konstanten in `code/core/src/tools/Constants.java` geändert werden.
