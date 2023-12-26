package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.*;
import LLVM.*;

import java.util.ArrayList;
import java.util.Objects;

//call void @a1(i32 %9)
//%14 = call i32 @a2(i32 %12, i32* %13)
public class CallInstruction extends Instr {
    private Function function;
    private ArrayList<Value> paramArrayList;
    public CallInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Function function, ArrayList<Value> paramArrayList) {
        super(llvmType, name, parentBlock);
        this.function = function;
        //实参
        this.paramArrayList = paramArrayList;
        addValue(function);
        for (Value param : paramArrayList) {
            addValue(param);
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (function.getReturnType() == LLVMType.VOID) {
            sb.append("call void ");
        } else {
            sb.append(getName());
            sb.append(" = call i32 ");
        }
        sb.append(function.getName());
        sb.append("(");
        int cnt = 0;
        int i;
        //实参
        for (i = 0; i < paramArrayList.size(); i++) {
            Value param = paramArrayList.get(i);
            //TODO
            //sb.append("i32");
            //用函数定义信息判断，形参
            if (function.getParamArrayList().size() > 0 && ((Param)function.getParamArrayList().get(i)).getParamDims().size() > 0) {
                sb.append("i32*");
            }
            else {
                sb.append("i32");
            }
            sb.append(" ");
            sb.append(param.getName());
            cnt++;
            if (cnt < paramArrayList.size()) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
    
    public void toAssembly() {
        super.toAssembly();
        //如果是系统调用的函数call
        //putch 11
        if (Objects.equals(function.getName(), "@putch")) {
//            if (paramArrayList.get(0) instanceof Constant) {
//                new LiAsm(Register.A0,Integer.parseInt(paramArrayList.get(0).getName()));
//            } else {
//                //如果有对应寄存器
//                if (MipsBuilder.getInstance().getRegByValue(paramArrayList.get(0)) != null) {
//                    new MoveAsm(Register.A0,MipsBuilder.getInstance().getRegByValue(paramArrayList.get(0)));
//                }
//                //没有就从栈上取
//                else {
//                    int offset = MipsBuilder.getInstance().getOffsetOfValue(paramArrayList.get(0));
//                    new MemAsm(MemAsm.Op.LW,Register.A0,Register.SP,offset);
//                }
//            }
            new LiAsm(Register.A0,Integer.parseInt(paramArrayList.get(0).getName()));
            new LiAsm(Register.V0,11);
            new SyscallAsm();
        }
        //putint
        else if (Objects.equals(function.getName(), "@putint")) {
            if (paramArrayList.get(0) instanceof Constant) {
                new LiAsm(Register.A0,Integer.parseInt(paramArrayList.get(0).getName()));
            } else {
                //如果有对应寄存器
                if (MipsBuilder.getInstance().getRegByValue(paramArrayList.get(0).getName()) != null) {
                    new MoveAsm(Register.A0,MipsBuilder.getInstance().getRegByValue(paramArrayList.get(0).getName()));
                }
                //没有就从栈上取
                else {
                    int offset = MipsBuilder.getInstance().getOffsetOfValue(paramArrayList.get(0).getName());
                    new MemAsm(MemAsm.Op.LW,Register.A0,Register.SP,offset);
                }
            }
            //new LiAsm(Register.A0,Integer.parseInt(paramArrayList.get(0).getName()));
            new LiAsm(Register.V0,1);
            new SyscallAsm();
        }
        //putstr 4
        else if (Objects.equals(function.getName(), "@putstr")) {
            //TODO 好像没用上
            new LaAsm(Register.A0,paramArrayList.get(0).getName());
            new LiAsm(Register.V0,4);
            new SyscallAsm();
        }
        //getint 5
        else if (Objects.equals(function.getName(), "@getint")){
            new LiAsm(Register.V0,5);
            new SyscallAsm();
            // the int stores in v0
            // 如果有对应的寄存器
            if (MipsBuilder.getInstance().getRegByValue(this.getName()) != null) {
                Register targetReg = MipsBuilder.getInstance().getRegByValue(this.getName());
                //从 v0 中 move 出来
                new MoveAsm(targetReg,Register.V0);
            }
            //如果本身没有对应的寄存器，则存到栈上
            else {
                MipsBuilder.getInstance().subCurOffset(4);
                int offset = MipsBuilder.getInstance().getCurOffset();
                MipsBuilder.getInstance().addOffsetOfValue(this.getName(),offset);
                new MemAsm(MemAsm.Op.SW,Register.V0,Register.SP,offset);
            }
        }
        
        //如果是定义函数的调用
        else {
            int curOffset = MipsBuilder.getInstance().getCurOffset();
            //将 sp 和 ra 存到 sp+curOffset-4 和 sp+curOffset-8 中
            new MemAsm(MemAsm.Op.SW, Register.SP,Register.SP,curOffset - 4);
            new MemAsm(MemAsm.Op.SW,Register.RA,Register.SP,curOffset - 8);
            int paramNum = 0;
            //将实参放入栈上 TODO:放到寄存器中
            for (Value param : paramArrayList) {
                paramNum++;
                Register tempReg = Register.K0;
                //先用 tempreg 暂时保存实参
                if (param instanceof Constant) {
                    new LiAsm(tempReg, Integer.parseInt(param.getName()));
                } else if (MipsBuilder.getInstance().getRegByValue(param.getName()) != null) {
                    Register srcReg = MipsBuilder.getInstance().getRegByValue(param.getName());
                    //TODO
                    tempReg = srcReg;
                } else {
                    new MemAsm(MemAsm.Op.LW, tempReg, Register.SP, MipsBuilder.getInstance().getOffsetOfValue(param.getName()));
                }
                //将 tempReg 中的值存入栈区
                new MemAsm(MemAsm.Op.SW, tempReg, Register.SP, curOffset - 8 - paramNum * 4);
            }
                //将 sp 设置为被调用函数的栈底地址，sp+curOffset-8
                new AluAsm(AluAsm.Op.ADDI,Register.SP,Register.SP,curOffset-8);
                //调用函数
                new JumpAsm(JumpAsm.Op.JAL,function.getName().substring(1));
                //恢复sp ra
                new MemAsm(MemAsm.Op.LW,Register.RA,Register.SP,0);
                new MemAsm(MemAsm.Op.LW,Register.SP,Register.SP,4);
                //如果有返回值
                if (MipsBuilder.getInstance().getRegByValue(this.getName()) != null) {
                    Register reg = MipsBuilder.getInstance().getRegByValue(this.getName());
                    new MoveAsm(reg,Register.V0);
                }
                else {
                    MipsBuilder.getInstance().subCurOffset(4);
                    curOffset = MipsBuilder.getInstance().getCurOffset();
                    MipsBuilder.getInstance().addOffsetOfValue(this.getName(),curOffset);
                    new MemAsm(MemAsm.Op.SW,Register.V0,Register.SP,curOffset);
                }
           // }
        }
    }
}
