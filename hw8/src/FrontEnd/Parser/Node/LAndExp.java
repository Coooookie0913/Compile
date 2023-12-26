package FrontEnd.Parser.Node;

import LLVM.BasicBlock;
import LLVM.IRBuilder;
import LLVM.Instruction.BrInstruction;
import LLVM.LLVMType;
import LLVM.Value;

import java.util.ArrayList;

//LAndExp -> EqExp {'&&' EqExp}
public class LAndExp extends Node{
    public LAndExp(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    //Block 都是从LOr传进来的
    //不是最后一个  1->自己生成的nextEqBlock 0-> LOr传进来的nextBlock
    //最后一个 1-> thenBlock 0-> nextBlock
    public void genIRforLAnd(BasicBlock thenBlock, BasicBlock nextBlock) {
        BasicBlock parentBlock = IRBuilder.getInstance().getCurBasicBlock();
        int i;
        for (i = 0; i < getChildren().size(); i++) {
            //不是最后一个
            if (i != getChildren().size() - 1) {
                if (getChildren().get(i) instanceof EqExp) {
                    BasicBlock nextEqBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
                    IRBuilder.getInstance().addBasicBlock(nextEqBlock);
                    Value cond = getChildren().get(i).genIR();
                    //它不需要名字
                    BrInstruction BrInstr = new BrInstruction(LLVMType.INSTR,null,parentBlock,cond,nextEqBlock,nextBlock);
                    IRBuilder.getInstance().addInstr(BrInstr);
                    //处理下一个 EqExp ，把 nextEqBlock 设置成cur
                    IRBuilder.getInstance().setCurBasicBlock(nextEqBlock);
                }
            }
            //最后一个
            else {
                Value cond = getChildren().get(i).genIR();
                BrInstruction BrInstr = new BrInstruction(LLVMType.INSTR,null,parentBlock,cond,thenBlock,nextBlock);
                IRBuilder.getInstance().addInstr(BrInstr);
            }
        }
    }
}
