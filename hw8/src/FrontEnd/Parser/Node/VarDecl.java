package FrontEnd.Parser.Node;

import LLVM.Value;

import java.util.ArrayList;

//int varDef {,varDef};
public class VarDecl extends Node{
    public VarDecl(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public Value genIR() {
        for (Node node : getChildren()) {
            node.genIR();
        }
        return null;
    }
}
