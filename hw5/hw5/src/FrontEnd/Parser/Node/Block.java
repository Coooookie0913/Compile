package FrontEnd.Parser.Node;

import LLVM.Value;

import java.util.ArrayList;

public class Block extends Node{
    public Block(int startLine, int endLine, SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public Value genIR() {
        //IRBuilder.getInstance().getTableStack().enterBlock();
        super.genIR();
        //IRBuilder.getInstance().getTableStack().leaveBlock();
        return null;
    }
}
