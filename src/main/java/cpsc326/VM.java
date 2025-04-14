/**
 * CPSC 326, Spring 2025
 * The virtual machine implementation.
 */

package cpsc326;

import java.util.List;
import java.util.ArrayList;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Struct;

/**
 * MyPL virtual machine for running MyPL programs (as VM
 * instructions).
 */
public class VM {

  /* special NULL value */
  public static final Object NULL = new Object() {
      public String toString() {return "null";}
    };
  
  /* the array heap as an oid to list mapping */
  private Map<Integer,List<Object>> arrayHeap = new HashMap<>();

  /* the struct heap as an oid to object (field to value map) mapping */
  private Map<Integer,Map<String,Object>> structHeap = new HashMap<>();

  /* the operand stack */
  private Deque<Object> operandStack = new ArrayDeque<>();

  /* the function (frame) call stack */
  private Deque<VMFrame> callStack = new ArrayDeque<>();

  /* the set of program function definitions (frame templates) */
  private Map<String,VMFrameTemplate> templates = new HashMap<>();

  /* the next unused object id */
  private int nextObjectId = 2025;

  /* debug flag for output debug info during vm execution (run) */
  private boolean debug = false;

  
  // helper functions

  /**
   * Create and throw an error.
   * @param msg The error message.
   */
  private void error(String msg) {
    MyPLException.vmError(msg);
  }

  /**
   * Create and throw an error (for a specific frame).
   * @param msg The error message.
   * @param frame The frame where the error occurred.
   */
  private void error(String msg, VMFrame frame) {
    String s = "%s in %s at %d: %s";
    String name = frame.template.functionName;
    int pc = frame.pc - 1;
    VMInstr instr = frame.template.instructions.get(pc);
    MyPLException.vmError(String.format(s, msg, name, pc, instr));
  }

  /**
   * Add a frame template to the VM.
   * @param template The template to add.
   */
  public void add(VMFrameTemplate template) {
    templates.put(template.functionName, template);
  }

  /**
   * For turning on debug mode to help with debugging the VM.
   * @param on Set to true to turn on debugging, false to turn it off.
   */
  public void debugMode(boolean on) {
    debug = on;
  }

  /**
   * Pretty-print the VM frames.
   */
  public String toString() {
    String s = "";
    for (var funName : templates.keySet()) {
      s += String.format("\nFrame '%s'\n", funName);
      VMFrameTemplate template = templates.get(funName);
      for (int i = 0; i < template.instructions.size(); ++i) 
        s += String.format("  %d: %s\n", i, template.instructions.get(i));
    }
    return s;
  }

  // Additional helpers for implementing the VM instructions
  
  /**
   * Helper to ensure the given value isn't NULL
   * @param x the value to check
   * @param frame the current stack frame
   */
  private void ensureNotNull(Object x, VMFrame frame) {
    if (x == NULL)
      error("null value error", frame);
  }
  
