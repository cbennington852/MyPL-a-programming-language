/**
 * CPSC 326, Spring 2025
 * The AST Parser implementation.
 *
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT
 */

package cpsc326;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
  

/**
 * Simple recursive descent parser for checking program syntax.
 */ 
public class ASTParser {

  private Lexer lexer;          // the lexer
  private Token currToken;      // the current token

  /**
   * Create a SimpleParser from the give lexer.
   * @param lexer The lexer for the program to parse.
   */ 
  public ASTParser(Lexer lexer) {
    this.lexer = lexer;
  }

  // list of valid tokens for function start
  List<TokenType> validVariableTokenTypes = new ArrayList<>(Arrays.asList(
      TokenType.INT_TYPE,
      TokenType.DOUBLE_TYPE,
      TokenType.CHAR_TYPE,
      TokenType.STRING_TYPE,
      TokenType.BOOL_TYPE,
      TokenType.VOID_TYPE
  // Also need to check if user has an array as a return parameter...
  ));

  // list of base types that are valid in expressions
  List<TokenType> validBaseTypes = new ArrayList<>(Arrays.asList(
      TokenType.INT_TYPE,
      TokenType.DOUBLE_TYPE,
      TokenType.STRING_TYPE,
      TokenType.BOOL_TYPE));

  /**
   * Run the parser.
   */
  public Program parse() {
    advance();
    Program p = program();
    eat(TokenType.EOS, "expecting end of file");
    return p;
  }

  /**
   * Generate and throw a mypl parser exception.
   * @param msg The error message.
   */
  private void error(String msg) {
    String lexeme = currToken.lexeme;
    int line = currToken.line;
    int column = currToken.column;
    String s = "[%d,%d] %s found '%s'";
    MyPLException.parseError(String.format(s, line, column, msg, lexeme));
  }

  /**
   * Move to the next lexer token, skipping comments.
   */
  private void advance() {
    currToken = lexer.nextToken();
    while (match(TokenType.COMMENT) || match(TokenType.MULCOMMENT))
      currToken = lexer.nextToken();
  }

  /**
   * Checks that the current token has the given token type.
   * @param targetTokenType The token type to check against.
   * @return True if the types match, false otherwise.
   */
  private boolean match(TokenType targetTokenType) {
    return currToken.tokenType == targetTokenType; 
  }

  /**
   * Checks that the current token is contained in the given list of
   * token types.
   * @param targetTokenTypes The token types ot check against.
   * @return True if the current type is in the given list, false
   * otherwise.
   */
  private boolean matchAny(List<TokenType> targetTokenTypes) {
    return targetTokenTypes.contains(currToken.tokenType);
  }

  /**
   * Advance to next token if current token matches the given token type.
   * @param targetType The token type to check against.
   */
  private void eat(TokenType targetTokenType, String msg) {
    if (!match(targetTokenType))
      error(msg);
    advance();
  }
  
  /**
   * Check if the current token is an allowed binary operator
   */
  private boolean isBinOp() {
    return matchAny(List.of(TokenType.PLUS, TokenType.MINUS, TokenType.TIMES,
                            TokenType.DIVIDE, TokenType.AND, TokenType.OR,
                            TokenType.EQUAL, TokenType.LESS, TokenType.GREATER,
                            TokenType.LESS_EQ, TokenType.GREATER_EQ,
                            TokenType.NOT_EQUAL));
  }

  /**
   * Check if the current token is a literal value
   */
  private boolean isLiteral() {
    return matchAny(List.of(TokenType.INT_VAL, TokenType.DOUBLE_VAL,
                            TokenType.STRING_VAL, TokenType.BOOL_VAL,
                            TokenType.NULL_VAL));
  }

  /**
   * Parse the program
   * @return the corresponding Program AST object
   */
  private Program program() {
    // TODO: implement this function
    Program programNode = new Program();
    while (!match(TokenType.EOS)) {
      if (match(TokenType.STRUCT)) {
        advance();
        //add to program
        programNode.structs.add(structDef());
      } else if (match(TokenType.ANNOTATION)) {
        advance();
        programNode.functions.add(VM_FunDef());
      }else {
        // else must be a function...
        programNode.functions.add(funDef());
      }
    }
    return programNode;
  }

