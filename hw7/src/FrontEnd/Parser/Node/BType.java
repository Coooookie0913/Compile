package FrontEnd.Parser.Node;

import java.util.ArrayList;

public class BType extends Node{
    public BType(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
}
