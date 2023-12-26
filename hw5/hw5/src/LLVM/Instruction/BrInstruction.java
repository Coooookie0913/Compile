package LLVM.Instruction;

import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

public class BrInstruction extends Instr {
    private Value cond;
    private BasicBlock thenBlock;
    private BasicBlock elseBlock;
    //如果是 br label <dest> ，将 cond 设置为1，elseBlock 为 null
    public BrInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Value cond, BasicBlock thenBlock, BasicBlock elseBlock) {
        super(llvmType, name, parentBlock);
        this.cond = cond;
        addValue(cond);
        this.thenBlock = thenBlock;
        addValue(thenBlock);
        this.elseBlock = elseBlock;
        if (elseBlock != null) {
            addValue(elseBlock);
        }
    }
    
    public String toString() {
        if (elseBlock != null) {
            return "br i1 " + cond.getName() + ", label %" + thenBlock.getName() +
                    ", label %" + elseBlock.getName();
        } else {
            return "br label %" + thenBlock.getName();
        }
    }
}
