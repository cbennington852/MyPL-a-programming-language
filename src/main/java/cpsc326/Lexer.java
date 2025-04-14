/**
 * CPSC 326, Spring 2025
 * MyPL Lexer Implementation.
 *
 * PUT YOUR NAME HERE IN PLACE OF THIS TEXT
 */

package cpsc326;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;


/**
 * The Lexer class takes an input stream containing mypl source code
 * and transforms (tokenizes) it into a stream of tokens.
 */
public class Lexer {

  private BufferedReader buffer; // handle to the input stream
  private int line = 1;          // current line number
  private int column = 0;        // current column number

  /**
   * Creates a new Lexer object out of an input stream. 
   */
  public Lexer(InputStream input) {
    buffer = new BufferedReader(new InputStreamReader(input));
  }

  /**
   * Helper function to read a single character from the input stream.
   * @return A single character
   */ 
  private char read() {
    try {
      ++column;
      return (char)buffer.read();
    } catch(IOException e) {
      error("read error", line, column + 1);
    }
    return (char)-1;
  }

  /**
   * Helper function to look ahead one character in the input stream. 
   * @return A single character
   */ 
  private char peek() {
    int ch = -1;
    try {
      buffer.mark(1);
      ch = (char)buffer.read();
      buffer.reset();
      return (char)ch;
    } catch(IOException e) {
      error("read error", line, column + 1);
    }
    return (char)-1;
  }


  /**
   * Helper function to check if the given character is an end of line
   * symbol. 
   * @return True if the character is an end of line character and
   * false otherwise.
   */ 
  private boolean isEOL(char ch) {
    if (ch == '\n')
      return true;
    if (ch == '\r' && peek() == '\n') {
      read();
      return true;
    }
    else if (ch == '\r')
      return true;
    return false;
  }
  
  /**
   * Helper function to check if the given character is an end of file
   * symbol.
   * @return True if the character is an end of file character and
   * false otherwise.
   */ 
  private boolean isEOF(char ch) {
    return ch == (char)-1; 
  }
  
  /**
   * Print an error message and exit the program.
   */
  private void error(String msg, int line, int column) {
    String s = "[%d,%d] %s";
    MyPLException.lexerError(String.format(s, line, column, msg));
  }

  /**
   * Obtains and returns the next token in the stream.
   * @return The next token in the stream.
   */
  public Token nextToken() {
    // read the initial character
    char ch = read();

    // TODO: Finish this method

    //(1) read all whitespace (while checking for EOF); 
    ch = bypassWhitespace(ch);

    //(2) check for EOF;
    if (isEOF(ch)) {
      return new Token(TokenType.EOS, "end-of-stream", line, column);
    }

    //thing that we will use to check
    Token returnToken = null;
    //(9) check for multi-line comment
    returnToken = readMultiLineComment(ch);
    if (!(returnToken == null)) {
      return returnToken;
    }

    //(3) check for single character tokens (e.g., arithmetic operators, punctuation, etc.); 
    //needs to be updated to include +,/,*, and other words like these
    returnToken = returnSingleCharToken(ch);
    if (!(returnToken == null)) {
      //we found a single char token... returning now
      return returnToken;
    }

    //(4) check for the trickier symbols that can involve or require two characters (e.g., < vs <=, !=, and so on); 
    //all things equal signs are done here
    returnToken = returnCheckMultiCharacter(ch);
    if (!(returnToken == null)) {
      return returnToken;
    }

    //(5) check for comments (note that we will use COMMENT tokens initially, then ignore them in the parser later); 
    returnToken = checkForComments(ch);
    if (!(returnToken == null)) {
      return returnToken;
    }

    //(6) check for string values; 
    returnToken = checkForStringValue(ch);
    if(!(returnToken == null)) {
      return returnToken;
    }

    //(7) check for integer and double values; 
    returnToken = readIntAndDoubleValues(ch);
    if (!(returnToken == null)) {
      return returnToken;
    }

    //(8) check for reserved words and check for booleans and void types
    returnToken = readReservedWords(ch);
    if (!(returnToken == null)) {
      return returnToken;
    }

    //(9) check for a annotation
    returnToken = checkForAnnotation(ch);
    if (!(returnToken == null)) {
      return returnToken;
    }
    

    //else, this 'token' is invalid, and will be returned as an error
    error("unrecognized symbol '" + ch+"'",line,column);

    return null;
  }

  private Token checkForAnnotation (char ch) {
    if (ch == '@') {
      if (read() != 'V') {
        error("called Invalid annoation", column, line);
      }
      if (read() != 'M') {
        error("called Invalid annoation", column, line);
      }
      return new Token(TokenType.ANNOTATION, "VM", column, line );
    }
    else {
      return null;
    }
  }

