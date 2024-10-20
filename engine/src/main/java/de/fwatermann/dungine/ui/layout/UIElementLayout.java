package de.fwatermann.dungine.ui.layout;

/**
 * Layout properties for UI elements.
 */
public class UIElementLayout {

  /**
   * Creates a new UIElementLayout.
   */
  public UIElementLayout() {}

  /** Default direction for flex items. */
  public static final FlexDirection DEFAULT_DIRECTION = FlexDirection.ROW;

  /** Default wrapping behavior for flex items. */
  public static final FlexWrap DEFAULT_WRAP = FlexWrap.NO_WRAP;

  /** Default justification of content within a container. */
  public static final JustifyContent DEFAULT_JUSTIFY_CONTENT = JustifyContent.FLEX_START;

  /** Default alignment of items within a container. */
  public static final AlignItems DEFAULT_ALIGN_ITEMS = AlignItems.STRETCH;

  /** Default alignment of content within a container. */
  public static final AlignContent DEFAULT_ALIGN_CONTENT = AlignContent.NORMAL;

  /** Default column gap between flex items. */
  public static final Unit DEFAULT_COLUMN_GAP = UnitReadOnly.px(0);

  /** Default row gap between flex items. */
  public static final Unit DEFAULT_ROW_GAP = UnitReadOnly.px(0);

  /** Default order of flex items. */
  public static final int DEFAULT_ORDER = 0;

  /** Default flex grow factor. */
  public static final int DEFAULT_FLEX_GROW = 0;

  /** Default flex shrink factor. */
  public static final int DEFAULT_FLEX_SHRINK = 0;

  /** Default alignment of a single item within a container. */
  public static final AlignSelf DEFAULT_ALIGN_SELF = AlignSelf.AUTO;

  /** Default positioning of items within a container. */
  public static final Position DEFAULT_POSITION = Position.RELATIVE;

  /** Default aspect ratio of the item. */
  public static final Unit DEFAULT_ASPECT_RATIO = Unit.auto();

  /** Default width of the item. */
  public static final Unit DEFAULT_WIDTH = Unit.auto();

  /** Default height of the item. */
  public static final Unit DEFAULT_HEIGHT = Unit.auto();

  /** Default top position of the item. */
  public static final Unit DEFAULT_TOP = Unit.auto();

  /** Default right position of the item. */
  public static final Unit DEFAULT_RIGHT = Unit.auto();

  /** Default bottom position of the item. */
  public static final Unit DEFAULT_BOTTOM = Unit.auto();

  /** Default left position of the item. */
  public static final Unit DEFAULT_LEFT = Unit.auto();

  // Container properties
  private FlexDirection direction = DEFAULT_DIRECTION;
  private FlexWrap wrap = DEFAULT_WRAP;
  private JustifyContent justifyContent = DEFAULT_JUSTIFY_CONTENT;
  private AlignItems alignItems = DEFAULT_ALIGN_ITEMS;
  private AlignContent alignContent = DEFAULT_ALIGN_CONTENT;
  private Unit columnGap = DEFAULT_COLUMN_GAP;
  private Unit rowGap = DEFAULT_ROW_GAP;

  // Child properties
  private int order = DEFAULT_ORDER;
  private int flexGrow = DEFAULT_FLEX_GROW;
  private int flexShrink = DEFAULT_FLEX_SHRINK;
  private AlignSelf alignSelf = DEFAULT_ALIGN_SELF;

  // Other
  private Position position = DEFAULT_POSITION;
  private Unit aspectRatio = DEFAULT_ASPECT_RATIO;
  private Unit width = DEFAULT_WIDTH;
  private Unit height = DEFAULT_HEIGHT;
  private Unit top = DEFAULT_TOP;
  private Unit right = DEFAULT_RIGHT;
  private Unit bottom = DEFAULT_BOTTOM;
  private Unit left = DEFAULT_LEFT;

  /**
   * Gets the direction of flex items.
   *
   * @return the direction of flex items.
   */
  public FlexDirection direction() {
    return this.direction;
  }

  /**
   * Sets the direction of flex items.
   *
   * @param direction the direction to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout direction(FlexDirection direction) {
    this.direction = direction;
    return this;
  }

  /**
   * Gets the wrapping behavior of flex items.
   *
   * @return the wrapping behavior of flex items.
   */
  public FlexWrap wrap() {
    return this.wrap;
  }

  /**
   * Sets the wrapping behavior of flex items.
   *
   * @param wrap the wrapping behavior to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout wrap(FlexWrap wrap) {
    this.wrap = wrap;
    return this;
  }

  /**
   * Sets both the direction and wrapping behavior of flex items.
   *
   * @param direction the direction to set.
   * @param wrap the wrapping behavior to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout flow(FlexDirection direction, FlexWrap wrap) {
    this.direction = direction;
    this.wrap = wrap;
    return this;
  }

  /**
   * Gets the justification of content within a container.
   *
   * @return the justification of content.
   */
  public JustifyContent justifyContent() {
    return this.justifyContent;
  }

  /**
   * Sets the justification of content within a container.
   *
   * @param justifyContent the justification to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout justifyContent(JustifyContent justifyContent) {
    this.justifyContent = justifyContent;
    return this;
  }

  /**
   * Gets the alignment of items within a container.
   *
   * @return the alignment of items.
   */
  public AlignItems alignItems() {
    return this.alignItems;
  }

