package FrontEnd.Parser.Node;

import FrontEnd.Lexer.Token;

import java.util.ArrayList;

public class TokenNode extends Node{
    private Token token;
    public TokenNode(int startLine, int endLine, SyntaxType SyntaxType, ArrayList<Node> children, Token token) {
        super(startLine, endLine, SyntaxType, children);
        this.token = token;
    }
    
    public Token getToken() {
        return token;
    }
}

