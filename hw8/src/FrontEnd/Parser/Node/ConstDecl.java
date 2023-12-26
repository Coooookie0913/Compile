package FrontEnd.Parser.Node;

import LLVM.Value;

import java.util.ArrayList;

public class ConstDecl extends Node{
    public ConstDecl(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    //const int <ConstDef> {,<ConstDef>};
    public Value genIR() {
        for (Node node : getChildren()) {
            node.genIR();
        }
        return null;
    }
}
