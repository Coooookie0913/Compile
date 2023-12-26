package FrontEnd.Parser.Node;

import java.util.ArrayList;

public class FuncType extends Node{
    public FuncType(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
}
