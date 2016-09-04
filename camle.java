// COMS22201: Skeleton of CAMLE compiler */

import java.io.*;
import java.util.*;
import java.lang.reflect.Array;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

class camle {
  public static void main(String[] args)
  {
    System.out.println("CAMLE - Compiler to Abstract Machine for Language Engineering");
    String opt = "", inFile = "", outFile = "";
    int pos;
    if (Array.getLength(args) == 1 && args[0].charAt(0) != '-') {
      opt = "";
      inFile = args[0];
    }
    else if (Array.getLength(args) == 2 && args[0].charAt(0) == '-' &&
             args[1].charAt(0) != '-') {
      opt = args[0];
      inFile = args[1];
    }
    else {
      System.out.println("Usage: antlr3 camle [option] filename");
      System.out.println("");
      System.out.println("Options:");
      System.out.println("  -lex");
      System.out.println("  -syn");
      System.out.println("  -hs");
      System.out.println("  -irt");
      System.out.println("  -cg");
      System.out.println("  -cg-O1");
      System.out.println("  -cg-O2");
      System.exit(1);
    }
    outFile = inFile;
    if ((pos = outFile.lastIndexOf('.')) != -1)
      outFile = outFile.substring(0, pos);
    String progname = outFile;
    outFile = outFile+".ass";

    try {
      CharStream cs = new ANTLRFileStream(inFile);			
      Lex lexO = new Lex(cs);
      if (opt.equals("-lex")) {
        Token T;
        T = lexO.nextToken();
        while (T.getType() != -1) {
          System.out.println(T.getType()+" \""+T.getText()+"\"");
          T = lexO.nextToken();
        }
        System.exit(0);
      }
      CommonTokenStream tokens = new CommonTokenStream(lexO);
      Syn synO = new Syn(tokens);
      Syn.program_return parserResult = synO.program();//start rule
      if(synO.getErrorFound())
          System.exit(1);
      CommonTree parserTree = (CommonTree) parserResult.getTree();
      if (opt.equals("-syn")) {
        System.out.println(parserTree.toStringTree());
        System.exit(0);
      }
      if (opt.equals("-hs")) {
    	System.out.println(HaskellParser.convert(parserTree, progname));
    	System.exit(0);
      }
      IRTree newIrt = Irt.convert(parserTree);
      if (opt.equals("-irt")) {
        System.out.println(newIrt);
        Memory.dumpData(System.out);
        System.exit(0);
      }
      ArrayList<Instruction> instructions = Cg.program(newIrt);
      if (opt.equals("-cg")) {
          Emit.dumpAll(instructions,System.out);
          System.exit(0);
      }
      CleanUp.removeDeadCode(instructions);
      if (opt.equals("-cg-O1")) {
        Emit.dumpAll(instructions,System.out);
        System.exit(0);
      }
      RegisterAllocator.reallocateRegistersAndCleanup(instructions);
      if (opt.equals("-cg-O2")) {
          Emit.dumpAll(instructions,System.out);
          System.exit(0);
      }
      PrintStream o = new PrintStream(new FileOutputStream(outFile));
      Emit.dumpAll(instructions, o);
    }
    catch(Exception e) {
      System.err.println("exception: "+e);
      e.printStackTrace();
    }
  }
}
