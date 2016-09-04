// COMS22201: Syntax analyser

parser grammar Syn;

options {
  memoize = true;
  tokenVocab = Lex;
  output = AST;
}

@header {
    import java.util.HashSet;
    import java.util.Set;
    import java.util.Map;
    import java.util.HashMap;
    import java.util.Stack;
}

@members
{
    boolean errorFound = false;
    Stack<String> paraphrases = new Stack<String>();
    private Set<String> symbolTable = new HashSet<String>();
    private Set<String> temporaries = new HashSet<String>();

	private String cleanString(String s){
		String tmp;
		tmp = s.replaceAll("^'", "");
		s = tmp.replaceAll("'$", "");
		tmp = s.replaceAll("''", "'");
		return tmp;
	}
    
    public String getErrorMessage(RecognitionException e, String[] tokenNames){
        String msg = super.getErrorMessage(e, tokenNames);
        if(msg.contains("IDNOTFOUND"))
            msg = "Undeclared identifier "+msg.split(" ")[1];
        if(msg.contains("ALREADYDECLARED"))
            msg = "Identifier " + msg.split(" ")[1] +" is already declared";
        if(msg.contains("FORERROR")){
            String id = msg.split(" ")[1];
            msg = "Identifier "+id+" is already declared as a non-register variable";
        }
        if(paraphrases.size()>0) {
            String paraphrase = (String)paraphrases.peek();
            msg = msg+" "+paraphrase;
        }
        if(msg.contains("IDALT")){
            String temp = msg.split(" ")[1];
            String id = temp.split("->")[0];
            String alt = temp.split("->")[1];
            msg = "Undeclared identifier "+id+". Perhaps you meant "+alt+"?";
        }
        if(msg.contains("EOF")&&!msg.contains("<EOF>"))
            return "missing \";\" between statements";
        return msg;
    }

    public boolean getErrorFound(){
        return errorFound;
    }

}

@rulecatch {
    catch (RecognitionException e){
        errorFound = true;
        reportError(e);
        recover(input,e);
    }
}

program :
    statements EOF!
  ;

statements :
    statement ( SEMICOLON^ statement )*
  ;

statement
@init  { paraphrases.push("in statement"); }
@after { paraphrases.pop(); }
  :  (WRITE^ OPENPAREN! exp CLOSEPAREN!) => (WRITE^ OPENPAREN! exp CLOSEPAREN!)
  | WRITE^ OPENPAREN! ( string | boolexp ) CLOSEPAREN!
  | WRITELN
  | (ID ASSIGN^ exp) => (var=ID ASSIGN^ exp) {symbolTable.add($var.getText());}
  | (REG^ var=ID) ASSIGN^ exp {
    if(symbolTable.contains($var.getText())){
        throw new FailedPredicateException(input,$var.getText(),"ALREADYDECLARED");
    }
    else {
        symbolTable.add($var.getText());
        temporaries.add($var.getText());
    }
    }
  | DEF^ arr=ID LS! INTNUM RS! {symbolTable.add($arr.getText());}
  | array ASSIGN^ exp
  | READ^ OPENPAREN! var=ID CLOSEPAREN! {symbolTable.add($var.getText());}
  | SKIP
  | IF^ boolexp THEN! statement ELSE! statement
  | WHILE^ boolexp DO! statement
  | FOR^ (forAssign) TO! (exp) DO! (statement)
  | OPENPAREN! statements CLOSEPAREN!
  ;

fragment forAssign :
    (var=ID ASSIGN exp) {
    if(symbolTable.contains($var.getText())&&!temporaries.contains($var.getText())){
        throw new FailedPredicateException(input,$var.getText(),"FORERROR");
    }
    else if(!symbolTable.contains($var.getText())){
        symbolTable.add($var.getText());
        temporaries.add($var.getText());
    }
    } -> ^(ASSIGN ^(REG ID) exp)
  ;

fragment array :
    (arr=ID^ LS! exp RS!)
    {if(!symbolTable.contains($arr.getText())){
        int min = 99999;
        String alt = "";
        for(String B : symbolTable){
            int dist = EditDistance.calculate($arr.getText(),B);
            if(dist<=min){
                min = dist;
                alt = B;
            }
        }
        if(alt.equals(""))
            throw new FailedPredicateException(input,$arr.getText(),"IDNOTFOUND");
        else
            throw new FailedPredicateException(input,$arr.getText()+"->"+alt,"IDALT");
     }
    }
  ;

boolexp
@init  { paraphrases.push("in boolean expression"); }
@after { paraphrases.pop(); }
  :  boolterm ((AND|OR)^ boolterm)*
  ;

boolterm :
    (NOT^)? bool
  ;

bool :
    TRUE | FALSE
  | (exp bop^ exp)=>(exp bop^ exp)
  | OPENPAREN! boolexp CLOSEPAREN!
  ;

fragment bop :
    EQ | LEQ
  ;

exp
@init  { paraphrases.push("in expression"); }
@after { paraphrases.pop(); }
  :  term ( ( ADD | SUB )^ term )*
  ;

term :
    factor ( MUL^ factor )*
  ;

factor :
    var=ID
    {if(!symbolTable.contains($var.getText())){
        int min = 99999;
        String alt = "";
        for(String B : symbolTable){
            int dist = EditDistance.calculate($var.getText(),B);
            if(dist<=min){
                min = dist;
                alt = B;
            }
        }
        if(alt.equals(""))
            throw new FailedPredicateException(input,$var.getText(),"IDNOTFOUND");
        else
            throw new FailedPredicateException(input,$var.getText()+"->"+alt,"IDALT");
     }
    }
  | INTNUM
  | array
  | OPENPAREN! exp CLOSEPAREN!
  ;

string
scope { String tmp; }
  : s=STRING { $string::tmp = cleanString($s.text); }-> STRING[$string::tmp]
  ;
