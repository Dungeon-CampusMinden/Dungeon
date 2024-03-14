grammar DungeonDSL;

@header{
    package dsl.antlr;
    import dsl.semanticanalysis.environment.IEnvironment;
}

options
{
  superClass = DungeonDSLParserWithEnvironment;
}

@parser::members
{
  public DungeonDSLParser(TokenStream input, IEnvironment environment)
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

/*TYPE_ID
    : 'asdf_type'
    ;*/

TRUE : 'true';
FALSE: 'false';
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

/*
 * fragments
 */
fragment STRING_ESCAPE_SEQ
    : '\\' .
    ;

/*
 * Parser rules
 */
program : definition* EOF
        ;

definition
        : dot_def
        | import_def
        | {isTypeName()}? object_def
        | entity_type_def
        | item_type_def
        | fn_def
        ;

import_def
    : '#import' path=STRING_LITERAL ':' sym_id=ID                   #import_unnamed
    | '#import' path=STRING_LITERAL ':' sym_id=ID 'as' sym_name=ID  #import_named
    ;

fn_def
    : 'fn' ID '(' param_def_list? ')' ret_type_def? stmt_block
    ;

stmt
    : expression ';'
    | var_decl
    | stmt_block
    | conditional_stmt
    | return_stmt
    | loop_stmt
    ;

loop_stmt
    : 'for' type_id=type_decl var_id=ID 'in' iteratable_id=expression stmt                         #for_loop
    | 'for' type_id=type_decl var_id=ID 'in' iteratable_id=expression 'count' counter_id=ID stmt   #for_loop_counting
    | 'while' expression stmt                                                               #while_loop
    ;

var_decl
    : 'var' id=ID '=' expression ';'   #var_decl_assignment
    | 'var' id=ID ':' type_decl ';'    #var_decl_type_decl
    ;

expression
    : assignee '=' expression           #expr_assignment
    | logic_or                          #expr_trivial
    ;

member_access_rhs
    : '.' func_call member_access_rhs?  #method_call_expression
    | '.' ID member_access_rhs?         #member_access_expression
    ;

assignee
    : func_call member_access_rhs   #assignee_func
    | ID member_access_rhs          #assignee_member_access
    | ID                            #assignee_identifier
    ;

logic_or
    : logic_or ( or='or' logic_and )
    | logic_and
    ;

logic_and
    : logic_and ( and='and' equality )
    | equality
    ;

equality
    : equality ( ( neq='!=' | eq='==' ) comparison )
    | comparison
    ;

comparison
    : comparison ( ( gt='>' | geq='>=' | lt='<' | leq='<=' ) term )
    | term
    ;

term
    : term ( ( minus='-' | plus='+' ) factor )
    | factor
    ;

factor
    : factor ( ( div='/' | mult='*' ) unary )
    | unary
    ;

unary
    : ( bang='!' | minus='-' ) unary
    | primary
    ;

func_call
        : ID '(' expression_list? ')'
        ;

stmt_block
    : OPEN_BRACE stmt* CLOSE_BRACE
    ;

return_stmt
    : 'return' expression? ';'
    ;

conditional_stmt
    : 'if' expression stmt else_stmt?
    ;

else_stmt
    : 'else' stmt
    ;

ret_type_def
    : ARROW type_id=type_decl
    ;

param_def
    : type_id=type_decl param_id=ID
    | type_decl                     {notifyErrorListeners("Missing identifier in parameter definition");}
    //| ID                            {notifyErrorListeners("Missing type specification in parameter definition");}
    ;

type_decl
    : type_decl '<' '>'                     #set_param_type
    | type_decl '[' ']'                     #list_param_type
    | '[' type_decl ARROW type_decl ']'     #map_param_type
    | ID                                    #id_param_type
    ;

param_def_list
        : param_def (',' param_def)*
        //| param_def
        ;

entity_type_def
        : 'entity_type' ID OPEN_BRACE component_def_list? CLOSE_BRACE ;

item_type_def
        : 'item_type' ID OPEN_BRACE property_def_list? CLOSE_BRACE ;

// used to specify, which components should be used in a game object
component_def_list
        : aggregate_value_def ',' component_def_list
        | aggregate_value_def
        ;

aggregate_value_def
        : //type_id=ID
        /*|*/ type_id=ID OPEN_BRACE property_def_list? CLOSE_BRACE
        ;

// TODO: maybe the problem here is, that object_def starts with an ID
//  - in single token deletion e.g. in the snippet
//  ```
//     my_type id {
//       prop1:id,
//       prop2:
//     }
//    ID is in the LL(2)-Set, which may lead the parser to believe, it found a new object definition!!
//  - need to verify this!!!
//    - this is in fact the problem!
//  - maybe this needs a semantic predicate to check, if the ID is registered as a type name in the type environment!!
//    - are there interdependencies between semantic analysis and the parsing of such a definition?
//      - currenlty no, because the DungeonDSL does not allow for construction of completely new data types in dng files!
//    - for which types is this construct actually used? is there any usefull application of this apport from dungeon_config?
//      - tasks, config, etc.
//      - it is useful in it's current state!
//    - the GameEnvironment is constructed whenever the DSL-Interpreter is created, it would in theory be possible to
//      add a semantic predicate for this...
//    - how does one add one such semantic predicate to the Parser? Does not look, like it is possible to add such an
//      external dependency in an easy way
//    - a cheap and easy alternative would be to add all possible type names to be used in this context directly to the grammar...
//    - could this be done with a semantic predicate purely based on syntactic information?
object_def
        : type_id=ID object_id=ID OPEN_BRACE property_def_list? CLOSE_BRACE
        //| type_id=TYPE_ID object_id=ID OPEN_BRACE property_def_list? CLOSE_BRACE
        ;

property_def_list
        : property_def (',' property_def)*
        //: property_def ',' property_def_list
        //| property_def
        ;

property_def
        : ID ':' expression
        //| ID ':'                {notifyErrorListeners("Missing expression in property definition");}
        ;

expression_list
        : expression ',' expression_list
        | expression
        ;

grouped_expression
    : '(' expression ')'
    ;

list_definition
    : '[' expression_list? ']'
    ;

set_definition
    : '<' expression_list? '>'
    ;

/*mem_acc_prim
    : func_call '.' mem_acc_prim    //#assignee_func_call
    | ID '.' mem_acc_prim           //#assignee_qualified_name
    | ID                        //#assignee_identifier
    ;*/

primary : ID member_access_rhs?
        | STRING_LITERAL
        | TRUE
        | FALSE
        | NUM
        | NUM_DEC
        | aggregate_value_def
        | set_definition
        | grouped_expression member_access_rhs?
        | func_call member_access_rhs?
        | list_definition
        ;

/*
 * -------------------- dot related definitions --------------------
 * dot grammar: https://graphviz.org/doc/info/lang.html
 *
 * The regular dot-grammar was heavily simplified to be used as a
 * definition language for task dependency graphs
 */

dot_def : 'graph' ID OPEN_BRACE dot_stmt_list? CLOSE_BRACE ;

dot_stmt_list
        : dot_stmt ';'? dot_stmt_list?
        ;

dot_stmt
        : dot_node_stmt
        | dot_edge_stmt
        ;

dot_edge_stmt
        : dot_node_list dot_edge_RHS+ dot_attr_list?
        ;

dot_node_list
        : ID ',' dot_node_list
        | ID
        ;

dot_edge_RHS
        : ARROW dot_node_list
        ;

dot_node_stmt
        : ID dot_attr_list?
        ;

dot_attr_list
        : '[' dot_attr+ ']'
        ;

dot_attr
        : ID '=' ID (';'|',')?                  #dot_attr_id
        | 'type' '=' dependency_type (';'|',')? #dot_attr_dependency_type
        ;

dependency_type
        : ('seq'|'sequence')            #dt_sequence
        | ('st_m'|'subtask_mandatory')  #dt_subtask_mandatory
        | ('st_o'|'subtask_optional')   #dt_subtask_optional
        | ('c_c'|'conditional_correct') #dt_conditional_correct
        | ('c_f'|'conditional_false')   #dt_conditional_false
        | ('seq_and'|'sequence_and')    #dt_sequence_and
        | ('seq_or'|'sequence_or')      #dt_sequence_or
        ;
