/**
 * CPSC 326, Spring 2025
 * Pretty print visitor.
 *
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT 
 */


package cpsc326;

import java.util.List;

public class PrintVisitor implements Visitor {

  private int indent = 0;

  /**
   * Prints message without ending newline
   */
  private void write(String s) {
    System.out.print(s);
  }

  /**
   * Increase the indent level by one
   */
  private void incIndent() {
    indent++;
  }

  /**
   * Decrease the indent level by one
   */
  private void decIndent() {
    indent--;
  }
  
  /**
   * Print an initial indent string
   */
  private String indent() {
    return "  ".repeat(indent);
  }

  /**
   * Prints a newline
   */
  private void newline() {
    System.out.println();
  }
  
  /**
   * Prints the program
   */
  public void visit(Program node) {
    // always one blank line at the "top"
    newline();
    for (StructDef s : node.structs)
      s.accept(this);
    for (FunDef f : node.functions)
      f.accept(this);
  }

  // TODO: Complete the rest of the visit functions.
  public void visit(StructDef node) {
    write("struct ");
    write(node.structName.lexeme);
    write(" {");
    
    incIndent(); // going over a line
    if (!node.fields.isEmpty()) {// not empty 
      newline();
    }
    if (node.fields.size() >= 1) {
      write(indent());
      node.fields.get(0).accept(this);
    }
    if (node.fields.size() >= 2) {
      for(int x = 1; x < node.fields.size(); x++) {
        write(",");
        newline();
        write(indent());
        node.fields.get(x).accept(this); //go print yourself
      }
    }
    
    decIndent();
    newline();
    write("}");
    newline();
    //newline between things
    newline();
  }
  // Use the above helper functions to write, deal with indentation,
  // and print newlines as part of your visit functions.

  public void visit(FunDef node) {
    node.returnType.accept(this); // print return type ... which is a datatype
    write(" " + node.funName.lexeme + "("); //print all of the function body type

    // print all of the params
    if (node.params.size() >= 1) {
      node.params.get(0).accept(this);
    }
    if (node.params.size() >= 2) {
      for (int x = 1; x < node.params.size(); x++) {
        write(", ");
        node.params.get(x).accept(this);
      }
    }
    //print rest
    write(") {");
    newline();
    incIndent();
    for (Stmt currNode : node.stmts ) {
      write(indent());
      currNode.accept(this);
      newline();
    }
    decIndent();
    write(indent() + "}");
    newline();
    //spacing between functions
    newline();
  }



  
  public void visit(DataType node) {
    if (node.isArray == true) {
      write("["+node.type.lexeme+"]");
    }
    else
    {
      write(node.type.lexeme);
    }
  }

  
  public void visit(VarDef node) {
    write(node.varName.lexeme + ": ");
    node.dataType.accept(this);
  }

  
  public void visit(ReturnStmt node) {
    write("return ");
    node.expr.accept(this);
  }
  
  //<var_stmt> ::= VAR ID ( <var_init> | <var_type> ( <var_init> | ε ) )
  public void visit(VarStmt node) {
    //check for normal type vs the : type
    if (node.dataType.isPresent()) {
      write("var "+ node.varName.lexeme + ": ");
      //write the type
      node.dataType.get().accept(this); // visit the var-type...
      if (node.expr.isPresent()) { // if the ( <var_init> | ε ) exits
        write(" = ");
        node.expr.get().accept(this);
      }
    }
    else {
      write("var ");
      write(node.varName.lexeme + " = ");
      node.expr.get().accept(this);
    }
  }

  //<assign_stmt> ::= <lvalue> ASSIGN <expr>
  public void visit(AssignStmt node) {
    //lvalue must not be empty, so we can take the first one
    VarRef firstNode = node.lvalue.get(0);
    write(firstNode.varName.lexeme);
    if (firstNode.arrayExpr.isPresent()) {
      write("[");
      firstNode.arrayExpr.get().accept(this); // print inside expr
      write("]");
    }
    for (int x = 1; x < node.lvalue.size(); x++) {
      //print the rest of the lvalues if they exist. 
      write(".");
      write(node.lvalue.get(x).varName.lexeme);
      if (node.lvalue.get(x).arrayExpr.isPresent()) {
        write("[");
        node.lvalue.get(x).arrayExpr.get().accept(this); // print inside expr
        write("]");
      }
    }
    //write the other part.. the equals symbol. 
    write(" = ");

    //CURRENT ISSUE: here!!!  
    node.expr.accept(this);
    
  }


  //<while_stmt> ::= WHILE <expr> <block>
  public void visit(WhileStmt node) {
    write("while ");
    node.condition.accept(this);;
    write("");
    stmtBlock(node.stmts);
  }

  /*
  for i from (3 * 4) to 20 {
    x = (x * i)
  }
   */
  public void visit(ForStmt node) {
    //first part of loop
    write("for " + node.varName.lexeme + " from ");
    node.fromExpr.accept(this);
    write(" to ");
    node.toExpr.accept(this);

    //block part
    stmtBlock(node.stmts);
  }

