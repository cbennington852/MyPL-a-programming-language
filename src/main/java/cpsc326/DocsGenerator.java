package cpsc326;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;






public class DocsGenerator {
    private Lexer lexer;    
    private String structs = ""; //init to nothing
    private String funDef = ""; //init to nothing
    private Token currToken;      // the current token

    //GET THE AST WITH THE SIMPLE PARSER
    private Program program;

    /**
    * Helper to check that the current token is a literal value.
    */
    private boolean isLiteralType() {
        return matchAny(List.of(TokenType.INT_TYPE, TokenType.DOUBLE_TYPE,
                                TokenType.STRING_TYPE, TokenType.BOOL_TYPE,
                                TokenType.VOID_TYPE));
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
        MyPLException.docsError(String.format(s, line, column, msg, lexeme));
    }

    /**
    * Move to the next lexer token, skipping comments.
    */
    private void advance() {
        currToken = lexer.nextToken();
        while (match(TokenType.COMMENT))
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

    private static InputStream istream(String str) {
      try {
        return new ByteArrayInputStream(str.getBytes("UTF-8")); 
      } catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }
  

    public DocsGenerator(String input) {
        Lexer lexer = new Lexer(istream(input));
        Program program = new ASTParser(lexer).parse();
        Lexer newLexer = new Lexer(istream(input));
        this.lexer = newLexer;
        this.program = program;
        currToken = this.lexer.nextToken(); 
        parseLexer();
    }

    //parse the Lexer
    private void parseLexer() {
        while (!match(TokenType.EOS)) {
            if (currToken.tokenType == TokenType.MULCOMMENT) {
                
                Token doc = currToken;
                advance(); // saved the doc, lets see what type of doc it is
                // found an MulComment, we want to see if this is an 
                //     1. struct

                if (currToken.tokenType == TokenType.STRUCT) {
                    
                    //we've got a struct
                    advance();
                    parseStruct(currToken.lexeme, doc.lexeme);
                    eat(TokenType.ID, "Not an ID");
                    
                }
                //     2. fun def
                else if (isLiteralType() || match(TokenType.ID) ) {
                    String returnType = currToken.lexeme;
                    advance();
                    
                    if (currToken.tokenType == TokenType.ID) {
                        String funName = currToken.lexeme;
                        advance();
                        if (currToken.tokenType == TokenType.LPAREN) {
                            //we've got a function
                            parseFunDef(funName,returnType,doc.lexeme);
                        }
                    }
                }
                else if (match(TokenType.ANNOTATION)) {
                    advance();//bypass annoations
                    if (isLiteralType() || match(TokenType.ID) ) {
                        String returnType = currToken.lexeme;
                        advance();
                        
                        if (currToken.tokenType == TokenType.ID) {
                            String funName = currToken.lexeme;
                            advance();
                            if (currToken.tokenType == TokenType.LPAREN) {
                                //we've got a function
                                parseFunDef(funName,returnType,doc.lexeme);
                            }
                        }
                    }
                }
                
                //     3. normal multi-line commentBody    
                else {
                    //do nothing
                }
            }
            else {
                advance();
            }
        }
        
        eat(TokenType.EOS, "expecting end of file");
    }

    //EXAMPLE STRUCT:
    /*
    <tr>
            <td class="structDef">
                <h3><u>Basket</u></h3>
                <p>Description of basket</p>
                <div class="structDefInputs">
                    <p> <strong>pears : </strong> The number of pears in the basket. int value between 1- 99.</p>
                    <p> <strong>apples : </strong> The number of apples in the basket. int value between 1- 99.</p>
                </div>
            </td>
        </tr>
    */
    private void parseStruct(String structName, String doc) {
        //get structDef
        StructDef thisDef = null;
        for (StructDef currNode : program.structs) {
            if (currNode.structName.lexeme.equals(structName)) {
                thisDef = currNode;
            }
        }
        if (thisDef == null) {
            String list = "";
            for (StructDef currNode : program.structs) {
               list += currNode.structName.lexeme;
            }
            String msg = "Struct not found...... \n" + list + "\n";
            msg += "input name: " + structName;
            error(msg);
        }
        //loop thru the comment, get each @ and the corresponding stuff 
        //add the structname!!
        structs += """
                <tr>
                <td class="structDef">
                <h3><u>
                """;
        structs += structName;
        structs += "</u></h3><p>";
        //add the description...
        //loop thru until we hit the first @ thingy
        String[] arr = doc.split("@");
        structs += arr[0];
        structs += "</p>";

        // starts on the @
        //add the @'s
        structs += "<div class=\"structDefInputs\">";
        for (int x = 1; x < arr.length; x++) {
            String result = "";
            String current = arr[x];
            String [] input = current.split(":");
            if (input.length != 2) {
                error("@' missing a :");
            }
            //get the type of the feild
            result += "<p> <strong>";
            result += input[0].stripTrailing().stripLeading();
            result += "</strong>";
            result += input[1];
            result += "</p>";
            structs += result;
        }
        structs += "</div>\r\n" + //
                        "            </td>\r\n" + //
                        "        </tr>";
        //<p> <span><u>int</u> <strong>pears : </strong> </span>The number of pears in the basket. int value between 1- 99.</p>
    }

    //EXAMPLE FUN_DEF:
    /*
    <tr>
            <td class="funDef">
                <h3><u>eatApple</u></h3>
                <p>Description of eat apple</p>
                <div class="structDefInputs">
                <ul>
                    <li><strong>numApples:</strong> an int, corresponding to how many apples to eat. </li>
                </ul>
                </div>
                <p><strong>returns</strong> nothing, apple is aten</p>
            </td>
        </tr>
    */
    private void parseFunDef(String name, String retType, String doc) {
        //loop thru the comment, get each @ and the corresponding stuff 
        //add the structname!!
        funDef += """
                <tr>
                <td class="funDef">
                <h3><u>
                """;
        funDef += name;
        funDef += "</u></h3><p>";
        //add the description...
        //loop thru until we hit the first @ thingy
        String[] arr = doc.split("@");
        funDef += arr[0];
        funDef += "</p>";

        FunDef func = null;
        for (int i = 0; i < program.functions.size(); i++) {
            if (program.functions.get(i).funName.lexeme.equals(name)) {
                func = program.functions.get(i);
            }
        }
        if (func == null) {
            error("Function not found. ");
        }

        // starts on the @
        //add the @'s
        funDef += "<div class=\"structDefInputs\"> <ul>";
        for (int x = 1; x < arr.length; x++) {
            String result = "";
            String current = arr[x];
            String [] input = current.split(":");
            if (input.length != 2) {
                error("@' missing a :");
            }
            result += "<li><p> <strong>";
            result += input[0];
            result += " :</strong>";
            String refName = input[0].stripTrailing().stripLeading();
            //get the varible type
            VarDef varDef = null;
            for (int i =0; i < func.params.size(); i++) {
                if (func.params.get(i).varName.lexeme.equals(refName)) {
                    varDef = func.params.get(i);
                }
            }
            if (varDef == null) {
                error("The @ call " + refName + " does not match and input");
            }
            //add the variable type
            result += " <i>";
            if (varDef.dataType.isArray == true) {
                result += "["+varDef.dataType.type.lexeme+"]";
            }
            else {
                result += varDef.dataType.type.lexeme;
            }
            result += " </i>";
            
            //next 
            result += input[1];
            result += "</p></li>";
            funDef += result;
        }

        funDef += "</ul><p><strong>returns: </strong> "+ retType +"</p>";
        funDef += "</div>\r\n" + //
                        "            </td>\r\n" + //
                        "        </tr>";
    }

    //assembles the full HTMLdocs as a string
    public String getHTMLdocs () {
        String str = "";
        str += HTML_Document.start;
        str += structs;
        str += HTML_Document.middle;
        str += funDef;
        str += HTML_Document.end;
        return str;
    }

    //get just the structs
    public String getStructs () {
        return structs;
    }

    //get just the funDef
    public String getFunDef () {
        return funDef;
    }


    
}
