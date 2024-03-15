parser grammar DungeonDSLParser;

@header{
import dsl.semanticanalysis.environment.IEnvironment;
}

options
{
  superClass = DungeonDSLParserWithEnvironment;
  tokenVocab = DungeonDSLLexer;
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
 * Parser rules
 */
program : definition* EOF
        ;

definition
        : dot_def
        | import_def
        | object_def
        | entity_type_def
        | item_type_def
        | fn_def
        ;

import_def
    : IMPORT path=STRING_LITERAL COLON sym_id=id                   #import_unnamed
    | IMPORT path=STRING_LITERAL COLON sym_id=id AS sym_name=id  #import_named
    ;

fn_def
    : FN id OPEN_PAR param_def_list? CLOSE_PAR ret_type_def? stmt_block
    ;

stmt
    : expression SEMICOLON
    | var_decl
    | stmt_block
    | conditional_stmt
    | return_stmt
    | loop_stmt
    ;

loop_stmt
    : FOR type_id=type_decl var_id=id IN iteratable_id=expression stmt                          #for_loop
    | FOR type_id=type_decl var_id=id IN iteratable_id=expression COUNT counter_id=id stmt      #for_loop_counting
    | WHILE expression stmt                                                                     #while_loop
    ;

var_decl
    : VAR id ASSIGN expression SEMICOLON   #var_decl_assignment
    | VAR id COLON type_decl SEMICOLON    #var_decl_type_decl
    ;

expression
    : assignee ASSIGN expression           #expr_assignment
    | logic_or                          #expr_trivial
    ;

member_access_rhs
    : DOT func_call member_access_rhs?  #method_call_expression
    | DOT id member_access_rhs?         #member_access_expression
    ;

assignee
    : func_call member_access_rhs   #assignee_func
    | id member_access_rhs          #assignee_member_access
    | id                            #assignee_identifier
    ;

logic_or
    : logic_or ( or=OR logic_and )
    | logic_and
    ;

logic_and
    : logic_and ( and=AND equality )
    | equality
    ;

equality
    : equality ( ( neq=NEQ | eq=EQ ) comparison )
    | comparison
    ;

comparison
    : comparison ( ( gt=CLOSE_ANGLE | geq=GEQ | lt=OPEN_ANGLE| leq=LEQ) term )
    | term
    ;

term
    : term ( ( minus=MINUS| plus=PLUS) factor )
    | factor
    ;

factor
    : factor ( ( div=DIV| mult=STAR) unary )
    | unary
    ;

unary
    : ( bang=BANG | minus=MINUS ) unary
    | primary
    ;

func_call
        : id OPEN_PAR expression_list? CLOSE_PAR
        ;

stmt_block
    : OPEN_BRACE stmt* CLOSE_BRACE
    ;

return_stmt
    : RETURN expression? SEMICOLON
    ;

conditional_stmt
    : IF expression stmt else_stmt?
    ;

else_stmt
    : ELSE stmt
    ;

ret_type_def
    : ARROW type_id=type_decl
    ;

param_def
    : type_id=type_decl param_id=id
    | type_decl                     {notifyErrorListeners("Missing identifier in parameter definition");}
    //| ID                            {notifyErrorListeners("Missing type specification in parameter definition");}
    ;

type_decl
    : type_decl OPEN_ANGLE CLOSE_ANGLE                     #set_param_type
    | type_decl OPEN_BRACK CLOSE_BRACK                     #list_param_type
    | OPEN_BRACK type_decl ARROW type_decl CLOSE_BRACK     #map_param_type
    | id                                    #id_param_type
    ;

param_def_list
        : param_def (COMMA param_def)*
        ;

entity_type_def
        : ENTITY_TYPE id OPEN_BRACE component_def_list? CLOSE_BRACE ;

item_type_def
        : ITEM_TYPE id OPEN_BRACE property_def_list? CLOSE_BRACE ;

// used to specify, which components should be used in a game object
component_def_list
        : aggregate_value_def COMMA component_def_list
        | aggregate_value_def
        ;

aggregate_value_def
        : type_id=id OPEN_BRACE property_def_list? CLOSE_BRACE
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
        : type_id=TYPE_ID object_id=ID OPEN_BRACE property_def_list? CLOSE_BRACE
        ;

property_def_list
        : {_input.LA(-1) == OPEN_BRACE}? property_def (COMMA property_def)*
        //: property_def ',' property_def_list
        //| property_def
        ;

property_def
        : id COLON expression
        //| ID ':'                {notifyErrorListeners("Missing expression in property definition");}
        ;

expression_list
        : expression COMMA expression_list
        | expression
        ;

grouped_expression
    : OPEN_PAR expression CLOSE_PAR
    ;

list_definition
    : OPEN_BRACK expression_list? CLOSE_BRACK
    ;

set_definition
    : OPEN_ANGLE expression_list? CLOSE_ANGLE
    ;

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

id  : ID
    | TYPE_ID
    | COUNT
    | GRAPH
    | TYPE
    | WHILE
    | SEQ
    | ST_M
    | ST_O
    | C_C
    | C_F
    | SEQ_AND
    | SEQ_OR
    ;

/*
 * -------------------- dot related definitions --------------------
 * dot grammar: https://graphviz.org/doc/info/lang.html
 *
 * The regular dot-grammar was heavily simplified to be used as a
 * definition language for task dependency graphs
 */

dot_def : GRAPH id OPEN_BRACE dot_stmt_list? CLOSE_BRACE ;

dot_stmt_list
        : dot_stmt SEMICOLON? dot_stmt_list?
        ;

dot_stmt
        : dot_node_stmt
        | dot_edge_stmt
        ;

dot_edge_stmt
        : dot_node_list dot_edge_RHS+ dot_attr_list?
        ;

dot_node_list
        : id COMMA dot_node_list
        | id
        ;

dot_edge_RHS
        : ARROW dot_node_list
        ;

dot_node_stmt
        : id dot_attr_list?
        ;

dot_attr_list
        : OPEN_BRACK dot_attr+ CLOSE_BRACK
        ;

dot_attr
        : ID ASSIGN ID (SEMICOLON|COMMA)?                  #dot_attr_id
        | TYPE ASSIGN dependency_type (SEMICOLON|COMMA)? #dot_attr_dependency_type
        ;

dependency_type
        : SEQ            #dt_sequence
        | ST_M  #dt_subtask_mandatory
        | ST_O   #dt_subtask_optional
        | C_C #dt_conditional_correct
        | C_F   #dt_conditional_false
        | SEQ_AND    #dt_sequence_and
        | SEQ_OR      #dt_sequence_or
        ;
