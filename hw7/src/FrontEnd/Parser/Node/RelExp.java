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

//RelExp -> AddExp {'<' AddExp | '>' AddExp | '<=' AddExp | '>=' AddExp}
//如果只有1个 AddExp 时，返回的是 i32
//如果存在比较，产生了 icmp 指令，返回的是 i1
public class RelExp extends Node{
    public RelExp(int startLine, int endLine, FrontEnd.Parser.Node.SyntaxType SyntaxType, ArrayList<Node> children) {
        super(startLine, endLine, SyntaxType, children);
    }
    
    public Value genIR() {
        BasicBlock basicBlock = IRBuilder.getInstance().getCurBasicBlock();
        Value operand1 = getChildren().get(0).genIR();
        Value operand2;
        int i;
        TokenType tokenType;
        IcmpOp op = null;
        //operand1 被 icmp更新过的标志
        int reFlag = 0;
        for (i = 1; i < getChildren().size(); i++) {
            if (getChildren().get(i) instanceof TokenNode) {
                tokenType = ((TokenNode)getChildren().get(i)).getToken().getType();
                if (tokenType == TokenType.LSS) {
                    op = IcmpOp.SLT;
                } else if (tokenType == TokenType.GRE) {
                    op = IcmpOp.SGT;
                } else if (tokenType == TokenType.LEQ) {
                    op = IcmpOp.SLE;
                } else {
                    op = IcmpOp.SGE;
                }
            }
            if (getChildren().get(i) instanceof AddExp) {
                if (reFlag == 1) {
                    ZextInstruction zextInstr = new ZextInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),basicBlock,operand1,LLVMType.INT1,LLVMType.INT32);
                    IRBuilder.getInstance().addInstr(zextInstr);
                    operand1 = zextInstr;
                }
                operand2 = getChildren().get(i).genIR();
                IcmpInstruction IcmpInstr = new IcmpInstruction(LLVMType.INSTR,IRBuilder.getInstance().genLocalVarName(),basicBlock,operand1,operand2,op);
                IRBuilder.getInstance().addInstr(IcmpInstr);
                //更新 operand1
                operand1 = IcmpInstr;
                reFlag = 1;
            }
        }
        return operand1;
    }
}