  /**
   * Helper to add two objects
   */
  private Object addHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int)x + (int)y;
    else if (x instanceof Double)
      return (double)x + (double)y;
    else
      return (String)x + (String)y;
  }

  /**
   * Helper to subtract two objects
   */
  private Object subHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int)x - (int)y;
    else
      return (double)x - (double)y;   
  }
  
  /**
   * Helper to multiply two objects
   */
  private Object mulHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int)x * (int)y;
    else
      return (double)x * (double)y;
  }

  /**
   * Helper to divide two objects
   */
  private Object divHelper(Object x, Object y, VMFrame f) {
    if (x instanceof Integer && (int)y != 0) 
      return (int)((int)x / (int)y);
    else if (x instanceof Double && (double)y != 0.0) 
      return (double)x / (double)y;
    else
      error("division by zero error", f);
    return null;
  }

  /**
   * Helper to compare if first object less than second
   */
  private Object cmpltHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int)x < (int)y;
    else if (x instanceof Double)
      return (double)x < (double)y;
    else
      return ((String)x).compareTo((String)y) < 0;
  }

  /**
   * Helper to compare if first object less than or equal second
   */
  private Object cmpleHelper(Object x, Object y) {
    if (x instanceof Integer)
      return (int)x <= (int)y;
    else if (x instanceof Double)
      return (double)x <= (double)y;
    else
      return ((String)x).compareTo((String)y) <= 0;
  }
  
  // the main run method

  /**
   * Execute the program
   */
  public void run() {
    // grab the main frame and "instantiate" it
    if (!templates.containsKey("main"))
      error("No 'main' function");
    VMFrame frame = new VMFrame(templates.get("main"));
    callStack.push(frame);

    // run loop until out of call frames or instructions in the frame
    while (!callStack.isEmpty() && frame.pc < frame.template.instructions.size()) {
      // get the next instruction
      VMInstr instr = frame.template.instructions.get(frame.pc);

      // for debugging:
      if (debug) {
        System.out.println();
        System.out.println("\t FRAME.........: " + frame.template.functionName);
        System.out.println("\t PC............: " + frame.pc);
        System.out.println("\t INSTRUCTION...: " + instr);
        Object val = operandStack.isEmpty() ? null : operandStack.peek();
        System.out.println("\t NEXT OPERAND..: " + val);
      }

      // increment the pc
      ++frame.pc;

      //how get operand?
      //instr.operand

      //----------------------------------------------------------------------
      // Literals and Variables
      //----------------------------------------------------------------------
      // PUSH,    // push operand A
      if (instr.opcode == OpCode.PUSH) {
        operandStack.push(instr.operand);
      }
      // POP,     // pop x
      else if (instr.opcode == OpCode.POP) {
        operandStack.pop();
      }
      // LOAD,    // push value at memory address (operand) A 
      else if (instr.opcode == OpCode.LOAD) {
        operandStack.push(frame.memory.get((int)instr.operand));
      }
      // STORE,   // pop x, store x at memory address (operand) A
      else if (instr.opcode == OpCode.STORE) {
        //pop x
        if (operandStack.isEmpty()) {
          String msg = "No operand passed into store";
          error(msg);
        }
        Object x = operandStack.pop();
        Object a = instr.operand;
        int currentIndex = frame.memory.size();
        //stor x at mem address A
        //check to see if further than the current index
        if (!(a instanceof Integer)) {
          error("opcode not an integer");
        }
        if ((int)a > currentIndex){
            error("invalid store index");
        }
        else if ((int)a == currentIndex) {
          frame.memory.add((int)a);
        }
        frame.memory.set((int)a, x);
      }

      // // arithmetic, relational, and logical operators
      // ADD,     // pop x, pop y, push (y + x) 
      else if (instr.opcode == OpCode.ADD) {
        //pop x
        Object x = operandStack.pop();
        //pop y
        Object y = operandStack.pop();
        //push (y + x)
        //check to see if one is null
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        //test case
       
        Object ret = addHelper(y,x);
        operandStack.push(ret);
        
      }
      // SUB,     // pop x, pop y, push (y - x) 
      else if (instr.opcode == OpCode.SUB) {
        //pop x 
        Object x = operandStack.pop();
        //pop y
        Object y = operandStack.pop();
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        //push (y - x)
        Object ret = subHelper(y, x);
        operandStack.push(ret);
      }
      // MUL,     // pop x, pop y, push (y * x)
      else if (instr.opcode == OpCode.MUL) {
        //pop x
        Object x = operandStack.pop();
        //pop y
        Object y = operandStack.pop();
        ensureNotNull(x, frame);
        ensureNotNull(y, frame); 
        //push (y * x)
        Object ret = mulHelper(x, y);
        operandStack.push(ret);
      }
      // DIV,     // pop x, pop y, push (y // x) or (y / x)
      else if (instr.opcode == OpCode.DIV) {
        //pop x
        Object x = operandStack.pop();
        //pop y
        Object y = operandStack.pop();
        //push y // x
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        //cannot divide by zero
        if (y instanceof Integer) {
          if ((int)y == 0) {
            error("INTEGER:: divide by zero ");
          }
        }
        else {
          if ((double)y == 0) {
            error("DOUBLE:: divide by zero ");
          }
        }
        Object ret = divHelper(y, x, frame);
        operandStack.push(ret);
      }
      // CMPLT,   // pop x, pop y, push (y < x)
      else if (instr.opcode == OpCode.CMPLT) {
        //pop x
        Object x = operandStack.pop();
        //pop y
        Object y = operandStack.pop();
        //push (y < x)
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        Object ret = cmpltHelper(y,x);
        
        operandStack.push(ret);
      }
      // CMPLE,   // pop x, pop y, push (y <= x)
      else if (instr.opcode == OpCode.CMPLE) {
        //pop x
        Object x = operandStack.pop();
        //pop y
        Object y = operandStack.pop();
        //push (y < x)
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        Object ret = cmpleHelper(y,x);
        operandStack.push(ret);
      }
      // CMPEQ,   // pop x, pop y, push (y == x)
      else if (instr.opcode == OpCode.CMPEQ) {
        //pop x
        Object x = operandStack.pop();
        //pop y
        Object y = operandStack.pop();
        //can actually be null???
        //Comparioson
        if (x instanceof Integer && y instanceof Integer)
          operandStack.push((int)x == (int)y);
        else if (x instanceof Double && y instanceof Double)
          operandStack.push((double)x == (double)y);
        else if (x instanceof Boolean && y instanceof Boolean)
          operandStack.push((boolean)x == (boolean)y);
        else if (x instanceof String && y instanceof String)
          operandStack.push(((String)x).compareTo((String)y) == 0);
        else if (x == VM.NULL && y == VM.NULL)
          operandStack.push(true);
        else if (x == VM.NULL ^ y == VM.NULL) // XOR
          operandStack.push(false);
        else
          error("Not a valid comparison");
      }
      // CMPNE,   // pop x, pop y, push (y != x)
      else if (instr.opcode == OpCode.CMPNE) {
        //pop x
        Object x = operandStack.pop();
        //pop y
        Object y = operandStack.pop();
        //can actually be null???
        //Comparioson
        if (x instanceof Integer && y instanceof Integer)
          operandStack.push(!((int)x == (int)y));
        else if (x instanceof Double && y instanceof Double)
          operandStack.push(!((double)x == (double)y));
        else if (x instanceof Boolean && y instanceof Boolean)
          operandStack.push(!((boolean)x == (boolean)y));
        else if (x instanceof String && y instanceof String)
          operandStack.push(!(((String)x).compareTo((String)y) == 0));
        else if (x == VM.NULL && y == VM.NULL)
          operandStack.push(false);
        else if (x == VM.NULL ^ y == VM.NULL) // XOR
          operandStack.push(true);
        else
          error("Not a valid comparison");

      }
      // AND,     // pop x, pop y, push (y and x)
      else if (instr.opcode == OpCode.AND) {
        //pop x
        Object x = operandStack.pop();
        //pop y
        Object y = operandStack.pop();
        //push y == x
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        operandStack.push((boolean)x && (boolean)y);
      }
      // OR,      // pop x, pop y, push (y or x)
      else if (instr.opcode == OpCode.OR) {
        //pop x
        Object x = operandStack.pop();
        //pop y
        Object y = operandStack.pop();
        //not null
        ensureNotNull(x, frame);
        ensureNotNull(y, frame);
        //push y == x
        operandStack.push((boolean)x || (boolean)y);
      }
      // NOT,     // pop x, push (not x)
      else if (instr.opcode == OpCode.NOT) {
        //pop x
        Object x = operandStack.pop();
        //not null
        ensureNotNull(x, frame);
        //push not x
        operandStack.push(!(boolean)x);
      }
      // // jump and branch
      // JMP,     // jump to given instruction offset A
      else if (instr.opcode == OpCode.JMP) {
        //get a
        Object a = instr.operand;
        //change instruction ptr to a
        if (!(a instanceof Integer)) 
          error("JMP operand not an int");
        frame.pc = (int)a;
      }
      // JMPF,    // pop x, if x is False jump to instruction offset A
      else if (instr.opcode == OpCode.JMPF) {
        //get a
        Object a = instr.operand;
        //pop x
        Object x = operandStack.pop();
        if (!(x instanceof Boolean)) 
          error("JMPF x should be a bool");
        //change instruction ptr to a
        if (!(a instanceof Integer)) 
          error("JMPF operand not an int");
        
        //String msg = "jumoed!" + "asd" + (boolean)x;
        //error(msg);
        if((boolean)x == false) {
          frame.pc = (int)a;
        }
        else {
          //do nothing
        }
        
      }
      // // functions
      // CALL,    // call function A (pop and push arguments)
      //CALL(A) calls function named A
      /*
        the set of program function definitions (frame templates) 
        private Map<String,VMFrameTemplate> templates = new HashMap<>(); 
      */
      /* 
        the function (frame) call stack 
        private Deque<VMFrame> callStack = new ArrayDeque<>();
      */
      else if (instr.opcode == OpCode.CALL) {
        //get a :: if a is empty we 
        Object a = instr.operand;
        //input operand should be a string
        if (!(a instanceof String)) 
          error("call must be a string");
        //find the function call
        VMFrameTemplate function = templates.get((String)a);
        
        //make a new VM frame
        VMFrame newFrame = new VMFrame(function);

        //add new VMFrame to the stack?
        callStack.push(newFrame);
        
        //VMFrame frame = new VMFrame(templates.get("main"));
        //actually run the function?
        frame = newFrame;
        
        //throw new UnsupportedOperationException();
      }
      // RET,     // return from current function
      //RET() exit from function “returning” x at top of stack
      else if (instr.opcode == OpCode.RET) {
        //pop x from stack?

        //I am confused; is this what I'm supposed to do?
        Object x;
        if (operandStack.size() == 0) {
          x =  VM.NULL;
        }
        else {
          x = operandStack.pop();
        }
        
        //remove VM frame?
        callStack.pop();
        //set to the prev frame
        if (callStack.size() != 0) {
          frame = callStack.getFirst();
        }
        //push the return onto the new opStack?
        operandStack.push(x);
        
        //throw new UnsupportedOperationException();
      }

      // // built ins
      // WRITE,   // pop x, print x to standard output
      else if (instr.opcode == OpCode.WRITE) {
        //pop x
        Object x = operandStack.pop();
        //print x to standard output
        System.out.printf(x.toString());
      }
      // READ,    // read standard input, push result onto stack
      //   -- for READ, use: new BufferedReader(new InputStreamReader(System.in)) and readLine()
      else if (instr.opcode == OpCode.READ) {
        //read standard in
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        try {
          input = read.readLine();
        } catch (IOException e) {
          error("IO exception in the line reader");
        }
        //push result onto stack
        operandStack.push(input);
      }
      // LEN,     // pop string x, push length(x) if str, else push obj(x).length
      else if (instr.opcode == OpCode.LEN) {
        //pop string x
        Object x = operandStack.pop();

        //two cases
        //string
        if (x instanceof String) {
          String ret = (String)x;
          operandStack.push(ret.length());
        }
        //array
        else if (x instanceof Integer) {//check to see if oid
          if (!(arrayHeap.containsKey((int)x))) {
            error("Not an valid array oid");
          }
          //the array
          List<Object> array = arrayHeap.get((int)x);
          //return the size
          operandStack.push(array.size());
        }
        //error
        else {
          error("Not valid LEN access, must be string or array");
        }
      }
      // GETC,    // pop int x, pop string y, push y[x]
      else if (instr.opcode == OpCode.GETC) {
        //pop int x
        Object x = operandStack.pop();
        if (!(x instanceof Integer)) 
          error("GETC arg1 should be a int");
        
        //pop string y
        Object y = operandStack.pop();
        if (!(y instanceof String)) 
          error("GETC arg2 should be a string");

        //push y[x]
        int input = (int)x;
        String str = (String)y;
        if (input < 0 || input >= str.length()) {
          error("String access out of bounds");
        }
        String ret = String.valueOf(str.charAt(input));
        operandStack.push(ret);
      }
      // TOINT,   // pop x, push int(x)
      //   -- for TOINT, can use intValue() on Double
      //   -- for TOINT, can use Integer.parseInt(...) for String (in try-catch block)
      else if (instr.opcode == OpCode.TOINT) {
        //pop x
        Object x = operandStack.pop();
        Object ret = null;
        if (x instanceof Double) {
          Double val = (Double)x;
          ret = (int)Math.round(val);
        }
        else if (x instanceof String) {
          //check formatt of string
          try {
            ret = Integer.parseInt((String)x);
          }
          catch(NumberFormatException e){
            error(""+e.getLocalizedMessage());
          }
          
        }
        else
        {
          error("TOINT used on a non double or string");
        }
        operandStack.push(ret);
      }
      // TODBL,   // pop x, push double(x)
      //   -- similarly for TODBL (but with corresponding Double versions)
      else if (instr.opcode == OpCode.TODBL) {
        //pop x
        Object x = operandStack.pop();
        Object ret = null;
        if (x instanceof Integer) {
          double val = (int)x;
          operandStack.push(val);
        }
        else if (x instanceof String) {
          try {
            double d = Double.parseDouble((String)x);
            operandStack.push(d);
          } catch (NumberFormatException e) {
            error("Invalid conversion" + e.getLocalizedMessage());
          }
        }
        else
        {
          error("TOINT used on a non double or string");
        }

      }
      // TOSTR,   // pop x, push str(x)
      //   -- for TOSTR, can use String.valueOf(...)
      else if (instr.opcode == OpCode.TOSTR) {
        //pop x
        Object x = operandStack.pop();

        //check for null
        if (x == VM.NULL) {
          error("Cannot convert string to null");
        }
        //push str(x)
        operandStack.push(String.valueOf(x));
      }

      // // heap
      // ALLOCS,  // allocate struct object, push oid x
      //  /* the struct heap as an oid to object (field to value map) mapping */
      //  private Map<Integer,Map<String,Object>> structHeap = new HashMap<>();
      else if (instr.opcode == OpCode.ALLOCS) {
        //allocate struct object
        Map<String,Object> struct = new HashMap<String,Object>();
        //get objID
        int objID = nextObjectId;
        nextObjectId++; 

        //add to struct heap?
        structHeap.put(objID, struct);
        //push oid x onto operand stack?
        operandStack.push(objID);
      }
      // SETF,    // pop value x, pop oid y, set obj(y)[A] = x
      else if (instr.opcode == OpCode.SETF) {
        String osStack = operandStack.toString();
        //pop x
        Object x = operandStack.pop();
        Object oid = operandStack.pop();
        Object a = instr.operand;//a is field
        //check to make sure that a is a string
        if (!(a instanceof String) || (oid == VM.NULL)) {
          error("Struct field type needs to be a string. ");
        }
        //check the oid is valid
        if (!(oid instanceof Integer) || (oid == VM.NULL)) {
          error("Struct oid needs to be a string");
        }
        //set obj(y)[A] = x
        Map<String, Object> struct = structHeap.get(oid);
        if (struct == null) {
          error("SETF:  oid: "+(int)oid +" does not correspond to a struct \n StructHeap: " + structHeap);
        }
        struct.put((String)a, x);
      }
      // GETF,    // pop oid x, push obj(x)[A] onto stack
      else if (instr.opcode == OpCode.GETF) {
        //pop oid x
        Object oid = operandStack.pop();
        Object a = instr.operand;
        //check the oid is valid
        if (!(oid instanceof Integer) || (oid == VM.NULL)) {
          error("Struct oid needs to be a string");
        }
        //check to make sure that a is a string
        if (!(a instanceof String) || (oid == VM.NULL)) {
          error("Struct field type needs to be a string. ");
        }

        //push obj(x)[A] onto stack
        Map<String, Object> struct = structHeap.get(oid);
        if (struct == null) {
          String msg = "GETF called on a invalid oid.. OID:" + oid +"  a: " + a +"\n Struct List: " + structHeap.toString();
          error(msg);
        }
        Object retValue  = struct.get((String)a);
        operandStack.push(retValue);
      }
      // ALLOCA,  // pop int x, allocate array object with x None values, push oid
      /* 
        the array heap as an oid to list mapping 
        private Map<Integer,List<Object>> arrayHeap = new HashMap<>();
      */
      else if (instr.opcode == OpCode.ALLOCA) {
        //pop int x
        Object x = operandStack.pop();
        //allocate array object with x none values

        //get a new oid
        int oid = nextObjectId;
        nextObjectId++; 

        //check x
        if (!(x instanceof Integer)) {
          error("AllocA needs the array size to be an int val.");
        }
        if ((int)x < 0) {
          error("Array length must be 0 or more");
        }

        //create the array
        List<Object> array = new ArrayList<>();
        //fill up the array with null values
        for (int i = 0; i < (int)x; i++) {
          array.add(VM.NULL);
        }
        //save the array
        arrayHeap.put(oid, array);

        //push oid
        operandStack.push(oid);
      }
      // SETI,    // pop value x, pop index y, pop oid z, set array obj(z)[y] = x
      else if (instr.opcode == OpCode.SETI) {
        if (operandStack.size() < 3) {
          error("Not enough Arguments for SETI");
        }
        //pop value x
        Object x = operandStack.pop();
        //pop index y
        Object y = operandStack.pop();
        //pop oid z
        Object oidZ = operandStack.pop();
        if (!(oidZ instanceof Integer)) {
          error("oid for SETI must be an integer");
        }
        if (!(y instanceof Integer)) {
          error("index for SETI must be an integer");
        }
        
        //set array obj(z)[y] = x
        List<Object> array = arrayHeap.get(oidZ);
        if (array == null) {
          String msg = "Array OID not found ... oidZ:" + oidZ + "... x: "+x+".... y: "+y; 
          msg += "\nArray Heap: " + arrayHeap.toString();
          error(msg);
        }
        /* 
        int thing = (int)oidZ;
        System.out.println("OID: " + thing);
        int thing2 = (int)x;
        System.out.println("x: " + thing2);
        int thing3 = (int)y;
        System.out.println("y: " + thing3);
        */
        //check for too large access index
        if (((int)y >= array.size()) || ((int)y < 0)) {
          error("Array index out of bounds");
        }
        //set
        array.set((int)y,x);
      }
      // GETI,    // pop index x, pop oid y, push obj(y)[x] onto stack
      else if (instr.opcode == OpCode.GETI){
        //pop index x
        Object x = operandStack.pop();
        if (!(x instanceof Integer)) {
          error("GETI index must be a int .. x:" + x);
        }
        //pop oidy
        Object y = operandStack.pop();
        if (!(y instanceof Integer)) {
          error("GETI oid must be a int.  oid:" + y.toString());
        }

        //push obj(y)[x] onto stack
        List<Object> array = arrayHeap.get((int)y);
        if (array == null) {
          error("GETI: Array not found.... y: "+y +"\n ArrayHeap: " + arrayHeap);
        }

        //check for too large access index
        if (((int)x >= array.size()) || ((int)x < 0)) {
          error("Array index out of bounds");
        }

        operandStack.push(array.get((int)x));
      }
      
      // TODO: Implement the remaining instructions (except for DUP and NOP, see below) ...
      //   -- see lecture notes for hints and tips
      //
      // Additional Hints: 
      //   -- use ensureNotNull(v, frame) if operand can't be null
      //   -- Deque supports pop(), peek(), isEmpty()
      //   -- for WRITE, use System.out.print(...)
      //   -- for READ, use: new BufferedReader(new InputStreamReader(System.in)) and readLine()
      //   -- for LEN, can check type via: if (value instanceof String) ...
      //   -- for GETC, can use String charAt() function
      //   -- for TOINT, can use intValue() on Double
      //   -- for TOINT, can use Integer.parseInt(...) for String (in try-catch block)
      //   -- similarly for TODBL (but with corresponding Double versions)
      //   -- for TOSTR, can use String.valueOf(...)
      //   -- in a number of places, can cast if type known, e.g., ((int)length)


      
      //----------------------------------------------------------------------
      // Special Instructions
      //----------------------------------------------------------------------

      else if (instr.opcode == OpCode.DUP) {
        Object val = operandStack.pop();
        operandStack.push(val);
        operandStack.push(val);
      }

      else if (instr.opcode == OpCode.NOP) {
        // do nothing
      }

      else
        error("Unsupported operation: " + instr);
    }

  }

  
}
