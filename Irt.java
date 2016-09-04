// COMS22201: IR tree construction

import java.util.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

public class Irt {
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

    public static ArrayList<IRTree> stringInstrPointers = new ArrayList<IRTree>();
    public static Set<String>		temporaries			= new HashSet<String>();
    public static int               labelCounter        = 0;
    public static boolean			addErrorCode        = false;
    public static boolean			NO_WORD_POOL		= true;

    public static void optimizeMemoryAllocation() {
        try {
            int stringBasePointer = Memory.getStringBasePointer();
            for (IRTree irt : stringInstrPointers) {
                int offset = Integer.parseInt(irt.getOp());
                String newAddr = String.valueOf(stringBasePointer + offset);
                irt.setOp(newAddr);
            }
        } catch (Exception e) {
            System.err.println("Error parsing offset");
        }
    }

    public static IRTree convert(CommonTree ast) {
        IRTree irt = new IRTree();
        program(ast, irt);
        optimizeMemoryAllocation();
        return irt;
    }

    public static void program(CommonTree ast, IRTree irt) {
        statements(ast, irt);
        if(addErrorCode){
	        IRTree irt1 = new IRTree(irt.getOp());
	        for(IRTree i : irt.getChildren()){
	        	irt1.addSub(i);
	        }
	        irt.removeChildren();
	        irt.setOp("SEQ");
	        irt.addSub(irt1);
	        IRTree irt2 = new IRTree("SEQ");
	        IRTree irt3 = new IRTree("SEQ");
	        IRTree irt4 = new IRTree("SEQ");
	        irt2.addSub(new IRTree("JUMP",new IRTree("NAME",new IRTree("HALT"))));
	        irt2.addSub(irt3);
	        irt3.addSub(new IRTree("LABEL",new IRTree("ERROR")));
	        irt3.addSub(irt4);
	        String errorAddr = String.valueOf(Memory.allocateString("Array Index out of bounds! Halting!"));
	        IRTree errorPrint = new IRTree(errorAddr);
	        stringInstrPointers.add(errorPrint);
	        irt4.addSub(new IRTree("WRS",new IRTree("MEM",new IRTree("CONST",errorPrint))));
	        irt4.addSub(new IRTree("LABEL",new IRTree("HALT")));
	        irt.addSub(irt2);
        }
    }

    public static void statements(CommonTree ast, IRTree irt) {
        Token t = ast.getToken();
        int tt = t.getType();
        if (tt == SEMICOLON) {
            IRTree irt1 = new IRTree();
            IRTree irt2 = new IRTree();
            CommonTree ast1 = (CommonTree) ast.getChild(0);
            CommonTree ast2 = (CommonTree) ast.getChild(1);
            statements(ast1, irt1);
            statements(ast2, irt2);
            irt.setOp("SEQ");
            irt.addSub(irt1);
            irt.addSub(irt2);
            if (irt1.getOp().equals("SKIP") && irt2.getOp().equals("SKIP")) {
                irt.removeSub(1);
                irt.removeSub(0);
                irt.setOp("SKIP");
            } else if (irt1.getOp().equals("SKIP")) {
                irt.removeSub(1);
                irt.removeSub(0);
                irt.setOp(irt2.getOp());
                for (int i = 0; i < 5; i++) {
                    IRTree child = irt2.getSub(i);
                    if (child != null)
                        irt.addSub(child);
                }
            } else if (irt2.getOp().equals("SKIP")) {
                irt.removeSub(1);
                irt.removeSub(0);
                irt.setOp(irt1.getOp());
                for (int i = 0; i < 5; i++) {
                    IRTree child = irt1.getSub(i);
                    if (child != null)
                        irt.addSub(child);
                }
            }
        } else {
            statement(ast, irt);
        }
    }

