/**
 * CPSC 326, Spring 2025
 * 
 * PUT YOUR NAME HERE
 */


package cpsc326;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


/**
 * Generates MyPL VM code from an AST.
 */
public class CodeGenerator implements Visitor {

  /* vm to add frames to */
  private VM vm;

  /* current frame template being generated */
  private VMFrameTemplate currTemplate;

  /* variable -> index mappings with respect to environments */
  private VarTable varTable = new VarTable();

  /* struct defs for field names */
  private Map<String,StructDef> structs = new HashMap<>();  


  /**
   * Create a new Code Generator given a virtual machine
   * @param vm the VM for storing generated frame templates
   */
  public CodeGenerator(VM vm) {
    this.vm = vm;
  }
  
  //----------------------------------------------------------------------
  // Helper functions
  //----------------------------------------------------------------------

  /**
   * Helper to add an instruction to the current frame.
   * @param instr the instruction to add
   */
  private void add(VMInstr instr) {
    currTemplate.add(instr);
  }

  private void debugerror(String msg) {
    MyPLException.staticError(msg);
  }
  /*Shows the current operand stack, stopping the program, used for debug*/
  private void showOperandStack() {
    String instrs = "\n";
    for (int x = 0; x < currTemplate.instructions.size(); x++) {
      VMInstr str = currTemplate.instructions.get(x);
      instrs += x + ": ";
      instrs += str.toString();
      instrs += "\n";
    }
    MyPLException.staticError(instrs);
  }
  /*Gets the current program counter of the program, useful for while and for loops*/
  private int getCurrentPC() {
    return currTemplate.instructions.size() -1;
  }
  /**
   * Helper to add an instruction to the current frame with a comment.
   * @param instr the instruction to add
   * @param comment the comment to assign to the instruction
   */
  private void add(VMInstr instr, String comment) {
    instr.comment = comment;
    currTemplate.add(instr);
  }

  /**
   * Helper to execute body statements that cleans up the stack for
   * single function call statements (whose returned values aren't
   * used).
   */
  private void execBody(List<Stmt> stmts) {
    for (var stmt : stmts) {
      stmt.accept(this);
      if (stmt instanceof CallRValue)
        add(VMInstr.POP(), "clean up call rvalue statement");
    }
  }
  
  //----------------------------------------------------------------------
  // Visitors for programs, functions, and structs
  //----------------------------------------------------------------------

  /**
   * Generates the IR for the program
   */
  public void visit(Program node) {
    // record each struct definitions and check for duplicate names
    for (StructDef s : node.structs)
      s.accept(this);
    // generate each function
    for (FunDef f : node.functions)
      f.accept(this);
  }

  
  /**
   * Generates a function definition
   */
  public void visit(FunDef node) {
    //  Create a new frame template (as currTemplate)
    currTemplate = new VMFrameTemplate(node.funName.lexeme);
    // • Push a new variable environment (via varTable)
    varTable.pushEnvironment();
    // • Store each argument provided on operand stack (from the CALL)
    for (int x = 0; x < node.params.size(); x++) {
      VarDef param = node.params.get(x); // might not even need this
      varTable.add(param.varName.lexeme);
      currTemplate.add(VMInstr.STORE(varTable.get(param.varName.lexeme))); // store the params
      //add params to the data table
      
      
    }
    // • Visit each statement node (to generate its code)
    for (int x = 0; x < node.stmts.size(); x++) {
      node.stmts.get(x).accept(this);
    }
    // • Add a return (PUSH, RET) if last statement isn’t a return
    if (currTemplate.instructions.isEmpty()) {
      currTemplate.add(VMInstr.PUSH(VM.NULL));
      currTemplate.add(VMInstr.RET());
    }
    else if (currTemplate.instructions.getLast() != VMInstr.RET()) {
      currTemplate.add(VMInstr.PUSH(VM.NULL));
      currTemplate.add(VMInstr.RET());
    }
    // • Pop the variable environment
    /////////////////////////////////////////////////////////////////
    //DEBUG
    /////////////////////////////////////////////////////////////////
    //shows the current VM instrs
    varTable.popEnvironment();
    // • Add the frame template to the VMFrame
    vm.add(currTemplate);
    // NOTE: to generate the body code (for this and other
    // statements), you can use: execBody(node.stmts)
  }

  
  /**
   * Adds the struct def to the list of structs.
   */
  public void visit(StructDef node) {
    structs.put(node.structName.lexeme, node);
  }

  
  /**
   * The visitor function for a variable definition, but this visitor
   * function is not used in code generation.
   */
  public void visit(VarDef node) {
    // nothing to do here
  }

  
  /**
   * The visitor function for data types, but not used in code generation.
   */
  public void visit(DataType node) {
    // nothing to do here
  }

