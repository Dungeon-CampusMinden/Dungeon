grammar ParseTreeValidation;

INDENT  : '  ';
SYMBOL  : ~[ \n]+;
SPACE   : ' ';
NEWLINE : [\n];
WS      : [\r]+ -> skip;

tree
  : branch* EOF
  ;

branch
  : INDENT* complex_branch
  | INDENT* single_symbol_branch
  ;

single_symbol_branch
  : SYMBOL NEWLINE?
  ;

complex_branch
  : rule=symbol_with_space matched_text NEWLINE?
  ;

matched_text
  : SYMBOL (SPACE SYMBOL)*
  ;

symbol_with_space
  : SYMBOL SPACE
  ;
