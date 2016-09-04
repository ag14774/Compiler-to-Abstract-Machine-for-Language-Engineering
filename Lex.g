// COMS22201: Lexical analyser

lexer grammar Lex;

//---------------------------------------------------------------------------
// KEYWORDS
//---------------------------------------------------------------------------
WRITE      : 'write' ;
WRITELN    : 'writeln' ;
DO         : 'do' ;
ELSE       : 'else' ;
FALSE      : 'false' ;
IF         : 'if' ;
READ       : 'read' ;
SKIP       : 'skip' ;
THEN       : 'then' ;
TRUE       : 'true' ;
WHILE      : 'while' ;
DEF        : 'def'  ;
REG        : 'register' ;
FOR        : 'for' ;
TO         : 'to' ;

//---------------------------------------------------------------------------
// OPERATORS
//---------------------------------------------------------------------------
SEMICOLON    : ';' ;
OPENPAREN    : '(' ;
CLOSEPAREN   : ')' ;
ASSIGN       : ':=';
ADD          : '+' ;
SUB          : '-' ;
MUL          : '*' ;
LEQ          : '<=';
EQ           : '=' ;
NOT          : '!' ;
AND          : '&' ;
OR           : '|' ;
LS           : '[' ;
RS           : ']' ;


INTNUM       : DIGIT+ ;

ID           : ch+=LETTER (ch+=LETTER | ch+=DIGIT)* {$ch.size()<=8}?;

fragment LETTER: 'a'..'z' | 'A'..'Z';
fragment DIGIT : '0'..'9';

STRING       : '\'' ('\'' '\'' | ~'\'')* '\'';

COMMENT      : '{' (~'}')* '}' {skip();} ;

WS           : (' ' | '\t' | '\r' | '\n' )+ {skip();} ;
