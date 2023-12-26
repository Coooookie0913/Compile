package FrontEnd.Parser.Node;

import java.util.ArrayList;

////ConstExp -> AddExp
public class ConstExp extends Node{
    public ConstExp(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public int calculate() {
        return getChildren().get(0).calculate();
    }
}
