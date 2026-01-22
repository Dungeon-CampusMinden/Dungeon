package blockly.vm.dgir.core;

public class TypeId {
  private final String ident;
  private final String namespace;
  private final Class<?> type;
  private final IDialect dialect;

  TypeId(String ident, String namespace, Class<?> type, IDialect dialect) {
    this.ident = ident;
    this.namespace = namespace;
    this.type = type;
    this.dialect = dialect;
  }

  public String getIdent() {
    return ident;
  }

  public String getNamespace() {
    return namespace;
  }

  public Class<?> getTypeClass() {
    return type;
  }

  public IDialect getDialect() {
    return dialect;
  }
}
