// COMS22201: IR tree

import java.util.*;

class IRTree
{
  private String op;
  private ArrayList<IRTree> sub;
  private IRTree parent;
  private IRTree next;

// Constructors for IR tree nodes with various numbers of subtrees

  public IRTree()
  {
    this.op = "";
    next = null;
    parent = null;
    sub = new ArrayList<IRTree>();
  }

  public IRTree(String op)
  {
    this.op = op;
    next = null;
    parent = null;
    sub = new ArrayList<IRTree>();
  }

  public IRTree(String op, IRTree sub1)
  {
    this.op = op;
    next = null;
    parent = null;
    sub = new ArrayList<IRTree>();
    addSub(sub1);
  }

  public IRTree(String op, IRTree sub1, IRTree sub2)
  {
    this.op = op;
    next = null;
    parent = null;
    sub = new ArrayList<IRTree>();
    addSub(sub1);
    addSub(sub2);
  }

  public IRTree(String op, IRTree sub1, IRTree sub2, IRTree sub3)
  {
    this.op = op;
    next = null;
    parent = null;
    sub = new ArrayList<IRTree>();
    addSub(sub1);
    addSub(sub2);
    addSub(sub3);
  }

  public IRTree(String op, IRTree sub1, IRTree sub2, IRTree sub3, IRTree sub4, IRTree sub5)
  {
    this.op = op;
    next = null;
    parent = null;
    sub = new ArrayList<IRTree>();
    addSub(sub1);
    addSub(sub2);
    addSub(sub3);
    addSub(sub4);
    addSub(sub5);
  }
  
// Methods to add operator and subtrees

  public void setOp(String op)
  {
    this.op = op;
  }

  public void addSub(IRTree sub1)
  {
    sub.add(sub1);
    sub1.parent = this;
    if(op.equals("SEQ")){
        if(sub.size()==2){
            sub.get(0).next = sub.get(1);
            sub.get(1).next = null;
        }
    }
  }
  
  public ArrayList<IRTree> getChildren(){
      return sub;
  }
  
  public void removeChildren(){
	  if(sub.size()>=1)
		  sub.get(0).next=null;
	  if(sub.size()>=2)
		  sub.get(1).next=null;
      sub.clear();
  }
  
  public void addSub(IRTree sub1, int i)
  {
    sub.add(i, sub1);
    sub1.parent = this;
    if(op.equals("SEQ")){
        if(sub.size()==2){
            sub.get(0).next = sub.get(1);
            sub.get(1).next = null;
        }
    }
  }
  
  public IRTree getParent(){
      return parent;
  }

// Methods to access operator and subtrees

  public String getOp()
  {
    return op;
  }
  
  public void removeSub(int i){
	  if(sub.size()>=1){
		  sub.get(0).next=null;
	  }
	  if(sub.size()>=2){
		  sub.get(1).next=null;
	  }
      sub.remove(i);
  }

  public IRTree getSub(int i)
  {
    if (i >= sub.size()) {
      return null;
    }
    return sub.get(i);
  }
  
  public IRTree findNextInstruction(){
      IRTree next = this;
      IRTree temp;
      while(true){
          temp = next;
          next = next.next;
          if(next == null){
              next = temp.parent;
              if(next==null){
                  return null;
              }
          }
          else if(next.getOp().equals("SEQ")){
              while(next.getSub(0).getOp().equals("SEQ")){
                  next = next.getSub(0);
              }
              return next.getSub(0);
          }
          else {
              return next;
          }
      }
  }

// toString

  public String toString()
  {
    int i;
    if (sub.size() == 0) {
      return op;
    }
    String s = "("+op;
    for (i=0; i<sub.size(); i++) {
      s += " "+sub.get(i).toString();
    }
    s += ")";
    return s;
  }
}
