// COMS22201: IR tree construction

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

public class HaskellParser {
    // The code below is generated automatically from the ".tokens" file of the
    // ANTLR syntax analysis, using the TokenConv program.
    //
// CAMLE TOKENS BEGIN
  public static final String[] tokenNames = new String[] {
"NONE", "NONE", "NONE", "NONE", "ADD", "AND", "ASSIGN", "CLOSEPAREN", "COMMENT", "DEF", "DIGIT", "DO", "ELSE", "EQ", "FALSE", "FOR", "ID", "IF", "INTNUM", "LEQ", "LETTER", "LS", "MUL", "NOT", "OPENPAREN", "OR", "READ", "REG", "RS", "SEMICOLON", "SKIP", "STRING", "SUB", "THEN", "TO", "TRUE", "WHILE", "WRITE", "WRITELN", "WS"};
  public static final int ADD=4;
  public static final int AND=5;
  public static final int ASSIGN=6;
  public static final int CLOSEPAREN=7;
  public static final int COMMENT=8;
  public static final int DEF=9;
  public static final int DIGIT=10;
  public static final int DO=11;
  public static final int ELSE=12;
  public static final int EQ=13;
  public static final int FALSE=14;
  public static final int FOR=15;
  public static final int ID=16;
  public static final int IF=17;
  public static final int INTNUM=18;
  public static final int LEQ=19;
  public static final int LETTER=20;
  public static final int LS=21;
  public static final int MUL=22;
  public static final int NOT=23;
  public static final int OPENPAREN=24;
  public static final int OR=25;
  public static final int READ=26;
  public static final int REG=27;
  public static final int RS=28;
  public static final int SEMICOLON=29;
  public static final int SKIP=30;
  public static final int STRING=31;
  public static final int SUB=32;
  public static final int THEN=33;
  public static final int TO=34;
  public static final int TRUE=35;
  public static final int WHILE=36;
  public static final int WRITE=37;
  public static final int WRITELN=38;
  public static final int WS=39;
// CAMLE TOKENS END

  
    public static String convert(CommonTree ast, String progname) {
        //return progname+" :: Stm\n"+progname+" = "+program(ast);
        return program(ast);
    }

    public static String program(CommonTree ast) {
        return statements(ast);
    }

    public static String statements(CommonTree ast) {
        Token t = ast.getToken();
        int tt = t.getType();
        if (tt == SEMICOLON) {
            CommonTree ast1 = (CommonTree) ast.getChild(0);
            CommonTree ast2 = (CommonTree) ast.getChild(1);
            return "(Comp "+statements(ast1)+" "+statements(ast2)+")";
        } else {
            return statement(ast);
        }
    }

    public static String statement(CommonTree ast) {
        CommonTree ast1, ast2, ast3;
        Token t = ast.getToken();
        int tt = t.getType();
        if (tt == WRITE) {
            ast1 = (CommonTree) ast.getChild(0);
            Token t2 = ast1.getToken();
            int tt2 = t2.getType();
            if (tt2 == STRING) {
                return "(WriteS \"" + t2.getText()+"\")";
            } else if (tt2 == NOT || tt2 == AND || tt2 == OR || tt2 == TRUE || tt2 == FALSE || tt2 == EQ || tt2 == LEQ) {
            	return "(WriteB "+bval(ast1)+")";
            } else {
            	return "(WriteA "+aval(ast1)+")";
            }
        } else if (tt == WRITELN) {
            return "WriteLn";
        } else if (tt == ASSIGN) {
            ast1 = (CommonTree) ast.getChild(0);
            ast2 = (CommonTree) ast.getChild(1);
            return "(Ass \""+ast1.getToken().getText()+"\" "+aval(ast2)+")";
        } else if (tt == READ) {
            ast1 = (CommonTree) ast.getChild(0);
            return "(Read \""+ast1.getToken().getText()+"\")";
        } else if (tt == SKIP) {
            return "Skip";
        } else if (tt == IF) {
            // bool expr
            ast1 = (CommonTree) ast.getChild(0);
            // ss1
            ast2 = (CommonTree) ast.getChild(1);
            // ss2
            ast3 = (CommonTree) ast.getChild(2);
            return "(If "+bval(ast1)+" "+statements(ast2)+" "+statements(ast3)+")";
        } else if (tt == WHILE) {
            // bool expr
            ast1 = (CommonTree) ast.getChild(0);
            // ss1
            ast2 = (CommonTree) ast.getChild(1);
            return "(While "+bval(ast1)+" "+statements(ast2)+")";
        } else if (tt == FOR){
        	//not originally included in While language
        	return "error";
        } else if (tt == DEF){
        	//not originally included in While language
        	return "error";
        } else {
            return "error";
        }
    }
    
    public static String bval(CommonTree ast){
    	CommonTree ast1, ast2;
        Token t = ast.getToken();
        int tt = t.getType();
        if(tt == NOT){
        	ast1 = (CommonTree)ast.getChild(0);
        	return "(Neg "+bval(ast1)+")";
        }
        else if(tt == AND){
        	ast1 = (CommonTree)ast.getChild(0);
        	ast2 = (CommonTree)ast.getChild(1);
        	return "(And "+bval(ast1)+" "+bval(ast2)+")";
        }
        else if(tt == OR){
        	//not in included in the original version
        }
        else if(tt == TRUE){
        	return "TRUE";
        }
        else if(tt == FALSE){
        	return "FALSE";
        }
        else if(tt == EQ){
        	ast1 = (CommonTree)ast.getChild(0);
        	ast2 = (CommonTree)ast.getChild(1);
        	return "(Eq "+aval(ast1)+" "+aval(ast2)+")";
        }
        else if(tt == LEQ){
        	ast1 = (CommonTree)ast.getChild(0);
        	ast2 = (CommonTree)ast.getChild(1);
        	return "(Le "+aval(ast1)+" "+aval(ast2)+")";
        }
        return "";
    }
    
    public static String aval(CommonTree ast){
    	CommonTree ast1, ast2;
        Token t = ast.getToken();
        int tt = t.getType();
        if(tt == INTNUM){
        	return "(N "+t.getText()+")";
        }
        else if(tt == ID){
        	return "(V \""+t.getText()+"\")";
        }
        else if(tt == ADD){
        	ast1 = (CommonTree)ast.getChild(0);
        	ast2 = (CommonTree)ast.getChild(1);
        	return "(Add "+aval(ast1)+" "+aval(ast2)+")";
        }
        else if(tt == SUB){
        	ast1 = (CommonTree)ast.getChild(0);
        	ast2 = (CommonTree)ast.getChild(1);
        	return "(Sub "+aval(ast1)+" "+aval(ast2)+")";
        }
        else if(tt == MUL){
        	ast1 = (CommonTree)ast.getChild(0);
        	ast2 = (CommonTree)ast.getChild(1);
        	return "(Mult "+aval(ast1)+" "+aval(ast2)+")";
        }
    	return "";
    }
}
