import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Instruction {

    String label;
    String opcode;
    String op1;
    String op2;
    String op3;
    int opUsed = 0;
    
    private Set<String> use;
    private Set<String> def;
    
    private boolean isMove;
    private String resultRegister;
    private String except;
    
    private int category;

    static final String cat1[] = {"ADD","SUB","MUL","DIV","XOR","ADDR","SUBR","MULR","DIVR",
    							  "ADDI","SUBI","MULI","DIVI","XORI","MOVIR","ITOR","RTOI",
    							  "IADDR","LOAD",};
    static final String cat2[] = {"RD","RDR","WR","WRR","STORE","JUMP"};
    static final String cat3[] = {"BGEZ","BGEZR","BLTZ","BLTZR","BEQZ","BEQZR","BNEZ","BNEZR"};
    
    public Instruction(String opcode){
    	initialize(opcode);
    }

    public Instruction(String opcode, String op1){
    	initialize(opcode,op1);
    }

    public Instruction(String opcode, String op1, String op2){
    	initialize(opcode,op1,op2);
    }

    public Instruction(String opcode, String op1, String op2, String op3){
    	initialize(opcode,op1,op2,op3);
    }
    
    public void initialize(String opcode){
    	isMove = false;
        resultRegister = "";
        except = "";
        this.opcode = opcode;
        op1 = "";
        op2 = "";
        op3 = "";
        opUsed = 0;
        use = new HashSet<String>();
        def = new HashSet<String>();
        if(Arrays.asList(cat1).contains(opcode))
        	category = 1;
        else if(Arrays.asList(cat2).contains(opcode))
        	category = 2;
        else if(Arrays.asList(cat3).contains(opcode))
        	category = 3;
        else
        	category = 4;
    }
    
    public void initialize(String opcode, String op1){
    	initialize(opcode);
        this.op1 = op1;
        op2 = "";
        op3 = "";
        opUsed = 1;
        if(category==2){
        	if(!opcode.equals("JUMP") && !opcode.equals("WR") && !opcode.equals("WRR")){
                resultRegister = op1;
                def.add(op1);
        	}
        	else {
        		use.add(op1);
        	}
        }
    }
    
    public void initialize(String opcode, String op1, String op2){
    	initialize(opcode);
        this.op1 = op1;
        this.op2 = op2;
        op3 = "";
        opUsed = 2;
        if(category==1){
            resultRegister = op1;
        	def.add(op1);
        	if(opcode.equals("ITOR")||opcode.equals("RTOI"))
        		use.add(op2);
        }
        if(category==3){
        	use.add(op1);
        }
    }
    
    public void initialize(String opcode, String op1, String op2, String op3){
    	initialize(opcode);
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        opUsed = 3;
        if(category==1){
            resultRegister = op1;
        	def.add(op1);
        	use.add(op2);
        	if(!opcode.endsWith("I") && !opcode.equals("LOAD"))
        		use.add(op3);
        }
        if(category==2){
        	use.add(op1);
        	use.add(op2);
        }
        switch(opcode){
        case "ADD":
            if(op2.equals("R0")){
                except=op3;
                isMove = true;
            }
            if(op3.equals("R0")){
                except=op2;
                isMove = true;
            }
            break;
        case "SUB":
            if(op3.equals("R0")){
                except = op2;
                isMove = true;
            }
            break;
        case "XOR":
            if(op2.equals("R0") ^ op3.equals("R0")){
                if(op2.equals("R0"))
                    except = op3;
                if(op3.equals("R0"))
                    except = op2;
                isMove = true;
            }
            break;
        case "ADDI":
            if(op3.equals("0") && !op2.equals("R0")){
                except = op2;
                isMove = true;
            }
            break;
        case "SUBI":
            if(op3.equals("0")){
                except = op2;
                isMove = true;
            }
            break;
        case "MULI":
            if(op3.equals("1")){
                except = op2;
                isMove = true;
            }
            break;
        case "DIVI":
            if(op3.equals("1")){
                except = op2;
                isMove = true;
            }
            break;
        case "XORI":
            if(op3.equals("0")){
                except = op2;
                isMove = true;
            }
            break;
        }
    }

    public void setLabel(String label){
        this.label = label;
    }

    public String getLabel(){
        return label;
    }
    
    public boolean isMove(){
        return isMove;
    }
    
    public String getResultRegister(){
        return resultRegister;
    }
    
    public String getException(){
        return except;
    }
    
    public Set<String> use(){
    	return use;
    }
    
    public Set<String> def(){
    	return def;
    }

    public static Instruction toInstruction(String line){
        String o[] = line.split(":");
        String label = extractLabel(o);
        int start = 1;
        if(label.equals(""))
            start--;
        String rest[] = o[start].trim().split(" |,|;");
        String opcode = rest[0];
        Instruction i;
        if(rest.length>=4)
        	i = new Instruction(opcode,rest[1],rest[2],rest[3]);
        else if(rest.length>=3)
        	i = new Instruction(opcode,rest[1],rest[2]);
        else if(rest.length>=2)
        	i = new Instruction(opcode,rest[1]);
        else
        	i = new Instruction(opcode);
        i.setLabel(label);
        return i;
    }

    static String extractLabel(String line[]) {
        if(line.length==1)
            return "";
        return line[0];
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String label="";
		String opcode="";
		String op1="";
		String op2="";
		String op3="";
		if(!this.label.equals(""))
			label = this.label + ":";
		opcode = this.opcode;
		if(!this.op1.equals(""))
			op1 = " " + this.op1;
		if(!this.op2.equals(""))
			op2 = " " + this.op2;
		if(!this.op3.equals(""))
			op3 = " " + this.op3;
		return label + opcode + op1 + op2 + op3;
	}
    
    

}
