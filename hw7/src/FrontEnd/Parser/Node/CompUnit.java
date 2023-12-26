package FrontEnd.Parser.Node;

import LLVM.IRBuilder;
import LLVM.Value;

import java.util.ArrayList;

public class CompUnit extends Node{
    public CompUnit(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public Value genIR() {
        IRBuilder.getInstance().getTableStack().enterBlock();
        IRBuilder.getInstance().setLocalVarCnt(0);
        for (Node node : getChildren()) {
            node.genIR();
        }
        IRBuilder.getInstance().getTableStack().leaveBlock();
        return null;
    }
}
