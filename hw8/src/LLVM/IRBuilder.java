package LLVM;

import FrontEnd.Symbol.TableStack;
import LLVM.Instruction.RetInstruction;

import java.util.Objects;

public class IRBuilder {
    //函数前缀
    private static final String FUNC_PREFIX = "@f_";
    //全局变量
    private static final String GLOBAL_VAR_PREFIX = "@g";
    //局部变量
    private static final String LOCAL_VAR_PREFIX = "%v";
    //BasicBlock
    private static final String BASIC_BLOCK_PREFIX = "bb";
    //函数形参
    private static final String PARAM_PREFIX = "%a";
    //单例模式
    private static IRBuilder irBuilder = new IRBuilder();
    
    //计数
    private int globalVarCnt;
    private int localVarCnt;
    private int basicBlockCnt;
    private int paramCnt;
    //当前正在处理，用于保证生成 IR 的结构
    private Module curModule;
    private Function curFunction;
    private BasicBlock curBasicBlock;
    private Instr lastInstr;
    //符号表栈
    private TableStack tableStack;
    //当前是否是 global
    private boolean isGlobal;
    //loop 下一个要去到的 block（逻辑上）
    private BasicBlock loopNextBlock;
    //loop 下一个 block
    private BasicBlock loopFollowBlock;
    public IRBuilder() {
        this.globalVarCnt = 0;
        this.localVarCnt = 0;
        this.paramCnt = 0;
        this.basicBlockCnt = 0;
        this.curModule = new Module();
        this.tableStack = new TableStack();
        //最开始的时候是global
        this.isGlobal = true;
    }
    
    public static IRBuilder getInstance() {
        return irBuilder;
    }
    
    public String genLocalVarName() {
        String name = LOCAL_VAR_PREFIX + localVarCnt;
        localVarCnt++;
        return name;
    }
    
    public  void setLocalVarCnt(int cnt) {
        localVarCnt = cnt;
    }
    
    public String genParamName() {
        String name = PARAM_PREFIX + paramCnt;
        paramCnt++;
        return name;
    }
    
    public void setParamCnt(int cnt) {
        paramCnt = cnt;
    }
    
    public String genBasicBlockName() {
        String name = BASIC_BLOCK_PREFIX + basicBlockCnt;
        basicBlockCnt++;
        return name;
    }
    
    public void setBasicBlockCnt(int cnt) {
        basicBlockCnt = cnt;
    }
    
    public String genGlobalVarName() {
        String name = GLOBAL_VAR_PREFIX + globalVarCnt;
        globalVarCnt++;
        return name;
    }
    
    public void setGlobalVarCnt(int cnt) {
        globalVarCnt = cnt;
    }
    
    public String genFuncName(String name) {
        if (Objects.equals(name, "main")) {
            return "@" + name;
        }
        return FUNC_PREFIX + name;
    }
    
    public TableStack getTableStack() {
        return this.tableStack;
    }
    
    //用于维持 IR 的结构
    //module
    public void addGlobalVar(GlobalVar globalVar) {
        curModule.addGlobalVal(globalVar);
    }
    public void addFunction(Function function) {
        curModule.addFunction(function);
        setCurFunction(function);
        lastInstr = null;
    }
    //function
    public void addBasicBlock(BasicBlock basicBlock) {
        curFunction.addBasicBlocks(basicBlock);
    }
    public void addParam(Param param) {
        curFunction.addParam(param);
    }
    //BasicBlock
    public void addInstr(Instr instr) {
        curBasicBlock.addInstr(instr);
        this.lastInstr = instr;
    }
    //set cur
    public void setCurFunction(Function function) {
        curFunction = function;
    }
    public void setCurBasicBlock(BasicBlock basicBlock) {
        curBasicBlock = basicBlock;
    }
    //是否global
    public void setGlobal(Boolean global) {
        this.isGlobal = global;
    }
    
    public boolean isGlobal() {
        return isGlobal;
    }
    
    public BasicBlock getCurBasicBlock() {
        return curBasicBlock;
    }
    
    public Function getCurFunction() {
        return curFunction;
    }
    
    public Module getCurModule() {
        return curModule;
    }
    
    public Function getFuncByName (String funcName) {
        return curModule.getFunctionByName(funcName);
    }
    
    public void checkAndPadRet() {
        if (!(lastInstr instanceof RetInstruction)) {
            if (curFunction.getReturnType() == LLVMType.VOID) {
                RetInstruction retInstruction = new RetInstruction(LLVMType.INSTR,null,curBasicBlock,LLVMType.VOID,null);
                curBasicBlock.addInstr(retInstruction);
            } else {
                RetInstruction retInstruction = new RetInstruction(LLVMType.INSTR,null,curBasicBlock,LLVMType.INT32,new Constant(LLVMType.INT32,"0"));
                curBasicBlock.addInstr(retInstruction);
            }
        }
    }
    
    public BasicBlock getLoopFollowBlock() {
        return loopFollowBlock;
    }
    
    public BasicBlock getLoopNextBlock() {
        return loopNextBlock;
    }
    
    public void setLoopNextBlock(BasicBlock loopNextBlock) {
        this.loopNextBlock = loopNextBlock;
    }
    
    public void setLoopFollowBlock(BasicBlock loopFollowBlock) {
        this.loopFollowBlock = loopFollowBlock;
    }
}