  @Override
  public void visit(ReturnStmt node) {
    node.expr.accept(this);
    currTemplate.add(VMInstr.RET());
  }

  @Override
  public void visit(VarStmt node) {
    //expression first? 
    if (node.expr.isPresent()) { // looks like ::  var x1: int = 3
      node.expr.get().accept(this);
      varTable.add(node.varName.lexeme); //new variable
      int index = varTable.get(node.varName.lexeme);
      currTemplate.add(VMInstr.STORE(index));
    }
    else {
      currTemplate.add(VMInstr.PUSH(VM.NULL));
      varTable.add(node.varName.lexeme); //new variable
      int index = varTable.get(node.varName.lexeme);
      currTemplate.add(VMInstr.STORE(index));
    }
  }

  @Override
  public void visit(AssignStmt node) {
    //visit expr
    if (node.lvalue.size() > 1) {
      // load the variable value (e.g., the p in p.x.y.z)
      
      VarRef first = node.lvalue.get(0);
      StructDef firstStruct = structs.get(first.varName.lexeme);
      //check for this to see if it is an array
      //is an array expr
      if (first.arrayExpr.isPresent()) {
        currTemplate.add(VMInstr.LOAD(varTable.get(first.varName.lexeme)));
        first.arrayExpr.get().accept(this);
        VMInstr getStuff = VMInstr.GETI();
        getStuff.comment = "first assign stmt";
        currTemplate.add(getStuff);
      }
      //not an array expr
      else {
        currTemplate.add(VMInstr.LOAD(varTable.get(first.varName.lexeme)));
      }
      
      // repeatedly add a GETF instruction for remaining path (e.g., x, y, and z)
      for (int x = 1; x < node.lvalue.size()-1; x++) {
        VarRef currNode = node.lvalue.get(x);
        if (currNode.arrayExpr.isPresent()) {
          // for array access, generate index code and use GETI
          VMInstr currInst = VMInstr.GETF(currNode.varName.lexeme);
          currTemplate.add(currInst); // get the feild
          currNode.arrayExpr.get().accept(this);
          VMInstr getStuff = VMInstr.GETI();
          getStuff.comment = "recursive assign stmt";
          currTemplate.add(getStuff);
          }
        else {
          VMInstr currInst = VMInstr.GETF(currNode.varName.lexeme);
          currInst.comment = "GetF #" + x;
          currTemplate.add(currInst); // get the feild
          //if we have a array access
        }
      }
     
      // for assignment statements, last instruction is a SETF or SETI
      VarRef lastlvalue = node.lvalue.getLast();
      if (lastlvalue.arrayExpr.isPresent()) { //is an array
        String feildname = lastlvalue.varName.lexeme;
        currTemplate.add(VMInstr.GETF(feildname));
        lastlvalue.arrayExpr.get().accept(this);
        node.expr.accept(this); // get the expr !!
        //debugerror("ASDas");
        currTemplate.add(VMInstr.SETI());
      }
      else {
          //ERROR HERE
          String feildname = lastlvalue.varName.lexeme;
          node.expr.accept(this); // get the value for x
          //debugerror("asdasda");

          currTemplate.add(VMInstr.SETF(feildname));
      }
    }
    else {
      if (node.lvalue.getFirst().arrayExpr.isPresent()) {//is an array access
        String arrayName = node.lvalue.getFirst().varName.lexeme;
        currTemplate.add(VMInstr.LOAD(varTable.get(arrayName)));
        //acccess the array
        node.lvalue.getFirst().arrayExpr.get().accept(this);
        node.expr.accept(this);
        
        currTemplate.add(VMInstr.SETI());

      }
      else {
        node.expr.accept(this); // get the value for x
        //single lvalue
        String newVarName = node.lvalue.get(0).varName.lexeme;
        // debugerror(newVarName + ": index:" + varTable.get(newVarName));
        currTemplate.add(VMInstr.STORE(varTable.get(newVarName)));
      }
    }
  }

