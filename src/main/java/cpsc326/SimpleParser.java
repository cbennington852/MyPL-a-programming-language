/**
 * CPSC 326, Spring 2025
 * The Simple Parser implementation.
 *
 * Charles Bennington
 */

package cpsc326;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;




/**
 * Simple recursive descent parser for checking program syntax.
 */ 
public class SimpleParser {

  private Lexer lexer;          // the lexer
  private Token currToken;      // the current token

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

  //list of base types that are valid in expressions
  List<TokenType> validBaseTypes = new ArrayList<>(Arrays.asList(
      TokenType.INT_TYPE,
      TokenType.DOUBLE_TYPE,
      TokenType.STRING_TYPE,
      TokenType.BOOL_TYPE
  ));
  /**
   * Create a SimpleParser from the give lexer.
   * @param lexer The lexer for the program to parse.
   */ 
  public SimpleParser(Lexer lexer) {
    this.lexer = lexer;
  }

  /**
   * Run the parser.
   */
  public void parse() {
    advance();
    program();
    eat(TokenType.EOS, "expecting end of file");
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
   * @param targetTokenType The token type to check against.
   */
  private void eat(TokenType targetTokenType, String msg) {
    if (!match(targetTokenType))
      error(msg);
    advance();
  }
  
  /**
   * Helper to check that the current token is a binary operator.
   */
  private boolean isBinOp() {
    return matchAny(List.of(TokenType.PLUS, TokenType.MINUS, TokenType.TIMES,
                            TokenType.DIVIDE, TokenType.AND, TokenType.OR,
                            TokenType.EQUAL, TokenType.LESS, TokenType.GREATER,
                            TokenType.LESS_EQ, TokenType.GREATER_EQ,
                            TokenType.NOT_EQUAL));
  }

  /**
   * Helper to check that the current token is a literal value.
   */
  private boolean isLiteral() {
    return matchAny(List.of(TokenType.INT_VAL, TokenType.DOUBLE_VAL,
                            TokenType.STRING_VAL, TokenType.BOOL_VAL,
                            TokenType.NULL_VAL));
  }

  /**
   * Checks for a valid program.
   */ 
  private void program() {
    while (!match(TokenType.EOS)) {
      if (match(TokenType.STRUCT)) {
        advance();
        structDef();
      } else if (match(TokenType.ANNOTATION)) {
        VM_FunDef();
      }else {
        // else must be a function...
        funDef();
      }
    }
  }

  /**
   * Checks for a valid struct definition.
      <struct_def>
   */ 
  private void structDef() {
    // TODO: implement this function

    //check for struct name
    eat(TokenType.ID, "Malformed Struct");
    //check for left bracket {
    eat(TokenType.LBRACE, "Struct Missing Left Brace");

    fields();
    //check for last Right Bracket
    eat(TokenType.RBRACE, "Struct missing Right Brace");
  }


  private void fields () {
    // check to see if felids is right
    while (!match(TokenType.RBRACE)) {
      
      field();
      if (match(TokenType.RBRACE)) { // end, no need for comma, just end ds loop
        break;
      }
      eat(TokenType.COMMA, "Not a Comma, Commas separate values.");
    }
  }

  // <field> ::= ID : <data_type>
  private void field () {
    eat(TokenType.ID, "Not an Identifier");
    eat(TokenType.COLON, "Not a Colon");
    data_type(); // checks for data type
  }

  private void base_type () {
    if (matchAny(validVariableTokenTypes)) {
      advance();
    }
    else  {
      error("expected a base_type");
    }
  }

  /**
   * Checks to see if is a data type
   * <data_type> ::= <base_type> | ID | LBRACKET ( <base_type> | ID ) RBRACKET
   */
  void data_type () {
    //check for base type
    if (matchAny(validVariableTokenTypes)) {
      base_type();
    }
    //check to see if array
    else if (match(TokenType.LBRACKET)) {
      advance();
      if (matchAny(validVariableTokenTypes) || match(TokenType.ID)) {
        advance();
      }
      else if(match(TokenType.ID)) {
        advance();
      }
      else{
        error("Malformed Array");
      }
      eat(TokenType.RBRACKET, "MalformedArray");
    }
    //could be a function with a return type set to be a struct
    else if (match(TokenType.ID)){
      advance();
    }
    else {
      error("Unexpected token in program");
    }
  }

  /**
   * <fun_def> ::= <return_type> ID LPAREN <params> RPAREN <block>
   */
  private void funDef() {
    //checked the return type
    return_type();

    //check for required stuff
    eat(TokenType.ID, "Malformed function name");
    eat(TokenType.LPAREN, "function missing left parens");

    //check the params
    params();

    //needs a paren at end of func
    eat(TokenType.RPAREN, "function missing right parens");

    //check for block
    block();
  }


  private void VM_FunDef() {
    eat(TokenType.ANNOTATION, "Expeceted an annotation");
    //checked the return type
    return_type();
    //check for required stuff
    eat(TokenType.ID, "Malformed function name");
    eat(TokenType.LPAREN, "function missing left parens");
    //check the params
    params();
    //needs a paren at end of func
    eat(TokenType.RPAREN, "function missing right parens");
    //Special routing to process this
    //needs a brace
    eat(TokenType.LBRACE, "Missing left brace");
    //check for statements
    while (!match(TokenType.RBRACE)) {
      eat(TokenType.ID, "Excpected an VM Instr");
      fun_call(); //they should all be fun_calls...
    }
    //needs a left brace
    eat(TokenType.RBRACE, "Missing right brace");
  }
  // <return_type> ::= <data_type> | VOID_TYPE
  private void return_type () {
    if (match(TokenType.VOID_TYPE)) {
      advance(); // all good; keep going
    } else {
      data_type(); // must be a data type
    }
  }

  /**
   * Checks the params
   * <params> ::= <param> ( COMMA <param> )∗| ϵ
   * <param> ::= ID : <data_type>
   */
  private void params () {
    if (match(TokenType.RPAREN)) {
      return; //leave
    }
    param();
    while (match(TokenType.COMMA)) {
      advance();
      param();
    }
  }

  /**
   * <param> ::= ID : <data_type>
   */
  private void param () {
    eat(TokenType.ID, "Not an Identifier");
    eat(TokenType.COLON, "Not a Colon");
    data_type(); // checks for data type
  }

  /**
   * Checks for the block function
   * <block> ::= RBRACE ( <stmt> )∗ RBRACE
   */
  void block () {
    //needs a brace
    eat(TokenType.LBRACE, "Missing left brace");

    //check for statements
    while (!match(TokenType.RBRACE)) {
      statement();
    }

    //needs a left brace
    eat(TokenType.RBRACE, "Missing right brace");
  
  }

  /**
   * Checks for a statement
   * <stmt> ::= <var_stmt> | <while_stmt> | <if_stmt> | <for_stmt> | <return_stmt> | <assign_stmt> | <fun_call>
   */
  void statement() {
    //return stmt
    if (match(TokenType.RETURN)) {
      return_stmt();
    }
    //while stmt
    else if (match(TokenType.WHILE)) {
      while_stmt();
    }
    //if stmt
    else if (match(TokenType.IF)) {
      if_stmt();
    }
    //for stmt
    else if (match(TokenType.FOR)) {
      for_stmt();
    }
    //var stmt
    else if (match(TokenType.VAR)) {
      var_stmt();
    }

    //these two should have same starting case...
    // <assign_stmt> ::= <lvalue> ASSIGN <expr>
    // <fun_call> ::= ID LPAREN <args> RPAREN
    else {
      eat(TokenType.ID, "Expected an ID");
      if (match(TokenType.LPAREN)) { // case: <fun_call> ::= ID LPAREN <args> RPAREN
        fun_call();
      }
      else {
        assign_stmt();
      }
    }
    
  }

  // <while_stmt> ::= WHILE <expr> <block>
  private void while_stmt () {
    eat(TokenType.WHILE, "Excpted a while");
    expr();
    block();
  }

  private void return_stmt () {
    eat(TokenType.RETURN, "Expected a return");
    expr();
  }

  //assignment statment
  // <assign_stmt> ::= <lvalue> ASSIGN <expr>
  private void assign_stmt () {
    lvalue();
    eat(TokenType.ASSIGN, "Excpeted an assign");
    expr();
  }

  // <lvalue> ::= ID ( LBRACKET <expr> RBRACKET | ϵ ) ( DOT ID ( LBRACKET <expr> RBRACKET | ϵ ) )∗
  private void lvalue () {
    // 1. var_rvalue
    // subcase 1.1.1: LBRACKET <expr> RBRACKET
    if (match(TokenType.LBRACKET)) {
      advance();
      expr();
      eat(TokenType.RBRACKET, "Expected a right bracket here");
    }
    // subcase 1.1.2: ϵ
    else {
      // do nothing?
    }

    // accounts for the *
    // subcase 1.2.1 ( DOT ID LBRACKET <expr> RBRACKET | ϵ)
    if (match(TokenType.DOT)) {
      while (match(TokenType.DOT)) {
        advance();
        eat(TokenType.ID, "malformed .somthing value");
        if (match(TokenType.LBRACKET)) {
          eat(TokenType.LBRACKET, "excpeted a [ here");
          expr();
          eat(TokenType.RBRACKET, "Needs closing ]");
        } else {
          // ϵ
        }
      }
    } else {
      // do nothing?
    }
  }

  /**
   * <var_stmt> ::= VAR ID ( <var_init> | <var_type> ( <var_init> | ϵ ) )
   * <var_init> ::= ASSIGN <expr>
   * <var_type> ::= COLON <data_type>
   */
  private void var_stmt () {
    eat(TokenType.VAR, "expected a VAR");
    eat(TokenType.ID, "Not a valid identifier");
    //case 1: <var_init>
    if (match(TokenType.ASSIGN)) {
      var_init();
    }
    //case 2: <var_type> ( <var_init> | ϵ )
    else {
      var_type();
      // ( <var_init> | ϵ )
      if (match(TokenType.ASSIGN)) {
        var_init();
      }
      else {
        //do nothing ϵ
      }
    }
  }

  // <var_type> ::= COLON <data_type>
  private void var_type () {
    eat(TokenType.COLON, "Expected a colon");
    data_type();
  }


  private void var_init () {
    advance();
    expr();
  }


  /**
   * Checks formatting for a for statement
   * *IMPORTANT* does not eat or check the for
   * <for_stmt> ::= FOR ID FROM <expr> TO <expr> <block>
   */
  private void for_stmt () {
    eat(TokenType.FOR, "expected a for");
    eat(TokenType.ID, "Expected a identifier in a for loop");
    eat(TokenType.FROM, "For loop requires a from");
    expr();
    eat(TokenType.TO, "The to is missing or malformed");
    expr();
    block();
  }


  /**
   * An if statement *IMPORTANT:* IF part not included 
   * <if_stmt> ::= IF <expr> <block> ( ELSE ( <if_stmt> | <block> ) | ϵ )
   */
  private void if_stmt () {
    eat(TokenType.IF, "Expected an if");
    expr(); //terminates properly
    block();
    
    // check for else statment
    if (match(TokenType.ELSE)) {
      advance();
      
      //case 1:  <if_stmt>
      if (match(TokenType.IF)) {
        if_stmt();
      }
      //case 2: <block>
      else {
        block();
      }
    }
    else {
      //do nothing
    }
  }

  /**
   * Checks for an expression.
   * <expr> ::= ( <rvalue> | NOT <expr> | LPAREN <expr> RPAREN ) ( <bin_op> <expr>| ϵ )
   * <bin_op> ::= PLUS | MINUS | TIMES | DIVIDE | AND | OR | EQUAL | LESS | GREATER |LESS_EQ | GREATER_EQ | NOT_EQUAL
   * <rvalue> ::= <literal> | <new_rvalue> | <var_rvalue> | <fun_call>
   * <new_rvalue> ::= NEW ID LPAREN <args> RPAREN | NEW ( ID | <base_type> ) LBRACKET <expr> RBRACKET
   * <literal> ::= INT_VAL | DOUBLE_VAL | STRING_VAL | BOOL_VAL | NULL_VAL
   */
  void expr () {
    //1. First part of Expr
    // NOT <expr>
    if (match(TokenType.NOT)) {
      advance();
      expr();
    }
    // LPAREN <expr> RPAREN
    else if (match(TokenType.LPAREN)) {
      advance(); // eat the thing we found
      expr();
      eat(TokenType.RPAREN, "Needs a right paren.");
    }
    // <rvalue>
    else{
      rvalue();
    }
    
    //2. 2nd part of BinOp
    if (isBinOp()) {
      advance();
      expr();
    }
    else{
      
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
  private void rvalue() {
      
      if (isLiteral()) {
        advance();
      }
      else if(match(TokenType.NEW)) {
        new_rvalue();
      }
      else if (match(TokenType.ID))
      {
        advance();
        //two cases: 
        //1. var_rvalue
          //subcase 1.1.1: LBRACKET <expr> RBRACKET
          if (match(TokenType.LBRACKET)) {
            advance();
            expr();
            eat(TokenType.RBRACKET, "Expected a right bracket here");
          }
          //subcase 1.1.2: ϵ
          else {
            //do nothing?
          }

          //accounts for the *
          // subcase 1.2.1 ( DOT ID LBRACKET <expr> RBRACKET | ϵ)
          if (match(TokenType.DOT)) {
            while (match(TokenType.DOT)) {
              advance();
              eat(TokenType.ID, "malformed .somthing value");
              if (match(TokenType.LBRACKET)) {
                eat(TokenType.LBRACKET, "excpeted a [ here");
                expr();
                eat(TokenType.RBRACKET, "Needs closing ]");
              }
              else {
                // ϵ
              }
            }
          } else {
            // do nothing?
          }
          

        //2. fun_call
        if (match(TokenType.LPAREN)) {
          fun_call();
        }
        
      }
      else {
        error("Expected a literal, return value or function call");
      }
  }

  /**
   * A function call. *IMPORTANT* only calls after the ID has been aten
   * <fun_call> ::= ID LPAREN <args> RPAREN
   */
  private void fun_call () {
    eat(TokenType.LPAREN, "expected function call left paren");
    args();
    eat(TokenType.RPAREN, "unclosed parens exepected right parens");
  }

  /**
   * Right value with new in it
   * <new_rvalue> ::= NEW ID LPAREN <args> RPAREN | NEW ( ID | <base_type> ) LBRACKET <expr> RBRACKET
   */
  private void new_rvalue() {
    eat(TokenType.NEW, "New keyword missing or malformed");
    //two outcomes...both acceptable
    if (match(TokenType.ID) || matchAny(validBaseTypes)) {
      advance();
    }

    //we are here. LPAREN <args> RPAREN |  LBRACKET <expr> RBRACKET
    if (match(TokenType.LPAREN)){
      advance();
      args(); //args eating the RPAREN?
      eat(TokenType.RPAREN, "Expected a right paren after new rvalue.");
    }
    else if (match(TokenType.LBRACKET)){
      advance();
      expr();
      eat(TokenType.RBRACKET, "Expected a right bracket after new rvalue.");
    }
    else {
      error("Not a valid new declaration");
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
  private void args () {
    //empty args
    if (match(TokenType.RPAREN)) {
      return; //just return, the other function can figure it out
    }
    //full args
    expr();
    while(match(TokenType.COMMA)) {
      advance(); //consume comma
      expr();
    }
  }


  // ... and so on ...   
  // TODO: implement the rest of the recursive descent functions 
  
}
