// COMS22201: Memory allocation for strings

import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

public class Memory {

  static ArrayList<Byte> memoryIntegers = new ArrayList<Byte>();
  static ArrayList<Byte> memoryStrings = new ArrayList<Byte>();
  static HashMap<String,Integer> stringPool = new HashMap<String,Integer>();
  static HashMap<String,Integer> lastIndexOfArray = new HashMap<String,Integer>();

  static public int allocateString(String text)
  { 
    if(stringPool.containsKey(text))
        return stringPool.get(text);
    int addr = memoryStrings.size();
    int size = text.length();
    for (int i=0; i<size; i++) {
      memoryStrings.add(new Byte("", text.charAt(i)));
    }
    memoryStrings.add(new Byte("", 0));
    stringPool.put(text,addr);
    return addr;
  }

  static public int allocateVariable(String varname){
    return allocateVariable(varname, 4);
  }
  
  static public int allocateVariable(String varname, int size){
      int addr = getVariableAddress(varname);
      if(addr!=-1)
          return addr;
      addr = memoryIntegers.size();
      int padding = addr%4;
      for(int i = 0;i<padding;i++){
        System.err.println("MEMORY ERROR DETECTED! ALLOCATING MORE MEMORY!!!");
        Memory.allocateString("");
      }
      addr = memoryIntegers.size();
      for (int i=0; i<size; i++) {
          memoryIntegers.add(new Byte(varname,0));
      }
      return addr;
    }
  
  static public int allocateArray(String arrayName, int size){
      allocateVariable(arrayName, size*4);
      lastIndexOfArray.put(arrayName, size-1);
      return getVariableAddress(arrayName);
  }
  
  static public int getBoundsOf(String arrayName){
      Integer last = lastIndexOfArray.get(arrayName);
      if(last == null)
          return -1;
      return last;
  }

  static int getStringBasePointer(){
    return memoryIntegers.size();
  }

  static public int getVariableAddress(String varname){
    for(int i = 0;i<memoryIntegers.size();i++){
        if(memoryIntegers.get(i).getName().equals(varname))
            return i;
    }
    return -1;
  }

  static public void dumpData(PrintStream o, ArrayList<Byte> array){
    Byte b;
    String s;
    int c;
    int size = array.size();
    for (int i=0; i<size; i++) {
      b = array.get(i);
      c = b.getContents();
      if (c >= 32) {
        s = String.valueOf((char)c);
      }
      else {
        s = ""; // "\\"+String.valueOf(c);
      }
      o.println("DATA "+c+" ; "+s+" "+b.getName());
    }
  }

  static public void dumpData(PrintStream o){
    dumpData(o, memoryIntegers);
    dumpData(o, memoryStrings);
  }

}

class Byte {
  String varname;
  int contents;

  Byte(String n, int c)
  {
    varname = n;
    contents = c;
  }

  String getName()
  {
    return varname;
  }

  int getContents()
  {
    return contents;
  }
}
