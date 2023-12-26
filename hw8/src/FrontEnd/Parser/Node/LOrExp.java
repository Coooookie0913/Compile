package FrontEnd.Parser.Node;

import LLVM.BasicBlock;
import LLVM.IRBuilder;
import LLVM.LLVMType;

import java.util.ArrayList;

//LOrExp -> LAndExp {'||' LAndExp}
public class LOrExp extends Node{
    public LOrExp(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    //来自 Cond 的 thenBlock elseBlock
    //不是最后一个 LAnd, 0 -> 自己生成的 nextAndBlock , 1 -> thenBlock
    //最后一个 LAnd, 0->elseBlock,1->thenBlock
    public void genIRforLOrExp(BasicBlock thenBlock,BasicBlock elseBlock) {
        BasicBlock parentBlock = IRBuilder.getInstance().getCurBasicBlock();
        int i;
        for (i = 0; i < getChildren().size(); i++) {
            //不是最后一个
            if (i != getChildren().size() - 1) {
                BasicBlock nextAndBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
                if (getChildren().get(i) instanceof LAndExp) {
                    ((LAndExp)getChildren().get(i)).genIRforLAnd(thenBlock,nextAndBlock);
                    IRBuilder.getInstance().addBasicBlock(nextAndBlock);
                    IRBuilder.getInstance().setCurBasicBlock(nextAndBlock);
                }
            }
            //最后一个
            else {
                ((LAndExp)getChildren().get(i)).genIRforLAnd(thenBlock,elseBlock);
            }
        }
    }
}
