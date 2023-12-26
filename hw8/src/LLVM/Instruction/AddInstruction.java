package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.AluAsm;
import BackEnd.assembly.LiAsm;
import BackEnd.assembly.MemAsm;
import LLVM.*;
import tools.RegAllocator;

public class AddInstruction extends Instr {
    private Value operand1;
    private Value operand2;
    public AddInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Value operand1, Value operand2) {
        super(llvmType, name, parentBlock);
        this.operand1 = operand1;
        this.operand2 = operand2;
        //作为 User 类，加入使用的 value
        addValue(operand1);
        addValue(operand2);
    }
    
    public String toString() {
        return this.getName() + " = add i32 " + operand1.getName() + ", " + operand2.getName();
    }
    
    //直接在 new 的时候就加入了mipsBuilder
    public void toAssembly() {
        //注释
        super.toAssembly();
        //使用 k0 k1
        Register reg1 = Register.K0;
        Register reg2 = Register.K1;
        //TODO 先全部放在栈上
        Register targetReg = MipsBuilder.getInstance().getRegByValue(this.getName());
        if (targetReg == null) {
            //先存$t0~$t9
//            //MipsBuilder中会自动更新对应关系
//            if (RegAllocator.getInstance().getFreeReg(this) != null) {
//                targetReg = RegAllocator.getInstance().getFreeReg(this);
//            }
//            //如果已经用完
//            else {
                targetReg = Register.K0;
//            }
        }
        //先将operand1保存到k0
        //常数
        String op1Name = operand1.getName();
        int flag1 = 0;
        int i = 0;
        for (i = 0; i < op1Name.length(); i++) {
            if (!Character.isDigit(op1Name.charAt(i))) {
                flag1 = 1;
                break;
            }
        }
        String op2Name = operand2.getName();
        int flag2 = 0;
        for (i = 0; i < op2Name.length(); i++) {
            if (!Character.isDigit(op2Name.charAt(i))) {
                flag2 = 1;
                break;
            }
        }
        if (operand1 instanceof Constant || flag1 == 0) {
            new LiAsm(reg1,Integer.parseInt(operand1.getName()));
        }
        //如果已经有对应的reg
        else if (MipsBuilder.getInstance().getRegByValue(operand1.getName()) != null) {
            reg1 = MipsBuilder.getInstance().getRegByValue(operand1.getName());
        }
        //没有对应的add，在栈上寻找，没有则新开辟
        else {
            int offset1 = MipsBuilder.getInstance().getOffsetOfValue(operand1.getName());
            //如果没有 offset 是MAXVALUE
            if (offset1 == Integer.MAX_VALUE) {
                MipsBuilder.getInstance().subCurOffset(4);
                offset1 = MipsBuilder.getInstance().getCurOffset();
                MipsBuilder.getInstance().addOffsetOfValue(operand1.getName(),offset1);
            }
            new MemAsm(MemAsm.Op.LW,reg1,Register.SP,offset1);
        }
        //operand2做相同操作
        //常数
        if (operand2 instanceof Constant || flag2 == 0) {
            new LiAsm(reg2,Integer.parseInt(operand2.getName()));
        }
        else if (MipsBuilder.getInstance().getRegByValue(operand2.getName()) != null) {
            reg2 = MipsBuilder.getInstance().getRegByValue(operand2.getName());
        }
        else {
            int offset2 = MipsBuilder.getInstance().getOffsetOfValue(operand2.getName());
            if (offset2 == Integer.MAX_VALUE) {
                MipsBuilder.getInstance().subCurOffset(4);
                offset2 = MipsBuilder.getInstance().getCurOffset();
                MipsBuilder.getInstance().addOffsetOfValue(operand2.getName(),offset2);
            }
            new MemAsm(MemAsm.Op.LW,reg2,Register.SP,offset2);
        }
        //计算
        new AluAsm(AluAsm.Op.ADDU,targetReg,reg1,reg2);
        //如果target本身没有寄存器,存到栈上
        if (MipsBuilder.getInstance().getRegByValue(this.getName()) == null) {
            MipsBuilder.getInstance().subCurOffset(4);
            int curOffset = MipsBuilder.getInstance().getCurOffset();
            MipsBuilder.getInstance().addOffsetOfValue(this.getName(),curOffset);
            new MemAsm(MemAsm.Op.SW,targetReg,Register.SP,curOffset);
        }
    }
}
