package FrontEnd.Parser.Node;

import FrontEnd.Lexer.TokenType;
import LLVM.BasicBlock;
import LLVM.IRBuilder;
import LLVM.Instruction.IcmpInstruction;
import LLVM.Instruction.IcmpOp;
import LLVM.Instruction.ZextInstruction;
import LLVM.LLVMType;
import LLVM.Value;

import java.util.ArrayList;

//EqExp -> RelExp {'==' RelExp | '!=' RelExp}
public class EqExp extends Node{
    public EqExp(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    //返回的是一个 i1 的 Value
    @Override
    public Value genIR() {
        Value operand1 = getChildren().get(0).genIR();
        BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
        //只有一个RelExp，返回的可能是 i32 也可能是 i1，取决于 RelExp 中的 AddExp 数量
        //如果是 i32 ，需要和 0 作比较
        if (getChildren().size() == 1) {
            //只有一个AddExp 返回 i32
            if (getChildren().get(0).getChildren().size() == 1) {
                Value operand2 = new Value(LLVMType.INT32,"0");
                IcmpInstruction icmpInstr = new IcmpInstruction(LLVMType.INSTR, IRBuilder.getInstance().genLocalVarName(),basicBlock,operand1,operand2,IcmpOp.NE);
                IRBuilder.getInstance().addInstr(icmpInstr);
                operand1 = icmpInstr;
                //返回 i1 时无需多余处理
            }
            return operand1;
        }
        //有多个 RelExp
        //均需将结果转化为 i32
        //当含有多个 AddExp 时，返回值是 i1
        else {
            LLVMType fromType = LLVMType.INT1;
            LLVMType toType = LLVMType.INT32;
            IcmpOp op = null;
            Value operand2 = null;
            //operand1
            if (getChildren().get(0).getChildren().size() > 1) {
                ZextInstruction ZextInstr = new ZextInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),basicBlock,operand1,fromType,toType);
                operand1 = ZextInstr;
                IRBuilder.getInstance().addInstr(ZextInstr);
            }
            int i;
            for (i = 1; i < getChildren().size(); i++) {
                //确定符号
                if (getChildren().get(i) instanceof TokenNode) {
                    TokenType tokenType = ((TokenNode)getChildren().get(i)).getToken().getType();
                    if (tokenType == TokenType.EQL) {
                        op = IcmpOp.EQ;
                    } else {
                        op = IcmpOp.NE;
                    }
                }
                if (getChildren().get(i) instanceof RelExp) {
                    //操作数2
                    operand2 = getChildren().get(i).genIR();
                    //化成 i32
                    if (getChildren().get(i).getChildren().size() > 1) {
                        ZextInstruction ZextInstr = new ZextInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),basicBlock,operand2,fromType,toType);
                        operand2 = ZextInstr;
                        IRBuilder.getInstance().addInstr(ZextInstr);
                    }
                    //Icmp
                    IcmpInstruction IcmpInstr = new IcmpInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),basicBlock,operand1,operand2,op);
                    IRBuilder.getInstance().addInstr(IcmpInstr);
                    operand1 = IcmpInstr;
                    //operand1 变成了 i1
                    //如果不是最后一次，那么就需要把 operand1 变成 i32 以继续后续计算
                    if (i != getChildren().size() - 1) {
                        ZextInstruction ZextInstr = new ZextInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),basicBlock,operand1,fromType,toType);
                        operand1 = ZextInstr;
                        IRBuilder.getInstance().addInstr(ZextInstr);
                    }
                }
            }
            return operand1;
        }
    }
}
