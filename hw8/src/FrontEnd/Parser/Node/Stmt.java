package FrontEnd.Parser.Node;

import FrontEnd.Lexer.TokenType;
import LLVM.*;
import LLVM.Instruction.BrInstruction;
import LLVM.Instruction.CallInstruction;
import LLVM.Instruction.RetInstruction;
import LLVM.Instruction.StoreInstruction;

import java.util.ArrayList;

// Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
//| [Exp] ';' //有无Exp两种情况
//| Block
///| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
///| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
//| 'break' ';' | 'continue' ';'
//| 'return' [Exp] ';' // 1.有Exp 2.无Exp
//| LVal '=' 'getint''('')'';'
//| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp

public class Stmt extends Node{
    //loop 下一个要去到的 block（逻辑上）
    //private BasicBlock loopNextBlock;
    //loop 下一个 block
    //private BasicBlock loopFollowBlock;
    private Function getIntFunc = new Function(LLVMType.FUNCTION,"@getint",LLVMType.INT32);
    private Function putChFunc = new Function(LLVMType.FUNCTION,"@putch",LLVMType.VOID);
    private Function putIntFunc = new Function(LLVMType.FUNCTION,"@putint",LLVMType.VOID);
    public Stmt(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    @Override
    public Value genIR() {
        if (getChildren().get(0) instanceof TokenNode) {
            TokenType tokenType = ((TokenNode) getChildren().get(0)).getToken().getType();
            if (tokenType == TokenType.IFTK) {
                return If_genIR();
            } else if (tokenType == TokenType.FORTK) {
                return For_genIR();
            } else if (tokenType == TokenType.BREAKTK) {
                return Break_genIR();
            } else if (tokenType == TokenType.CONTINUETK) {
                return Continue_genIR();
            } else if (tokenType == TokenType.RETURNTK) {
                return Return_genIR();
            } else if (tokenType == TokenType.PRINTFTK){
                return Printf_genIR();
            } else {
                //只有一个;
                return null;
            }
        } else if (getChildren().get(0) instanceof Block) {
            return Block_genIR();
        } else if (getChildren().get(0) instanceof Exp) {
            return Exp_genIR();
        } else if (getChildren().get(0) instanceof LVal) {
            if (getChildren().get(2) instanceof Exp) {
                return LValAssign_genIR();
            } else {
                return LValGetInt_genIR();
            }
        }
        //不知道是啥
        else {
            return null;
        }
    }
    
    public Value If_genIR() {
        //'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
        BasicBlock thenBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
        IRBuilder.getInstance().addBasicBlock(thenBlock);
        //有 else
        if (getChildren().size() > 5) {
            //basicBlock
            BasicBlock elseBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            BasicBlock followBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(elseBlock);
            IRBuilder.getInstance().addBasicBlock(followBlock);
            //分析Cond
            ((Cond)getChildren().get(2)).genIRforCond(thenBlock,elseBlock);
            //分析thenBlock，结束后跳转到followBlock
            IRBuilder.getInstance().setCurBasicBlock(thenBlock);
            getChildren().get(4).genIR();
            //无条件跳转 ，将 cond 设置为1（其实用不上)，elseBlock 为 null
            BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),new Constant(LLVMType.INT1,"1"),followBlock,null);
            IRBuilder.getInstance().addInstr(brInstr);
            //分析elseBlock，结束后跳转到followBlock
            IRBuilder.getInstance().setCurBasicBlock(elseBlock);
            getChildren().get(6).genIR();
            BrInstruction brInstr2 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),new Constant(LLVMType.INT1,"1"),followBlock,null);
            IRBuilder.getInstance().addInstr(brInstr2);
            //一切结束后 cur 是 followBlock
            IRBuilder.getInstance().setCurBasicBlock(followBlock);
        }
        //无 else
        else {
            //followBlock
            BasicBlock followBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(followBlock);
            //分析Cond
            ((Cond)getChildren().get(2)).genIRforCond(thenBlock,followBlock);
            //分析thenBlock，结束后跳转到followBlock
            IRBuilder.getInstance().setCurBasicBlock(thenBlock);
            getChildren().get(4).genIR();
            BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),new Constant(LLVMType.INT1,"1"),followBlock,null);
            IRBuilder.getInstance().addInstr(brInstr);
            IRBuilder.getInstance().setCurBasicBlock(followBlock);
        }
        return null;
    }
    
    //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    //  0    1     2       3     4    5      6      7    8
    //ForStmt → LVal '=' Exp
    public Value For_genIR() {
        int t;
        if (getChildren().size() == 9) {
            //无缺省
            t = 0;
        } else if (getChildren().size() == 8) {
            if (getChildren().get(2) instanceof TokenNode) {
                //lose ForStmt1
                t = 1;
            } else if (getChildren().get(4) instanceof TokenNode) {
                //lose Cond
                t = 2;
            } else {
                //lose ForStmt2
                t = 3;
            }
        } else if (getChildren().size() == 7) {
            if (getChildren().get(2) instanceof TokenNode) {
                if (getChildren().get(3) instanceof TokenNode) {
                    //lose ForStmt1 Cond
                    t = 4;
                } else {
                    //lose ForStmt1 ForStmt2
                    t = 5;
                }
            } else {
                //lose Cond ForStmt2
                t = 6;
            }
        } else {
            //all lose
            t = 7;
        }
        BasicBlock condBlock = null;
        //loop body
        BasicBlock loopBody = null;
        //ForStmt2
        BasicBlock ForStmt2Block = null;
        //follow Block
        BasicBlock followBlock = null;
        //均无缺少
        if (t == 0) {
            //Cond BasicBlock
            condBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(condBlock);
            //loop body
            loopBody = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(loopBody);
            //ForStmt2
            ForStmt2Block = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(ForStmt2Block);
            //follow Block
            followBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(followBlock);
    
            //ForStmt1 生成一条 store 指令 并跳转到 condBlock
            getChildren().get(2).genIR();
            BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),condBlock,null);
            IRBuilder.getInstance().addInstr(brInstr);
            //分析Cond 1->loopBody 0->follow
            IRBuilder.getInstance().setCurBasicBlock(condBlock);
            ((Cond)getChildren().get(4)).genIRforCond(loopBody,followBlock);
            //分析 loopBody 并跳转至ForStmt2
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
            IRBuilder.getInstance().setLoopNextBlock(ForStmt2Block);
            IRBuilder.getInstance().setLoopFollowBlock(followBlock);
            getChildren().get(8).genIR();
            BrInstruction brInstr2 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),ForStmt2Block,null);
            IRBuilder.getInstance().addInstr(brInstr2);
            //分析 ForStmt2 跳转至 condBlock
            IRBuilder.getInstance().setCurBasicBlock(ForStmt2Block);
            getChildren().get(6).genIR();
            BrInstruction brInstr3 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),condBlock,null);
            IRBuilder.getInstance().addInstr(brInstr3);
        }
        //缺少 ForStmt1
        else if (t == 1) {
            //Cond BasicBlock
            condBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(condBlock);
            //loop body
            loopBody = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(loopBody);
            //ForStmt2
            ForStmt2Block = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(ForStmt2Block);
            //follow Block
            followBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(followBlock);
            //跳转到 Cond Block
            BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),condBlock,null);
            IRBuilder.getInstance().addInstr(brInstr);
            //分析Cond 1->loopBody 0->follow
            IRBuilder.getInstance().setCurBasicBlock(condBlock);
            ((Cond)getChildren().get(3)).genIRforCond(loopBody,followBlock);
            //分析 loopBody 并跳转至ForStmt2
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
            IRBuilder.getInstance().setLoopFollowBlock(followBlock);
            IRBuilder.getInstance().setLoopNextBlock(ForStmt2Block);
            getChildren().get(7).genIR();
            BrInstruction brInstr2 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),ForStmt2Block,null);
            IRBuilder.getInstance().addInstr(brInstr2);
            //分析 ForStmt2 跳转至 condBlock
            IRBuilder.getInstance().setCurBasicBlock(ForStmt2Block);
            getChildren().get(5).genIR();
            BrInstruction brInstr3 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),condBlock,null);
            IRBuilder.getInstance().addInstr(brInstr3);
            IRBuilder.getInstance().setCurBasicBlock(condBlock);
        }
        //缺少 Cond
        else if (t == 2) {
            //loop body
            loopBody = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(loopBody);
            //ForStmt2
            ForStmt2Block = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(ForStmt2Block);
            //follow Block
            followBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(followBlock);
    
            //ForStmt1 生成一条 store 指令 并跳转到 loop Body
            getChildren().get(2).genIR();
            BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),loopBody,null);
            IRBuilder.getInstance().addInstr(brInstr);
            //分析loopBody 跳转至ForStmt2
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
            IRBuilder.getInstance().setLoopNextBlock(ForStmt2Block);
            IRBuilder.getInstance().setLoopFollowBlock(followBlock);
            getChildren().get(7).genIR();
            BrInstruction brInstr2 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),ForStmt2Block,null);
            IRBuilder.getInstance().addInstr(brInstr2);
            //分析 ForStmt2 跳转至 loopBody
            IRBuilder.getInstance().setCurBasicBlock(ForStmt2Block);
            getChildren().get(5).genIR();
            BrInstruction brInstr3 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),loopBody,null);
            IRBuilder.getInstance().addInstr(brInstr3);
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
        }
        //缺少 ForStmt2
        else if (t == 3) {
            //Cond BasicBlock
            condBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(condBlock);
            //loop body
            loopBody = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(loopBody);
            //follow Block
            followBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(followBlock);
    
            //ForStmt1 生成一条 store 指令 并跳转到 condBlock
            getChildren().get(2).genIR();
            BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),condBlock,null);
            IRBuilder.getInstance().addInstr(brInstr);
            //分析Cond 1->loopBody 0->follow
            IRBuilder.getInstance().setCurBasicBlock(condBlock);
            ((Cond)getChildren().get(4)).genIRforCond(loopBody,followBlock);
            //分析 loopBody 并跳转至 condBlock
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
            IRBuilder.getInstance().setLoopNextBlock(condBlock);
            IRBuilder.getInstance().setLoopFollowBlock(followBlock);
            getChildren().get(7).genIR();
            BrInstruction brInstr2 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),condBlock,null);
            IRBuilder.getInstance().addInstr(brInstr2);
            IRBuilder.getInstance().setCurBasicBlock(condBlock);
        }
        //缺少 ForStmt1 和 Cond
        else if (t == 4) {
            //loop body
            loopBody = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(loopBody);
            //ForStmt2
            ForStmt2Block = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(ForStmt2Block);
            //follow Block
            followBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(followBlock);
    
            //直接跳转到 loop Body
            BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),loopBody,null);
            IRBuilder.getInstance().addInstr(brInstr);
            //分析loopBody 跳转至 ForStmt2
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
            IRBuilder.getInstance().setLoopFollowBlock(followBlock);
            IRBuilder.getInstance().setLoopNextBlock(ForStmt2Block);
            getChildren().get(6).genIR();
            BrInstruction brInstr2 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),ForStmt2Block,null);
            IRBuilder.getInstance().addInstr(brInstr2);
            //分析 ForStmt2 跳转至 loopBody
            IRBuilder.getInstance().setCurBasicBlock(ForStmt2Block);
            getChildren().get(4).genIR();
            BrInstruction brInstr3 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),loopBody,null);
            IRBuilder.getInstance().addInstr(brInstr3);
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
        }
        //缺少 ForStmt1 和 ForStmt2
        else if (t == 5) {
            //Cond BasicBlock
            condBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(condBlock);
            //loop body
            loopBody = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(loopBody);
            //follow Block
            followBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(followBlock);
    
            //直接跳转到 condBlock
            BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),condBlock,null);
            IRBuilder.getInstance().addInstr(brInstr);
            //分析Cond 1->loopBody 0->follow
            IRBuilder.getInstance().setCurBasicBlock(condBlock);
            ((Cond)getChildren().get(3)).genIRforCond(loopBody,followBlock);
            //分析 loopBody 并跳转至 condBlock
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
            IRBuilder.getInstance().setLoopNextBlock(condBlock);
            IRBuilder.getInstance().setLoopFollowBlock(followBlock);
            getChildren().get(6).genIR();
            BrInstruction brInstr2 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),condBlock,null);
            IRBuilder.getInstance().addInstr(brInstr2);
            IRBuilder.getInstance().setCurBasicBlock(condBlock);
        }
        //缺少 Cond 和 ForStmt2
        else if (t == 6) {
            //loop body
            loopBody = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(loopBody);
            //follow Block
            followBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(followBlock);
    
            //ForStmt1 生成一条 store 指令 并跳转到 loop Body
            getChildren().get(2).genIR();
            BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),loopBody,null);
            IRBuilder.getInstance().addInstr(brInstr);
            //分析loopBody 跳转至 loopBody
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
            IRBuilder.getInstance().setLoopNextBlock(loopBody);
            IRBuilder.getInstance().setLoopFollowBlock(followBlock);
            getChildren().get(6).genIR();
            BrInstruction brInstr2 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),loopBody,null);
            IRBuilder.getInstance().addInstr(brInstr2);
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
        }
        //全都缺
        else {
            //loop body
            loopBody = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(loopBody);
            //follow Block
            followBlock = new BasicBlock(LLVMType.BASIC_BLOCK,IRBuilder.getInstance().genBasicBlockName(),IRBuilder.getInstance().getCurFunction());
            IRBuilder.getInstance().addBasicBlock(followBlock);
    
            //直接跳转到 loop Body
            BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),loopBody,null);
            IRBuilder.getInstance().addInstr(brInstr);
            //分析loopBody 跳转至 loopBody
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
            IRBuilder.getInstance().setLoopFollowBlock(followBlock);
            IRBuilder.getInstance().setLoopNextBlock(loopBody);
            getChildren().get(5).genIR();
            BrInstruction brInstr2 = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                    new Constant(LLVMType.INT1,"1"),loopBody,null);
            IRBuilder.getInstance().addInstr(brInstr2);
            IRBuilder.getInstance().setCurBasicBlock(loopBody);
        }
        IRBuilder.getInstance().setCurBasicBlock(followBlock);
        return null;
    }
    
    public Value Break_genIR() {
        //直接跳转到followBlock
        BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                new Constant(LLVMType.INT1,"1"),IRBuilder.getInstance().getLoopFollowBlock(),null);
        IRBuilder.getInstance().addInstr(brInstr);
        return null;
    }
    
    public Value Continue_genIR() {
        //直接跳转到nextBlock
        BrInstruction brInstr = new BrInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                new Constant(LLVMType.INT1,"1"),IRBuilder.getInstance().getLoopNextBlock(),null);
        IRBuilder.getInstance().addInstr(brInstr);
        return null;
    }
    
    // 'return' [Exp] ';'
    public Value Return_genIR() {
        BasicBlock parentBlock = IRBuilder.getInstance().getCurBasicBlock();
        // return;
        if (getChildren().size() == 2) {
            RetInstruction retInstr = new RetInstruction(LLVMType.INSTR,null,parentBlock,LLVMType.VOID,null);
            IRBuilder.getInstance().addInstr(retInstr);
        }
        // return Exp;
        else {
            Value retExp = getChildren().get(1).genIR();
            RetInstruction retInstr = new RetInstruction(LLVMType.INSTR,null,parentBlock,LLVMType.INT32,retExp);
            IRBuilder.getInstance().addInstr(retInstr);
        }
        return null;
    }
    
    //'printf''('FormatString{','Exp}')'';'
    //call void @putch(i32 58)
    //call void @putint(i32 %4)
    public Value Printf_genIR() {
        //TODO
        String formatString = ((TokenNode)getChildren().get(2)).getToken().getContent();
        formatString = formatString.substring(1,formatString.length()-1);
        ArrayList<Value> expArrayList = new ArrayList<>();
        int i;
        for (i = 3; i < getChildren().size(); i++) {
            if (getChildren().get(i) instanceof Exp) {
                Value exp = getChildren().get(i).genIR();
                expArrayList.add(exp);
            }
        }
        int j = 0;
        for (i = 0; i < formatString.length(); i++) {
            //putCh
            if (formatString.charAt(i) != '%' && formatString.charAt(i) != '\\') {
                ArrayList<Value> paramArrayList = new ArrayList<>();
                Param param = new Param(LLVMType.PARAM, Integer.valueOf(formatString.charAt(i)).toString(),"i32",new ArrayList<>());
                paramArrayList.add(param);
                CallInstruction callInstr = new CallInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                        putChFunc,paramArrayList);
                IRBuilder.getInstance().addInstr(callInstr);
            }
            //'\n'或者'\"'(不会出现)
            else if (formatString.charAt(i) == '\\') {
               i++;
                ArrayList<Value> paramArrayList = new ArrayList<>();
                Param param = new Param(LLVMType.PARAM, Integer.valueOf('\n').toString(),"i32",new ArrayList<>());
                paramArrayList.add(param);
                CallInstruction callInstr = new CallInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                        putChFunc,paramArrayList);
                IRBuilder.getInstance().addInstr(callInstr);
            }
            //putInt
            else {
                //吃掉 %d
                i++;
                //得到实参
                Value exp = expArrayList.get(j);
                j++;
                ArrayList<Value> paramArrayList = new ArrayList<>();
                paramArrayList.add(exp);
                CallInstruction callInstr = new CallInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),
                        putIntFunc,paramArrayList);
                IRBuilder.getInstance().addInstr(callInstr);
            }
        }
        return null;
    }
    
    // LVal '=' Exp ';'
    public Value LValAssign_genIR() {
        //TODO 目前只考虑了单变量
        Value LValValue = ((LVal)getChildren().get(0)).genIRforAssign();
        Value ExpValue = getChildren().get(2).genIR();
        BasicBlock parentBlock = IRBuilder.getInstance().getCurBasicBlock();
        StoreInstruction storeInstr = new StoreInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),parentBlock,
                ExpValue,LValValue,"i32");
        IRBuilder.getInstance().addInstr(storeInstr);
        return null;
    }
    
    //%3 = call i32 @getint()
    //store i32 %3, i32* %1
    //LVal '=' 'getint''('')'';'
    public Value LValGetInt_genIR() {
        //先 call getint 再 store
        CallInstruction callInstr = new CallInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),IRBuilder.getInstance().getCurBasicBlock(),
                getIntFunc,new ArrayList<>());
        IRBuilder.getInstance().addInstr(callInstr);
        Value to = ((LVal)getChildren().get(0)).genIRforAssign();
        StoreInstruction storeInstr = new StoreInstruction(LLVMType.INSTR,null,IRBuilder.getInstance().getCurBasicBlock(),callInstr,to,"i32");
        IRBuilder.getInstance().addInstr(storeInstr);
        return null;
    }
    
    public Value Exp_genIR() {
        //TODO
        getChildren().get(0).genIR();
        return null;
    }
    
    public Value Block_genIR() {
        IRBuilder.getInstance().getTableStack().enterBlock();
        //IRBuilder.getInstance().setLocalVarCnt(0);
        super.genIR();
        IRBuilder.getInstance().getTableStack().leaveBlock();
        return null;
    }
}