    public static void statement(CommonTree ast, IRTree irt) {
        CommonTree ast1, ast2, ast3;
        IRTree irt1 = new IRTree(), irt2 = new IRTree();
        Token t = ast.getToken();
        int tt = t.getType();
        if (tt == WRITE) {
            ast1 = (CommonTree) ast.getChild(0);
            String type = arg(ast1, irt1);
            if (type.equals("int")) {
                irt.setOp("WR");
                irt.addSub(irt1);
            } else if (type.equals("string")) {
                irt.setOp("SEQ");
                // irt1 itself is thrown away
                // left child holds another SEQ or a SKIP if it's the last one
                // right child holds a WRS instruction
                if (!irt1.getOp().equals("WRS")) {
                    irt.addSub(irt1.getSub(0));
                    irt.addSub(irt1.getSub(1));
                } else {
                    irt.setOp("WRS");
                    irt.addSub(irt1.getSub(0));
                }
            } else if (type.equals("bool")) {
                irt.setOp("SEQ");
                irt.addSub(irt1.getSub(0));
                irt.addSub(irt1.getSub(1));
            } else {
                irt.setOp("WR");
                irt.addSub(irt1);
            }
        } else if (tt == WRITELN) {
            String a = String.valueOf(Memory.allocateString("\n"));
            irt.setOp("WRS");
            irt1.setOp(a);
            stringInstrPointers.add(irt1);
            irt.addSub(new IRTree("MEM", new IRTree("CONST", irt1)));
        } else if (tt == ASSIGN) {
            irt.setOp("MOVE");
            ast1 = (CommonTree) ast.getChild(0);
            ast2 = (CommonTree) ast.getChild(1);
            assignLeft(ast1, irt1);
            assignRight(ast2, irt2);
            irt.addSub(irt1);
            irt.addSub(irt2);
        } else if (tt == READ) {
            irt.setOp("MOVE");
            ast1 = (CommonTree) ast.getChild(0);
            assignLeft(ast1, irt1);
            irt.addSub(irt1);
            irt.addSub(new IRTree("READ"));
        } else if (tt == SKIP) {
            irt.setOp("SKIP");
        } else if (tt == IF) {
            String n1 = getNewLabel();
            String n2 = getNewLabel();
            // bool expr
            ast1 = (CommonTree) ast.getChild(0);
            // st1
            ast2 = (CommonTree) ast.getChild(1);
            // st2
            ast3 = (CommonTree) ast.getChild(2);
            // irt1 will be a CJUMP or a series of CJUMPs
            translate(ast1, n1, n2, irt1);
            irt.setOp("SEQ");
            irt.addSub(irt1);
            expandIf(ast2, ast3, n1, n2, irt2);
            irt.addSub(irt2);
            if (irt2.getSub(1).getSub(0).getOp().equals("SKIP") && irt2.getSub(1).getSub(1).getOp().equals("LABEL")) {
                irt.setOp("SKIP");
                irt.removeSub(1);
                irt.removeSub(0);
            }
        } else if (tt == WHILE) {
            irt.setOp("SEQ");
            String n1 = getNewLabel();
            String n2 = getNewLabel();
            String n = getNewLabel();
            irt.addSub(new IRTree("LABEL", new IRTree(n1)));
            IRTree irt0 = new IRTree("SEQ");
            irt.addSub(irt0);
            IRTree leftIrt0 = new IRTree();
            ast1 = (CommonTree) ast.getChild(0);
            translate(ast1, n2, n, leftIrt0);
            irt0.addSub(leftIrt0);
            IRTree rightIrt0 = new IRTree();
            ast2 = (CommonTree) ast.getChild(1);
            expandWhile(ast2, n1, n2, n, rightIrt0);
            irt0.addSub(rightIrt0);
        } else if (tt == FOR){
        	irt.setOp("SEQ");
        	IRTree initial = new IRTree();
        	statement((CommonTree)ast.getChild(0),initial);
        	irt.addSub(initial);
        	IRTree loop = new IRTree("SEQ");
        	irt.addSub(loop);
        	String n1 = getNewLabel();
        	String n2 = getNewLabel();
        	String n = getNewLabel();
        	loop.addSub(new IRTree("LABEL", new IRTree(n1)));
        	IRTree irt0 = new IRTree("SEQ");
        	loop.addSub(irt0);
        	IRTree leftIrt0 = new IRTree();
        	CommonTree condition = new CommonTree();
        	condition.token = new CommonToken(LEQ, "<=");
        	condition.insertChild(0, ast.getChild(0).getChild(0).getChild(0));
        	condition.insertChild(1, ast.getChild(1));
        	translate(condition, n2, n, leftIrt0);
        	irt0.addSub(leftIrt0);
        	IRTree rightIrt0 = new IRTree();
        	ast2 = (CommonTree) ast.getChild(2);
        	CommonTree modifiedTree = new CommonTree();
        	modifiedTree.token = new CommonToken(SEMICOLON, ";");
        	modifiedTree.insertChild(0, ast2);
        	CommonTree addition = new CommonTree();
        	addition.token = new CommonToken(ASSIGN, ":=");
        	modifiedTree.insertChild(1, addition);
        	CommonTree leftAddition = new CommonTree();
        	leftAddition.token = ((CommonTree)ast.getChild(0).getChild(0).getChild(0)).getToken();
        	addition.insertChild(0, leftAddition);
        	CommonTree rightAddition = new CommonTree();
        	rightAddition.token = new CommonToken(ADD, "+");
        	addition.insertChild(1, rightAddition);
        	CommonTree rightAdditionleft = new CommonTree();
        	rightAdditionleft.token = ((CommonTree)ast.getChild(0).getChild(0).getChild(0)).getToken();
        	rightAddition.insertChild(0, rightAdditionleft);
        	CommonTree rightAdditionright = new CommonTree();
        	rightAdditionright.token = new CommonToken(INTNUM, "1");
        	rightAddition.insertChild(1, rightAdditionright);
        	expandWhile(modifiedTree, n1, n2, n, rightIrt0);
        	irt0.addSub(rightIrt0);
        	
        } else if (tt == DEF){
            irt.setOp("SKIP");
            String arrayName = ((CommonTree)ast.getChild(0)).getToken().getText();
            int size = Integer.parseInt(((CommonTree)ast.getChild(1)).getToken().getText());
            Memory.allocateArray(arrayName, size);
        } else {
            error(tt);
        }
    }

