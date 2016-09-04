import java.util.*;
import java.io.*;

public class Emit{

    static void dumpAll(ArrayList<Instruction> instructions, PrintStream o){
      for(Instruction instr : instructions){
        o.println(instr.toString());
      }
      Memory.dumpData(o);
    }
}
