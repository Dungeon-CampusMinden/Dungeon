package de.fwatermann.dungine.ui.layout;

public class UIElementLayout {

  public static final FlexDirection DEFAULT_DIRECTION = FlexDirection.ROW;
  public static final FlexWrap DEFAULT_WRAP = FlexWrap.NO_WRAP;
  public static final JustifyContent DEFAULT_JUSTIFY_CONTENT = JustifyContent.FLEX_START;
  public static final AlignItems DEFAULT_ALIGN_ITEMS = AlignItems.STRETCH;
  public static final AlignContent DEFAULT_ALIGN_CONTENT = AlignContent.NORMAL;
  public static final Unit DEFAULT_COLUMN_GAP = UnitReadOnly.px(0);
  public static final Unit DEFAULT_ROW_GAP = UnitReadOnly.px(0);

  public static final int DEFAULT_ORDER = 0;
  public static final int DEFAULT_FLEX_GROW = 0;
  public static final int DEFAULT_FLEX_SHRINK = 0;
  public static final AlignSelf DEFAULT_ALIGN_SELF = AlignSelf.AUTO;

  //Container properties
  private FlexDirection direction = DEFAULT_DIRECTION;
  private FlexWrap wrap = DEFAULT_WRAP;
  private JustifyContent justifyContent = DEFAULT_JUSTIFY_CONTENT;
  private AlignItems alignItems = DEFAULT_ALIGN_ITEMS;
  private AlignContent alignContent = DEFAULT_ALIGN_CONTENT;
  private Unit columnGap = DEFAULT_COLUMN_GAP;
  private Unit rowGap = DEFAULT_ROW_GAP;

  //Child properties
  private int order = DEFAULT_ORDER;
  private int flexGrow = DEFAULT_FLEX_GROW;
  private int flexShrink = DEFAULT_FLEX_SHRINK;
  private AlignSelf alignSelf = DEFAULT_ALIGN_SELF;

  //Other
  private Unit width = Unit.auto();
  private Unit height = Unit.auto();

  public FlexDirection direction() {
    return this.direction;
  }

  public UIElementLayout direction(FlexDirection direction) {
    this.direction = direction;
    return this;
  }

  public FlexWrap wrap() {
    return this.wrap;
  }

  public UIElementLayout wrap(FlexWrap wrap) {
    this.wrap = wrap;
    return this;
  }

  public UIElementLayout flow(FlexDirection direction, FlexWrap wrap) {
    this.direction = direction;
    this.wrap = wrap;
    return this;
  }

  public JustifyContent justifyContent() {
    return this.justifyContent;
  }

  public UIElementLayout justifyContent(JustifyContent justifyContent) {
    this.justifyContent = justifyContent;
    return this;
  }

  public AlignItems alignItems() {
    return this.alignItems;
  }

  public UIElementLayout alignItems(AlignItems alignItems) {
    this.alignItems = alignItems;
    return this;
  }

  public AlignContent alignContent() {
    return this.alignContent;
  }

  public UIElementLayout alignContent(AlignContent alignContent) {
    this.alignContent = alignContent;
    return this;
  }

  public Unit columnGap() {
    return this.columnGap;
  }

  public UIElementLayout columnGap(Unit columnGap) {
    this.columnGap = columnGap;
    return this;
  }

  public Unit rowGap() {
    return this.rowGap;
  }

  public UIElementLayout rowGap(Unit rowGap) {
    this.rowGap = rowGap;
    return this;
  }

  public int order() {
    return this.order;
  }

  public UIElementLayout order(int order) {
    this.order = order;
    return this;
  }

  public int flexGrow() {
    return this.flexGrow;
  }

  public UIElementLayout flexGrow(int flexGrow) {
    this.flexGrow = flexGrow;
    return this;
  }

  public int flexShrink() {
    return this.flexShrink;
  }

  public UIElementLayout flexShrink(int flexShrink) {
    this.flexShrink = flexShrink;
    return this;
  }

  public AlignSelf alignSelf() {
    return this.alignSelf;
  }

  public UIElementLayout alignSelf(AlignSelf alignSelf) {
    this.alignSelf = alignSelf;
    return this;
  }

  public Unit width() {
    return this.width;
  }

  public UIElementLayout width(Unit width) {
    this.width = width;
    return this;
  }

  public Unit height() {
    return this.height;
  }

  public UIElementLayout height(Unit height) {
    this.height = height;
    return this;
  }
}
