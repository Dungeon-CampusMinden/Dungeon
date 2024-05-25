package main;

import dsl.semanticanalysis.symbol.Symbol;

// ranking: higher value = higher ranked
public record RankedSymbol(Symbol symbol, int ranking) { }
