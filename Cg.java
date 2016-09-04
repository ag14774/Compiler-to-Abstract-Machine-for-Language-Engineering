// COMS22201: Code generation

import java.util.*;

public class Cg {
    static int                    registerCount = 1;
    static ArrayList<Instruction> instructions  = new ArrayList<Instruction>();
    static Map<String,String>     temporaries   = new HashMap<String,String>();

    public static ArrayList<Instruction> program(IRTree irt) {
        clearRegister("R0"); // Initialize R0 to 0
        statement(irt);
        clearRegister("R0");
        halt(); // Program must end with HALT
        return instructions;
        // Memory.dumpData(o); // Dump DATA lines: initial memory contents
    }

    private static void clearRegister(String register) {
        emit("XOR " + register + "," + register + "," + register);
    }

    private static void halt() {
        emit("HALT");
    }

    private static void statement(IRTree irt) {
        if (irt.getOp().equals("SEQ")) {
            statement(irt.getSub(0));
            statement(irt.getSub(1));
        } else if (irt.getOp().equals("WRS") && irt.getSub(0).getOp().equals("MEM") && irt.getSub(0).getSub(0).getOp().equals("CONST")) {
            String a = irt.getSub(0).getSub(0).getSub(0).getOp();
            emit("WRS " + a);
        } else if (irt.getOp().equals("WR")) {
            String e = expression(irt.getSub(0), false, null);
            emit("WR " + e);
        } else if (irt.getOp().equals("MOVE")) {
            String e;
            String offset;
            if(irt.getSub(0).getOp().equals("TEMP")){
            	offset = irt.getSub(0).getSub(0).getOp();
            	String reg;
            	if(temporaries.containsKey(offset))
            		reg = temporaries.get(offset);
            	else{
            		reg = getNewRegister();
            		temporaries.put(offset, reg);
            	}
            	e = rightMove(irt.getSub(1), reg);
            	if(e.startsWith("R"))
            		emit("ADDI " + reg + "," + e + ",0");
            	else
            		emit("ADDI " + reg + ",R0," + e);
            }
            else {
            	String reg = getNewRegister();
            	e = rightMove(irt.getSub(1), reg);
            	if(!e.startsWith("R")){
            		emit("ADDI " + reg + ",R0," + e);
            		e = reg;
            	}
	            offset = expression(irt.getSub(0).getSub(0),true, null);
	            if(offset.startsWith("R")){
	                emit("STORE " + e + "," + offset + ",0");
	            }
	            else{
	                emit("STORE " + e + ",R0," + offset);
	            }
            }
        } else if (irt.getOp().equals("CJUMP")) {
            String instr = irt.getSub(0).getOp();
            // TRY EVALUATE EXPRESSIONS AT COMPILATION TIME
            String e1 = expression(irt.getSub(1), true, null);
            String e2 = expression(irt.getSub(2), true, null);

            if (!e1.startsWith("R") && !e2.startsWith("R")) {
                boolean result = evaluateTrivialCJUMP(instr, e1, e2);
                String n1 = irt.getSub(3).getSub(0).getOp();
                String n2 = irt.getSub(4).getSub(0).getOp();
                if (result) {
                    boolean isLabelNext = followedByLabel(irt, n1);
                    if(!isLabelNext){
                        emit("JMP " + n1);
                    }
                } else {
                    boolean isLabelNext = followedByLabel(irt, n2);
                    if(!isLabelNext){
                        emit("JMP " + n2);
                    }
                }
            } else {
                boolean labelNext = followedByLabel(irt);
                if (labelNext) {
                    boolean falseNext = followedByFalseLabel(irt);
                    if (!falseNext)
                        instr = reverseOp(instr);
                    String expr = subtractCJUMP(e1, e2, instr);
                    emitConditional(irt, expr, instr, !falseNext);
                } else {
                    String expr = subtractCJUMP(e1, e2, instr);
                    emitConditional(irt, expr, instr, false);
                    String falseLabel = irt.getSub(4).getSub(0).getOp();
                    emit("JMP " + falseLabel);
                }
            }
        } else if (irt.getOp().equals("LABEL")) {
            String label = irt.getSub(0).getOp();
            emit(label + ": ");
        } else if (irt.getOp().equals("JUMP")) {
            boolean labelFollows = followedByLabel(irt);
            if (!labelFollows) {
                String label = irt.getSub(0).getSub(0).getOp();
                emit("JMP " + label);
            }
        } else if (irt.getOp().equals("SKIP")) {
            
        } else if (irt.getOp().equals("HALT")){
            halt();
        } else {
            error(irt.getOp());
        }
    }