  /*
  private FunDef funDef() {
    FunDef funDefNode = new FunDef();
    
    // checked the return type
    funDefNode.returnType = return_type();

    // check for required stuff
    //get the function name
    funDefNode.funName = currToken;
    eat(TokenType.ID, "Malformed function name");
    eat(TokenType.LPAREN, "function missing left parens");

    // check the params
    funDefNode.params = params();

    // needs a paren at end of func
    eat(TokenType.RPAREN, "function missing right parens");

    // check for block
    funDefNode.stmts = block();

    return funDefNode;
  }
   */

   private VM_FunDef VM_FunDef() {
    VM_FunDef node = new VM_FunDef();

    //checked the return type
    node.returnType = return_type();

    //check for required stuff
    node.funName = currToken;
    eat(TokenType.ID, "Malformed function name");
    eat(TokenType.LPAREN, "function missing left parens");

    //check the params
    node.params = params();

    //needs a paren at end of func
    eat(TokenType.RPAREN, "function missing right parens");

    //Special routing to process this
    //needs a brace
    eat(TokenType.LBRACE, "Missing left brace");
    node.VM_stmts = new ArrayList<>();
    //check for statements
    while (!match(TokenType.RBRACE)) {
      //process each individual VMInstruction
      node.VM_stmts.add(VM_instr());
    }

    //needs a left brace
    eat(TokenType.RBRACE, "Missing right brace");
    return node;
  }


  private VMInstr VM_instr() {
    VMInstr node = new VMInstr(null);
    //eat the ID
    String VM_Call = currToken.lexeme;
    //convert into opcode....
    switch (VM_Call) {
      case "PUSH":
          node.opcode = OpCode.PUSH;
          break;
      case "POP":
          node.opcode = OpCode.POP;
          break;
      case "STORE":
          node.opcode = OpCode.STORE;
          break;
      case "LOAD":
          node.opcode = OpCode.LOAD;
          break;
      case "ADD":
          node.opcode = OpCode.ADD;
          break;
      case "SUB":
          node.opcode = OpCode.SUB;
          break;
      case "MUL":
          node.opcode = OpCode.MUL;
          break;
      case "DIV":
          node.opcode = OpCode.DIV;
          break;
      case "CMPLT":
          node.opcode = OpCode.CMPLT;
          break;
      case "CMPLE":
          node.opcode = OpCode.CMPLE;
          break;
      case "CMPEQ":
          node.opcode = OpCode.CMPEQ;
          break;
      case "CMPNE":
          node.opcode = OpCode.CMPNE;
          break;
      case "AND":
          node.opcode = OpCode.AND;
          break;
      case "OR":
          node.opcode = OpCode.OR;
          break;
      case "NOT":
          node.opcode = OpCode.NOT;
          break;
      case "JMP":
          node.opcode = OpCode.JMP;
          break;
      case "JMPF":
          node.opcode = OpCode.JMPF;
          break;
      case "CALL":
          node.opcode = OpCode.CALL;
          break;
      case "RET":
          node.opcode = OpCode.RET;
          break;
      case "WRITE":
          node.opcode = OpCode.WRITE;
          break;
      case "READ":
          node.opcode = OpCode.READ;
          break;
      case "LEN":
          node.opcode = OpCode.LEN;
          break;
      case "GETC":
          node.opcode = OpCode.GETC;
          break;
      case "TOINT":
          node.opcode = OpCode.TOINT;
          break;
      case "TODBL":
          node.opcode = OpCode.TODBL;
          break;
      case "TOSTR":
          node.opcode = OpCode.TOSTR;
          break;
      case "ALLOCS":
          node.opcode = OpCode.ALLOCS;
          break;
      case "SETF":
          node.opcode = OpCode.SETF;
          break;
      case "GETF":
          node.opcode = OpCode.GETF;
          break;
      case "ALLOCA":
          node.opcode = OpCode.ALLOCA;
          break;
      case "SETI":
          node.opcode = OpCode.SETI;
          break;
      case "GETI":
          node.opcode = OpCode.GETI;
          break;
      case "DUP":
          node.opcode = OpCode.DUP;
          break;
      case "NOP":
          node.opcode = OpCode.NOP;
          break;
      default:
          error("Not an Opcode called in OpCode section");
    }
    eat(TokenType.ID, "VMINSTR...expected an id");
    eat(TokenType.LPAREN, "Wheres the LPAREN");
    //get the operand
    if (!match(TokenType.RPAREN)) {
      String operandVal = currToken.lexeme;
      if (currToken.tokenType == TokenType.INT_VAL)
          node.operand = Integer.parseInt(operandVal);
      else if (currToken.tokenType == TokenType.DOUBLE_VAL)
          node.operand = Double.parseDouble(operandVal);
      else if (currToken.tokenType == TokenType.STRING_VAL) {
          operandVal = operandVal.replace("\\n", "\n");
          operandVal = operandVal.replace("\\t", "\t");
          operandVal = operandVal.replace("\\r", "\r");
          node.operand = operandVal;
      } else if (operandVal.equals("true"))
          node.operand = true;
      else if (operandVal.equals("false"))
          node.operand = false;
      else if (operandVal.equals("null"))
          node.operand = VM.NULL;
      advance();
    }
    else {//no operand
      
    }
    
    //check the stuff and return ultimately 
    eat(TokenType.RPAREN, "Bruh wheres the RPAREN ");
    return node;
  }

