grammar ParseTreeValidation;

INDENT    : '  ';
DONT_CARE : '$DC$';
SYMBOL    : ~[ \n]+;
SPACE     : ' ';
NEWLINE   : [\n];
WS        : [\r]+ -> skip;

LINE_COMMENT
        : '//' ~[\r\n]* -> channel(HIDDEN)
        ;

tree
  : branch* EOF
  ;

branch
  : INDENT* complex_branch
  | INDENT* single_symbol_branch
  | INDENT* dont_care_branch
  ;

dont_care_branch
  : DONT_CARE SPACE* NEWLINE?
  ;

single_symbol_branch
  : SYMBOL SPACE* NEWLINE?
  ;

complex_branch
  : rule=symbol_with_space matched_text SPACE* NEWLINE?
  ;

matched_text
  : SYMBOL (SPACE SYMBOL)*
  ;

symbol_with_space
  : SYMBOL SPACE
  ;
