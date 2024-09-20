package de.fwatermann.dungine.ui.layout;

import de.fwatermann.dungine.ui.UIContainer;
import de.fwatermann.dungine.ui.UIElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

public abstract class UILayouter {

  public static void layout(UIContainer<?> container, Vector2i viewport, boolean recurse) {
    List<UIElement<?>> elements = getElements(container);
    sizeElements(container, viewport);

    List<FlexLine> lines = lines(elements, container, viewport);

    for (FlexLine line : lines) {
      shrinkGrowElements(line, viewport, container);
      alignItemSelfStretch(line.elements, line.crossSize, container.layout());
    }

    justifyContent(lines, container, viewport);
    alignContent(lines, container, viewport);

    if (recurse) {
      for (UIElement<?> element : elements) {
        if (element instanceof UIContainer<?> c) {
          layout(c, viewport, true);
        }
      }
    }
  }

  private static List<UIElement<?>> getElements(UIContainer<?> container) {
    List<UIElement<?>> elements = new ArrayList<>(container.elements().toList());
    elements.sort(
        (a, b) -> {
          int out = Integer.compare(a.layout().order(), b.layout().order());
          if (container.layout().direction().isReverse()) {
            out *= -1;
          }
          return out;
        });
    return elements;
  }

  private static void sizeElements(UIContainer<?> container, Vector2i viewport) {
    container
        .elements()
        .forEach(
            (element) -> {
              UIElementLayout layout = element.layout();

              Vector2f size = new Vector2f(0, 0);
              if (layout.width().type() != Unit.UnitType.AUTO) {
                size.x = layout.width().toPixels(viewport, container.size().x);

              } else if(container.layout().direction().isRow()) {
                layout.flexGrow(1);
                layout.alignSelf(AlignSelf.STRETCH);
              }
              if (layout.height().type() != Unit.UnitType.AUTO) {
                size.y = layout.height().toPixels(viewport, container.size().y);
              } else if(container.layout().direction().isColumn()) {
                layout.flexGrow(1);
                layout.alignSelf(AlignSelf.STRETCH);
              }
              element.size().set(size.x, size.y, 0.0f);
            });
  }

  private static List<FlexLine> lines(
      List<UIElement<?>> elements, UIContainer<?> container, Vector2i viewport) {
    UIElementLayout containerLayout = container.layout();
    List<FlexLine> lines = new ArrayList<>();
    lines.add(new FlexLine());

    if (containerLayout.direction().isRow()) {
      float columnGap = containerLayout.columnGap().toPixels(viewport, container.size().x);

      if (containerLayout.wrap() == FlexWrap.NO_WRAP) {
        for (UIElement<?> element : elements) {
          lines.getLast().crossSize = Math.max(lines.getLast().crossSize, element.size().y);
          lines.getLast().mainSize += element.size().x;
          lines.getLast().elements.add(element);
        }
        lines.getLast().mainSize += columnGap * (lines.getLast().elements.size() - 1);
        return lines;
      }

      for (UIElement<?> element : elements) {
        if (lines.getLast().mainSize + element.size().x > container.size().x) {
          lines.getLast().mainSize -= columnGap; // Remove last column gap
          lines.add(new FlexLine());
        }
        lines.getLast().elements.add(element);
        lines.getLast().mainSize += element.size().x + columnGap;
        lines.getLast().crossSize = Math.max(lines.getLast().crossSize, element.size().y);
      }
      lines.getLast().mainSize -= columnGap; // Remove last column gap

    } else if (containerLayout.direction().isColumn()) {
      float rowGap = containerLayout.rowGap().toPixels(viewport, container.size().y);

      if (containerLayout.wrap() == FlexWrap.NO_WRAP) {
        for (UIElement<?> element : elements) {
          lines.getLast().crossSize = Math.max(lines.getLast().crossSize, element.size().x);
          lines.getLast().mainSize += element.size().y;
          lines.getLast().elements.add(element);
        }
        lines.getLast().mainSize += rowGap * (lines.getLast().elements.size() - 1);
        return lines;
      }

      for (UIElement<?> element : elements) {
        if (lines.getLast().mainSize + element.size().y > container.size().y) {
          lines.getLast().mainSize -= rowGap;
          lines.add(new FlexLine());
        }
        lines.getLast().elements.add(element);
        lines.getLast().mainSize += element.size().y + rowGap;
        lines.getLast().crossSize = Math.max(lines.getLast().crossSize, element.size().x);
      }
      lines.getLast().mainSize -= rowGap;
    }

    if (containerLayout.wrap() == FlexWrap.WRAP_REVERSE) {
      Collections.reverse(lines);
    }

    return lines;
  }

