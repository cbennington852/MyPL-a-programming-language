/*
    Test cases for the myPL docs
 */

package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.List;


public class myPLdocsTests {
    InputStream istream(String str) {
    try {
      return new ByteArrayInputStream(str.getBytes("UTF-8")); 
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  

  @Test
  void multiLineComment() {
    var p = "/* this is a multi-line comment */";
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.MULCOMMENT, t.tokenType);
    assertEquals(" this is a multi-line comment ", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(34, t.column);
  }

  @Test
  void multiLineComment2() {
    var p = """
    /* this is a multi-line comment 
    int double + cat*/
    """;
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.MULCOMMENT, t.tokenType);
    assertEquals("""
     this is a multi-line comment 
    int double + cat""", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(50, t.column);
  }

  @Test
  void multiLineComment3() {
    var p = """
    /* this is a multi-line comment 
    int double + cat*/
    void main() { 
        var x1: int = 3
        var x2: double = 2.7
        var x3: bool = true
        var x4: string = "abc"
        print(x1)
        print(x2) 
        print(x3) 
        print(x4)
      }
    """;
    Lexer lexer = new Lexer(istream(p));
    Token t = lexer.nextToken();
    assertEquals(TokenType.MULCOMMENT, t.tokenType);
    assertEquals("""
     this is a multi-line comment 
    int double + cat""", t.lexeme);
    assertEquals(1, t.line);
    assertEquals(50, t.column);
  }

  @Test
  void nonTerminatedMultiComment() {
    var p = "/* this is a multi-line comment.. not terminated right";
    Lexer lexer = new Lexer(istream(p));
    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [1,55] non-terminated multi-line comment";
    assertEquals(m, e.getMessage());
  }

  @Test
  void randomMultiComment() {
    var p = "string int */";
    Lexer lexer = new Lexer(istream(p));
    lexer.nextToken();
    lexer.nextToken();
    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [1,12] non-terminated multi-line comment";
    assertEquals(m, e.getMessage());
  }

  

  /*********************************************************************/
  // TESTING HTML DOCUMENT CREATION n'stuff
  /*********************************************************************/

  @Test
  void basicStruct() {
    var p = """
        /* 
          Im cooking it
          @x : peas 
          @y : cheese
        */
        struct Type1 {
          x: int, 
          y: int
        }

        void main () {
          print("fun")
        }
        """;
    
  
    DocsGenerator docs = new DocsGenerator(p);
    String result = docs.getStructs();
    String hope = """
            <tr>
            <td class="structDef">
            <h3><u>
            Type1</u></h3><p>
              Im cooking it
              </p><div class="structDefInputs"><p> <strong>x </strong> peas
              </p><p> <strong>y </strong> cheese
            </p></div>
                        </td>
                    </tr>
                                 
                          """;
    result = result.replaceAll("\\s", "");
    hope = hope.replaceAll("\\s", "");
    assertEquals(result, hope);
  }

  @Test
  void basicStructPoorFormatting() {
    var p = """
        /* 
          this is a type @x : peas @y : cheese
        */
        struct Type1 {
          x: int, 
          y: int
        }
        """;
    
    DocsGenerator docs = new DocsGenerator(p);
    String result = docs.getStructs();

    String hope = """
                    <tr><tdclass="structDef"><h3><u>Type1</u></h3><p>this is a type</p><divclass="structDefInputs"><p><strong>x</strong>peas</p><p><strong>y</strong>cheese</p></div></td></tr>    
                          """;
    result = result.replaceAll("\\s", "");
    hope = hope.replaceAll("\\s", "");
    assertEquals(result, hope);
  }

  
  //funDef testing
  @Test
  void basicFunDef() {
    var p = """
        /* 
          this function returns true
          @ n : a random number
        */
        bool is_true(n: int) {
          return true
        }
        """;
    
    DocsGenerator docs = new DocsGenerator(p);
    String result = docs.getFunDef();
    System.out.println(result);
    String hope = """
                    <tr><tdclass="funDef"><h3><u>is_true</u></h3><p>thisfunctionreturnstrue</p><divclass="structDefInputs"><ul><li><p><strong>n:</strong><i>int</i>arandomnumber</p></li></ul><p><strong>returns:</strong>bool</p></div></td></tr>
                          """;
    result = result.replaceAll("\\s", "");
    hope = hope.replaceAll("\\s", "");
    assertEquals(hope,result );
  }



   @Test
    void oneSymbol() {
      var p = "@VM";
      Lexer lexer = new Lexer(istream(p));
      Token t = lexer.nextToken();
      assertEquals(TokenType.ANNOTATION, t.tokenType);
      assertEquals("VM", t.lexeme);
      assertEquals(3, t.line);
      assertEquals(1, t.column);
    }

      @Test
  void leadingZero() {
    var p = "@V";
    Lexer lexer = new Lexer(istream(p));
    Exception e = assertThrows(MyPLException.class, () -> lexer.nextToken());
    var m = "LEXER_ERROR: [3,1] called Invalid annoation";
    assertEquals(m, e.getMessage());
  }


 

}