    public static void expandWhile(CommonTree branch, String n1, String n2, String n, IRTree irt) {
        IRTree br = new IRTree();
        irt.setOp("SEQ");
        irt.addSub(new IRTree("LABEL", new IRTree(n2)));
        IRTree irt1 = new IRTree("SEQ", br);
        statements(branch, br);
        IRTree irt2 = new IRTree("SEQ", new IRTree("JUMP", new IRTree("NAME", new IRTree(n1))), new IRTree("LABEL", new IRTree(n)));
        irt.addSub(irt1);
        irt1.addSub(irt2);
    }

    public static void expandIf(CommonTree branch1, CommonTree branch2, String n1, String n2, IRTree irt) {
        IRTree br1 = new IRTree();
        IRTree br2 = new IRTree();
        statements(branch1, br1);
        statements(branch2, br2);
        expandIf(br1, br2, n1, n2, irt);
    }
    
    public static void expandIf(IRTree br1, IRTree br2, String n1, String n2, IRTree irt) {
        String n = getNewLabel();
        irt.setOp("SEQ");
        irt.addSub(new IRTree("LABEL", new IRTree(n1)));
        IRTree irt1 = new IRTree("SEQ", br1);
        IRTree irt2 = new IRTree("SEQ", new IRTree("JUMP", new IRTree("NAME", new IRTree(n))));
        IRTree irt3 = new IRTree("SEQ", new IRTree("LABEL", new IRTree(n2)));
        IRTree irt4 = new IRTree("SEQ", br2, new IRTree("LABEL", new IRTree(n)));
        irt.addSub(irt1);
        irt1.addSub(irt2);
        irt2.addSub(irt3);
        irt3.addSub(irt4);
        // CUT THE TREE IF ELSE IS SKIP
        if (irt4.getSub(0).getOp().equals("SKIP")) {
            irt1.removeSub(1);
            irt1.addSub(new IRTree("LABEL", new IRTree(n2)));
        }
        if (irt1.getSub(0).getOp().equals("SKIP")) {
            irt.removeSub(1);
            irt.removeSub(0);
            irt.addSub(irt3.getSub(0));
            irt.addSub(irt3.getSub(1));
            irt4.removeSub(1);
            irt4.addSub(new IRTree("LABEL", new IRTree(n1)));
        }
    }

