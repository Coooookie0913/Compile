package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.JumpAsm;
import BackEnd.assembly.LiAsm;
import BackEnd.assembly.MemAsm;
import BackEnd.assembly.MoveAsm;
import LLVM.*;

public class RetInstruction extends Instr {
    private LLVMType returnType;
    private Value returnValue;
    //ret void 的话 returnValue 是 null
    public RetInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, LLVMType returnType, Value returnValue) {
        super(llvmType, name, parentBlock);
        this.returnType = returnType;
        this.returnValue = returnValue;
        addValue(returnValue);
    }
    
    public String toString() {
        if (returnType != LLVMType.VOID) {
            return "ret " + returnType.toString().toLowerCase() + " " + returnValue.getName();
        } else {
            return "ret void";
        }
    }
    
    public void toAssembly() {
        super.toAssembly();
        //如果不是 return null
        if (returnValue != null) {
            if (returnValue instanceof Constant) {
                new LiAsm(Register.V0,Integer.parseInt(returnValue.getName()));
            }
            else if (MipsBuilder.getInstance().getRegByValue(returnValue.getName()) != null) {
                Register reg = MipsBuilder.getInstance().getRegByValue(returnValue.getName());
                new MoveAsm(Register.V0,reg);
            }
            else {
                int offset = MipsBuilder.getInstance().getOffsetOfValue(returnValue.getName());
                if (offset == Integer.MAX_VALUE) {
                    MipsBuilder.getInstance().subCurOffset(4);
                    offset = MipsBuilder.getInstance().getCurOffset();
                    MipsBuilder.getInstance().addOffsetOfValue(returnValue.getName(),offset);
                }
                new MemAsm(MemAsm.Op.LW,Register.V0,Register.SP,offset);
            }
        }
        new JumpAsm(JumpAsm.Op.JR,Register.RA);
    }
}