  /**
   * Parse a struct definition
   * @return the corresponding StructDef AST object
      //set the name and the fields
   */
  private StructDef structDef() {
    StructDef structNode = new StructDef();
    // save struct name
    structNode.structName = currToken;
    // check for struct name
    eat(TokenType.ID, "Malformed Struct");
    // check for left bracket {
    eat(TokenType.LBRACE, "Struct Missing Left Brace");

    //call feilds
    structNode.fields = fields();
    
    // check for last Right Bracket
    eat(TokenType.RBRACE, "Struct missing Right Brace");
    
    return structNode;
  }

  private List<VarDef> fields() {
    List<VarDef> listFelids = new ArrayList<>();
    // check to see if felids is right
    while (!match(TokenType.RBRACE)) {

      listFelids.add(field());
      if (match(TokenType.RBRACE)) { // end, no need for comma, just end ds loop
        break;
      }
      eat(TokenType.COMMA, "Not a Comma, Commas separate values.");
    }

    return listFelids;
  }

  // <field> ::= ID : <data_type>
  private VarDef field() {
    VarDef varNode = new VarDef();
    //get the var name
    varNode.varName = currToken;
    eat(TokenType.ID, "Not an Identifier");
    eat(TokenType.COLON, "Not a Colon");
    varNode.dataType = data_type(); // checks for data type
    return varNode;
  }

  /**
   * Checks to see if is a data type
   * <data_type> ::= <base_type> | ID | LBRACKET ( <base_type> | ID ) RBRACKET
   */
  DataType data_type() {
    DataType dateTypeNode = new DataType();
    // check for base type
    if (matchAny(validVariableTokenTypes)) {
      //log the type
      dateTypeNode.type = currToken;
      base_type();
      dateTypeNode.isArray = false;
    }
    // check to see if array
    else if (match(TokenType.LBRACKET)) {
      
      dateTypeNode.isArray = true;
      advance();
      if (matchAny(validVariableTokenTypes) || match(TokenType.ID)) {
        //log the type
        dateTypeNode.type = currToken;
        advance();
      } else {
        error("Malformed Array");
      }
      eat(TokenType.RBRACKET, "MalformedArray");
    }
    // could be a function with a return type set to be a struct
    else if (match(TokenType.ID)) {
      //log info
      dateTypeNode.type = currToken;
      dateTypeNode.isArray = false;
      //advance to next
      advance();
    } else {
      error("Unexpected token in program");
    }
    return dateTypeNode;
  }

  private void base_type() {
    if (matchAny(validVariableTokenTypes)) {
      advance();
    } else {
      error("expected a base_type");
    }
  }