    public static void translate(CommonTree boolExpr, String n1, String n2, IRTree irt) {
        Token t = boolExpr.getToken();
        IRTree irt1 = new IRTree();
        IRTree irt2 = new IRTree();
        IRTree irt3 = new IRTree();
        CommonTree ast1, ast2;
        int tt = t.getType();
        if (tt == EQ | tt == LEQ) {
            irt.setOp("CJUMP");
            irt.addSub(new IRTree(t.getText()));
            ast1 = (CommonTree) boolExpr.getChild(0);
            ast2 = (CommonTree) boolExpr.getChild(1);
            expression(ast1, irt1);
            expression(ast2, irt2);
            irt.addSub(irt1);
            irt.addSub(irt2);
            irt.addSub(new IRTree("NAME", new IRTree(n1)));
            irt.addSub(new IRTree("NAME", new IRTree(n2)));
        }
        else if (tt == TRUE) {
            irt.setOp("JUMP");
            irt.addSub(new IRTree("NAME", new IRTree(n1)));
        }
        else if (tt == FALSE) {
            irt.setOp("JUMP");
            irt.addSub(new IRTree("NAME", new IRTree(n2)));
        }
        else if (tt == AND) {
            String next = getNewLabel();
            irt.setOp("SEQ");
            irt.addSub(irt1);
            ast1 = (CommonTree) boolExpr.getChild(0);
            ast2 = (CommonTree) boolExpr.getChild(1);
            translate(ast1, next, n2, irt1);
            irt2.setOp("SEQ");
            irt.addSub(irt2);
            irt2.addSub(new IRTree("LABEL", new IRTree(next)));
            irt2.addSub(irt3);
            translate(ast2, n1, n2, irt3);
        }
        else if (tt == OR) {
            String next = getNewLabel();
            irt.setOp("SEQ");
            irt.addSub(irt1);
            ast1 = (CommonTree) boolExpr.getChild(0);
            ast2 = (CommonTree) boolExpr.getChild(1);
            translate(ast1, n1, next, irt1);
            irt2.setOp("SEQ");
            irt.addSub(irt2);
            irt2.addSub(new IRTree("LABEL", new IRTree(next)));
            irt2.addSub(irt3);
            translate(ast2, n1, n2, irt3);
        }
        else if (tt == NOT){
            translate((CommonTree)boolExpr.getChild(0), n2, n1, irt);
        }
    }

    public static void assignRight(CommonTree ast, IRTree irt) {
        expression(ast, irt);
    }

    public static void assignLeft(CommonTree ast, IRTree irt) {
        Token t = ast.getToken();
        int tt = t.getType();
        if ( (tt == ID) && temporaries.contains(t.getText())) {
        	irt.setOp("TEMP");
        	irt.addSub(new IRTree(t.getText()));
        }
        else if (tt == ID) {
            irt.setOp("MEM");
            IRTree irt1 = new IRTree();
            IRTree irt2 = new IRTree();
            IRTree irt3 = new IRTree();
            id(ast, irt1, false);
            if(ast.getChildCount()>0){
                IRTree eseq = new IRTree("ESEQ");
                arrayCheck(ast, irt2);
                arrayAccess(ast, irt3, irt1);
                eseq.addSub(irt2);
                eseq.addSub(irt3);
                irt.addSub(eseq);
            }
            else {
                irt.addSub(irt1);
            }
        }
        else if (tt == REG) {
        	irt.setOp("TEMP");
        	CommonTree ast1 = (CommonTree) ast.getChild(0);
        	Token tok = ast1.getToken();
        	IRTree irt1 = new IRTree(tok.getText());
        	temporaries.add(tok.getText());
        	irt.addSub(irt1);
        }
    }
    
