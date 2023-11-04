package tools;

import FrontEnd.Lexer.Token;
import FrontEnd.Parser.SyntaxType;
import FrontEnd.Visitor.ErrorMessage;
import FrontEnd.Visitor.ErrorType;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Printer {
    private ArrayList<ErrorMessage> ErrorList;
    private PrintStream outPs;
    private PrintStream errorPs;
    
    public Printer() throws FileNotFoundException {
        ErrorList = new ArrayList<>();
        outPs = new PrintStream("output.txt");
        errorPs = new PrintStream("error.txt");
    }
    
    public void printTokenList(ArrayList<Token> tokenList) throws FileNotFoundException {
        for (Token token : tokenList) {
            outPs.println(token.getType() + " " + token.getContent());
        }
    }
    
    public void printToken (Token token) {
        outPs.println(token.getType() + " " + token.getContent());
    }
    
    public void printSyntax(SyntaxType type) {
        outPs.println("<" + type.toString() + ">");
    }
    
    public void addErrorMessage(ErrorMessage errorMessage) {
        ErrorList.add(errorMessage);
    }
    
    public void printError() throws FileNotFoundException {
        Collections.sort(ErrorList);
        for (ErrorMessage errorMessage:ErrorList) {
            errorPs.println(errorMessage.getLine() + " " + errorMessage.getType());
        }
    }
}