    private static void emitConditional(IRTree irt, String exprRegister, String op, boolean useFalse) {
        String label;
        if (useFalse)
            label = irt.getSub(4).getSub(0).getOp();
        else
            label = irt.getSub(3).getSub(0).getOp();
        switch (op) {
        case "<=":
            emit("BGEZ " + exprRegister + "," + label);
            break;
        case "<":
            emit("BLTZ " + exprRegister + "," + label);
            break;
        case ">=":
            emit("BGEZ " + exprRegister + "," + label);
            break;
        case ">":
            emit("BLTZ " + exprRegister + "," + label);
            break;
        case "=":
            emit("BEQZ " + exprRegister + "," + label);
            break;
        case "!=":
            emit("BNEZ " + exprRegister + "," + label);
            break;
        }
    }

    private static boolean evaluateTrivialCJUMP(String instr, String e1, String e2) {
        boolean result = false;
        switch (instr) {
        case "<=":
            if (Integer.parseInt(e1) <= Integer.parseInt(e2))
                result = true;
            break;
        case "<":
            if (Integer.parseInt(e1) < Integer.parseInt(e2))
                result = true;
            break;
        case ">=":
            if (Integer.parseInt(e1) >= Integer.parseInt(e2))
                result = true;
            break;
        case ">":
            if (Integer.parseInt(e1) > Integer.parseInt(e2))
                result = true;
            break;
        case "=":
            if (Integer.parseInt(e1) == Integer.parseInt(e2))
                result = true;
            break;
        case "!=":
            if (Integer.parseInt(e1) != Integer.parseInt(e2))
                result = true;
            break;
        }
        return result;
    }

    private static String reverseOp(String op) {
        switch (op) {
        case "<=":
            return ">";
        case "<":
            return ">=";
        case ">=":
            return "<";
        case ">":
            return "<=";
        case "=":
            return "!=";
        case "!=":
            return "=";
        default:
            return op;
        }
    }

    private static boolean followedByLabel(IRTree irt) {
        if (irt.getOp().equals("CJUMP")) {
            String label1 = irt.getSub(3).getSub(0).getOp();
            String label2 = irt.getSub(4).getSub(0).getOp();
            IRTree next = irt.findNextInstruction();
            if (next != null && next.getOp().equals("LABEL")) {
                if (next.getSub(0).getOp().equals(label1) || next.getSub(0).getOp().equals(label2))
                    return true;
            }
        } else if (irt.getOp().equals("JUMP")) {
            String label = irt.getSub(0).getSub(0).getOp();
            IRTree next = irt.findNextInstruction();
            if (next != null && next.getOp().equals("LABEL")) {
                if (next.getSub(0).getOp().equals(label))
                    return true;
            }
        }
        return false;
    }

    private static boolean followedByLabel(IRTree irt, String label) {
        IRTree next = irt.findNextInstruction();
        if (next != null && next.getOp().equals("LABEL")) {
            if (next.getSub(0).getOp().equals(label))
                return true;
        }
        return false;
    }

    private static boolean followedByFalseLabel(IRTree irt) {
        if (irt.getOp().equals("CJUMP")) {
            String label1 = irt.getSub(4).getSub(0).getOp();
            IRTree next = irt.findNextInstruction();
            if (next != null && next.getOp().equals("LABEL")) {
                if (next.getSub(0).getOp().equals(label1))
                    return true;
            }
        }
        return false;
    }

