grammar DungeonDSL;

@header{
    package antlr.main;
}

/*
 * Lexer rules
 */

TYPE_SPECIFIER
        : 'quest_config'
        ;

DOUBLE_LINE : '--';
ARROW       : '->';

ID  : [_a-zA-Z][a-zA-Z0-9_]*;
NUM : [1-9][0-9]*;
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

/*
 * fragments
 */
fragment STRING_ESCAPE_SEQ
    : '\\' .
    ;

/*
 * Parser rules
 */

// TODO:
// - expression grammar
// - proper stmt definition

program : definition* EOF
        //| fn_def
        //| stmt
        ;

definition
        : dot_def
        | object_def
        | game_obj_def
        ;

game_obj_def
        : 'game_object' ID '{' component_def_list? '}' ;

// used to specify, which components should be used in a game object
component_def_list
        : component_def ',' component_def_list
        | component_def
        ;

// TODO: could component_def and object_def be united (component_def
//  would be 'anonymous object', which is defined in place)?
// used to configure a used component
component_def
        : ID
        | ID '{' property_def_list '}' ;

object_def  : type_id=TYPE_SPECIFIER object_id=ID '{' property_def_list? '}' #grammar_type_obj_def
            | type_id=ID object_id=ID '{' property_def_list? '}' #other_type_obj_def
            ;


property_def_list
        : property_def ',' property_def_list
        | property_def
        ;

property_def
        : ID ':' primary;

func_call
        : ID '(' param_list? ')'
        ;

param_list
        : primary ',' param_list
        | primary
        ;

// temporary, for testing
primary : ID
        | STRING_LITERAL
        | NUM
        | func_call
        ;

/*
 * -------------------- dot related definitions --------------------
 * dot grammar: https://graphviz.org/doc/info/lang.html
 *
 * simplifications:
 * - don't support subgraphs
 * - don't support ports
 */

dot_def : graph_type=('graph'|'digraph') ID '{' dot_stmt_list? '}' ;

dot_stmt_list
        : dot_stmt ';'? dot_stmt_list?
        ;

dot_stmt
        : dot_node_stmt
        | dot_edge_stmt
        | dot_attr_stmt
        | dot_assign_stmt
        ;

dot_assign_stmt
        : ID '=' ID
        ;

dot_edge_stmt
        : ID dot_edge_RHS+ dot_attr_list?
        ;

dot_edge_RHS
        : dot_edge_op ID
        ;

// dot specifies the keywords as case insensitive,
// we require them to be lowercase for simplicity
dot_attr_stmt
        : ('graph' | 'node' | 'edge') dot_attr_list
        ;

dot_node_stmt
        : ID dot_attr_list?
        ;

dot_attr_list
        : '[' dot_a_list? ']' dot_a_list?
        ;

dot_a_list
        : ID '=' ID (';'|',')? dot_a_list?
        ;

dot_edge_op
        : ARROW
        | DOUBLE_LINE
        ;
