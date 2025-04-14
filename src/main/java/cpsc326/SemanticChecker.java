/**
 * CPSC 326, Spring 2025
 * The Semantic Checker implementation.
 * 
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT
 */


package cpsc326;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


public class SemanticChecker implements Visitor {

  // for tracking function and struct definitions: 
  private Map<String,FunDef> functions = new HashMap<>();   
  private Map<String,StructDef> structs = new HashMap<>();  
  // for tracking variable types:
  private SymbolTable symbolTable = new SymbolTable();      
  // for holding the last inferred type:
  private DataType currType;                                

  //----------------------------------------------------------------------
  // Helper functions
  //----------------------------------------------------------------------
  
  /**
   */
  private boolean isBaseType(String type) {
    return List.of("int", "double", "bool", "string").contains(type); 
  }
  
  /**
   */
  private boolean isBuiltInFunction(String name) {
    return List.of("print", "println", "readln", "size", "get", "int_val",
                   "dbl_val", "str_val").contains(name);
  }
  
  /**
   * Create an error message
   */
  private void error(String msg) {
    MyPLException.staticError(msg);
  }
  
  /**
   * Creates an error 
   */
  private void error(String msg, Token token) {
    String s = "[%d,%d] %s";
    MyPLException.staticError(String.format(s, token.line, token.column, msg));
  }
  
  /**
   * Checks if the field name is a field in the struct
   * definition. This is a helper method for checking and inferring
   * assignment statement lvalues and var rvalue paths.
   * @param fieldName the field name to check for
   * @param structDef the struct definition to check
   * @returns true if a match and false otherwise
   */
  private boolean isStructField(String fieldName, StructDef structDef) {
    for (var field : structDef.fields) 
      if (field.varName.lexeme.equals(fieldName))
        return true;
    return false;
  }

  
  /**
   * Obtains the data type for the field name in the struct
   * definition. This is a helper method for checking and inferring
   * assignment statement lvalues and var rvalue paths.
   * @param fieldName the field name 
   * @param structDef the struct definition
   * @returns the corresponding data type or null if no such field exists
   */
  private DataType getStructFieldType(String fieldName, StructDef structDef) {
    for (var field : structDef.fields)
      if (field.varName.lexeme.equals(fieldName))
        return field.dataType;
    return null;
  }  


  private void clearParam (VarDef currNode) {
      //3.2 checks for duplicates
      if (symbolTable.existsInCurrEnv(currNode.varName.lexeme)) {//duplicate found
        error("Duplicate parameter found", currNode.varName);
      }
      else {
        symbolTable.add(currNode.varName.lexeme, currNode.dataType);//all good, add the parameter like normal
      }

      //3.3 checks for correct type
      if (isBaseType(currNode.dataType.type.lexeme)) {
        //were good
      }
      else if (structs.get(currNode.dataType.type.lexeme) != null){ //must be a struct
        //we good
      }
      else {
        error("Unrecognized param name", currNode.varName);
      }
  }

  private void clearDataType (DataType node) {
    //3.3 checks for correct type
      if (isBaseType(node.type.lexeme)) {
        //were good
      }
      else if (structs.get(node.type.lexeme) != null){ //must be a struct
        //we good
      }
      else {
        error("Unrecognized param type" + node.type.lexeme, node.type);
      }
  }
  
  //----------------------------------------------------------------------
  // Visit Functions
  //----------------------------------------------------------------------

  /**
   * Checks the program
   */
  public void visit(Program node) {

    // 1. record each struct definitions and check for duplicate names
    for (StructDef currNode : node.structs) {
      if (structs.containsKey(currNode.structName.lexeme)) {
        error("Duplicate struct declaration found: " + currNode.structName.lexeme);
      }
      else {
        structs.put(currNode.structName.lexeme, currNode); //adds struct def
      }
    }

    // 2. record each function definition and check for duplicate names
    for (FunDef currNode: node.functions) {
      if (functions.containsKey(currNode.funName.lexeme)) {
        error("Duplicate function name: "+currNode.funName.lexeme);
      }
      else {
        functions.put(currNode.funName.lexeme, currNode);
      }

    }
    // 3. check for a main function
    /*
    void main() {
    }
     */
    FunDef mainNode = functions.get("main");
    if (mainNode == null) {
      error("Main function not found");
    }
    //3.1 main must have no parameters
    if (!mainNode.params.isEmpty()) {//params is not empty
      error("Main function contains parameters; should be empty");
    }
    //3.2 main must be or return type void
    if (mainNode.returnType.type.tokenType != TokenType.VOID_TYPE) {// if return is not VOID
      error("main should only have a void return type");
    }

    // 4. check each struct
    for (StructDef s : node.structs) {
      s.accept(this);
    }

    //add all of the pre-defined functions 
    /*
    private boolean isBuiltInFunction(String name) {
    return List.of("print", "println", "readln", "size", "get", "int_val",
                   "dbl_val", "str_val").contains(name);
    }
     */
    //definitions for all of the pre-build functions
    
    // check each function
    for (FunDef f : node.functions) {
      f.accept(this);
    }
    
  }
  