  private Token readMultiLineComment (char ch) {
    String contents = "";
    if ((ch == '/') && (peek() == '*')) {
      //multi-line comment
      ch = read();
      ch = read();
      while (true) {
        if (isEOF(ch)) {
          error("non-terminated multi-line comment", line, column);
        }
        if (((ch == '*') && (peek() == '/'))) {
          read();
          return new Token(TokenType.MULCOMMENT, contents, line, column);
        }
        contents += ch;
        ch = read();
      }
    }
    //check for random unbounded
    else if ((ch == '*') && (peek() == '/')) {
      error("non-terminated multi-line comment", line, column);
      return null;
    }
    else {
      return null;
    }
  }


  /**
   * Reads reserved words like true, false, null, for
   * Permissable Identifiers Letters, numbers, _ 
   * @param ch
   * @return
   */
  private Token readReservedWords (char ch) {
    if (Character.isLetter(ch)) {
      String word = "";
      int savedColumn = column;
      int savedLine = line;
      boolean permissibleID = Character.isLetter(ch) || Character.isDigit(ch) || (ch == '_');

      //Read the word --> stop reading if encounter non-letter, whitespace, EOL, EOF
      //ID's can actually have non-letters in them, so we should look out for this
      //Example input: x xs f0_0 foo_bar foo_bar_baz quix__
      while ((Character.isLetter(peek()) || Character.isDigit(peek()) || (peek() == '_')) 
            && (peek() != ' ') 
            && (!isEOL(peek())) && (!isEOF(peek()))) 
      {
        word += ch;
        ch = read();
      }

      //add the last little bit?
      word += ch;
      if (peek() == ' ') {
        read(); // swallow whitespace
      }
      
      //check to see what type of word dis is...
      TokenType type = null;
      switch (word) {
        case "true":
          type = TokenType.BOOL_VAL;
          break;
        case "false":
          type = TokenType.BOOL_VAL;
          break;
        case "null":
          type = TokenType.NULL_VAL;
          break;
        case "int":
          type = TokenType.INT_TYPE;
          break;
        case "double":
          type = TokenType.DOUBLE_TYPE;
          break;
        case "string":
          type = TokenType.STRING_TYPE;
          break;
        case "bool":
          type = TokenType.BOOL_TYPE;
          break;
        case "void":
          type = TokenType.VOID_TYPE;
          break;
        case "and":
          type = TokenType.AND;
          break;
        case "or":
          type = TokenType.OR;
          break;
        case "not":
          type = TokenType.NOT;
          break;
        case "if":
          type = TokenType.IF;
          break;
        case "else":
          type = TokenType.ELSE;
          break;
        case "while":
          type = TokenType.WHILE;
          break;
        case "for":
          type = TokenType.FOR;
          break;
        case "var":
          type = TokenType.VAR;
          break;
        case "from":
          type = TokenType.FROM;
          break;
        case "to":
          type = TokenType.TO;
          break;
        case "return":
          type = TokenType.RETURN;
          break;
        case "struct":
          type = TokenType.STRUCT;
          break;
        case "new":
          type = TokenType.NEW;
          break;
        default:
          type = TokenType.ID;
      }

      //determined type
      return new Token(type, word, savedLine, savedColumn);
    }  
    else {
      return null;
    }
  }
 
 
  /**
   * Lex parsing for int values and double values. UNFINISHED
   * @param ch Input char
   * @return The token if it is a number(int or double), otherwise it returns null
   */

   //SEE numbersWithNoSpaces
  private Token readIntAndDoubleValues (char ch) {
      String numberContents = "";
      TokenType numType = null;
      int periodCount = 0;
      int savedColumn = column;
      int savedLine = line;
      if (Character.isDigit(ch)) { //is a number, don't know which yet

        //keeps looping while the ch is a number of some kind
        //NEEDS TO BE REPLACED WITH A peek()
        //if digit keep looping, if . only loop is period count is != 1
        while (Character.isDigit(peek()) || (peek() == '.') ) { 
          if ((peek() == '.') && (periodCount >= 1)){
            break;
          }
          if (ch == '.') {
            periodCount++;
          }
          numberContents += ch;
          ch = read();
          // error handling
          // current character is a . and the next is not a digit
          if ((ch == '.') && (!Character.isDigit(peek()))) {
            error("missing digit after decimal", line, column+1);
          }
        }
        numberContents += ch;

        //looping done ... there are some things to check for 
        //period count should be 1 or zero
        if (periodCount == 1) {
          numType = TokenType.DOUBLE_VAL;
          //check to make sure we have a valid number
          try {
            Double.parseDouble(numberContents);
          } catch (NumberFormatException e) {
            error(e.getMessage(), line, column);
          }
        }
        else {
          numType = TokenType.INT_VAL;
          // check to make sure we have a valid number
          if ((numberContents.length() > 1) && (numberContents.charAt(0) == '0')) {
            error("leading zero in number", line, column-1);
          }
         
        }
        return new Token(numType, numberContents, savedLine, savedColumn);
      }
      else { //NaN  return null
        return null;
      }
  }