  private static void shrinkGrowElements(FlexLine line, Vector2i viewport, UIContainer<?> container) {

    List<UIElement<?>> elements = line.elements;

    UIElementLayout containerLayout = container.layout();
    Vector3f containerSize = container.size();

    int sumFlexGrow = 0;
    int sumFlexShrink = 0;
    Vector3f accSize = new Vector3f();
    for (UIElement<?> element : elements) {
      UIElementLayout layout = element.layout();
      sumFlexGrow += layout.flexGrow();
      sumFlexShrink += layout.flexShrink();
      accSize.x += element.size().x;
      accSize.y += element.size().y;
    }

    float rowGap = containerLayout.rowGap().toPixels(viewport, containerSize.y);
    float columnGap = containerLayout.columnGap().toPixels(viewport, containerSize.x);

    float remainingSpaceX =
        containerSize.x - accSize.x - columnGap * (elements.size() - 1);
    float remainingSpaceY =
        containerSize.y
            - accSize.y
            - containerLayout.rowGap().toPixels(viewport, containerSize.y) * (elements.size() - 1);

    if (containerLayout.direction().isRow()) {
      for (UIElement<?> element : elements) {
        UIElementLayout layout = element.layout();
        if (remainingSpaceX > 0 && layout.flexGrow() > 0) {
          element.size().x += (remainingSpaceX / sumFlexGrow) * layout.flexGrow();
        } else if (remainingSpaceX < 0 && layout.flexShrink() > 0) {
          element.size().x += (remainingSpaceX / sumFlexShrink) * layout.flexShrink();
        }
      }
      if(remainingSpaceX != 0 && sumFlexGrow + sumFlexShrink != 0) {
        //Recalculate row sizes
        float elementSizes = line.elements.stream().map(e -> e.size().x).reduce(0.0f, Float::sum);
        float gaps = columnGap * (line.elements.size() - 1);
        line.mainSize = elementSizes + gaps;
      }
    } else if (containerLayout.direction().isColumn()) {
      for (UIElement<?> element : elements) {
        UIElementLayout layout = element.layout();
        if (remainingSpaceY > 0 && layout.flexGrow() > 0) {
          element.size().y += (remainingSpaceY / sumFlexGrow) * layout.flexGrow();
        } else if (remainingSpaceY < 0 && layout.flexShrink() > 0) {
          element.size().y += (remainingSpaceY / sumFlexShrink) * layout.flexShrink();
        }
      }
      if(remainingSpaceY != 0 && sumFlexGrow + sumFlexShrink != 0) {
        //Recalculate column sizes
        float elementSizes = line.elements.stream().map(e -> e.size().y).reduce(0.0f, Float::sum);
        float gaps = rowGap * (line.elements.size() - 1);
        line.mainSize = elementSizes + gaps;
      }
    } else {
      throw new IllegalStateException(
          "Unknown FlexDirection (this case should not be reachable): "
              + containerLayout.direction());
    }
  }

  private static void alignItemSelfStretch(
      List<UIElement<?>> elements, float maxCross, UIElementLayout containerLayout) {
    for (UIElement<?> element : elements) {
      UIElementLayout layout = element.layout();
      if (containerLayout.alignItems() == AlignItems.STRETCH
          && (layout.alignSelf() == AlignSelf.AUTO || layout.alignSelf() == AlignSelf.STRETCH)) {
        if (containerLayout.direction() == FlexDirection.ROW
            || containerLayout.direction() == FlexDirection.ROW_REVERSE) {
          element.size().y = maxCross;
        } else if (containerLayout.direction() == FlexDirection.COLUMN
            || containerLayout.direction() == FlexDirection.COLUMN_REVERSE) {
          element.size().x = maxCross;
        }
      }
    }
  }

