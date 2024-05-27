package main;

import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

// ranking: higher value = higher ranked
public record RankedSymbol(Symbol symbol, IType symbolType, int ranking) {}
