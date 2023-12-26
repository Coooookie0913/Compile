package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.AluAsm;
import BackEnd.assembly.MemAsm;
import LLVM.BasicBlock;
import LLVM.Instr;
import LLVM.LLVMType;
import LLVM.Use;

import java.util.ArrayList;

//<result> = alloca <type>
public class AllocaInstruction extends Instr {
    //如果是数组 那么targetType是 [n * i32]
    //%2 = alloca i32
    //%3 = alloca i32*
    //%4 = alloca [3 x i32]*
    private String targetType;
    public AllocaInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, String targetType) {
        super(llvmType, name, parentBlock);
        this.targetType = targetType;
    }
    
    public String toString() {
        return this.getName() + " = alloca " + targetType;
    }
    
    @Override
    public String getTargetType() {
        return targetType;
    }
    
    public void toAssembly() {
        super.toAssembly();
        //分配空间
        if (targetType.charAt(0) == '[') {
            //数组长度
            StringBuilder sb = new StringBuilder();
            int i;
            for (i = 1; i < targetType.length(); i++) {
                if (!Character.isDigit(targetType.charAt(i))) {
                    break;
                } else {
                    sb.append(targetType.charAt(i));
                }
            }
            int len = Integer.parseInt(sb.toString());
            MipsBuilder.getInstance().subCurOffset(len * 4);
        } else {
            //不是数组
            MipsBuilder.getInstance().subCurOffset(4);
        }
        //如果alloca的指针有对应reg，将地址赋给reg
        if (MipsBuilder.getInstance().getRegByValue(this.getName()) != null) {
            Register pointerReg = MipsBuilder.getInstance().getRegByValue(this.getName());
            int curOffset = MipsBuilder.getInstance().getCurOffset();
            new AluAsm(AluAsm.Op.ADDI,pointerReg,Register.SP,curOffset);
        }
        //如果没有对应reg，将指针值存入栈中
        else {
            //当前空间的首地址，最低地址
            //TODO 为什么呢
            int curOffset = MipsBuilder.getInstance().getCurOffset();
            new AluAsm(AluAsm.Op.ADDI,Register.K0,Register.SP,curOffset);
            //保存k0
            MipsBuilder.getInstance().subCurOffset(4);
            curOffset = MipsBuilder.getInstance().getCurOffset();
            MipsBuilder.getInstance().addOffsetOfValue(this.getName(),curOffset);
            new MemAsm(MemAsm.Op.SW,Register.K0,Register.SP,curOffset);
        }
    }
}
