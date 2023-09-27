package FrontEnd;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

public class TokenStream {
    private ArrayList<Token> tokenList;
    
    public TokenStream(ArrayList<Token> tokenArrayList) {
        tokenList = tokenArrayList;
    }
    
    public void append(Token token) {
        tokenList.add(token);
    }
    
    public void output(String filename) throws FileNotFoundException {
        PrintStream ps = new PrintStream(filename);
        System.setOut(ps);
        for (Token token:tokenList) {
            System.out.println(token.getType() + " " + token.getContent());
        }
    }
}
