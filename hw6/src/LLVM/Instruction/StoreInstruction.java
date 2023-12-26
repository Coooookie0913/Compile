package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.LaAsm;
import BackEnd.assembly.LiAsm;
import BackEnd.assembly.MemAsm;
import LLVM.*;

//store <ty> <value>, <ty>* <pointer>
public class StoreInstruction extends Instr {
    private Value from;
    private Value to;
    //private LLVMType targetType;
    private String targetType;
    public StoreInstruction(LLVMType llvmType,String name,BasicBlock parentBlock, Value from, Value to, String targetType) {
        super(llvmType, name, parentBlock);
        this.from = from;
        this.to = to;
        this.targetType = targetType;
        addValue(from);
        addValue(to);
    }
    
    public String toString() {
        return "store " + targetType.toString().toLowerCase() + " " + from.getName() + ", "
                + targetType.toString().toLowerCase() + "* " + to.getName();
    }
    
    public void toAssembly() {
        super.toAssembly();
        Register fromReg = Register.K0;
        Register toReg = Register.K1;
        //把 address 存入 toReg
        if (to instanceof GlobalVar) {
            new LaAsm(toReg,to.getName().substring(1));
        }
        else if (MipsBuilder.getInstance().getRegByValue(to) != null) {
            toReg = MipsBuilder.getInstance().getRegByValue(to);
        }
        else {
            //从内存中获取
            //TODO 一定保证内存中有吗？
            new MemAsm(MemAsm.Op.LW,toReg,Register.SP,MipsBuilder.getInstance().getOffsetOfValue(to.getName()));
        }
        
        //对于单变量定义赋值，Init中有可能是 Instr 也可能是 constant
        if (from instanceof Init) {
            //from = ((Init)from).getInitValList().get(0);
            int i;
            int length = ((Init) from).getInitValList().size();
            for (i = 0 ; i < length; i++) {
                Value from1 = ((Init) from).getInitValList().get(i);
                //待存入的值
                //语句赋值常数
                if (from1 instanceof Constant) {
                    new LiAsm(fromReg, Integer.parseInt(from1.getName()));
                } else if (MipsBuilder.getInstance().getRegByValue(from1) != null) {
                    fromReg = MipsBuilder.getInstance().getRegByValue(from1);
                } else {
                    int offset = MipsBuilder.getInstance().getOffsetOfValue(from1.getName());
                    if (offset == Integer.MAX_VALUE) {
                        MipsBuilder.getInstance().subCurOffset(4);
                        offset = MipsBuilder.getInstance().getCurOffset();
                        MipsBuilder.getInstance().addOffsetOfValue(from1.getName(), offset);
                    }
                    new MemAsm(MemAsm.Op.LW, fromReg, Register.SP, offset);
                }
                new MemAsm(MemAsm.Op.SW, fromReg, toReg, i*4);
            }
        } else if (from instanceof Constant) {
            new LiAsm(fromReg, Integer.parseInt(from.getName()));
            new MemAsm(MemAsm.Op.SW, fromReg, toReg, 0);
        } else if (MipsBuilder.getInstance().getRegByValue(from) != null) {
            fromReg = MipsBuilder.getInstance().getRegByValue(from);
            new MemAsm(MemAsm.Op.SW, fromReg, toReg, 0);
        } else {
            int offset = MipsBuilder.getInstance().getOffsetOfValue(from.getName());
            if (offset == Integer.MAX_VALUE) {
                MipsBuilder.getInstance().subCurOffset(4);
                offset = MipsBuilder.getInstance().getCurOffset();
                MipsBuilder.getInstance().addOffsetOfValue(from.getName(), offset);
            }
            new MemAsm(MemAsm.Op.LW, fromReg, Register.SP, offset);
            new MemAsm(MemAsm.Op.SW, fromReg, toReg, 0);
        }
        //new MemAsm(MemAsm.Op.SW, fromReg, toReg, 0);
//        //待存入的值
//        //语句赋值常数
//        if (from instanceof Constant) {
//            new LiAsm(fromReg,Integer.parseInt(from.getName()));
//        }
//        else if (MipsBuilder.getInstance().getRegByValue(from) != null) {
//            fromReg = MipsBuilder.getInstance().getRegByValue(from);
//        }
//        else {
//            int offset = MipsBuilder.getInstance().getOffsetOfValue(from.getName());
//            if (offset == Integer.MAX_VALUE) {
//                MipsBuilder.getInstance().subCurOffset(4);
//                offset = MipsBuilder.getInstance().getCurOffset();
//                MipsBuilder.getInstance().addOffsetOfValue(from.getName(),offset);
//            }
//            new MemAsm(MemAsm.Op.LW,fromReg,Register.SP,offset);
//        }
//        new MemAsm(MemAsm.Op.SW,fromReg,toReg,0);
    }
}
