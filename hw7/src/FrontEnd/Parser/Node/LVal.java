package FrontEnd.Parser.Node;

import FrontEnd.Symbol.ConstSymbol;
import FrontEnd.Symbol.Symbol;
import FrontEnd.Symbol.VarSymbol;
import LLVM.*;
import LLVM.Instruction.*;

import java.util.ArrayList;
import java.util.Objects;

//LVal → Ident {'[' Exp ']'}
public class LVal extends Node{
    private String varName;
    //private Symbol symbol;
    public LVal(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
        varName = ((TokenNode)getChildren().get(0)).getToken().getContent();
        //symbol = IRBuilder.getInstance().getTableStack().getSymbol(varName);
    }
    
    //对于0维变量，返回对应的IR
    //得到是这个变量存储的寄存器
    public Value genIRforCalculate() {
        String varName = ((TokenNode)getChildren().get(0)).getToken().getContent();
        Symbol symbol = IRBuilder.getInstance().getTableStack().getSymbol(varName);
        return symbol.getIr();
    }
    
    public int calculate() {
        String varName = ((TokenNode)getChildren().get(0)).getToken().getContent();
        Symbol symbol = IRBuilder.getInstance().getTableStack().getSymbol(varName);
        
        if (symbol == null) {
            return 0;
        }
        
        ArrayList<Integer> dims = new ArrayList<>();
        for (Node node : getChildren()) {
            if (node instanceof Exp) {
                dims.add(node.calculate());
            }
        }
        
            //0维变量
            if (symbol.getDims().size() == 0) {
                if (symbol.getIr() instanceof GlobalVar) {
                    dims.add(0);
                    return Integer.parseInt(((GlobalVar)symbol.getIr()).getConstInteger(dims).getName());
                }
                else if (symbol.getIr() instanceof LocalVar) {
                    dims.add(0);
                    return Integer.parseInt(((LocalVar)symbol.getIr()).getConstInteger(dims).getName());
                }
            }
            //数组
            else {
                if (symbol.getIr() instanceof GlobalVar) {
                    return Integer.parseInt(((GlobalVar)symbol.getIr()).getConstInteger(dims).getName());
                }
                else if (symbol.getIr() instanceof LocalVar) {
                    return Integer.parseInt(((LocalVar)symbol.getIr()).getConstInteger(dims).getName());
                }
            }
            //TODO
            return 0;
    }
    
