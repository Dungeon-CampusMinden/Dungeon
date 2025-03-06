grammar blockly;

@header{
    package antlr.main;
}

start   : (expr+)*;

expr        : '(' ex=expr ')'                                  #Parenthese_Expr
            | left=expr op=COMPARE_OPERATOR right=expr         #Compare_Expr
            | NOT right=expr                                   #Not_Expr
            | left=expr op=(AND | OR) right=expr               #Logic_Expr
            | right_value                                      #Atom_Expr
            ;

right_value : func_call
            | value
            | var
            ;

func_call   : id=ID '(' args=arguments? ')';
var         : ID;
value   : BOOLEAN | INT | STRING;
arguments    : (expr ','?)+;

// Lexer

WS      : [ \t\r\n]+ -> skip;

IF: 'falls';

BOOLEAN : 'wahr' | 'falsch';
COMPARE_OPERATOR    : '<' | '<=' | '>' | '>=' | '!=' |  '==' ;
AND   : '&&';
OR    : '||';
MULTI : '*';
DIV   : '/';
PLUS  : '+';
MINUS : '-';
NOT   : 'nicht';

ID      : (CHAR | '_')(CHAR | DIGIT | '_')*;
INT     : DIGIT+;
STRING  : '\u0027' ASCII*? '\u0027'
        | '\u0022' ASCII*? '\u0022';

fragment DIGIT  : [0-9];
fragment CHAR   : [a-zA-Z];
fragment ASCII  : '\u0000'..'\u00FF';