  /**
   * Parse a function definition
   * @return the corresponding FunDef AST object
   */
  private FunDef funDef() {
    FunDef funDefNode = new FunDef();
    
    // checked the return type
    funDefNode.returnType = return_type();

    // check for required stuff
    //get the function name
    funDefNode.funName = currToken;
    eat(TokenType.ID, "Malformed function name");
    eat(TokenType.LPAREN, "function missing left parens");

    // check the params
    funDefNode.params = params();

    // needs a paren at end of func
    eat(TokenType.RPAREN, "function missing right parens");

    // check for block
    funDefNode.stmts = block();

    return funDefNode;
  }

  /**
   * Checks for the block function
   * <block> ::= RBRACE ( <stmt> )∗ RBRACE
   */
  private List<Stmt> block () {
    List<Stmt> stmtsList = new ArrayList<>();
    // needs a brace
    eat(TokenType.LBRACE, "Missing left brace");

    // check for statements
    while (!match(TokenType.RBRACE)) {
      stmtsList.add(statement());
    }

    // needs a left brace
    eat(TokenType.RBRACE, "Missing right brace");
    return stmtsList;
  }

  /**
   * Checks for a statement
   * <stmt> ::= <var_stmt> | <while_stmt> | <if_stmt> | <for_stmt> | <return_stmt>
   * | <assign_stmt> | <fun_call>
   */
  Stmt statement() {
    // return stmt
    if (match(TokenType.RETURN)) {
      return return_stmt();
    }
    
    // while stmt
    else if (match(TokenType.WHILE)) {
      return while_stmt();
    }
    // var stmt
    else if (match(TokenType.VAR)) {
      return var_stmt();//working
    }
    // for stmt
    else if (match(TokenType.FOR)) {
      return for_stmt();
    }
    // if stmt
    else if (match(TokenType.IF)) {
      return if_stmt();
    }

    // these two should have same starting case...
    // <assign_stmt> ::= <lvalue> ASSIGN <expr>
    // <lvalue> ::= ID ( LBRACKET <expr> RBRACKET | ϵ ) ( DOT ID ( LBRACKET <expr> RBRACKET | ϵ ) )∗
    // <fun_call> ::= ID LPAREN <args> RPAREN
    else {
      Token savedToken = currToken;
      eat(TokenType.ID, "Expected an ID");
      if (match(TokenType.LPAREN)) { // case: <fun_call> ::= ID LPAREN <args> RPAREN
        //AST : CallRValue
        return fun_call(savedToken);
      } else {
        return assign_stmt(savedToken);
      }
    }
    

  }


  //assignment statment
  // <assign_stmt> ::= <lvalue> ASSIGN <expr>
  private AssignStmt assign_stmt (Token savedToken) {
    AssignStmt node = new AssignStmt();
    node.lvalue = lvalue(savedToken);
    eat(TokenType.ASSIGN, "Excpeted an assign");
    node.expr = expr();
    return node;
  }

  // <lvalue> ::= ID ( LBRACKET <expr> RBRACKET | ϵ ) ( DOT ID ( LBRACKET <expr> RBRACKET | ϵ ) )∗
  private List<VarRef> lvalue (Token savedToken) {
    List<VarRef> nodeList = new ArrayList<>();

    //save the ID  
    VarRef initNode = new VarRef();
    initNode.varName = savedToken; 
    
    // 1. var_rvalue
    // subcase 1.1.1: LBRACKET <expr> RBRACKET
    if (match(TokenType.LBRACKET)) {
      advance();
      //saved expr
      initNode.arrayExpr = Optional.of(expr());
      eat(TokenType.RBRACKET, "Expected a right bracket here");
    }
    // subcase 1.1.2: ϵ
    else {
      // do nothing?
    }
    //save the first node
    nodeList.add(initNode);
    // accounts for the *
    // subcase 1.2.1 ( DOT ID LBRACKET <expr> RBRACKET | ϵ)
    if (match(TokenType.DOT)) {
      while (match(TokenType.DOT)) {
        advance();
        VarRef node = new VarRef();
        node.varName = currToken;
        eat(TokenType.ID, "malformed .somthing value");
        if (match(TokenType.LBRACKET)) {
          eat(TokenType.LBRACKET, "excpeted a [ here");
          node.arrayExpr = Optional.of(expr());
          eat(TokenType.RBRACKET, "Needs closing ]");
        } else {
          // ϵ
        }
        nodeList.add(node);
      }
    } else {
      // do nothing?
    }
    return nodeList;
  }