  /**
   * Sets the alignment of items within a container.
   *
   * @param alignItems the alignment to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout alignItems(AlignItems alignItems) {
    this.alignItems = alignItems;
    return this;
  }

  /**
   * Gets the alignment of content within a container.
   *
   * @return the alignment of content.
   */
  public AlignContent alignContent() {
    return this.alignContent;
  }

  /**
   * Sets the alignment of content within a container.
   *
   * @param alignContent the alignment to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout alignContent(AlignContent alignContent) {
    this.alignContent = alignContent;
    return this;
  }

  /**
   * Gets the column gap between flex items.
   *
   * @return the column gap.
   */
  public Unit columnGap() {
    return this.columnGap;
  }

  /**
   * Sets the column gap between flex items.
   *
   * @param columnGap the column gap to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout columnGap(Unit columnGap) {
    this.columnGap = columnGap;
    return this;
  }

  /**
   * Gets the row gap between flex items.
   *
   * @return the row gap.
   */
  public Unit rowGap() {
    return this.rowGap;
  }

  /**
   * Sets the row gap between flex items.
   *
   * @param rowGap the row gap to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout rowGap(Unit rowGap) {
    this.rowGap = rowGap;
    return this;
  }

  /**
   * Gets the order of flex items.
   *
   * @return the order of flex items.
   */
  public int order() {
    return this.order;
  }

  /**
   * Sets the order of flex items.
   *
   * @param order the order to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout order(int order) {
    this.order = order;
    return this;
  }

  /**
   * Gets the flex grow factor.
   *
   * @return the flex grow factor.
   */
  public int flexGrow() {
    return this.flexGrow;
  }

  /**
   * Sets the flex grow factor.
   *
   * @param flexGrow the flex grow factor to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout flexGrow(int flexGrow) {
    this.flexGrow = flexGrow;
    return this;
  }

  /**
   * Gets the flex shrink factor.
   *
   * @return the flex shrink factor.
   */
  public int flexShrink() {
    return this.flexShrink;
  }

  /**
   * Sets the flex shrink factor.
   *
   * @param flexShrink the flex shrink factor to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout flexShrink(int flexShrink) {
    this.flexShrink = flexShrink;
    return this;
  }

  /**
   * Gets the alignment of a single item within a container.
   *
   * @return the alignment of a single item.
   */
  public AlignSelf alignSelf() {
    return this.alignSelf;
  }

  /**
   * Sets the alignment of a single item within a container.
   *
   * @param alignSelf the alignment to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout alignSelf(AlignSelf alignSelf) {
    this.alignSelf = alignSelf;
    return this;
  }

  /**
   * Gets the positioning of items within a container.
   *
   * @return the positioning of items.
   */
  public Position position() {
    return this.position;
  }

  /**
   * Sets the positioning of items within a container.
   *
   * @param position the positioning to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout position(Position position) {
    this.position = position;
    return this;
  }

  /**
   * Gets the aspect ratio of the item.
   *
   * @return the aspect ratio of the item.
   */
  public Unit aspectRatio() {
    return this.aspectRatio;
  }

  /**
   * Sets the aspect ratio of the item.
   *
   * @param aspectRatio the aspect ratio to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout aspectRatio(Unit aspectRatio) {
    this.aspectRatio = aspectRatio;
    return this;
  }

  /**
   * Gets the width of the item.
   *
   * @return the width of the item.
   */
  public Unit width() {
    return this.width;
  }

  /**
   * Sets the width of the item.
   *
   * @param width the width to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout width(Unit width) {
    this.width = width;
    return this;
  }

  /**
   * Gets the height of the item.
   *
   * @return the height of the item.
   */
  public Unit height() {
    return this.height;
  }

  /**
   * Sets the height of the item.
   *
   * @param height the height to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout height(Unit height) {
    this.height = height;
    return this;
  }

  /**
   * Gets the left position of the item.
   *
   * @return the left position of the item.
   */
  public Unit left() {
    return this.left;
  }

  /**
   * Sets the left position of the item.
   *
   * <p>Notice: the left and right properties cannot be set at the same time. If both are set, the
   * right property will be ignored.
   *
   * @param left the left position to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout left(Unit left) {
    this.left = left;
    return this;
  }

  /**
   * Gets the bottom position of the item.
   *
   * @return the bottom position of the item.
   */
  public Unit bottom() {
    return this.bottom;
  }

  /**
   * Sets the bottom position of the item.
   *
   * <p>Notice: the top and bottom properties cannot be set at the same time. If both are set, the
   * top property will be ignored.
   *
   * @param bottom the bottom position to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout bottom(Unit bottom) {
    this.bottom = bottom;
    return this;
  }

  /**
   * Gets the right position of the item.
   *
   * @return the right position of the item.
   */
  public Unit right() {
    return this.right;
  }

  /**
   * Sets the right position of the item.
   *
   * <p>Notice: the left and right properties cannot be set at the same time. If both are set, the
   * right property will be ignored.
   *
   * @param right the right position to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout right(Unit right) {
    this.right = right;
    return this;
  }

  /**
   * Gets the top position of the item.
   *
   * @return the top position of the item.
   */
  public Unit top() {
    return this.top;
  }

  /**
   * Sets the top position of the item.
   *
   * <p>Notice: the top and bottom properties cannot be set at the same time. If both are set, the
   * bottom property will be ignored.
   *
   * @param top the top position to set.
   * @return the updated UIElementLayout instance.
   */
  public UIElementLayout top(Unit top) {
    this.top = top;
    return this;
  }
}