  @Override
  public void visit(WhileStmt node) {
    currTemplate.add(VMInstr.NOP());
    int startOfWhile = getCurrentPC();
    //save current place, the start of while
    node.condition.accept(this);
    currTemplate.add(VMInstr.NOP());
    int JMPFlocation = getCurrentPC();
    // visit all of the statements (with new environment)
    varTable.pushEnvironment();
    for (Stmt currNode: node.stmts) {
      currNode.accept(this);
    }
    varTable.popEnvironment();
    // • add a JMP instruction (to starting index)
    currTemplate.add(VMInstr.JMP(startOfWhile));
    // • update the JMPF instruction (to instruction index after JMP)
    currTemplate.add(VMInstr.NOP());
    //add at speciic index
    int endOfWhile = getCurrentPC();
    currTemplate.instructions.set(JMPFlocation, VMInstr.JMPF(endOfWhile));
  }

  @Override
  public void visit(ForStmt node) {
    varTable.pushEnvironment();
    //declare the new inc and set it equal to from
    node.fromExpr.accept(this);
    varTable.add(node.varName.lexeme);
    currTemplate.add(VMInstr.STORE(varTable.get(node.varName.lexeme)));
    //get the new 'to' part
    currTemplate.add(VMInstr.NOP());
    int startOfWhile = getCurrentPC();
    //build the condition here
    currTemplate.add(VMInstr.LOAD(varTable.get(node.varName.lexeme)));
    node.toExpr.accept(this);
    currTemplate.add(VMInstr.CMPLE());
    currTemplate.add(VMInstr.NOP());
    int JMPFlocation = getCurrentPC();
    for (Stmt currNode: node.stmts) {
      currNode.accept(this);
    }
    currTemplate.add(VMInstr.LOAD(varTable.get(node.varName.lexeme)));
    currTemplate.add(VMInstr.PUSH(1));
    currTemplate.add(VMInstr.ADD());
    currTemplate.add(VMInstr.STORE(varTable.get(node.varName.lexeme)));

    // • add a JMP instruction (to starting index)
    currTemplate.add(VMInstr.JMP(startOfWhile));
    // • update the JMPF instruction (to instruction index after JMP)
    
    currTemplate.add(VMInstr.NOP());
    //add at speciic index
    int endOfWhile = getCurrentPC();
    currTemplate.instructions.set(JMPFlocation, VMInstr.JMPF(endOfWhile));
    varTable.popEnvironment();
    
  }


