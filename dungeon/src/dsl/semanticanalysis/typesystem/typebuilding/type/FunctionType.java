package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.Symbol;
import java.util.ArrayList;
import java.util.List;
import org.neo4j.ogm.annotation.NodeEntity;

// TODO: extend this for named parameters
@NodeEntity
public class FunctionType extends Symbol implements IType {
  private final IType returnType;
  private final ArrayList<IType> parameterTypes;

  public IType getReturnType() {
    return returnType;
  }

  public List<IType> getParameterTypes() {
    return parameterTypes;
  }

  @Override
  public boolean equals(Object other) {
    try {
      FunctionType otherFuncType = (FunctionType) other;
      boolean equal = this.returnType == otherFuncType.returnType;
      equal &= this.parameterTypes.size() == otherFuncType.parameterTypes.size();
      for (int i = 0; i < this.parameterTypes.size() && equal; i++) {
        equal = this.parameterTypes.get(i) == otherFuncType.parameterTypes.get(i);
      }
      return equal;
    } catch (ClassCastException ex) {
      return false;
    }
  }

  public FunctionType(IType returnType, IType... parameterTypes) {
    super(
        calculateTypeName(returnType, new ArrayList<>(List.of(parameterTypes))), Scope.NULL, null);
    this.returnType = returnType;
    this.parameterTypes = new ArrayList<>(List.of(parameterTypes));
  }

  public FunctionType(IType returnType, List<IType> parameterTypes) {
    super(calculateTypeName(returnType, parameterTypes), Scope.NULL, null);
    this.returnType = returnType;
    this.parameterTypes = new ArrayList<>(parameterTypes);
  }

  public static String calculateTypeName(IType returnType, List<IType> parameterTypes) {
    StringBuilder nameBuilder = new StringBuilder();
    nameBuilder.append("$fn(");
    for (int i = 0; i < parameterTypes.size(); i++) {
      IType parameterType = parameterTypes.get(i);
      if (parameterType == null) {
        boolean b = true;
      }
      nameBuilder.append(parameterType.getName());
      if (i != parameterTypes.size() - 1) {
        nameBuilder.append(", ");
      }
    }
    nameBuilder.append(") -> ");
    nameBuilder.append(returnType.getName());
    nameBuilder.append("$");
    return nameBuilder.toString();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Kind getTypeKind() {
    return Kind.FunctionType;
  }
}
