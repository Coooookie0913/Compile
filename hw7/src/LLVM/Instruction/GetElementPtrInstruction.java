package LLVM.Instruction;

import BackEnd.MipsBuilder;
import BackEnd.Register;
import BackEnd.assembly.AluAsm;
import BackEnd.assembly.LaAsm;
import BackEnd.assembly.MemAsm;
import LLVM.*;

import java.util.Objects;

// 在 genIR 的时候全部化成一维数组
//<result> = getelementptr <ty>, <ty>* <ptrval>, {<ty> <index>}*
public class GetElementPtrInstruction extends Instr {
    //形如 [n * i32]
    //或者 i32*
    private String targetType;
    //genIR 的时候就生成一个整数
    //private int offset;
    private Value offset;
    private Value pointer;
    public GetElementPtrInstruction(LLVMType llvmType, String name, BasicBlock parentBlock, Value pointer, String targetType, Value offset) {
        super(llvmType, name, parentBlock);
        //就没用 user value了
        this.targetType = targetType;
        this.offset = offset;
        this.pointer = pointer;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public String toString() {
        //pointer
        //TODO 没搞明白
        //if (pointer instanceof LoadInstruction && ((LoadInstruction)pointer).getTargetType() == LLVMType.INT32_POINTER) {
            //return this.getName() + " = getelementptr " + targetType + ", " + targetType + "* " + pointer.getName() +
            //        ", i32 " + offset;
        //}
        //else {
            //return this.getName() + " = getelementptr " + targetType + ", " + targetType + "* " + pointer.getName() +
            //        ", i32 0, i32 " + offset;
        //}
        
        //Symbol symbol = IRBuilder.getInstance().getTableStack().getSymbol(pointer.getName());
        
//        define dso_local i32 @a3(i32 %0, i32* %1, [3 x i32] *%2) {
//                    %4 = alloca [3 x i32]*
//                    store [3 x i32]* %2, [3 x i32]* * %4
//                    %5 = alloca i32*
//                    store i32* %1, i32* * %5
//                    %6 = alloca i32
//                    store i32 %0, i32 * %6
//                    %7 = load i32, i32* %6
//                    %8 = load i32*, i32* * %5
//                    %9 = getelementptr i32, i32* %8, i32 1
//                    %10 = load i32, i32* %9
//                    %11 = mul i32 %7, %10
//                    %12 = load [3 x i32] *, [3 x i32]* * %4
//                    %13 = getelementptr [3 x i32], [3 x i32]* %12, i32 2
//                    %14 = getelementptr [3 x i32], [3 x i32]* %13, i32 0, i32 1
//                    %15 = load i32, i32 *%14
//                    %16 = sub i32 %11, %15
//                    ret i32 %16
//        }
        //未知长度的数组指针 %1
        if (Objects.equals(targetType, "i32*")) {
            //return this.getName() + " = getelementptr " + targetType + ", " + targetType + "* " + pointer.getName() +
            //        ", i32 " + offset;
            return this.getName() + " = getelementptr " + "i32, i32* " + pointer.getName() +
                    ", i32 " + offset.getName();
        }
        //[n * i32]
        else {
            return this.getName() + " = getelementptr " + targetType + ", " + targetType + "* " + pointer.getName() +
                    ", i32 0, i32 " + offset.getName();
        }
    }
    
    public void toAssembly() {
        super.toAssembly();
        Register pointerReg = Register.K0;
        Register offsetReg = Register.K1;
        Register targetReg = MipsBuilder.getInstance().getRegByValue(this.getName());
        if (targetReg == null) {
            targetReg = Register.K0;
        }
        
        //获得 pointer 中的值，存入 pointerReg
        if (pointer instanceof GlobalVar) {
            new LaAsm(pointerReg,pointer.getName().substring(1));
        }
        else if (MipsBuilder.getInstance().getRegByValue(pointer.getName()) != null) {
            pointerReg = MipsBuilder.getInstance().getRegByValue(pointer.getName());
        }
        else {
            new MemAsm(MemAsm.Op.LW,pointerReg,Register.SP,MipsBuilder.getInstance().getOffsetOfValue(pointer.getName()));
        }
        String offsetName = offset.getName();
        int flag = 0;
        int i;
        for (i = 0; i < offsetName.length(); i++) {
            if (!Character.isDigit(offsetName.charAt(i))) {
                flag = 1;
                break;
            }
        }
        //offset 是常数，直接*4与pointer相加
        if (offset instanceof Constant || flag == 0) {
            new AluAsm(AluAsm.Op.ADDI,targetReg,pointerReg,4*Integer.parseInt(offset.getName()));
        }
        else {
            //offset在寄存器中
            if (MipsBuilder.getInstance().getRegByValue(offset.getName()) != null) {
                offsetReg = MipsBuilder.getInstance().getRegByValue(offset.getName());
            }
            //offset在栈上
            else {
                int offset1 = MipsBuilder.getInstance().getOffsetOfValue(offset.getName());
                if (offset1 == Integer.MAX_VALUE) {
                    MipsBuilder.getInstance().subCurOffset(4);
                    offset1 = MipsBuilder.getInstance().getCurOffset();
                    MipsBuilder.getInstance().addOffsetOfValue(offset.getName(),offset1);
                }
                new MemAsm(MemAsm.Op.LW,offsetReg,Register.SP,offset1);
            }
            //offsetReg*4
            new AluAsm(AluAsm.Op.SLL,Register.K1,offsetReg,2);
            //offset+pointer
            new AluAsm(AluAsm.Op.ADDU,targetReg,Register.K1,pointerReg);
        }
        if (MipsBuilder.getInstance().getRegByValue(this.getName()) == null) {
            MipsBuilder.getInstance().subCurOffset(4);
            int curOffset = MipsBuilder.getInstance().getCurOffset();
            MipsBuilder.getInstance().addOffsetOfValue(this.getName(),curOffset);
            new MemAsm(MemAsm.Op.SW,targetReg,Register.SP,curOffset);
        }
    }
    
}