  /**
   * Checks to see if we have a string value, and then returns said string value
   * @param ch input char
   * @return Token returns null if nothing is found
   */
  private Token checkForStringValue (char ch) {
    if (ch == '"') { //we have the start of a string.
      String stringContents = "";
      int savedColumn = column;
      int savedLine = line;
      ch = read(); //skip past the "
      //loop thru and collect the rest of the string!
      while (ch != '"') {
          //if EOF throw error
          if (isEOF(ch) || isEOL(ch)) {
            //supposed to have error here
            error("non-terminated string", line, column);
          }
          else {
            stringContents += ch;
            ch = read();
          }
      }

      //we now have the string...
      return new Token(TokenType.STRING_VAL, stringContents, savedLine, savedColumn);
    }
    else {
      return null;
    }
  }

  /**
   * bypasses whitespace
   * @param c input Char
   * @return the char
   */
   private char bypassWhitespace(char ch) {
     while (Character.isWhitespace(ch) || isEOL(ch)) {
       // do nothing with said whitespace

       // if EOL "End of Line"
       if (isEOL(ch)) {
         column = 0;
         line++;
         ch = read();
       }
       else {
         // move froward
         ch = read();
       }
     }
     return ch;
   }


/**
 * Checks for comments 
 * @param c input char
 * @return returns a token if found, otherwise returns null
 */
  private Token checkForComments(char c ) {
    if (c == '#') { //comment detected
      String commentBody = "";
      int savedColumn = column; //save the first position of the column
      //loop thru and add all of the comments to the body
      while ((!isEOL(peek())) && (!isEOF(peek()))) { //advances down the line fully
        commentBody += read();
      }

      //return da comment
      return new Token(TokenType.COMMENT, commentBody, line, savedColumn);
    }
    else {
      //no comments
      return null;
    }
  }

  /**
   * checks and returns potentally mulit-line tokens
   * @param c The input Char
   * @return Returns Token if successful, otherwise returns null.
   */
  private Token returnCheckMultiCharacter(char c) {
    TokenType retType = null;

    switch (c) {
      case '=':
        //two cases: 
        // check next char to determine which
        if (peek() == '=') { // case (1): it's an == operator
          retType = TokenType.EQUAL;
          read(); // get rid of the other one
          return new Token(retType, "==", line, column);
        }
        else { // case (2): it's an assignment operator.
          retType = TokenType.ASSIGN;
          break;
        }
      case '<':
        //two cases
        if(peek() == '=') {//case (1): <= operator
          //re-adjust the line swallowing the next char...
          read();
          //then return the true value
          return new Token(TokenType.LESS_EQ, "<=", line, column-1); //re-adjust to give actual position
        }
        else { //case(2): = operator
          retType = TokenType.LESS;
          break;
        }
      case '>':
        // two cases
        if (peek() == '=') {// case (1): >= operator
          // re-adjust the line swallowing the next char...
          read();
          // then return the true value
          return new Token(TokenType.GREATER_EQ, ">=", line, column-1); // re-adjust to give actual position
        } else { // case(2): = operator
          retType = TokenType.GREATER;
        }
        break;
      case '!':
        if (peek() == '=') {
          // re-adjust the line swallowing the next char...
          read();
          // then return the true value
          return new Token(TokenType.NOT_EQUAL, "!=", line, column - 1); // re-adjust to give actual position
        }
        else {
          error("expecting !=", line, column);
        }
      default:
        //did not find any matching 
        return null;
    }
    return new Token(retType, String.valueOf(c), line, column);
  } 

  /**
   * Returns the next single char Token for the lexer
   * @param c The input Char
   * @return Returns Token if successful, otherwise returns null. 
   */
  private Token returnSingleCharToken(char c) {
    TokenType retType = null;
    switch (c) {
      case '.':
        retType = TokenType.DOT;
        break;
      case ':':
        retType = TokenType.COLON;
        break;
      case ',':
        retType = TokenType.COMMA;
        break;
      case '(':
        retType = TokenType.LPAREN;
        break;
      case ')':
        retType = TokenType.RPAREN;
        break;
      case '[':
        retType = TokenType.LBRACKET;
        break;
      case ']':
        retType = TokenType.RBRACKET;
        break;
      case '{':
        retType = TokenType.LBRACE;
        break;
      case '}':
        retType = TokenType.RBRACE;
        break;
      case '+':
        retType = TokenType.PLUS;
        break;
      case '-':
        retType = TokenType.MINUS;
        break;
      case '/':
        retType = TokenType.DIVIDE;
        break;
      case '*':
        retType = TokenType.TIMES;
        break;
      default:
        return null; 
      //break;
    }
    return new Token(retType, String.valueOf(c), line, column);
  }
}