    public static void arrayAccess(CommonTree ast, IRTree irt, IRTree irt1/*id tree*/){
        IRTree irt2 = new IRTree();
        irt.setOp("BINOP");
        expression((CommonTree)ast.getChild(0), irt2);
        irt.addSub(new IRTree("+"));
        irt.addSub(irt1);
        irt.addSub(new IRTree("BINOP",new IRTree("*"),irt2,new IRTree("CONST",new IRTree("4"))));
    }
    
    public static void arrayCheck(CommonTree ast, IRTree irt){
        Token t = ast.getToken();
        IRTree irt2 = new IRTree();
        irt.setOp("SEQ");
        expression((CommonTree)ast.getChild(0), irt2);
        String l1 = getNewLabel();
        String l3 = getNewLabel();
        IRTree n1 = new IRTree("NAME",new IRTree(l1));
        IRTree n2 = new IRTree("NAME",new IRTree("ERROR"));
        IRTree n3 = new IRTree("NAME",new IRTree(l3));
        IRTree left = new IRTree("SEQ");
        IRTree leftleft = new IRTree("CJUMP",new IRTree("<="),irt2,new IRTree("CONST",new IRTree(String.valueOf(Memory.getBoundsOf(t.getText())))),n1,n2);
        IRTree positiveCheck = new IRTree("CJUMP",new IRTree(">="),irt2,new IRTree("CONST",new IRTree("0")),n3,n2);
        IRTree leftright = new IRTree("SEQ",new IRTree("LABEL",new IRTree(l1)),positiveCheck);
        left.addSub(leftleft);
        left.addSub(leftright);
        IRTree right = new IRTree();
        right.setOp("SEQ");
        right.addSub(new IRTree("LABEL",new IRTree(l3)));
        right.addSub(new IRTree("SKIP"));
        irt.addSub(left);
        irt.addSub(right);
        addErrorCode = true;
    }

    public static String arg(CommonTree ast, IRTree irt) {
        Token t = ast.getToken();
        int tt = t.getType();
        if (tt == STRING) {
        	if(!NO_WORD_POOL){
	            IRTree lastPointer = null;
	            String tx = t.getText();
	            String words[] = tx.split("((?<= )|(?= ))");
	            IRTree pointer = irt;
	            for (int i = words.length - 1; i >= 0; i--) {
	                String word = words[i];
	                String addr = String.valueOf(Memory.allocateString(word));
	                IRTree addrNode = new IRTree(addr);
	                IRTree child = new IRTree("WRS", new IRTree("MEM", new IRTree("CONST", addrNode)));
	                stringInstrPointers.add(addrNode);
	                IRTree nextWord = new IRTree("SEQ");
	                if (i == 0)
	                    lastPointer = pointer;
	                pointer.addSub(nextWord);
	                pointer.addSub(child);
	                pointer = nextWord;
	            }
	            pointer.setOp("SKIP");
	            irt.setOp("SEQ"); // this node will be thrown away
	            // REMOVE UNECESSARY SKIP
	            IRTree lastPrint = lastPointer.getSub(1).getSub(0);
	            lastPointer.removeSub(1);
	            lastPointer.removeSub(0);
	            lastPointer.setOp("WRS");
	            lastPointer.addSub(lastPrint);
        	}
            else {
                int a = Memory.allocateString(t.getText());
                String st = String.valueOf(a);
                irt.setOp("WRS");
                IRTree irt2 = new IRTree(st);
                stringInstrPointers.add(irt2);
                IRTree irt1 = new IRTree("MEM",new IRTree("CONST",irt2));
                irt.addSub(irt1);
            }
            return "string";
        } else if (tt == NOT || tt == AND || tt == OR || tt == TRUE || tt == FALSE || tt == EQ || tt == LEQ) {
            irt.setOp("SEQ");
            String n1 = getNewLabel();
            String n2 = getNewLabel();
            String addrTrue = String.valueOf(Memory.allocateString("true"));
            String addrFalse = String.valueOf(Memory.allocateString("false"));
            IRTree trueNode = new IRTree(addrTrue);
            IRTree falseNode = new IRTree(addrFalse);
            stringInstrPointers.add(trueNode);
            stringInstrPointers.add(falseNode);
            IRTree ifTrue = new IRTree("WRS", new IRTree("MEM", new IRTree("CONST",trueNode)));
            IRTree ifFalse = new IRTree("WRS", new IRTree("MEM", new IRTree("CONST",falseNode)));
            IRTree left = new IRTree();
            IRTree right = new IRTree();
            translate(ast, n1, n2, left);
            expandIf(ifTrue, ifFalse, n1, n2, right);
            irt.addSub(left);
            irt.addSub(right);
            return "bool";
        } else {
            expression(ast, irt);
            return "int";
        }
    }