  @Override
  public void visit(IfStmt node) {
    //if stmt
    node.condition.accept(this);
    currTemplate.add(VMInstr.NOP()); // placeholder for JMPF
    int ifJMPF = getCurrentPC();
    for (Stmt curr : node.ifStmts) {
      curr.accept(this);
    }
    currTemplate.add(VMInstr.NOP());
    int afterFirstIfLoop = getCurrentPC();
    //else if
    if (node.elseIf.isPresent()) { // this takes priority
      currTemplate.instructions.set(ifJMPF, VMInstr.JMPF(getCurrentPC()+1));
      node.elseIf.get().accept(this);
    }
    else if (node.elseStmts.isPresent()) { // otherwise jmp to check this
      currTemplate.instructions.set(ifJMPF, VMInstr.JMPF(getCurrentPC()+1));
      for (Stmt curr: node.elseStmts.get()) {
        curr.accept(this);
      }
    }
    else {
      currTemplate.instructions.set(ifJMPF, VMInstr.JMPF(getCurrentPC()));
    }
    
    //end location
    VMInstr end = VMInstr.NOP();
    end.comment = "End of if loop section";
    currTemplate.add(end);
    VMInstr jmper = VMInstr.JMP(getCurrentPC());
    jmper.comment = "go to end";
    currTemplate.instructions.set(afterFirstIfLoop, jmper);
    
    
    //throw new UnsupportedOperationException("Unimplemented method 'if not written yet'");
  }
  
  @Override
  public void visit(BasicExpr node) {
    node.rvalue.accept(this);
  }

  @Override
  public void visit(UnaryExpr node) {
    node.expr.accept(this);
    currTemplate.add(VMInstr.NOT());
  }

  @Override
  public void visit(BinaryExpr node) {
    String binOp = node.binaryOp.lexeme;
    if (binOp.equals(">")) {
      node.rhs.accept(this);
      node.lhs.accept(this);
      currTemplate.add(VMInstr.CMPLT());
      return;
    }
    if (binOp.equals(">=")) {
      node.rhs.accept(this);
      node.lhs.accept(this);
      currTemplate.add(VMInstr.CMPLE());
      return;
    }
    node.lhs.accept(this);
    node.rhs.accept(this);
    if (binOp.equals("+")) {
      currTemplate.add(VMInstr.ADD());
    }
    else if (binOp.equals("-")) {
      currTemplate.add(VMInstr.SUB());
    }
    else if (binOp.equals("*")) {
      currTemplate.add(VMInstr.MUL());
    }
    else if (binOp.equals("/")) {
      currTemplate.add(VMInstr.DIV());
    }
    else if (binOp.equals("and")) {
      currTemplate.add(VMInstr.AND());
    }
    else if (binOp.equals("or")) {
      currTemplate.add(VMInstr.OR());
    }
    else if (binOp.equals("<")) {
      currTemplate.add(VMInstr.CMPLT());
    }
    else if (binOp.equals("<=")) {
      currTemplate.add(VMInstr.CMPLE());
    }
    else if (binOp.equals("==")) {
      currTemplate.add(VMInstr.CMPEQ());
    }
    else if (binOp.equals("!=")) {
      currTemplate.add(VMInstr.CMPEQ());
      currTemplate.add(VMInstr.NOT());
    }
    else {
      throw new UnsupportedOperationException("Unimplemented method 'binary expr not written yet'" + binOp);
    }
  }

