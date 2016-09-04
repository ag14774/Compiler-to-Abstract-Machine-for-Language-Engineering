import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

public class CleanUp {
	
	static boolean deadCodeRemoved = true;

    public static boolean removeDeadCodeOnePass(ArrayList<Instruction> instructions) {
        boolean result = false;
        boolean removeJumpToo = false;
        LabelCleanUp(instructions);
        simpleMoveRemoval(instructions);
        ListIterator<Instruction> it = instructions.listIterator(0);
        while (it.hasNext()) {
            removeJumpToo = false;
            Instruction temp = it.next();
            if (temp.opcode.equals("JMP")) {
                String jumpLabel = temp.op1;
                ListIterator<Instruction> inner = instructions.listIterator(it.nextIndex());
                while (inner.hasNext()) {
                    Instruction check = inner.next();
                    if (check.getLabel().equals("") && !check.opcode.equals("HALT")) {
                        inner.remove();
                        result = true;
                    } else {
                        if (check.getLabel().equals(jumpLabel)) {
                            removeJumpToo = true;
                        }
                        break;
                    }
                }
                if(removeJumpToo){
                    int index = inner.nextIndex()-2;
                    if(index<0)
                        index = 0;
                    it = instructions.listIterator(index);
                    if(it.hasNext()){
                        Instruction temp2 = it.next();
                        if(temp2.opcode.equals("JMP") && temp2.op1.equals(jumpLabel)){
                            it.remove();
                            result = true;
                        }
                    }
                }else{
                    it = inner;
                }
            }
        }
        return result;
    }

    public static void removeDeadCode(ArrayList<Instruction> instructions) {
        while (removeDeadCodeOnePass(instructions));
    }
    
    public static void removeDeadCodeAfterLivenessAnalysis(ArrayList<Instruction> instructions, ArrayList<HashSet<String>> out){
    	ArrayList<Integer> toRemove = new ArrayList<Integer>();
    	for(int i=0;i<instructions.size();i++){
    		Instruction temp = instructions.get(i);
    		if(temp.opUsed==3 && !temp.opcode.equals("STORE") && !out.get(i).contains(temp.getResultRegister())){
    			toRemove.add(i-toRemove.size());
    		}
    	}
    	for(int i:toRemove){
    		instructions.remove(i);
    	}
    	if(toRemove.isEmpty())
    		deadCodeRemoved = false;
    }

    public static void LabelCleanUp(ArrayList<Instruction> instructions) {
        Set<String> labelsUsed = analyseLabelUsage(instructions);
        Iterator<Instruction> it = instructions.iterator();
        while (it.hasNext()) {
            Instruction temp = it.next();
            if (!temp.getLabel().equals("") && !labelsUsed.contains(temp.getLabel())) {
                temp.setLabel("");
                if (temp.opcode.equals(""))
                    it.remove();
            }
        }
    }
    
    public static void simpleMoveRemoval(ArrayList<Instruction> instructions) {
    	Iterator<Instruction> it = instructions.iterator();
    	while(it.hasNext()){
    		Instruction temp = it.next();
    		String opcode = temp.opcode;
    		boolean remove = false;
    		switch(opcode){
    		case "ADD":
    			if(temp.op2.equals("R0")&&temp.op1.equals(temp.op3))
    				remove = true;
    			if(temp.op3.equals("R0")&&temp.op1.equals(temp.op2))
    				remove = true;
    			break;
    		case "SUB":
    			if(temp.op3.equals("R0")&&temp.op1.equals(temp.op2))
    				remove = true;
    			break;
    		case "ADDI":
    			if(temp.op1.equals(temp.op2) && temp.op3.equals("0"))
    				remove = true;
    			break;
    		case "SUBI":
    			if(temp.op1.equals(temp.op2) && temp.op3.equals("0"))
    				remove = true;
    			break;
    		}
    		if(remove)
    			it.remove();
    	}
    }
    
    public static Set<String> analyseLabelUsage(ArrayList<Instruction> instructions) {
        Set<String> labelsUsed = new HashSet<String>();
        for (Instruction in : instructions) {
            switch (in.opcode) {
            case "JMP":
                labelsUsed.add(in.op1);
                break;
            case "IADDR":
                labelsUsed.add(in.op2);
                break;
            case "BGEZ":
                labelsUsed.add(in.op2);
                break;
            case "BGEZR":
                labelsUsed.add(in.op2);
                break;
            case "BLTZ":
                labelsUsed.add(in.op2);
                break;
            case "BLTZR":
                labelsUsed.add(in.op2);
                break;
            case "BEQZ":
                labelsUsed.add(in.op2);
                break;
            case "BEQZR":
                labelsUsed.add(in.op2);
                break;
            case "BNEZ":
                labelsUsed.add(in.op2);
                break;
            case "BNEZR":
                labelsUsed.add(in.op2);
                break;
            }
        }
        return labelsUsed;
    }

}