  /*
  if (x < y) {
    println("x")
  }
  else if (x > y) {
    print("y")
  }
  else if (x == y) {
    println("x or y")
  }
  else {
    print("oops")
  }
   */
  public void visit(IfStmt node) {
    //first part of the visit statement
    write("if ");
    node.condition.accept(this);
    
    //start of the first block
    stmtBlock(node.ifStmts);

    //handle the else if
    if (node.elseIf.isPresent()) {
      newline();
      write(indent() + "else ");
      //adjust the if stmt incIndent
      node.elseIf.get().accept(this);
    }

    //handle the else
    if (node.elseStmts.isPresent()) {
      newline();
      write(indent()+"else");
      stmtBlock(node.elseStmts.get());
    }
  }


  private void stmtBlock (List<Stmt> nodeList) {
    write(" {");
    newline();
    incIndent();
    for (Stmt currNode : nodeList) {
      write(indent());
      currNode.accept(this);  
      newline();
    }
    decIndent();
    write(indent()+ "}");
  }

  //<rvalue> ::= <literal> | <new_rvalue> | <var_rvalue> | <fun_call>
  public void visit(BasicExpr node) {
    //supposed to wrap values with something?
    node.rvalue.accept(this);
  }

  
  public void visit(UnaryExpr node) {
    write(node.unaryOp.lexeme+" (");
    node.expr.accept(this);
    write(")");
  }

  //<bin_op> ::= PLUS | MINUS | TIMES | DIVIDE | AND | OR | EQUAL | LESS | GREATER | LESS_EQ | GREATER_EQ | NOT_EQUAL
  /**
 * Represents an expression with a left-hand-side (lhs) expression, a binary
 * operator, and a righ-hand-side (rhs) expression
 */
  public void visit(BinaryExpr node) {
    //visit left
    write("(");
    node.lhs.accept(this);
    write(" " + node.binaryOp.lexeme + " "); // hw3_print_3 out is bugged out the z has bad spacing
    node.rhs.accept(this);
    write(")");
  }

  
  public void visit(CallRValue node) {
    write(node.funName.lexeme);
    write("(");
    if (node.args.size() >= 1) {
      node.args.get(0).accept(this);
    }
    for (int x = 1; x < node.args.size(); x++) {
      write(", ");
      node.args.get(x).accept(this);
    }
    write(")");
  }

  /**
 * Represents a literal (constant) value
 */
 //<literal> ::= INT_VAL | DOUBLE_VAL | STRING_VAL | BOOL_VAL | NULL_VAL
  public void visit(SimpleRValue node) {
    //check for type and apply specific stuff.. 
    //EX: strings get "str"
    if (node.literal.tokenType == TokenType.STRING_VAL) {
      write("\"" + node.literal.lexeme + "\"");
    }
    else {
      write(node.literal.lexeme);
    }
  }

  /**
 * Represents a new struct expression
 */
  public void visit(NewStructRValue node) {
    write("new ");
    write(node.structName.lexeme);
    write("(");
    if (node.args.size() >= 1) { //if is not empty
      node.args.get(0).accept(this);
    }
    if (node.args.size() >= 2) {// we need those commas!!
      for (int x = 1; x < node.args.size(); x++) {
        write(", ");
        node.args.get(x).accept(this);
      }
    }
    write(")");
  }



  /**
 * Represents a new array expression
 */
 //<new_rvalue> ::= NEW ID LPAREN <args> RPAREN | NEW ( ID | <base_type> ) LBRACKET <expr> RBRACKET
 // this fucntion is this part -> NEW ( ID | <base_type> ) LBRACKET <expr> RBRACKET
  public void visit(NewArrayRValue node) {
    write("new ");
    write(node.type.lexeme);
    write("[");
    node.arrayExpr.accept(this);
    write("]");
  }

  //<var_rvalue> ::= ID ( LBRACKET <expr> RBRACKET | ε ) ( DOT ID ( LBRACKET <expr> RBRACKET | ε ) )∗
  /**
 * Represents an rvalue consisting of one or more variable references
 * (forming a path expression)
 */
  public void visit(VarRValue node) {
    //first node is the id? 
    VarRef initNode = node.path.get(0);
    write(initNode.varName.lexeme);
    if (initNode.arrayExpr.isPresent()) {
      write("[");
      initNode.arrayExpr.get().accept(this); //print expr
      write("]");
    }  
    //second part with the dots
    if (node.path.size() >= 2) {
      for (int x = 1; x < node.path.size(); x++) {
        write(".");
        write(node.path.get(x).varName.lexeme);
        if (node.path.get(x).arrayExpr.isPresent()) {
          write("[");
          node.path.get(x).arrayExpr.get().accept(this); 
          write("]");
        }
      }
    }
  }

  @Override
  public void visit(VM_FunDef node) {
    node.returnType.accept(this); // print return type ... which is a datatype
    write(" " + node.funName.lexeme + "("); //print all of the function body type

    // print all of the params
    if (node.params.size() >= 1) {
      node.params.get(0).accept(this);
    }
    if (node.params.size() >= 2) {
      for (int x = 1; x < node.params.size(); x++) {
        write(", ");
        node.params.get(x).accept(this);
      }
    }
    //print rest
    write(") {");
    newline();
    incIndent();
    for (VMInstr currNode : node.VM_stmts ) {
      write(indent());
      write(currNode.toString());
      newline();
    }
    decIndent();
    write(indent() + "}");
    newline();
    //spacing between functions
    newline();
  }

  
}
