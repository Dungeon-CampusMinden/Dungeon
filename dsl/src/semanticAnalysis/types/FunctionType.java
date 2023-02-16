package semanticAnalysis.types;

import java.util.ArrayList;
import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;

public class FunctionType extends Symbol implements IType {
    private final IType returnType;
    private final ArrayList<IType> parameterTypes;

    public IType getReturnType() {
        return returnType;
    }

    public ArrayList<IType> getParameterTypes() {
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

    public FunctionType(IType returnType, ArrayList<IType> parameterTypes) {
        super(calculateTypeName(returnType, parameterTypes), Scope.NULL, null);
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public static String calculateTypeName(IType returnType, ArrayList<IType> parameterTypes) {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append("$fn(");
        for (int i = 0; i < parameterTypes.size(); i++) {
            IType parameterType = parameterTypes.get(i);
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
