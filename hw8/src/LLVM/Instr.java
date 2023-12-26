package LLVM;

import BackEnd.assembly.CommentAsm;

public class Instr extends User{
    private BasicBlock parentBlock;
    
    public Instr(LLVMType llvmType, String name, BasicBlock parentBlock) {
        super(llvmType, name);
        this.parentBlock = parentBlock;
    }
    
    public void setParentBlock(BasicBlock parentBlock) {
        this.parentBlock = parentBlock;
    }
    
    public BasicBlock getParentBlock() {
        return parentBlock;
    }
    
    //方便debug
    public void toAssembly() {
        //new CommentAsm("\n# " + this.toString());
    }
}