    // Gives irt: CONST|MEM -> constant|(MEM->constant)
    public static void expression(CommonTree ast, IRTree irt) {
        IRTree irt1 = new IRTree();
        IRTree irt2 = new IRTree();
        IRTree irt3 = new IRTree();
        Token t = ast.getToken();
        int tt = t.getType();
        if (tt == INTNUM) {
            constant(ast, irt1);
            irt.setOp("CONST");
            irt.addSub(irt1);
        } else if (tt == ID && temporaries.contains(t.getText())){
        	irt.setOp("TEMP");
        	irt.addSub(new IRTree(t.getText()));
        } else if (tt == ID) {
            id(ast, irt1, true);
            irt.setOp("MEM");
            if(ast.getChildCount()>0){
                IRTree eseq = new IRTree("ESEQ");
                arrayCheck(ast,irt2);
                arrayAccess(ast,irt3,irt1);
                eseq.addSub(irt2);
                eseq.addSub(irt3);
                irt.addSub(eseq);
            }
            else {
                irt.addSub(irt1);
            }
        } else if (tt == ADD || tt == SUB || tt == MUL) {
            arithmetic(ast, irt);
        }
    }

    public static void arithmetic(CommonTree ast, IRTree irt) {
        IRTree irt1 = new IRTree();
        IRTree irt2 = new IRTree();
        Token t = ast.getToken();
        int tt = t.getType();
        if (tt == ADD || tt == SUB || tt == MUL) {
            irt.setOp("BINOP");
            irt.addSub(new IRTree(t.getText()));
            irt.addSub(irt1);
            irt.addSub(irt2);
            expression((CommonTree) ast.getChild(0), irt1);
            expression((CommonTree) ast.getChild(1), irt2);
        }
    }

    // Gives irt: CONST -> <memory address>
    public static void id(CommonTree ast, IRTree irt, boolean right) {
        IRTree irt1 = new IRTree();
        Token t = ast.getToken();
        int tt = t.getType();
        if (tt == ID) {
            String var = t.getText();
            int addr;
            if (right)
                addr = Memory.getVariableAddress(var);
            else
                addr = Memory.allocateVariable(var);
            if (addr == -1)
                error(tt);
            irt.setOp("CONST");
            irt1.setOp(String.valueOf(addr));
            irt.addSub(irt1);
        } else {
            error(tt);
        }
    }

    // Gives irt: <constant>
    public static void constant(CommonTree ast, IRTree irt) {
        Token t = ast.getToken();
        int tt = t.getType();
        if (tt == INTNUM) {
            String tx = t.getText();
            irt.setOp(tx);
        } else {
            error(tt);
        }
    }

    public static String getNewLabel() {
        String result = "N" + labelCounter;
        labelCounter++;
        return result;
    }

    private static void error(int tt) {
        System.out.println("IRT error: " + tokenNames[tt]);
        System.exit(1);
    }
}
