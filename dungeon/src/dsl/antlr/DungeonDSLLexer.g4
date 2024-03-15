lexer grammar DungeonDSLLexer;

@header{
import dsl.semanticanalysis.environment.IEnvironment;
import dsl.antlr.DungeonDSLLexerWithEnvironment;
}

options
{
  superClass = DungeonDSLLexerWithEnvironment;
}

@lexer::members
{
  public DungeonDSLLexer(CharStream input, IEnvironment environment)
  {
    this(input);
    this.environment = environment;
  }
}

/*
 * Lexer rules
 */

DOUBLE_LINE : '--';
ARROW       : '->';
OPEN_BRACE  : '{';
CLOSE_BRACE : '}';
OPEN_PAR    : '(';
CLOSE_PAR   : ')';
COLON       : ':';
SEMICOLON   : ';';
DOT         : '.';
COMMA       : ',';
NEQ         : '!=';
EQ          : '==';
GEQ         : '>=';
LEQ         : '<=';
OPEN_BRACK  : '[';
CLOSE_BRACK : ']';
CLOSE_ANGLE : '>';
OPEN_ANGLE  : '<';
MINUS       : '-';
PLUS        : '+';
DIV         : '/';
STAR        : '*';
BANG        : '!';
ASSIGN      : '=';

FOR         : 'for';
OR          : 'or';
IF          : 'if';
ELSE        : 'else';
AND         : 'and';
TRUE        : 'true';
FALSE       : 'false';
AS          : 'as';
FN          : 'fn';
IN          : 'in';
COUNT       : 'count';
RETURN      : 'return';
VAR         : 'var';
ENTITY_TYPE : 'entity_type';
ITEM_TYPE   : 'item_type';
GRAPH       : 'graph';
TYPE        : 'type';
WHILE       : 'while';
IMPORT      : '#import';
SEQ         : 'seq'|'sequence';
ST_M        : 'st_m'|'subtask_mandatory';
ST_O        : 'st_o'|'subtask_optional';
C_C         : 'c_c'|'conditional_correct';
C_F         : 'c_f'|'conditional_false';
SEQ_AND     : 'seq_and'|'sequence_and';
SEQ_OR      : 'seq_or'|'sequence_or';

TYPE_ID     : [_a-zA-Z][a-zA-Z0-9_]* {isStrTypeName(getText())}?;
ID          : [_a-zA-Z][a-zA-Z0-9_]*;
NUM         : ([0-9]|[1-9][0-9]*);
NUM_DEC     : [0-9]+'.'[0-9]+;
WS          : [ \t\r\n]+ -> skip;

LINE_COMMENT
        : '//' ~[\r\n]* -> channel(HIDDEN)
        ;

BLOCK_COMMENT
        : '/*' .*? '*/' -> channel(HIDDEN)
        ;

STRING_LITERAL  : '\'' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f'] )* '\''
                | '"' ( STRING_ESCAPE_SEQ | ~[\\\r\n\f"] )* '"'
                ;

/*
 * fragments
 */
fragment STRING_ESCAPE_SEQ
    : '\\' .
    ;
