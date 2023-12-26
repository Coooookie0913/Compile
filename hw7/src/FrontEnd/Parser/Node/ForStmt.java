package FrontEnd.Parser.Node;

import LLVM.BasicBlock;
import LLVM.IRBuilder;
import LLVM.Instruction.StoreInstruction;
import LLVM.LLVMType;
import LLVM.Value;

import java.util.ArrayList;

//ForStmt → LVal '=' Exp
public class ForStmt extends Node{
    public ForStmt(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    @Override
    public Value genIR() {
        //存储该变量的寄存器
        Value LValValue = ((LVal)getChildren().get(0)).genIRforAssign();
        Value ExpValue = getChildren().get(2).genIR();
        BasicBlock parentBlock = IRBuilder.getInstance().getCurBasicBlock();
        StoreInstruction storeInstr = new StoreInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),parentBlock,
                ExpValue,LValValue,"i32");
        IRBuilder.getInstance().addInstr(storeInstr);
        return null;
    }
}
