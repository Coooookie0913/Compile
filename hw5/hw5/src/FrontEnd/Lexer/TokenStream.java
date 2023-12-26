package FrontEnd.Lexer;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

public class TokenStream {
    private ArrayList<Token> tokenList;
    private int curPos;
    
    public TokenStream(ArrayList<Token> tokenArrayList) {
        tokenList = tokenArrayList;
        curPos = 0;
    }
    
    public void append(Token token) {
        tokenList.add(token);
    }
    //输出 token
    public void output(String filename) throws FileNotFoundException {
        PrintStream ps = new PrintStream(filename);
        System.setOut(ps);
        for (Token token:tokenList) {
            System.out.println(token.getType() + " " + token.getContent());
        }
    }
    //返回当前token
    public Token peek() {
        if (curPos < tokenList.size()) {
            return tokenList.get(curPos);
        } else {
            return null;
        }
    }
    //读取下一个token
    public void read() {
        curPos++;
        peek();
    }
    //避免回溯，向前看
    public Token watch(int watchStep) {
        if (curPos + watchStep < tokenList.size()) {
            return tokenList.get(curPos + watchStep);
        } else {
            return null;
        }
    }
}