  @Override
  public void visit(CallRValue node) {
    String callName = node.funName.lexeme;
    if (callName.equals("print")) { //printstmt
      //call the expr. 
      node.args.get(0).accept(this);
      if (node.args.isEmpty()) {
        //print nothing?
        throw new UnsupportedOperationException("Unimplemented method 'no args'");
      }
      currTemplate.add(VMInstr.WRITE());
    }
    else if (callName.equals("println")) { //printstmt
      //call the expr. 
      node.args.get(0).accept(this);
      if (node.args.isEmpty()) {
        //print nothing?
        throw new UnsupportedOperationException("Unimplemented method 'no args'");
      }
      currTemplate.add(VMInstr.WRITE());
      currTemplate.add(VMInstr.PUSH('\n'));
      currTemplate.add(VMInstr.WRITE());
    }
    else if (callName.equals("str_val")) {
      node.args.get(0).accept(this);
      currTemplate.add(VMInstr.TOSTR());
    }
    else if (callName.equals("int_val")) {
      node.args.get(0).accept(this);
      currTemplate.add(VMInstr.TOINT());
    }
    else if (callName.equals("dbl_val")) {
      node.args.get(0).accept(this);
      currTemplate.add(VMInstr.TODBL());
    }
    else if (callName.equals("size")) {
      node.args.get(0).accept(this);
      currTemplate.add(VMInstr.LEN());
    }
    else if (callName.equals("get")) {
      node.args.get(1).accept(this); //string
      node.args.get(0).accept(this); // index
      currTemplate.add(VMInstr.GETC());
    }
    else if (callName.equals("readln")) {
      currTemplate.add(VMInstr.READ());
    }
    else {
      //must be a funcall!
      
      //1. locate function being called
      String funcName = node.funName.lexeme;
      
      //2. push input's onto stack in reverse order
      for (int x = node.args.size()-1; x >= 0; x--) {
        Expr arg = node.args.get(x);
        arg.accept(this); // adds the arg
      }

      //3. call the function
      currTemplate.add(VMInstr.CALL(funcName));

      // TODO Auto-generated method stub
      //throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
    
  }

  @Override
  public void visit(SimpleRValue node) {
      String val = node.literal.lexeme;
      if (node.literal.tokenType == TokenType.INT_VAL)
          add(VMInstr.PUSH(Integer.parseInt(val)));
      else if (node.literal.tokenType == TokenType.DOUBLE_VAL)
          add(VMInstr.PUSH(Double.parseDouble(val)));
      else if (node.literal.tokenType == TokenType.STRING_VAL) {
          val = val.replace("\\n", "\n");
          val = val.replace("\\t", "\t");
          val = val.replace("\\r", "\r");
          add(VMInstr.PUSH(val));
      } else if (val.equals("true"))
          add(VMInstr.PUSH(true));
      else if (val.equals("false"))
          add(VMInstr.PUSH(false));
      else if (val.equals("null"))
          add(VMInstr.PUSH(VM.NULL));
  }

  @Override
  public void visit(NewStructRValue node) {
    //add an ALLOCS instruction
    currTemplate.add(VMInstr.ALLOCS());
    //initialize the corresponding fields:
    //... get the field information from the StructDef
    StructDef struct = structs.get(node.structName.lexeme);
    //... use SETF to set the object fields
    for (int x = 0; x < node.args.size(); x++) {
      Expr currNode = node.args.get(x); // get the current node
      currTemplate.add(VMInstr.DUP()); // duplicate the oid
      currNode.accept(this); //get the expr
      String argName = struct.fields.get(x).varName.lexeme;
      VMInstr setf = VMInstr.SETF(argName);
      setf.comment = "setf ::" + argName;
      currTemplate.add(setf);
    }
  }

  @Override
  public void visit(NewArrayRValue node) {
    //make a new array
    node.arrayExpr.accept(this);
    currTemplate.add(VMInstr.ALLOCA());
  }

  @Override
  public void visit(VarRValue node) {
    if (node.path.size() > 1) { // path expr... do this later
      // load the variable value (e.g., the p in p.x.y.z)
      VarRef first = node.path.get(0);
      StructDef firstStruct = structs.get(first.varName.lexeme);
      currTemplate.add(VMInstr.LOAD(varTable.get(first.varName.lexeme)));
      if (first.arrayExpr.isPresent()) { // first one is an array
        first.arrayExpr.get().accept(this);
        VMInstr firstInstr = VMInstr.GETI();
        firstInstr.comment = "first is an array";
        currTemplate.add(firstInstr);
      }
      // repeatedly add a GETF instruction for remaining path (e.g., x, y, and z)
      for (int x = 1; x < node.path.size(); x++) {
        VarRef currNode = node.path.get(x);
        //if we have a array access
        if (currNode.arrayExpr.isPresent()) {
          // for array access, generate index code and use GETI
          currTemplate.add(VMInstr.GETF(currNode.varName.lexeme));
          currNode.arrayExpr.get().accept(this);
          VMInstr getStuff = VMInstr.GETI();
          getStuff.comment = "path expr stuff";
          currTemplate.add(getStuff);
        }
        else {
          currTemplate.add(VMInstr.GETF(currNode.varName.lexeme)); // get the feild

        }
      }
      // for assignment statements, last instruction is a SETF or SETI
    }
    else {
      if (node.path.get(0).arrayExpr.isPresent()) {// is an array
        //it's an array ..... me tired
        //GETI,    // pop index x, pop oid y, push obj(y)[x] onto stack
        VarRef currNode = node.path.getFirst();
        int varIndex = varTable.get(node.path.get(0).varName.lexeme);
        currTemplate.add(VMInstr.LOAD(varIndex));
        //index access
        currNode.arrayExpr.get().accept(this);
        VMInstr getStuff = VMInstr.GETI();
        getStuff.comment = "pp0;";
        currTemplate.add(getStuff);
      }
      else {
        int varIndex = varTable.get(node.path.get(0).varName.lexeme);
        currTemplate.add(VMInstr.LOAD(varIndex));
      }
    }
  }

  @Override
  public void visit(VM_FunDef node) {
    //  Create a new frame template (as currTemplate)
    currTemplate = new VMFrameTemplate(node.funName.lexeme);
    // • Push a new variable environment (via varTable)
    varTable.pushEnvironment();
   
    for (int x = 0; x < node.VM_stmts.size(); x++) {
      currTemplate.add(node.VM_stmts.get(x)); // add each stmt
    }
    // • Add a return (PUSH, RET) if last statement isn’t a return
    if (currTemplate.instructions.isEmpty()) {
      currTemplate.add(VMInstr.PUSH(VM.NULL));
      currTemplate.add(VMInstr.RET());
    }
    else if (currTemplate.instructions.getLast() != VMInstr.RET()) {
      currTemplate.add(VMInstr.PUSH(VM.NULL));
      currTemplate.add(VMInstr.RET());
    }
    //shows the current VM instrs
    varTable.popEnvironment();
    // • Add the frame template to the VMFrame
    vm.add(currTemplate);
  }

  /*
  public void visit(FunDef node) {
    //  Create a new frame template (as currTemplate)
    currTemplate = new VMFrameTemplate(node.funName.lexeme);
    // • Push a new variable environment (via varTable)
    varTable.pushEnvironment();
    // • Store each argument provided on operand stack (from the CALL)
    for (int x = 0; x < node.params.size(); x++) {
      VarDef param = node.params.get(x); // might not even need this
      varTable.add(param.varName.lexeme);
      currTemplate.add(VMInstr.STORE(varTable.get(param.varName.lexeme))); // store the params
      //add params to the data table
    }
    // • Visit each statement node (to generate its code)
    for (int x = 0; x < node.stmts.size(); x++) {
      node.stmts.get(x).accept(this);
    }
    // • Add a return (PUSH, RET) if last statement isn’t a return
    if (currTemplate.instructions.isEmpty()) {
      currTemplate.add(VMInstr.PUSH(VM.NULL));
      currTemplate.add(VMInstr.RET());
    }
    else if (currTemplate.instructions.getLast() != VMInstr.RET()) {
      currTemplate.add(VMInstr.PUSH(VM.NULL));
      currTemplate.add(VMInstr.RET());
    }
    // • Pop the variable environment
    /////////////////////////////////////////////////////////////////
    //DEBUG
    /////////////////////////////////////////////////////////////////
    //shows the current VM instrs
    varTable.popEnvironment();
    // • Add the frame template to the VMFrame
    vm.add(currTemplate);
    // NOTE: to generate the body code (for this and other
    // statements), you can use: execBody(node.stmts)
  }
   */
}
  // TODO: Finish the remaining visit functions ... 


