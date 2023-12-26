package FrontEnd.Parser.Node;

import java.util.ArrayList;

//UnaryOp → '+' | '−' | '!'
public class UnaryOp extends Node{
    public UnaryOp(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
}
