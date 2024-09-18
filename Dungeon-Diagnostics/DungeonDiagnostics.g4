grammar DungeonDiagnostics;

// Parser-Kopfbereich für dynamische Methoden und Variablen
@parser::members {
    private boolean isVariableDeclaration() {
        return _input.LT(1).getType() == VAR;
    }
}

// Parser-Regeln

program: statement* ;

statement
    : var_decl                          # VarDeclStmt
    | otherCode                         # OtherCodeStmt
    ;

var_decl
    : VAR id=ID '=' expression ';'   #var_decl_assignment
    | VAR id=ID ':' type_decl ';'    #var_decl_type_decl
    ;

expression
    : ID
    | NUM
    | NUM_DEC
    | STRING_LITERAL
    | TRUE
    | FALSE
    ;

type_decl
    : type_decl '<>'                        #set_param_type
    | type_decl '[]'                        #list_param_type
    | '[' type_decl ARROW  type_decl ']'    #map_param_type
    | taskTypes                             #task_types
    | ID                                    #id_param_type
    ;

taskTypes
    : 'single_choice_task'
    | 'multiple_choice_task'
    ;



otherCode
    : { !isVariableDeclaration() }? (LBRACE | RBRACE | COMMA | DOT | LPAREN | RPAREN | .)*? (';' | '\n' | LBRACE | RBRACE)
    ;



fragment STRING_ESCAPE_SEQ
    : '\\' .
    ;
// Lexer-Regeln

DOUBLE_LINE : '--';
ARROW       : '->';

TRUE : 'true';
FALSE: 'false';
VAR : 'var';
ID  : [_a-zA-Z][a-zA-Z0-9_]*;
NUM : ([0-9]|[1-9][0-9]*);
NUM_DEC: [0-9]+'.'[0-9]+;
WS  : [ \t\r\n]+ -> skip;

LINE_COMMENT
        : '//' ~[\r\n]* -> channel(HIDDEN)
        ;

BLOCK_COMMENT
        : '/*' .*? '*/' -> channel(HIDDEN)
        ;


STRING_LITERAL  : '\'' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f'] )* '\''
                | '"' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f"] )* '"'
                ;

LBRACE      : '{';
RBRACE      : '}';
COMMA       : ',';
DOT         : '.';
LPAREN      : '(';
RPAREN      : ')';