  /**
   * Checks a function definition signature and body ensuring valid
   * data types and no duplicate parameters
   */
  public void visit(FunDef node) {
    String funName = node.funName.lexeme;
    // 1. check signature if it is main
    if (isBuiltInFunction(funName)) {
      error("Cannot redefine a pre-built function");
    }
    // 2. add an environment for params
    symbolTable.pushEnvironment(); //adds a new environment
    
    // 3. check and add the params (no duplicate param var names)
    for (VarDef currNode : node.params) {
      clearParam(currNode);
    }

    // 4. add the return type
    symbolTable.add("return", node.returnType); //we can then check the return function really easily later. 

    //4.1 check the return type exists. 
    if (node.returnType.type.tokenType == TokenType.VOID_TYPE) //void exception
    {}
    else {
      clearDataType(node.returnType);
    }
    
    // 5. check the body statements
    for (Stmt currNode: node.stmts) {
      currNode.accept(this);
    }

    //go back up after parsed thru the environment. 
    symbolTable.popEnvironment();
  }

  /**
   * Checks structs for duplicate fields and valid data types
   */
  public void visit(StructDef node) {
    symbolTable.pushEnvironment();

    //1. check for valid data types
    for (VarDef currNode : node.fields) {
      clearDataType(currNode.dataType);
    }
    
    //2. check for duplicate data types
    for (VarDef currNode: node.fields) {
      if (symbolTable.existsInCurrEnv(currNode.varName.lexeme)) {
        error("Duplicate struct name", currNode.varName);
      }
      else {
        symbolTable.add(currNode.varName.lexeme, currNode.dataType);
      }
    }

    symbolTable.popEnvironment();
  }

  @Override
  public void visit(DataType node) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visit'");
  }

  @Override // might be bugged or have oversight
  public void visit(VarDef node) {

    //1. Validate datatype
    clearDataType(node.dataType);

    //2. ensure varname is not a duplicate
    if (symbolTable.existsInCurrEnv(node.varName.lexeme)) {
      error("duplicate varname", node.varName);
    }
    else {
      symbolTable.add(node.varName.lexeme, node.dataType);
    }

  }