  /**
   * An if statement *IMPORTANT:* IF part not included 
   * <if_stmt> ::= IF <expr> <block> ( ELSE ( <if_stmt> | <block> ) | ϵ )
   */
  private IfStmt if_stmt () {
    IfStmt node = new IfStmt();
    eat(TokenType.IF, "Expected an if");
    node.condition = expr(); //terminates properly
    node.ifStmts = block();
    
    // check for else statment
    if (match(TokenType.ELSE)) {
      advance();
      
      //case 1:  <if_stmt>
      if (match(TokenType.IF)) {
        node.elseIf = Optional.of(if_stmt());
      }
      //case 2: <block>
      else {
        node.elseStmts = Optional.of(block());
      }
    }
    else {
      //do nothing
    }
    return node;
  }

  /**
   * Checks formatting for a for statement
   * *IMPORTANT* does not eat or check the for
   * <for_stmt> ::= FOR ID FROM <expr> TO <expr> <block>
   */
  private ForStmt for_stmt () {
    ForStmt node = new ForStmt();
    eat(TokenType.FOR, "expected a for");
    node.varName = currToken;
    eat(TokenType.ID, "Expected a identifier in a for loop");
    eat(TokenType.FROM, "For loop requires a from");
    node.fromExpr = expr();
    eat(TokenType.TO, "The to is missing or malformed");
    node.toExpr = expr();
    node.stmts = block();
    return node;
  }


  /**
   * <var_stmt> ::= VAR ID ( <var_init> | <var_type> ( <var_init> | ϵ ) )
   * <var_init> ::= ASSIGN <expr>
   * <var_type> ::= COLON <data_type>
   */
  private VarStmt var_stmt () {
    VarStmt node = new VarStmt();
    eat(TokenType.VAR, "expected a VAR");
    node.varName = currToken; //save the variable name
    eat(TokenType.ID, "Not a valid identifier");
    // case 1: <var_init>
    
    if (match(TokenType.ASSIGN)) {
      
      node.expr = Optional.of(var_init());
    }
    // case 2: <var_type> ( <var_init> | ϵ )
    else {
      
      node.dataType = Optional.of(var_type());
      
      // ( <var_init> | ϵ )
      if (match(TokenType.ASSIGN)) {
        node.expr = Optional.of(var_init());
        
      } else {
        // do nothing ϵ
      }
    }
   
    return node;
  }

  // <var_type> ::= COLON <data_type>
  private DataType var_type() {
    eat(TokenType.COLON, "Expected a colon");
    return data_type();
  }

  private Expr var_init() {
    advance();
    return expr();
  }


// <while_stmt> ::= WHILE <expr> <block>
private WhileStmt while_stmt() {
  WhileStmt node = new WhileStmt();
  eat(TokenType.WHILE, "Excpted a while");
  node.condition = expr();
  node.stmts = block();
  return node;
}

private ReturnStmt return_stmt() {
  ReturnStmt node = new ReturnStmt();
  eat(TokenType.RETURN, "Expected a return");
  node.expr = expr();
  return node;
}



