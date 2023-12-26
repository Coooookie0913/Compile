package FrontEnd.Parser.Node;

import java.util.ArrayList;

public class Decl extends Node{
    public Decl(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
}