    private static String subtractCJUMP(String e1, String e2, String instr) {
        String result = "";
        result = getNewRegister();
        if (instr.equals(">=") || instr.equals("<") || instr.equals("=") || instr.equals("!=")) {
        	if(!e1.startsWith("R")){
        		String tmp = e1;
        		e1 = getNewRegister();
        		emit("ADDI "+e1+",R0,"+tmp);
        	}
        	if(e2.startsWith("R"))
        		emit("SUB " + result + "," + e1 + "," + e2);
        	else
        		emit("SUBI " + result + "," + e1 + "," + e2);
        }
        else {
        	if(!e2.startsWith("R")){
        		String tmp = e2;
        		e2 = getNewRegister();
        		emit("ADDI "+e2+",R0,"+tmp);
        	}
        	if(e1.startsWith("R"))
        		emit("SUB " + result + "," + e2 + "," + e1);
        	else
        		emit("SUBI " + result + "," + e2 + "," + e1);
        }
        return result;
    }

    private static String rightMove(IRTree irt, String register) {
        if (irt.getOp().equals("READ")) {
            return read(irt, register);
        } else {
            return expression(irt, true, register);
        }
    }

    private static String read(IRTree irt, String register) {
        if (irt.getOp().equals("READ")) {
        	if(register == null)
        		register = getNewRegister();
            emit("RD " + register);
        }
        return register;
    }

    private static String expression(IRTree irt, boolean returnValue, String result) {
//        String result = "";
        if (irt.getOp().equals("CONST")) {
            String t = irt.getSub(0).getOp();
            if (returnValue)
                result = t;
            else {
            	if(result==null)
            		result = getNewRegister();
                emit("ADDI " + result + ",R0," + t);
            }
        } else if (irt.getOp().equals("TEMP")) {
        	result = temporaries.get(irt.getSub(0).getOp());
        } else if (irt.getOp().equals("MEM")) {
            String offset = expression(irt.getSub(0), true, null);
            if(result==null)
            	result = getNewRegister();
            if (offset.startsWith("R")) {
                emit("LOAD " + result + "," + offset + ",0");
            } else {
                emit("LOAD " + result + ",R0," + offset);
            }
        } else if (irt.getOp().equals("BINOP")) {
            result = arithmetic(irt, result);
            if (!result.startsWith("R") && !returnValue) {
                String tmp = result;
                result = getNewRegister();
                emit("ADDI " + result + ",R0," + tmp);
            }
        } else if (irt.getOp().equals("ESEQ")){
            statement(irt.getSub(0));
            result = expression(irt.getSub(1),false, result);
        } else {
            error(irt.getOp());
        }
        return result;
    }

    private static String arithmetic(IRTree irt, String result) {
//        String result = "";
        if (irt.getOp().equals("BINOP")) {
            String op = irt.getSub(0).getOp();
            String instr = "";
            if (op.equals("+")) {
                instr = "ADD";
            } else if (op.equals("-")) {
                instr = "SUB";
            } else if (op.equals("*")) {
                instr = "MUL";
            }
            String res1 = expression(irt.getSub(1), true, null);
            String res2 = expression(irt.getSub(2), true, null);
            
            if(res1.startsWith("R") && !res2.startsWith("R")){
            	if(result==null)
            		result = getNewRegister();
            	emit(instr + "I" + " " + result + "," + res1 + "," + res2);
            }
            else if(!res1.startsWith("R") && res2.startsWith("R")){
            	if(result==null)
            		result = getNewRegister();
            	emit(instr + "I" + " " + result + "," + res2 + "," + res1);
            	if(instr.equals("SUB"))
            		emit("SUB" + " " + result + ",R0," + result);
            }
            else if (!res1.startsWith("R") && !res2.startsWith("R")) {
                if (instr.equals("ADD"))
                    result = String.valueOf(Integer.parseInt(res1) + Integer.parseInt(res2));
                else if (instr.equals("SUB"))
                    result = String.valueOf(Integer.parseInt(res1) - Integer.parseInt(res2));
                else if (instr.equals("MUL"))
                    result = String.valueOf(Integer.parseInt(res1) * Integer.parseInt(res2));
            } else {
            	if(result==null)
            		result = getNewRegister();
                emit(instr + " " + result + "," + res1 + "," + res2);
            }
        } else {
            error(irt.getOp());
        }
        return result;
    }

    private static void emit(String s) {
        instructions.add(Instruction.toInstruction(s));
    }

    private static void error(String op) {
        System.out.println("CG error: " + op);
        System.exit(1);
    }

    private static String getNewRegister() {
        String res = "R" + registerCount;
        registerCount++;
        return res;
    }

}