  /**
   * Checks for an expression.
   * <expr> ::= ( <rvalue> | NOT <expr> | LPAREN <expr> RPAREN ) ( <bin_op> <expr>| ϵ )
   * <bin_op> ::= PLUS | MINUS | TIMES | DIVIDE | AND | OR | EQUAL | LESS |
   * GREATER |LESS_EQ | GREATER_EQ | NOT_EQUAL
   * <rvalue> ::= <literal> | <new_rvalue> | <var_rvalue> | <fun_call>
   * <new_rvalue> ::= NEW ID LPAREN <args> RPAREN | NEW ( ID | <base_type> )  LBRACKET <expr> RBRACKET
   * <literal> ::= INT_VAL | DOUBLE_VAL | STRING_VAL | BOOL_VAL | NULL_VAL
   */
  private Expr expr () {
    // 1. First part of Expr
    Expr mainExprNode;

    // NOT <expr> (unary expression)
    if (match(TokenType.NOT)) {
      UnaryExpr exprNode = new UnaryExpr();
      exprNode.unaryOp = currToken;
      advance();
      exprNode.expr = expr();
      mainExprNode = exprNode;
    }
    // LPAREN <expr> RPAREN (grouping)
    else if (match(TokenType.LPAREN)) {
      advance(); // eat LPAREN
      mainExprNode = expr();
      eat(TokenType.RPAREN, "Needs a right paren.");
    }
    // <rvalue>
    else { //<rvalue> ::= <literal> | <new_rvalue> | <var_rvalue> | <fun_call>
      BasicExpr node = new BasicExpr();
      node.rvalue = rvalue();
      mainExprNode = node; // set the main node, cannot return now, must check other stuff
    }

    // 2. Handling binary expressions
    //   * <expr> ::= ( <rvalue> | NOT <expr> | LPAREN <expr> RPAREN ) ( <bin_op> <expr>| ϵ )
    //   * <bin_op> ::= PLUS | MINUS | TIMES | DIVIDE | AND | OR | EQUAL | LESS |
    //looking at this part ... ( <bin_op> <expr>| ϵ )
    if (isBinOp()) {
      BinaryExpr binExprNode = new BinaryExpr();
      while (isBinOp()) {
        //save the binary operator
        binExprNode.binaryOp = currToken;
        //eat the binary operator
        advance();
        //save the left hand side
        binExprNode.lhs = mainExprNode;
        //call the stuff next to ths expr
        binExprNode.rhs = expr();
      }

      return binExprNode;
    }
    else {
      return mainExprNode;
    }
  }

  /**
   * A right value like 6,
   * <rvalue> ::= <literal> | <new_rvalue> | <var_rvalue> | <fun_call>
   *
   * <new_rvalue> ::= NEW ID LPAREN <args> RPAREN | NEW ( ID | <base_type> ) LBRACKET <expr> RBRACKET
   * <var_rvalue> ::= ID ( LBRACKET <expr> RBRACKET | ϵ ) ( DOT ID ( LBRACKET <expr> RBRACKET | ϵ ) )∗
   * <fun_call> ::= ID LPAREN <args> RPAREN
   */
  private RValue rvalue () {
    if (isLiteral()) {
      SimpleRValue rvalueNode = new SimpleRValue();
      rvalueNode.literal = currToken;
      advance();
      return rvalueNode;
    } 
    else if (match(TokenType.NEW)) {
      return new_rvalue();
    } 
    else if (match(TokenType.ID)) {
      Token savedToken = currToken;
      advance();
      // two cases:
      // 1. fun_call 
      if (match(TokenType.LPAREN)) {
        return fun_call(savedToken);
      }
      // 2. var_rvalue
      else {
        return varRValue(savedToken);
      }
    } 
    else {
      error("Expected a literal, return value or function call");
      return null;
    }
  }

  //<var_rvalue> ::= ID ( LBRACKET <expr> RBRACKET | ϵ ) ( DOT ID ( LBRACKET <expr> RBRACKET | ϵ ) )∗
  private VarRValue varRValue (Token savedToken) {
      VarRValue node = new VarRValue();
      List<VarRef> nodeList = new ArrayList<>();
      VarRef initNode = new VarRef();
      //saved token going in
      initNode.varName = savedToken;
      if (match(TokenType.LBRACKET)) {
        advance();
        initNode.arrayExpr = Optional.of(expr());
        eat(TokenType.RBRACKET, "Expected a right bracket here");
      }
      // subcase 1.1.2: ϵ
      else {
        // do nothing?
      }
      
      //add the first node to the list
      nodeList.add(initNode);

      // accounts for the *
      // subcase 1.2.1 ( DOT ID LBRACKET <expr> RBRACKET | ϵ)
      if (match(TokenType.DOT)) {
        while (match(TokenType.DOT)) {
          advance();
          VarRef newNode = new VarRef();
          newNode.varName = currToken; // add the ID value
          eat(TokenType.ID, "malformed .somthing value");
          if (match(TokenType.LBRACKET)) {
            eat(TokenType.LBRACKET, "excpeted a [ here");
            newNode.arrayExpr = Optional.of(expr());
            eat(TokenType.RBRACKET, "Needs closing ]");
          } else {
            // ϵ
          }
          nodeList.add(newNode);
        }
      } else {
        // do nothing?
      }

      node.path = nodeList;
      return node;
  }