    //TODO
    //只考虑了0维变量
    public Value genIRforAssign() {
        Symbol symbol = IRBuilder.getInstance().getTableStack().getSymbol(varName);
        //if (getChildren().size() == 1) {
        if (symbol.getDims().size() == 0) {
            //String varName = ((TokenNode)getChildren().get(0)).getToken().getContent();
            //对应的是这个var存储的变量
            return symbol.getIr();
        }
        //数组，如果是赋值语句的话，不会得到数组指针
        //得到存储这个值的寄存器并返回（i32*）
        //LVal '=' Exp ';'
        else {
            ArrayList<Integer> dims = new ArrayList<>();
            if (symbol instanceof VarSymbol || symbol instanceof ConstSymbol) {
                //对应数组被压成一维以前的各维度数
                dims = symbol.getDims();
            }
            //现在各维度
            ArrayList<Value> rParamDims  = new ArrayList<>();
            for (Node node : getChildren()) {
                if (node instanceof Exp) {
                    rParamDims.add(node.genIR());
                }
            }
            //得到压缩到一维的坐标
            int i,j;
            Value offset = new Value(LLVMType.INT32,Integer.toString(0));
            for (i = 0; i < rParamDims.size(); i++) {
                Value subOffset = rParamDims.get(i);
                for (j = i+1; j < dims.size(); j++) {
                    Constant constant = new Constant(LLVMType.INT32,dims.get(j).toString());
                    MulInstruction mulInstr = new MulInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),subOffset,constant);
                    IRBuilder.getInstance().addInstr(mulInstr);
                    subOffset = mulInstr;
                }
                AddInstruction addInstr = new AddInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),offset,subOffset);
                IRBuilder.getInstance().addInstr(addInstr);
                offset = addInstr;
            }
            //得到对应位置的指针
            Value pointer = null;
            if (symbol.getIr() instanceof AllocaInstruction && Objects.equals(symbol.getIr().getTargetType(), "i32*")) {
                //先load出二重指针
                LoadInstruction loadInstr = new LoadInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),symbol.getIr(),"i32*");
                pointer = loadInstr;
                IRBuilder.getInstance().addInstr(loadInstr);
            }
            else {
                pointer = symbol.getIr();
            }
            //%9 = getelementptr i32, i32* %8, i32 1
            GetElementPtrInstruction getElementPtrInstr = new GetElementPtrInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),
                    pointer,pointer.getTargetType(),offset);
            IRBuilder.getInstance().addInstr(getElementPtrInstr);
            //把存指针的load出来
            //LoadInstruction loadInstr = new LoadInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),
            //        getElementPtrInstr,LLVMType.INT32_POINTER);
            //IRBuilder.getInstance().addInstr(loadInstr);
            return getElementPtrInstr;
        }
    }
    
    //TODO
    //只考虑了0维变量
    //得到的是load了这个变量值的寄存器
    public Value genIRforExp() {
        Symbol symbol = IRBuilder.getInstance().getTableStack().getSymbol(varName);
        //0维变量
        //if (getChildren().size() == 1) {
        if (symbol.getDims().size() == 0) {
            Value storeIr = symbol.getIr();
            //需要将它load出来
            LoadInstruction loadInstr = new LoadInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),storeIr,"i32");
            IRBuilder.getInstance().addInstr(loadInstr);
            return loadInstr;
        }
        //数组
        //LVal → Ident {'[' Exp ']'}
        else {
            ArrayList<Integer> dims = new ArrayList<>();
            if (symbol instanceof VarSymbol || symbol instanceof ConstSymbol) {
                //对应数组被压成一维以前的各维度数
                dims = symbol.getDims();
            }
            //现在各维度
            /*ArrayList<Integer> rParamDims = new ArrayList<>();
            for (Node node : getChildren()) {
                if (node instanceof Exp) {
                    //TODO 可能不是constExp
                    int subRParamDim = ((Exp)node).calculate();
                    rParamDims.add(subRParamDim);
                }
            }*/
            ArrayList<Value> rParamDims  = new ArrayList<>();
            for (Node node : getChildren()) {
                if (node instanceof Exp) {
                    rParamDims.add(node.genIR());
                }
            }
            //得到压缩到一维的坐标
            /*int i,j;
            int offset = 0;
                for (i = 0; i < rParamDims.size(); i++) {
                    int subOffset = rParamDims.get(i);
                    for (j = i + 1;j < dims.size(); j++) {
                        subOffset *= dims.get(j);
                    }
                    offset += subOffset;
                }*/
            int i,j;
            Value offset = new Value(LLVMType.INT32,Integer.toString(0));
            for (i = 0; i < rParamDims.size(); i++) {
                Value subOffset = rParamDims.get(i);
                for (j = i+1; j < dims.size(); j++) {
                    Constant constant = new Constant(LLVMType.INT32,dims.get(j).toString());
                    MulInstruction mulInstr = new MulInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),subOffset,constant);
                    IRBuilder.getInstance().addInstr(mulInstr);
                    subOffset = mulInstr;
                }
                AddInstruction addInstr = new AddInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),offset,subOffset);
                IRBuilder.getInstance().addInstr(addInstr);
                offset = addInstr;
            }
            Value pointer = null;
            
            //TODO 除了 funcSymbol 不会有 symbol 的 ir是allocaInstr，不会进入 if 分支
            //形参是 allocaInstruction
            if (symbol.getIr() instanceof AllocaInstruction && Objects.equals(symbol.getIr().getTargetType(), "i32*")) {
                //先load出二重指针
                LoadInstruction loadInstr = new LoadInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),symbol.getIr(),"i32*");
                pointer = loadInstr;
                IRBuilder.getInstance().addInstr(loadInstr);
            }
            else {
                pointer = symbol.getIr();
            }
            //得到对应位置的指针(直接传给函数)
            //%9 = getelementptr i32, i32* %8, i32 1
            GetElementPtrInstruction getElementPtrInstr = new GetElementPtrInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),
                    pointer,pointer.getTargetType(),offset);
            IRBuilder.getInstance().addInstr(getElementPtrInstr);
            //如果是要得到指针，直接返回指针
            if (rParamDims.size() < symbol.getDims().size()) {
                return getElementPtrInstr;
            }
            //如果是得到值，则需要load
            else {
                LoadInstruction loadInstr;
                loadInstr = new LoadInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),
                        getElementPtrInstr,"i32");
                IRBuilder.getInstance().addInstr(loadInstr);
                return loadInstr;
            }
        }
    }
}
