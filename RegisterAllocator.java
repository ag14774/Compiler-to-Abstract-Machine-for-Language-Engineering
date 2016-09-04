import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class RegisterAllocator {
	static ArrayList<HashSet<String>> in = new ArrayList<HashSet<String>>();
	static ArrayList<HashSet<String>> out = new ArrayList<HashSet<String>>();
	static ArrayList<HashSet<String>> in2 = new ArrayList<HashSet<String>>();
    static ArrayList<HashSet<String>> out2 = new ArrayList<HashSet<String>>();
    static Map<String,Integer> labels = new HashMap<String,Integer>();
	
	static Map<String,Set<String>> graph = new HashMap<String,Set<String>>();
	
	static Map<String,Integer> colour = new HashMap<String,Integer>();
	
	static int predictedMinRegisters = 0;
	
	static void addEdgeOnce(String a, String b){
	    if(!graph.containsKey(a)){
	        Set<String> newSet = new HashSet<String>();
	        graph.put(a, newSet);
	    }
	    Set<String> n = graph.get(a);
	    n.add(b);
	}
	
	static void addEdge(String a, String b){
	    addEdgeOnce(a,b);
	    addEdgeOnce(b,a);
	}
	
	static Set<String> removeNode(String v){
	    Set<String> neighbours = new HashSet<String>(graph.get(v));
	    graph.remove(v);
	    for(String x : neighbours){
	        graph.get(x).remove(v);
	    }
	    return neighbours;
	}
	
	static int degree(String v){
	    Set<String> neighbours = graph.get(v);
	    if(neighbours==null)
	        return 0;
	    return neighbours.size();
	}
	
	static void addNodeWithNeighbours(String v, Set<String> neighbours){
        for(String x : neighbours){
            addEdge(x , v);
        }
    }
	
	static String findNodeWithDegreeAtMost(int k){
	    for(String key : graph.keySet()){
	        if(degree(key)<k)
	            return key;
	    }
	    int size = graph.keySet().size();
	    int item = new Random().nextInt(size);
	    int i = 0;
	    for(String ran : graph.keySet()){
	        if(i==item)
	            return ran;
	        i++;
	    }
	    return null;
	}
	
	static int initializeSets(ArrayList<Instruction> instructions){
		in = new ArrayList<HashSet<String>>();
		out = new ArrayList<HashSet<String>>();
		in2 = new ArrayList<HashSet<String>>();
	    out2 = new ArrayList<HashSet<String>>();
		int n = instructions.size();
		for(int i = 0;i<n;i++){
			in.add(new HashSet<String>());
			out.add(new HashSet<String>());
			in2.add(new HashSet<String>());
            out2.add(new HashSet<String>());
            Instruction temp = instructions.get(i);
            if(!temp.getLabel().equals("")){
            	int instPointer = i;
            	for(int j=i;j<n;j++){
            		Instruction temp2 = instructions.get(j);
            		if(!temp2.opcode.equals("")){
            			instPointer = j;
            			break;
            		}
            	}
                labels.put(temp.getLabel(), instPointer);
            }
		}
		return n;
	}
	
	static Set<Integer> colours_not_used(String v, int k){
	    Set<Integer> notUsed = new HashSet<Integer>();
	    for(int i = 1;i<=k;i++)
	        notUsed.add(i);
	    Set<String> neighbours = graph.get(v);
	    if(neighbours!=null){
    	    for(String n : neighbours){
    	        notUsed.remove(colour.get(n));
    	    }
	    }
	    return notUsed;
	}
	
	static int select_random_color(Set<Integer> colors){
	    int size = colors.size();
        int item = new Random().nextInt(size);
        int i = 0;
	    for(int c : colors){
            if(i==item)
                return c;
            i++;
        }
        return -1;
	}
	
	static void colour_graph(int k){
	    if(!graph.isEmpty()){
	        String v= findNodeWithDegreeAtMost(k);
	        Set<String> neighbours = removeNode(v);
	        colour_graph(k);
	        addNodeWithNeighbours(v, neighbours);
	        Set<Integer> unusedColours = colours_not_used(v, k);
	        int c = select_random_color(unusedColours);
	        colour.put(v, c);
	    }
	}
	
	static void print_colours(){
	    for(Map.Entry<String, Integer> entry : colour.entrySet()){
	        System.out.println(entry.getKey()+"-->"+entry.getValue());
	    }
	}
	
	static void createGraph(ArrayList<Instruction> instructions){
		for(int i=0;i<instructions.size();i++){
		    Instruction s = instructions.get(i);
		    String x = s.getResultRegister();
		    if(!x.equals("")){
		        Set<String> outSet = out.get(i);
    		    if(!s.isMove()){
    		        for(String v : outSet){
    		            if(!v.equals("") && !v.equals(x)){
    		                addEdge(x,v);
    		            }
    		        }
    		    }
    		    else{
                    for(String v : outSet){
                        if(!v.equals("") && !v.equals(s.getException()) && !v.equals(x)){
                            addEdge(x,v);
                        }
                    }
    		    }
		    }
		}
	}
	
	static void printGraph(){
	    for(Map.Entry<String,Set<String>> entry : graph.entrySet()){
	        System.out.println(entry.getKey()+"  "+((HashSet<String>)entry.getValue()));
	    }
	}
	
	static boolean conditionCheck(){
	    for(int i = 0;i<in.size();i++){
	        if(!in.get(i).equals(in2.get(i)))
	            return false;
	        if(!out.get(i).equals(out2.get(i)))
                return false;
	    }
	    return true;
	}
	
	static void reallocateRegisters(int k){
	    try{
	        colour_graph(k);
	        if(colour.get("R0")!=null && colour.get("R0")!=1){
	            Integer temp = colour.get("R0");
	            for(String key : colour.keySet()){
	                if(colour.get(key)==1){
	                    colour.put(key, temp);
	                }
	            }
	            colour.put("R0", 1);
	        }
	    }
	    catch(Exception e){
	    	System.err.println("Cannot colour graph with "+k+" colours! Retrying with one more colour!");
	        reallocateRegisters(k+1);
	    }
	}
	
	static void reallocateRegistersAndCleanup(ArrayList<Instruction> instructions){
	    RegisterAllocator.livenessAnalysis(instructions);
	    RegisterAllocator.createGraph(instructions);
//	    RegisterAllocator.printGraph();
	    RegisterAllocator.reallocateRegisters(predictedMinRegisters);
//	    RegisterAllocator.print_colours();
	    for(Instruction i : instructions){
	    	int ops = 0;
	        if(!i.op1.equals("")){
	            Integer c = colour.get(i.op1);
	            if(c!=null){
	                i.op1 = "R"+(c-1);
	            }
	            ops++;
	        }
	        if(!i.op2.equals("")){
	            Integer c = colour.get(i.op2);
                if(c!=null){
                    i.op2 = "R"+(c-1);
                }
                ops++;
            }
	        if(!i.op3.equals("")){
	            Integer c = colour.get(i.op3);
                if(c!=null){
                    i.op3 = "R"+(c-1);
                }
                ops++;
            }
	        switch(ops){
	        case 0:
	        	i.initialize(i.opcode);
	        	break;
	        case 1:
	        	i.initialize(i.opcode,i.op1);
	        	break;
	        case 2:
	        	i.initialize(i.opcode,i.op1,i.op2);
	        	break;
	        case 3:
	        	i.initialize(i.opcode,i.op1,i.op2,i.op3);
	        	break;
	        }
	    }
	    CleanUp.simpleMoveRemoval(instructions);
	    while(CleanUp.deadCodeRemoved){
	    	RegisterAllocator.livenessAnalysis(instructions);
	    	CleanUp.removeDeadCodeAfterLivenessAnalysis(instructions, out);
	    }
	}
	
	static Set<Integer> succ(int index, ArrayList<Instruction> instructions){
	    Set<Integer> result = new HashSet<Integer>();
	    Instruction temp = instructions.get(index);
	    switch(temp.opcode){
	    case "JMP":
	        result.add(labels.get(temp.op1));
	        break;
	    case "BGEZ":
	        result.add(labels.get(temp.op2));
	        result.add(index+1);
            break;
	    case "BGEZR":
	        result.add(labels.get(temp.op2));
	        result.add(index+1);
            break;
	    case "BLTZ":
	        result.add(labels.get(temp.op2));
	        result.add(index+1);
            break;
	    case "BLTZR":
	        result.add(labels.get(temp.op2));
	        result.add(index+1);
            break;
	    case "BEQZ":
	        result.add(labels.get(temp.op2));
	        result.add(index+1);
            break;
	    case "BEQZR":
	        result.add(labels.get(temp.op2));
	        result.add(index+1);
            break;
	    case "BNEZ":
	        result.add(labels.get(temp.op2));
	        result.add(index+1);
            break;
	    case "BNEZR":
	        result.add(labels.get(temp.op2));
	        result.add(index+1);
            break;
	    case "HALT":
	        break;
        default:
            result.add(index+1);
            break; 
	    }
	    return result;
	}
	
	static void livenessAnalysis(ArrayList<Instruction> instructions){
		int n = initializeSets(instructions);
		//initialize out(last) here.
		do {
    		for(int i = n-1 ; i>=0 ; i--){
    			Instruction cur = instructions.get(i);
    			in2.get(i).clear();
    			in2.get(i).addAll(in.get(i));
    			out2.get(i).clear();
    			out2.get(i).addAll(out.get(i));
    			for(int j : succ(i,instructions)){
    			    out.get(i).addAll(in.get(j));
    			}
    			in.get(i).addAll(cur.use());
    			Set<String> temp = new HashSet<String>();
    			temp.addAll(out.get(i));
    			temp.removeAll(cur.def());
    			in.get(i).addAll(temp);
    		}
		} while(!conditionCheck());
		
		for(int i = 0;i<n;i++){
//			System.out.println(instructions.get(i) + " : "+in.get(i)+"  "+out.get(i));
		    if(predictedMinRegisters<in.get(i).size())
		        predictedMinRegisters=in.get(i).size();
		    if(predictedMinRegisters<out.get(i).size())
                predictedMinRegisters=out.get(i).size();
		}
		
	}
	
}
