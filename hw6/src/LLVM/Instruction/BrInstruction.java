package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.BranchAsm;
import BackEnd.assembly.JumpAsm;
import BackEnd.assembly.MemAsm;
import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Value;

import javax.print.DocFlavor;

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
    
    public void toAssembly() {
        super.toAssembly();
        Register reg = MipsBuilder.getInstance().getRegByValue(cond);
        //两种br指令
        if (elseBlock != null) {
            //如果未分配寄存器，则在栈上，取出
            if (reg == null) {
                reg = Register.K0;
                int offset = MipsBuilder.getInstance().getOffsetOfValue(cond.getName());
                new MemAsm(MemAsm.Op.LW,reg,Register.SP,offset);
            }
            new BranchAsm(BranchAsm.Op.BNE,reg,Register.ZERO, thenBlock.getName());
            new BranchAsm(BranchAsm.Op.BEQ,reg,Register.ZERO, elseBlock.getName());
        } else {
            new JumpAsm(JumpAsm.Op.J,thenBlock.getName());
        }
    }
}