/*

 * Represents a variable declaration and (optionally) its definition
 * (value assignment)
 
  class VarStmt implements Stmt {
    public Token varName;
    public Optional<DataType> dataType = Optional.empty();
    public Optional<Expr> expr = Optional.empty();
    public void accept(Visitor v) {v.visit(this);}
  }
 */
  @Override
  public void visit(VarStmt node) {
    //1.ensure varname is not a duplicate
    if (symbolTable.existsInCurrEnv(node.varName.lexeme)) {
      error("duplicate varname", node.varName);
    }

    //2. ensure that if there is a full stmt, the return type matches the expr
    if (node.dataType.isPresent()) { //var x2:
      
      //Two cases: expr or no expr
      if (node.expr.isPresent()) {//1. we have an expr //var x2: double = 0.0

        node.expr.get().accept(this);//this fucker is messing with the symbol table????
    
        DataType rvalueDataType = currType;
        if (currType == null) {
          error("here", node.varName);
        } 
        //DataType lvalueDataType = node.dataType.get();
        DataType lvalueDataType = new DataType();
        lvalueDataType.isArray = node.dataType.get().isArray;
        lvalueDataType.type = new Token(node.dataType.get().type.tokenType, node.dataType.get().type.lexeme, node.dataType.get().type.line, node.dataType.get().type.column);
        
        if ((node.dataType.get().type.tokenType == rvalueDataType.type.tokenType) && (node.dataType.get().isArray == rvalueDataType.isArray)) { //if types match (should check for arrays stuff)
          
          symbolTable.add(node.varName.lexeme, lvalueDataType);
        }
        else if (rvalueDataType.type.tokenType == TokenType.VOID_TYPE) { //if = null
          symbolTable.add(node.varName.lexeme, lvalueDataType);
        }
        else {
          String error = "Type Mismatch: " + node.dataType.get().type + " other type:" + currType.type + "\n Lvalue Arr" + lvalueDataType.isArray + "Rvalue Arr: " + rvalueDataType.isArray;
          error(error, node.varName);
        }

        //random search 
        if (structs.containsKey(lvalueDataType.type.lexeme)) { //check for struct
          if (rvalueDataType.type.lexeme.equals("get")) {
            symbolTable.add(node.varName.lexeme, node.dataType.get());
          }
          else if (!rvalueDataType.type.lexeme.equals(lvalueDataType.type.lexeme)) {
            if ((rvalueDataType.type.lexeme.equals(" void ")))//check for void; null is okay
            {

            }
            else {
              String msg = "struct type mismatch " + " Rvalue:" + rvalueDataType.type.lexeme + " Lvalue:" + lvalueDataType.type.lexeme;
              error(msg, lvalueDataType.type);
            }
          }
          else {
            symbolTable.add(node.varName.lexeme, node.dataType.get());
          }
        }
        
      }
      else {//2. we dont't have expr //var x2: double
        symbolTable.add(node.varName.lexeme, node.dataType.get()); 
      }
    }
    else if (node.expr.isPresent()) { //var x1 = 0
      
      //maybe he wants the rvalue to be one function?? -- this is basically one rvalue
      node.expr.get().accept(this);
      //after the expr call we have the dataType
      if (currType.type.tokenType == TokenType.VOID_TYPE) { //check for edge case
        error("This cannot be null", node.varName);
      }
      else {
        
        symbolTable.add(node.varName.lexeme, currType);
      }
    }
    else {//neither is present
      
    }

  }

  /**
 * Represents an expression composed of a single rvalue
 */
  @Override
  public void visit(BasicExpr node) {
    node.rvalue.accept(this);
  }
    
  @Override
  public void visit(UnaryExpr node) {
    node.expr.accept(this);
    if (node.unaryOp.lexeme.equals("not")) // not is the only choice
      if (!currType.type.tokenType.equals(TokenType.BOOL_TYPE))
        error ("expecting boolean expression", currType.type);
  }

  /**
 * Represents an expression with a left-hand-side (lhs) expression, a binary
 * operator, and a righ-hand-side (rhs) expression
 
class BinaryExpr implements Expr {
  public Expr lhs;
  public Token binaryOp;
  public Expr rhs; 
  public void accept(Visitor v) {v.visit(this);}
}
*/
  @Override
  public void visit(BinaryExpr node) {//must return a currType
    
    //get type of lhs
    node.lhs.accept(this);
    DataType lhsType = currType;

    //get type of rhs
    node.rhs.accept(this); // issue here
    DataType rhsType = currType;

    //get the op
    String op = node.binaryOp.lexeme;
    boolean typesEqual = rhsType.type.tokenType == lhsType.type.tokenType;
    currType = new DataType();

    //array cases
    if ((rhsType.isArray == true) || (lhsType.isArray == true)) {
      //check to make sure proper op
      if (false==(op.equals("==") || op.equals("!="))) { //check for invalid op
        error("invalid operator for arrays", node.binaryOp);
      }

      //void (comp) void
      if (((lhsType.type.tokenType) == (TokenType.VOID_TYPE)) && ((rhsType.type.tokenType) == (TokenType.VOID_TYPE))) {
        Token newToken = new Token(TokenType.BOOL_TYPE, op, node.binaryOp.line, node.binaryOp.column);
        currType.type = newToken;
        return;
      }
      //void (comp) array
      else if (lhsType.type.tokenType == TokenType.VOID_TYPE) {
        Token newToken = new Token(TokenType.BOOL_TYPE, op, node.binaryOp.line, node.binaryOp.column);
        currType.type = newToken;
        return;
      }
      //array (comp) void
      else if (rhsType.type.tokenType == TokenType.VOID_TYPE) {
        Token newToken = new Token(TokenType.BOOL_TYPE, op, node.binaryOp.line, node.binaryOp.column);
        currType.type = newToken;
        return;
      }
    }
    
    //see the type-rules.pdf
    //1. string + string
    if (typesEqual && (rhsType.type.tokenType == TokenType.STRING_TYPE) && (op.equals("+"))) {
      //return a string
      Token newToken = new Token(TokenType.STRING_TYPE, op, node.binaryOp.line, node.binaryOp.column);
      currType.type = newToken;
      return;
    }
    //2. number (arth) number = number
    else if (
    typesEqual && 
    ((rhsType.type.tokenType == TokenType.INT_TYPE) || (rhsType.type.tokenType == TokenType.DOUBLE_TYPE)) && 
    ((op.equals("+")) || (op.equals("-")) || (op.equals("*")) || (op.equals("/")))) 
    {
      //error("HERE?");
      Token newToken = new Token(rhsType.type.tokenType, op, node.binaryOp.line, node.binaryOp.column);
      currType.type = newToken;
      return;
    }
    //3. == != bullshit
    else if (((typesEqual) || (rhsType.type.tokenType == TokenType.VOID_TYPE) || (lhsType.type.tokenType == TokenType.VOID_TYPE)) 
    && ((op.equals("==")) || (op.equals("!=")))) {
      Token newToken = new Token(TokenType.BOOL_TYPE, op, node.binaryOp.line, node.binaryOp.column);
      currType.type = newToken;
      return;
    }
    //4. < > stuff
    else if (
      (typesEqual) && //do types have to be equal?
      ((rhsType.type.tokenType == TokenType.INT_TYPE) || (rhsType.type.tokenType == TokenType.DOUBLE_TYPE) || (rhsType.type.tokenType == TokenType.STRING_TYPE))
      && ((op.equals("<")) || (op.equals(">")) || (op.equals(">=")) || (op.equals("<=")))
    ){
      Token newToken = new Token(TokenType.BOOL_TYPE, op, node.binaryOp.line, node.binaryOp.column);
      currType.type = newToken;
      return;
    }
    //5/6 and, or
    else if (typesEqual
      && ((op.equals("or")) || (op.equals("and")))
      && (rhsType.type.tokenType == TokenType.BOOL_TYPE)
    )  {
      Token newToken = new Token(TokenType.BOOL_TYPE, op, node.binaryOp.line, node.binaryOp.column);
      currType.type = newToken;
      return;
    }
    //7. HANDLED IN URINARY EXPR
    else {
      String msg = "Not handled: " + node.binaryOp.lexeme + "RHS: " + rhsType.type.tokenType.name() + " LHS: " + lhsType.type.tokenType.name();
      error(msg);
    }
    //4. 
    
  }


  @Override
  public void visit(ReturnStmt node) {
    //we use symbolTable.add("return", node.returnType); to check and see what the return type is and if it matches
    currType = null;
    node.expr.accept(this);//which switches the currType
    DataType funRetType = symbolTable.get("return");
    
    if ((funRetType.type.tokenType == currType.type.tokenType) && (funRetType.isArray == currType.isArray)) {
      //all good no issues
    }
    else if (TokenType.VOID_TYPE == currType.type.tokenType) { // we can return null regardless of the type
      //also fine
    }
    else {
      String msg = "invalid return type: " + currType.type.lexeme +" " +currType.isArray+ "\n Function ret type: " + funRetType.type.lexeme + " " +funRetType.isArray;
      error(msg, funRetType.type);
    }
  }

  /*
        x1 = 1
        x2 = "abc"
        x3 = 1.0
        x4 = true
   */
  @Override
  public void visit(AssignStmt node) {
    //getting the rvalue!!
    currType = null;
    node.expr.accept(this);
    DataType rvalue = currType;

    //we will be checking only ID's
    if (node.lvalue.size() != 1) {
      DataType retType = pathExpr(node.lvalue);
      //throw new UnsupportedOperationException("Unimplemented method :: Not written yet!!!");

      if ((retType.type.tokenType == rvalue.type.tokenType) && (retType.isArray == rvalue.isArray)) {
        //also good :)
      }
      else if ((rvalue.type.tokenType == TokenType.VOID_TYPE) && (!rvalue.isArray)) {
        //good!
      }
      else {
        error("assign error type. ", node.lvalue.get(0).varName);
      }
    }
    else {
      int x = node.lvalue.size() - 1;
      VarRef currNode = node.lvalue.get(x);
      if (!symbolTable.exists(node.lvalue.get(x).varName.lexeme)) {
        error("symbol doesn't exist in this current env", currNode.varName);
      }
      DataType symbolTableType = symbolTable.get(node.lvalue.get(x).varName.lexeme);
      DataType finalType = null;
      // we've got a possible array
      if (currNode.arrayExpr.isPresent()) {
        //check to make sure that the token is also an array
        if (symbolTableType.isArray == false) {
          String msg = "Called [] on a non-array type : type = " + symbolTableType.isArray + " id = " + symbolTableType.type.lexeme +" \n"+ symbolTable.toString();
          error(msg, currNode.varName);
        }
        //check that the expr is INT..non array
        currType = null;
        currNode.arrayExpr.get().accept(this);
        DataType exprType = currType;
        if ((exprType.type.tokenType != TokenType.INT_TYPE) && (exprType.isArray != false)) {
          error("value in the [] cannot be an array and must be int", currNode.varName);
        }

        //change the [int] into a int :: telling us that we got the object out of array.
        DataType newType = new DataType();
        newType.isArray = false;
        newType.type = symbolTableType.type;
        finalType = newType;
      }
      //we've got non-array bracket call lvalue
      else {
        finalType = symbolTableType;
      }  
      //non-array value
      //check that r-value is same as the l-value
      if ((finalType.type.tokenType == rvalue.type.tokenType) && (finalType.isArray == rvalue.isArray)) {
        //also good :)
      }
      else if ((rvalue.type.tokenType == TokenType.VOID_TYPE) && (!rvalue.isArray)) {
        //good!
      }
      else {
        error("assign error type. ", currNode.varName);
      }
    }

  }

  public DataType pathExpr(List<VarRef> list) {
    if (!symbolTable.exists(list.get(0).varName.lexeme)) {
      error("not a varible" , list.get(0).varName);  
    }
    //get struct type.
    DataType firstVarname = symbolTable.get(list.get(0).varName.lexeme);
    //check first one.. should be a struct
    if (!structs.containsKey(firstVarname.type.lexeme)) {
      error("no such struct", firstVarname.type);
    }
    StructDef prevStruct = structs.get(firstVarname.type.lexeme);
    
    //center loop, these should be structs
    DataType finalType = null;
    for (int x = 1; x < list.size(); x++) {//stops just before end
      //check to see if current is a struct val of previous
      String currentFeildname = list.get(x).varName.lexeme;
      if (!isStructField(currentFeildname, prevStruct)) {
        error("is not a struct feild", list.get(x).varName);
      }

      DataType fieldType = new DataType();
      fieldType.type = getStructFieldType(currentFeildname,prevStruct).type;
      fieldType.isArray = getStructFieldType(currentFeildname,prevStruct).isArray;
      if (fieldType.type.tokenType == TokenType.ID) {
        VarRef current = list.get(x);
        //also need to check to see if the struct type is an array and this is an array
        if (current.arrayExpr.isPresent() == fieldType.isArray) {
          //good
        }
        else {
          if (x == (list.size()-1)) { // we good
            //only do this if the thing is an 
            if ((fieldType.isArray == true) && (current.arrayExpr.isPresent() == false)) {
              fieldType.isArray = true;
            }
            else {
              fieldType.isArray = false;
            }
            
          }
          else {
            String msg = "array type mismatch in path expr: token ->"+ current.varName + "\n arrIsPresent: " + current.arrayExpr.isPresent() + "  defArrayIsPresent: " + fieldType.isArray;
            msg += "\n " + fieldType.isArray + " " + fieldType.type;
            error(msg, current.varName);
          }
        }

        //check to make sure expr is int if exists
        if (current.arrayExpr.isPresent()) {
          currType = null;
          current.arrayExpr.get().accept(this);
          if (currType.type.tokenType != TokenType.INT_TYPE) {
            error("Path array is not an int", current.varName);
          }
        }
        
        //good is struct
        if (!(x == (list.size()-1))) {
          prevStruct = structs.get(fieldType.type.lexeme);
        }
        
      }
      else if (x == (list.size()-1)) {//last call..
        //non ID datatype
        //any datatype
        VarRef current = list.get(x);
        if ((fieldType.isArray == true) && (current.arrayExpr.isPresent() == false)) {
          fieldType.isArray = true;
        }
        else {
          fieldType.isArray = false;
        }
        //error("type  " + fieldType.type + "  isArray: " +fieldType.isArray);

        finalType = fieldType;
      }
      else {
        //can't have a non-struct in middle of path expr
        error("non-struct in middle of path", list.get(x).varName);
      }
    }

    //end of loop?
    //return the final type. .
    if (finalType == null) {
      String currentFeildname = list.getLast().varName.lexeme; 
      DataType fieldType = getStructFieldType(currentFeildname,prevStruct);
      String prevStructMsg = prevStruct.structName.lexeme + " " + prevStruct.fields.toString(); // current struct, not previous
      //error(" Current Feild: " + list.getLast().varName + " prevStruct: [" +prevStructMsg + " ]");
      finalType = fieldType;
      if (fieldType.isArray) {
          //two cases: 
          //1. array expr present, and it is a non-array thingy
          if (list.getLast().arrayExpr.isPresent()) {
            //good
            //finalType.isArray = false;
            finalType = new DataType();
            finalType.type = fieldType.type;
            finalType.isArray = false;
          }
          //2. array expr not present, and it is an array  
          else {
            
          }
        }
    }
    //error("final type" + finalType.type);
    return finalType;
  }

  /*
  //getting the rvalue!!
    currType = null;
    node.expr.accept(this);
    DataType rvalue = currType;

    //we will be checking only ID's
    if (node.lvalue.size() != 1) {
      //time to write this....
      
      //struct chain stuff
      VarRef baseCase = node.lvalue.get(0);
      StructDef previousStruct = null;
      DataType previousNodeDatatype = symbolTable.get(baseCase.varName.lexeme);
      if (structs.containsKey(previousNodeDatatype.type.lexeme)) {
        previousStruct = structs.get(previousNodeDatatype.type.lexeme);
      }
      else {
        error("invalid struct chain call", baseCase.varName);
      }
      for (int x = 1; x < node.lvalue.size(); x++) {
        VarRef currentElement = node.lvalue.get(x);
        //1.check to see if previous element has a child same as this current type...
        if (isStructField(currentElement.varName.lexeme, previousStruct)) {
          //good!
          previousStruct = structs.get(currentElement.varName.lexeme);
        }
        else {
          error("not a struct feild", currentElement.varName);
        }

        //2. update the previous struct and element
        if (structs.containsKey(currentElement.varName.lexeme)) {
          previousStruct = structs.get(currentElement.varName.lexeme);
        }
      }

      
      // TODO Auto-generated method stub
      //throw new UnsupportedOperationException("Unimplemented method :: Not written yet!!!");
    }
    else {
      int x = node.lvalue.size() - 1;
      VarRef currNode = node.lvalue.get(x);
      if (!symbolTable.exists(node.lvalue.get(x).varName.lexeme)) {
        error("symbol doesn't exist in this current env", currNode.varName);
      }
      DataType symbolTableType = symbolTable.get(node.lvalue.get(x).varName.lexeme);
      DataType finalType = null;
      // we've got a possible array
      if (currNode.arrayExpr.isPresent()) {
        //check to make sure that the token is also an array
        if (symbolTableType.isArray == false) {
          String msg = "Called [] on a non-array type : type = " + symbolTableType.isArray + " id = " + symbolTableType.type.lexeme +" \n"+ symbolTable.toString();
          error(msg, currNode.varName);
        }
        //check that the expr is INT..non array
        currType = null;
        currNode.arrayExpr.get().accept(this);
        DataType exprType = currType;
        if ((exprType.type.tokenType != TokenType.INT_TYPE) && (exprType.isArray != false)) {
          error("value in the [] cannot be an array and must be int", currNode.varName);
        }

        //change the [int] into a int :: telling us that we got the object out of array.
        DataType newType = new DataType();
        newType.isArray = false;
        newType.type = symbolTableType.type;
        finalType = newType;
      }
      //we've got non-array bracket call lvalue
      else {
        finalType = symbolTableType;
      }  
      //non-array value
      //check that r-value is same as the l-value
      if ((finalType.type.tokenType == rvalue.type.tokenType) && (finalType.isArray == rvalue.isArray)) {
        //also good :)
      }
      else if ((rvalue.type.tokenType == TokenType.VOID_TYPE) && (!rvalue.isArray)) {
        //good!
      }
      else {
        error("assign error type. ", currNode.varName);
      }
    }
   */

  @Override
  public void visit(WhileStmt node) {
    
    //get the currType of the expr
    currType = null;
    node.condition.accept(this);
    DataType stmtType = currType;

    //while stmt expr types should always be non-array boolean. 
    if ((stmtType.type.tokenType == TokenType.BOOL_TYPE) && (stmtType.isArray == false)) {
      //good
    }
    else {
      error("invalid while stmt return type", stmtType.type);
    }
    //check each statement
    symbolTable.pushEnvironment();
    for (Stmt currStmt : node.stmts) {
      currStmt.accept(this);
    }
    symbolTable.popEnvironment();
  }


  /*
    for x from 1 to 10 {
      var y = 10
      var z = 20 + x
    }
   */
  @Override
  public void visit(ForStmt node) {
    //make a new environment for the "for"
    symbolTable.pushEnvironment();

    //add the new varname to the "for" environment
    DataType iteratorType = new DataType();
    Token newToke = new Token(TokenType.INT_TYPE, node.varName.lexeme, node.varName.line, node.varName.column);
    iteratorType.isArray = false;
    iteratorType.type = newToke;
    symbolTable.add(node.varName.lexeme, iteratorType);


    //to must be an non array int
    currType = null;
    node.toExpr.accept(this);
    DataType toExpr = currType;
    if (!((toExpr.type.tokenType == TokenType.INT_TYPE) && (toExpr.isArray == false))) {
      error("invalid for stmt 'to' type", toExpr.type);
    }

    //from must be a int
    currType = null;
    node.fromExpr.accept(this);
    DataType fromExpr = currType;
    if ((fromExpr.type.tokenType == TokenType.INT_TYPE) && (fromExpr.isArray == false)) { // appears to be getting the value above it...
      //all good
    }
    else {
      String msg = "invalid for stmt 'from' type" + " Type: " + fromExpr.type.tokenType + " isArray: " + fromExpr.isArray + " lexme: " + fromExpr.type.lexeme;
      String msg2 = "invalid for stmt 'to' type" + " Type: " + toExpr.type.tokenType + " isArray: " + toExpr.isArray + " lexme: " + toExpr.type.lexeme;
      error(msg+"\n"+msg2, fromExpr.type);
    }

    //go through the stmts
    for (Stmt currStmt : node.stmts) {
      currStmt.accept(this);
    }
    //remove environment, we are done!
    symbolTable.popEnvironment();
  }

  @Override
  public void visit(IfStmt node) {
    //get the expr type
    currType = null;
    node.condition.accept(this);
    DataType stmtType = currType;

    //the expr must be only of type bool
    if ((stmtType.type.tokenType == TokenType.BOOL_TYPE) && (stmtType.isArray == false)) {
      //good
    }
    else {
      error("invalid if stmt return type", stmtType.type);
    }
    //remember to pop and push enviorments
    //visit the if stmts
    symbolTable.pushEnvironment();
    for (Stmt currStmt : node.ifStmts) {
      currStmt.accept(this);
    }
    symbolTable.popEnvironment();

    //do we have else if statments?
    if (node.elseIf.isPresent()) {
      node.elseIf.get().accept(this); // visit the else if
    }

    //do we have else statments?
    if (node.elseStmts.isPresent()) {
      symbolTable.pushEnvironment();
      for (Stmt currStmt : node.elseStmts.get()) {
        currStmt.accept(this);
      }
      symbolTable.popEnvironment();
    }
  }

  
  //Rvalues 
  //add all of the pre-defined functions 
    /*
    private boolean isBuiltInFunction(String name) {
    return List.of("print", "println", "readln", "size", "get", "int_val",
                   "dbl_val", "str_val").contains(name);
    }
     */
    //definitions for all of the pre-build functions
  @Override
  public void visit(CallRValue node) {
    //get the function 
    FunDef funcNode = null;
    if (functions.containsKey(node.funName.lexeme)) {
      funcNode = functions.get(node.funName.lexeme);
    }
    else {
      //could be a build in function
      String funName = node.funName.lexeme;
      List<Expr> inputList = node.args;
      int size = inputList.size();
      DataType voidReturn = new DataType();
      voidReturn.isArray = false;
      voidReturn.type = new Token(TokenType.VOID_TYPE, funName, 0, 0);
      //print 
      if (funName.equals("print")) {
        if (inputList.size() != 1) {
          error("print stmt too many args", node.funName);
        }

        //cannot print arrays
        currType = null;
        inputList.get(0).accept(this);
        DataType first = currType;
        if (first.type.tokenType == TokenType.ID) {
          error("cannot print struct", first.type);
        }
        if (first.isArray) {
          error("cannot print array");
        }
        //otherwise were good
        currType = voidReturn; //set data type
      }
      //println :: 
      else if (funName.equals("println")) {
        if (inputList.size() != 1) {
          error("print stmt too many args", node.funName);
        }
        //otherwise were good
        
        currType = null;
        inputList.get(0).accept(this);
        DataType first = currType;
        if (first.type.tokenType == TokenType.ID) {
          error("cannot print struct", first.type);
        }
        if (first.isArray) {
          error("cannot print array");
        }
        
        currType = voidReturn;
      }
      //readln :: returns line from std input. returns a string
      else if (funName.equals("readln")) {
        if (size != 0) {
          error("invalid number of params in readln");
        }
        DataType stringReturn = new DataType();
        stringReturn.isArray = false;
        stringReturn.type = new Token(TokenType.STRING_TYPE, funName, 0, 0);
        currType = stringReturn; 
      }
      // size :: returns in, can only take in arrays or strings
      else if (funName.equals("size")) {
        if (size != 1) {
          error("invalid number of params in size");
        }
        //get the first inpit
        currType = null;
        inputList.get(0).accept(this);
        DataType first = currType;
        if ((first.type.tokenType == TokenType.STRING_TYPE) || (first.isArray == true)) {
          //we good
          DataType stringReturn = new DataType();
          stringReturn.isArray = false;
          stringReturn.type = new Token(TokenType.INT_TYPE, funName, 0, 0);
          currType = stringReturn; 
        }
        else {
          error("only allows for strings or arrays");
        }
      }
      //get function :: get(i, "foo")
      //can also take in arrays :: var x3: double = get(0, new double[10])
      else if (funName.equals("get")) {
        if (size != 2) {
          error("invalid number of params in get");
        }
        //get args
        currType = null;
        inputList.get(0).accept(this);
        DataType first = currType;
        currType = null;
        inputList.get(1).accept(this);
        DataType second = currType;

        //1st arg should be an INT_TYPE
        if (!((first.type.tokenType == TokenType.INT_TYPE) && (first.isArray == false))) {
          error("get first type should be int");
        }
        //2nd arg should be a STRING_TYPE or Array
        if ((second.type.tokenType == TokenType.STRING_TYPE) || (second.isArray == true)) {
          
        }
        else {
          error("get second type should be string or array");
        }

        //set return type
        DataType typeReturn = new DataType();
        typeReturn.isArray = false;
        typeReturn.type = new Token(second.type.tokenType, funName, 0, 0);
        currType = typeReturn; 
      }
      //CASTING  :: several stuff to be had
      // str_val :: double,int  --> string
      // int_val :: string      --> int
      // dbl_val :: string      --> double
      else if (funName.equals("str_val")) {
        if (size != 1) {
          error("invalid number of params in cast");
        }
        //get args
        currType = null;
        inputList.get(0).accept(this);
        DataType first = currType;
        //should be int or double
        if (!(((first.type.tokenType == TokenType.INT_TYPE) && (first.isArray == false))|| ((first.type.tokenType == TokenType.DOUBLE_TYPE) && (first.isArray == false)))) {
          error("get first type should be int or double");
        }
        //should be string
        DataType strReturn = new DataType();
        strReturn.isArray = false;
        strReturn.type = new Token(TokenType.STRING_TYPE, funName, 0, 0);
        currType = strReturn;
      }
       // int_val :: string ,double     --> int
      else if (funName.equals("int_val")) {
        if (size != 1) {
          error("invalid number of params in cast");
        }
        //get args
        currType = null;
        inputList.get(0).accept(this);
        DataType first = currType;
        //should be string
        if (!(((first.type.tokenType == TokenType.STRING_TYPE) && (first.isArray == false)) || ((first.type.tokenType == TokenType.DOUBLE_TYPE) && (first.isArray == false)))) {
          error("casting should have an string");
        }
        //should be int
        DataType strReturn = new DataType();
        strReturn.isArray = false;
        strReturn.type = new Token(TokenType.INT_TYPE, funName, 0, 0);
        currType = strReturn;
      }
      else if (funName.equals("dbl_val")) {
        if (size != 1) {
          error("invalid number of params in cast");
        }
        //get args
        currType = null;
        inputList.get(0).accept(this);
        DataType first = currType;
        //should be string
      if (!(((first.type.tokenType == TokenType.STRING_TYPE) && (first.isArray == false)) || ((first.type.tokenType == TokenType.INT_TYPE) && (first.isArray == false)))) {
          error("casting should have an string");
        }
        //should be int
        DataType strReturn = new DataType();
        strReturn.isArray = false;
        strReturn.type = new Token(TokenType.DOUBLE_TYPE, funName, 0, 0);
        currType = strReturn;
      }
      else {
        error("cannot find function definition. ", node.funName);
      }
      
      return; // go back we are done here
    }
    
    //check the args to make sure they line up...
    if (funcNode.params.size() != node.args.size()) {
      error("Inavlid number of params ", node.funName);
    }
    for (int x = 1; x < funcNode.params.size(); x++) {
      //get each indiviudal varible 
      DataType defCurr = funcNode.params.get(x).dataType;
      Expr callCurrExpr = node.args.get(x);
      
      //we want to check each Expr's dataType...
      currType = null;
      callCurrExpr.accept(this);
      DataType callExerDataType = currType;

      //check to see if same type
      if ((callExerDataType.type.tokenType == defCurr.type.tokenType) && (callExerDataType.isArray == defCurr.isArray)) {
      //all good no issues
      }
      else if ((callExerDataType.type.tokenType == TokenType.VOID_TYPE) && (callExerDataType.isArray == false)) {
        //also okay
      }
      else {
        String msg = "type mismatch: " + callExerDataType.type.tokenType + " isArray: " + callExerDataType.isArray;
        error(msg, node.funName);
      }
    }

    //should set currType just in case
    currType = null;
    DataType retType = funcNode.returnType;
    currType = retType;
  }
 

  public void visit(SimpleRValue node) {

    currType = null;
    //also have to deal with .... uhhhhh arrays here, and in the caller function
    TokenType literalType = node.literal.tokenType;
    int line = node.literal.line;
    int column = node.literal.column;
    Token typeToken = null;
    if (literalType == TokenType.INT_VAL) typeToken = new Token(TokenType.INT_TYPE, "int", line, column);
    else if (literalType == TokenType.DOUBLE_VAL) typeToken = new Token(TokenType.DOUBLE_TYPE, " double ", line, column);
    else if (literalType == TokenType.STRING_VAL) typeToken = new Token(TokenType.STRING_TYPE, "string", line, column);
    else if (literalType == TokenType.BOOL_VAL) typeToken = new Token(TokenType.BOOL_TYPE, "bool", line, column);
    else if (literalType == TokenType.NULL_VAL) typeToken = new Token(TokenType.VOID_TYPE, " void ", line, column);
    else {
      error("bad value", node.literal);
    }
    currType = new DataType(); //array false on default
    

    currType.type = typeToken;
  }
  //this will set the currType which will be set after this is called!!!

  /*
  /**
 * Represents an rvalue consisting of one or more variable references
 * (forming a path expression)
 */ 
  @Override
  public void visit(VarRValue node) {
    //we've got a path!!
    if (node.path.size() > 1) {
      currType = null;
      currType = pathExpr(node.path);
      //throw new UnsupportedOperationException("Unimplemented method 'visit' (haven't written path variables yet)");
      if (currType == null) {
        error("null confusion", node.path.get(0).varName);
      } 
    }
    else {
      visit(node.path.get(0));//visit, we will figure this out later
    }
  }

  
  public void visit(VarRef node) {
    //we are referencing a variable that should be in the table... get it's DataType from table
    
    if (!symbolTable.exists(node.varName.lexeme)) {
      error("this value dones't exist!" + symbolTable.toString(), node.varName);
    }
    
    DataType nodeRefDataType = symbolTable.get(node.varName.lexeme);
    currType = null;
    currType = new DataType();
    //we have array, evaluate and check expr
    if (node.arrayExpr.isPresent()) {
      node.arrayExpr.get().accept(this);
      DataType exprType = currType; //get the current type
      if ((exprType.type.tokenType != TokenType.INT_TYPE ) || (exprType.isArray == true)) {
        error("Bad array index call", node.varName);
      }
      //we are accessing a index at array, se we remove the isArray mod
      //nodeRefDataType.isArray = false;
      //if we are here the expr is good!
      currType.isArray = false;
      currType.type = new Token(nodeRefDataType.type.tokenType, nodeRefDataType.type.lexeme, nodeRefDataType.type.line, nodeRefDataType.type.column);
    }
    else {
      currType.isArray = nodeRefDataType.isArray;
      currType.type = new Token(nodeRefDataType.type.tokenType, nodeRefDataType.type.lexeme, nodeRefDataType.type.line, nodeRefDataType.type.column);
    }
    //return the datatype
    
  }

  @Override
  public void visit(NewStructRValue node) {

    //1. Verify struct is defined
    StructDef def = null;
    if (structs.containsKey(node.structName.lexeme)) {
      def = structs.get(node.structName.lexeme);

    }
    else {
      error("struct does not exist", node.structName);
    }

    //2. Verify that the user input is same as struct def
    //2.1 check to make sure that the sizes are equvalent
    if (node.args.size() != def.fields.size()) {
      error("invalid number of params", node.structName);
    }
    //2.2 loops thru the list and ensures that the call is same as the structs defs.. 
    for (int x = 0; x < def.fields.size(); x++) {
      //get def type
      DataType defType = def.fields.get(x).dataType;

      currType = null;
      node.args.get(x).accept(this);
      DataType userInputType = currType;
      if ((userInputType.type.lexeme.equals(defType.type.lexeme)) && (userInputType.isArray == defType.isArray)) {
        //we good :)
      }
      else if (userInputType.type.tokenType == TokenType.VOID_TYPE) {
        //also good
      }
      else {
        String msg = "Type mismatch... \ninput type: ["  + userInputType.type + " isArray: "+userInputType.isArray+ " ] \n  def type: ["  + defType.type + " isArray: "+defType.isArray+ " ]";
        //msg += "\n extra:" + structs.get("board").fields.get(0).dataType.isArray;
        error(msg, userInputType.type);
      }
    }

  
    //3. set the currType
    DataType returnType = new DataType();
    returnType.isArray = false; // will cause issues later
    returnType.type = node.structName;
    currType = null;
    currType = returnType;
  }

  @Override
  public void visit(NewArrayRValue node) {
    //check to make sure expr is an int value
    node.arrayExpr.accept(this);
    DataType exprType = currType;
    if (exprType.type.tokenType != TokenType.INT_TYPE) {
      error("Array Length must be an int", node.type);
    }
    
    //some cases to consider
    //we can make an array of structs :: we would need to check that the struct type is the same as the declared one
    if (node.type.tokenType == TokenType.ID) { //we've got a struct
      //we need to check that the struct exists. 
      if (!structs.containsKey(node.type.lexeme)) {
        error("this struct does not exist", node.type);
      }
      //set the return type of struct to include the struct name 
    }
    
    //otherwise return the type of array
    DataType newCurrType = new DataType();
    newCurrType.isArray = true;
    newCurrType.type = node.type;

    
    
    //return the currType
    currType = null;
    currType = newCurrType;

  }

  @Override
  public void visit(VM_FunDef node) {
    String funName = node.funName.lexeme;
    // 1. check signature if it is main
    if (isBuiltInFunction(funName)) {
      error("Cannot redefine a pre-built function");
    }
    // 2. add an environment for params
    symbolTable.pushEnvironment(); //adds a new environment
    // 3. check and add the params (no duplicate param var names)
    for (VarDef currNode : node.params) {
      clearParam(currNode);
    }
    // 4. add the return type
    symbolTable.add("return", node.returnType); //we can then check the return function really easily later. 

    //4.1 check the return type exists. 
    if (node.returnType.type.tokenType == TokenType.VOID_TYPE) //void exception
    {}
    else {
      clearDataType(node.returnType);
    }
    //go back up after parsed thru the environment. 
    symbolTable.popEnvironment();
  }

  
  
  // TODO: Finish the remaining visit functions
  
}
