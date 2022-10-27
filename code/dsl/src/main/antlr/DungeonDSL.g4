grammar DungeonDSL;

@header{
    package antlr.main;
}

program   : 'hello' ID;
ID  : [a-z]+ ;
WS  : [ \t\r\n]+ -> skip ;