  /**
   * A function call. *IMPORTANT* only calls after the ID has been aten
   * <fun_call> ::= ID LPAREN <args> RPAREN
   */
   /* 
   Represents a function call (which can be used as both a statement
 * and an rvalue)
 */
  private CallRValue fun_call(Token savedToken) {
    CallRValue node = new CallRValue();
    node.funName = savedToken;
    eat(TokenType.LPAREN, "expected function call left paren");
    node.args = args();
    eat(TokenType.RPAREN, "unclosed parens exepected right parens");
    return node;
  }

  /**
   * Right value with new in it
   * <new_rvalue> ::= NEW ID LPAREN <args> RPAREN | NEW ( ID | <base_type> ) LBRACKET <expr> RBRACKET
   */
  private NewRValue new_rvalue() {
    eat(TokenType.NEW, "New keyword missing or malformed");
    
    //save the thing here can be either ID or <base_type>
    Token saved = currToken;
    
    // two outcomes...both acceptable
    if (match(TokenType.ID) || matchAny(validBaseTypes)) {
      advance();
    }
    // we are here. LPAREN <args> RPAREN | LBRACKET <expr> RBRACKET
    if (match(TokenType.LPAREN)) { // LPAREN <args> RPAREN ..... new struct
      NewStructRValue node = new NewStructRValue();
      advance();
      node.args = args(); 
      if (saved.tokenType != TokenType.ID) { //solve a small error case
        error("Expected an ID");
      }
      node.structName = saved;
      eat(TokenType.RPAREN, "Expected a right paren after new rvalue.");
      return node;
    } else if (match(TokenType.LBRACKET)) { // LBRACKET <expr> RBRACKET ..... new array value
      NewArrayRValue node = new NewArrayRValue();
      advance();
      node.arrayExpr = expr();
      node.type = saved;
      eat(TokenType.RBRACKET, "Expected a right bracket after new rvalue.");
      return node;
    } else {
      error("Not a valid new declaration");
      return null; // pleasing JAVA gods
    }
  }

  /**
   * Handles args... like
   * 42 A single expression.
   * x A single variable expression.
   * a, b Two expressions separated by a comma.
   * 1, 2, 3, 4 A list of expressions.
   * <args> ::= <expr> ( COMMA <expr> )∗| ϵ
   */
  private List<Expr> args() {
    List<Expr> nodeList = new ArrayList<>();
    // empty args
    if (match(TokenType.RPAREN)) {
      return nodeList; // just return, the other function can figure it out
    }
    // full args
    nodeList.add(expr());
    while (match(TokenType.COMMA)) {
      advance(); // consume comma
      nodeList.add(expr());
    }

    return nodeList;
  }

  /**
   * Checks the params
   * <params> ::= <param> ( COMMA <param> )∗| ϵ
   * <param> ::= ID : <data_type>
   */
  private List<VarDef> params() {
    List<VarDef> listParams = new ArrayList<>();
    if (match(TokenType.RPAREN)) {
      return listParams; // leave return a empty array
    }
    listParams.add(param());
    while (match(TokenType.COMMA)) {
      advance();
      listParams.add(param());
    }
    return listParams;
  }


  /**
   * <param> ::= ID : <data_type>
   */
  private VarDef param() {
    VarDef varNode = new VarDef();
    // get the var name
    varNode.varName = currToken;
    eat(TokenType.ID, "Not an Identifier");
    eat(TokenType.COLON, "Not a Colon");
    varNode.dataType = data_type(); // checks for data type
    return varNode;
  }


  // <return_type> ::= <data_type> | VOID_TYPE
  private DataType return_type() {
    DataType dataTypeNode = new DataType();
    if (match(TokenType.VOID_TYPE)) {
      dataTypeNode.type = currToken;
      dataTypeNode.isArray = false;
      advance(); // all good; keep going
    } else {
      return data_type(); // must be a data type
    }

    return dataTypeNode;
  }


  // ... and so on ...   
  // TODO: implement the rest of the recursive descent functions 
  
}