  private static void justifyContent(
      List<FlexLine> lines, UIContainer<?> container, Vector2i viewport) {

    UIElementLayout containerLayout = container.layout();
    Vector3f containerSize = container.size();

    if (containerLayout.direction().isRow()) {
      float currentY = 0.0f;
      float columnGap = containerLayout.columnGap().toPixels(viewport, containerSize.x);

      for (FlexLine line : lines) {
        switch (containerLayout.justifyContent()) {
          case FLEX_START -> {
            float currentX = 0.0f;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, currentY, 0.0f);
              currentX += element.size().x + columnGap;
            }
          }
          case FLEX_END -> {
            float currentX = 0.0f;
            for (UIElement<?> element : line.elements) {
              element.position().set(containerSize.x - currentX - element.size().x, currentY, 0.0f);
              currentX += element.size().x + columnGap;
            }
          }
          case CENTER -> {
            float currentX = containerSize.x / 2 - line.mainSize / 2;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, currentY, 0.0f);
              currentX += element.size().x + columnGap;
            }
          }
          case SPACE_BETWEEN -> {
            float space = (containerSize.x - line.mainSize) / (line.elements.size() - 1);
            float currentX = 0.0f;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, currentY, 0.0f);
              currentX += element.size().x + space;
            }
          }
          case SPACE_AROUND -> {
            float space = (containerSize.x - line.mainSize) / (line.elements.size() * 2);
            float currentX = space;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, currentY, 0.0f);
              currentX += element.size().x + 2 * space;
            }
          }
          case SPACE_EVENLY -> {
            float space = (containerSize.x - line.mainSize) / (line.elements.size() + 1);
            float currentX = space;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, currentY, 0.0f);
              currentX += element.size().x + space;
            }
          }
        }
        currentY += line.crossSize + containerLayout.rowGap().toPixels(viewport, containerSize.y);
      }
    } else if (containerLayout.direction().isColumn()) {

      float currentX = 0.0f;
      float rowGap = containerLayout.rowGap().toPixels(viewport, containerSize.y);

      for (FlexLine line : lines) {
        switch (containerLayout.justifyContent()) {
          case FLEX_START -> {
            float currentY = 0.0f;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, currentY, 0.0f);
              currentY += element.size().y + rowGap;
            }
          }
          case FLEX_END -> {
            float currentY = 0.0f;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, containerSize.y - currentY, 0.0f);
              currentY += element.size().y + rowGap;
            }
          }
          case CENTER -> {
            float currentY = containerSize.y / 2 - line.mainSize / 2;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, currentY, 0.0f);
              currentY += element.size().y + rowGap;
            }
          }
          case SPACE_BETWEEN -> {
            float space = (containerSize.y - line.mainSize) / (line.elements.size() - 1);
            float currentY = 0.0f;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, currentY, 0.0f);
              currentY += element.size().y + space;
            }
          }
          case SPACE_AROUND -> {
            float space = (containerSize.y - line.mainSize) / (line.elements.size() * 2);
            float currentY = space;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, currentY, 0.0f);
              currentY += element.size().y + space * 2;
            }
          }
          case SPACE_EVENLY -> {
            float space = (containerSize.y / line.mainSize) / (line.elements.size() + 1);
            float currentY = space;
            for (UIElement<?> element : line.elements) {
              element.position().set(currentX, currentY, 0.0f);
              currentY += element.size().y + space;
            }
          }
        }
        currentX +=
            line.crossSize + containerLayout.columnGap().toPixels(viewport, containerSize.x);
      }
    }
  }

  private static void alignItems(FlexLine line, UIContainer<?> container, Vector2f linePos) {
    UIElementLayout containerLayout = container.layout();

    for (UIElement<?> element : line.elements) {
      AlignItems align = containerLayout.alignItems();
      UIElementLayout layout = element.layout();

      if (layout.alignSelf() != AlignSelf.AUTO) {
        align =
            switch (layout.alignSelf()) {
              case AUTO -> align;
              case FLEX_START -> AlignItems.FLEX_START;
              case FLEX_END -> AlignItems.FLEX_END;
              case CENTER -> AlignItems.CENTER;
              case STRETCH -> AlignItems.STRETCH;
            };
      }

      if (containerLayout.direction().isRow()) {
        switch (align) {
          case FLEX_START -> {
            element.position().y = linePos.y;
          }
          case FLEX_END -> {
            element.position().y = linePos.y + line.crossSize - element.size().y;
          }
          case CENTER -> {
            element.position().y = linePos.y + line.crossSize / 2 - element.size().y / 2;
          }
          case STRETCH -> {
            element.position().y = linePos.y;
            element.size().y = line.crossSize;
          }
        }
      } else if (containerLayout.direction().isColumn()) {
        switch (align) {
          case FLEX_START -> {
            element.position().x = linePos.x;
          }
          case FLEX_END -> {
            element.position().x = linePos.x + line.crossSize - element.size().x;
          }
          case CENTER -> {
            element.position().x = linePos.x + line.crossSize / 2 - element.size().x / 2;
          }
          case STRETCH -> {
            element.position().x = linePos.x;
            element.size().x = line.crossSize;
          }
        }
      }
    }
  }

  private static void alignContent(
      List<FlexLine> lines, UIContainer<?> container, Vector2i viewport) {

    UIElementLayout containerLayout = container.layout();
    Vector3f containerSize = container.size();

    if (containerLayout.direction().isRow()) {
      float rowGap = containerLayout.rowGap().toPixels(viewport, containerSize.y);
      float lineSizeNoGaps = lines.stream().map(l -> l.crossSize).reduce(0.0f, Float::sum);
      float lineSize = lineSizeNoGaps + rowGap * (lines.size() - 1);
      switch (containerLayout.alignContent()) {
        case FLEX_START, NORMAL -> {
          float currentY = 0.0f;
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(0.0f, currentY);
            alignItems(line, container, linePos);
            currentY += line.crossSize += rowGap;
          }
        }
        case FLEX_END -> {
          float currentY = 0.0f;
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(0.0f, containerSize.y - currentY - line.crossSize);
            alignItems(line, container, linePos);
            currentY += line.crossSize += rowGap;
          }
        }
        case CENTER -> {
          float currentY = containerSize.y / 2 - (lineSize / 2);
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(0.0f, currentY);
            alignItems(line, container, linePos);
            currentY += line.crossSize + rowGap;
          }
        }
        case SPACE_BETWEEN -> {
          float space = (containerSize.y - lineSizeNoGaps) / (lines.size() - 1);
          float currentY = 0.0f;
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(0.0f, currentY);
            alignItems(line, container, linePos);
            currentY += line.crossSize + space;
          }
        }
        case SPACE_AROUND -> {
          float space = (containerSize.y - lineSizeNoGaps) / (lines.size() * 2);
          float currentY = space;
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(0.0f, currentY);
            alignItems(line, container, linePos);
            currentY += line.crossSize + space * 2;
          }
        }
        case SPACE_EVENLY -> {
          float space = (containerSize.y - lineSizeNoGaps) / (lines.size() + 1);
          float currentY = space;
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(0.0f, currentY);
            alignItems(line, container, linePos);
            currentY += line.crossSize + space;
          }
        }
        case STRETCH -> {
          float space = (containerSize.y - lineSize) / lines.size();
          float currentY = 0.0f;
          for (FlexLine line : lines) {
            line.crossSize += space;
            Vector2f linePos = new Vector2f(0.0f, currentY);
            alignItems(line, container, linePos);
            currentY += line.crossSize + rowGap;
          }
        }
      }

    } else if (containerLayout.direction().isColumn()) {
      float columnGap = containerLayout.columnGap().toPixels(viewport, containerSize.x);
      float linesSizeNoGap = lines.stream().map(l -> l.crossSize).reduce(0.0f, Float::sum);
      float linesSize = linesSizeNoGap + columnGap * (lines.size() - 1);

      switch (containerLayout.alignContent()) {
        case FLEX_START, NORMAL -> {
          float currentX = 0.0f;
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(currentX, 0.0f);
            alignItems(line, container, linePos);
            currentX += line.crossSize += columnGap;
          }
        }
        case FLEX_END -> {
          float currentX = 0.0f;
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(containerSize.x - currentX - line.crossSize, 0.0f);
            alignItems(line, container, linePos);
            currentX += line.crossSize += columnGap;
          }
        }
        case CENTER -> {
          float currentX = containerSize.x / 2 - (linesSize / 2);
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(currentX, 0.0f);
            alignItems(line, container, linePos);
            currentX += line.crossSize + columnGap;
          }
        }
        case SPACE_BETWEEN -> {
          float space = (containerSize.x - linesSizeNoGap) / (lines.size() - 1);
          float currentX = 0.0f;
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(currentX, 0.0f);
            alignItems(line, container, linePos);
            currentX += line.crossSize + space;
          }
        }
        case SPACE_AROUND -> {
          float space = (containerSize.x - linesSizeNoGap) / (lines.size() * 2);
          float currentX = space;
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(currentX, 0.0f);
            alignItems(line, container, linePos);
            currentX += line.crossSize + space * 2;
          }
        }
        case SPACE_EVENLY -> {
          float space = (containerSize.x - linesSizeNoGap) / (lines.size() + 1);
          float currentX = space;
          for (FlexLine line : lines) {
            Vector2f linePos = new Vector2f(currentX, 0.0f);
            alignItems(line, container, linePos);
            currentX += line.crossSize + space;
          }
        }
        case STRETCH -> {
          float space = (containerSize.x - linesSize) / lines.size();
          float currentX = 0.0f;
          for (FlexLine line : lines) {
            line.crossSize += space;
            Vector2f linePos = new Vector2f(currentX, 0.0f);
            alignItems(line, container, linePos);
            currentX += line.crossSize + columnGap;
          }
        }
      }
    }
  }

  private static class FlexLine {

    private float mainSize = 0.0f;
    private float crossSize = 0.0f;
    private final List<UIElement<?>> elements = new ArrayList<>();

    private FlexLine() {}
  }
}
