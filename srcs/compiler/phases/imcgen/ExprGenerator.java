package compiler.phases.imcgen;

import compiler.data.abstree.*;
import compiler.data.abstree.visitor.AbsVisitor;
import compiler.data.imcode.*;
import compiler.data.layout.*;
import compiler.phases.frames.Frames;
import compiler.phases.seman.SemAn;

import java.util.Stack;
import java.util.Vector;

/**
 * @author sliva
 */
public class ExprGenerator implements AbsVisitor<ImcExpr, Stack<Frame>> {

    //Temp temp = new Temp();

    @Override
    public ImcExpr visit(AbsAtomExpr atomExpr, Stack<Frame> visArg) {
        if (atomExpr.type == AbsAtomExpr.Type.INT) {
            ImcGen.exprImCode.put(atomExpr, new ImcCONST(Integer.parseInt(atomExpr.expr)));
        } else if (atomExpr.type == AbsAtomExpr.Type.VOID) {
            ImcGen.exprImCode.put(atomExpr, new ImcCONST(0));
        } else if (atomExpr.type == AbsAtomExpr.Type.CHAR) {
            ImcGen.exprImCode.put(atomExpr, new ImcCONST((int) atomExpr.expr.charAt(1)));
        } else if (atomExpr.type == AbsAtomExpr.Type.BOOL) {
            if (atomExpr.expr.equals("true"))
                ImcGen.exprImCode.put(atomExpr, new ImcCONST(1));
            else if (atomExpr.expr.equals("false"))
                ImcGen.exprImCode.put(atomExpr, new ImcCONST(0));
        } else if (atomExpr.type == AbsAtomExpr.Type.PTR) {
            ImcGen.exprImCode.put(atomExpr, new ImcCONST(0));
        }

        return null;
    }

    @Override
    public ImcExpr visit(AbsBinExpr binExpr, Stack<Frame> visArg) {
        binExpr.fstExpr.accept(this, visArg);
        binExpr.sndExpr.accept(this, visArg);

        ImcExpr expr1 = ImcGen.exprImCode.get(binExpr.fstExpr);
        ImcExpr expr2 = ImcGen.exprImCode.get(binExpr.sndExpr);

        if (binExpr.oper != null)
            switch (binExpr.oper) {
                case ADD:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.ADD, expr1, expr2));
                    break;
                case SUB:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.SUB, expr1, expr2));
                    break;
                case MOD:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.MOD, expr1, expr2));
                    break;
                case MUL:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.MUL, expr1, expr2));
                    break;
                case DIV:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.DIV, expr1, expr2));
                    break;
                case EQU:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.EQU, expr1, expr2));
                    break;
                case NEQ:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.NEQ, expr1, expr2));
                    break;
                case LTH:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.LTH, expr1, expr2));
                    break;
                case LEQ:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.LEQ, expr1, expr2));
                    break;
                case GEQ:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.GEQ, expr1, expr2));
                    break;
                case GTH:
                    ImcGen.exprImCode.put(binExpr, new ImcBINOP(ImcBINOP.Oper.GTH, expr1, expr2));
                    break;
                default:
                    break;
            }

        return null;
    }


    @Override
    public ImcExpr visit(AbsUnExpr unExpr, Stack<Frame> visArg) {
        unExpr.subExpr.accept(this, visArg);
        ImcExpr expr = ImcGen.exprImCode.get(unExpr.subExpr);
        switch (unExpr.oper) {
            case ADD:
                ImcGen.exprImCode.put(unExpr, expr);
                break;
            case SUB:
                ImcGen.exprImCode.put(unExpr, new ImcUNOP(ImcUNOP.Oper.NEG, expr));
                break;
            case NOT:
                ImcGen.exprImCode.put(unExpr, new ImcUNOP(ImcUNOP.Oper.NOT, expr));
                break;
            case ADDR:
                ImcGen.exprImCode.put(unExpr, expr);
                break;
            case DATA:
                ImcGen.exprImCode.put(unExpr, new ImcMEM(expr));
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public ImcExpr visit(AbsFunName funName, Stack<Frame> visArg) {
        Vector<ImcExpr> imcExprs = new Vector<>();
        Frame frame = Frames.frames.get((AbsFunDecl)SemAn.declaredAt.get(funName));

        if(frame == null || frame.depth == 1)
            imcExprs.add(new ImcTEMP(visArg.peek().FP));
        else {
            ImcExpr expr = new ImcTEMP(visArg.peek().FP);
            for(int i = frame.depth; i < visArg.peek().depth; i++){
                expr = new ImcMEM(expr);
            }
            imcExprs.add(expr);
        }

        for(AbsExpr arg : funName.args.args()){
            imcExprs.add(ImcGen.exprImCode.get(arg));
        }
        ImcGen.exprImCode.put(funName, new ImcCALL(frame.label, imcExprs));
        return null;
    }

//    @Override
//    public ImcExpr visit(AbsFunName funName, Stack<Frame> visArg) {
//        Vector<ImcExpr> allArgs = new Vector<>(funName.args.numArgs());
//        Frame frame = Frames.frames.get((AbsFunDecl) SemAn.declaredAt.get(funName));
//
//        int localDepth = visArg.peek().depth;
//        int numberMEM = localDepth - frame.depth + 1;
//
//
//        if (numberMEM == 0) {
//            allArgs.add(new ImcTEMP(temp));
//        } else {
//            ImcExpr expr2 = povozi(numberMEM);
//            allArgs.add(expr2);
//        }
//
//        for (AbsExpr arg : funName.args.args()) {
//            ImcExpr expr = ImcGen.exprImCode.get(arg);
//            allArgs.add(expr);
//        }
//
//        ImcGen.exprImCode.put(funName, new ImcCALL(frame.label, allArgs));
//        return null;
//    }
//
//    private ImcExpr povozi(int numberMEM) {
//        if (numberMEM == 0)
//            return new ImcTEMP(temp);
//        numberMEM--;
//        return new ImcMEM(povozi(numberMEM));
//    }

    @Override
    public ImcExpr visit(AbsVarName varName, Stack<Frame> visArg) {
        Access varAcces = Frames.accesses.get((AbsVarDecl) SemAn.declaredAt.get(varName));
        if (varAcces instanceof AbsAccess){
            ImcExpr address = new ImcNAME(((AbsAccess) varAcces).label);
            ImcGen.exprImCode.put(varName, new ImcMEM(address));
            return null;
        } else {
            RelAccess access = (RelAccess) varAcces;
            ImcExpr addr = new ImcTEMP(visArg.peek().FP);
            for (int i = access.depth; i < visArg.peek().depth; i++){
                addr = new ImcMEM(addr);
            }
            addr = new ImcBINOP(ImcBINOP.Oper.ADD, addr, new ImcCONST(access.offset));
            ImcGen.exprImCode.put(varName, new ImcMEM(addr));
            return null;
        }
    }


    @Override
    public ImcExpr visit(AbsArrExpr arrExpr, Stack<Frame> visArg) {
        ImcExpr array = ((ImcMEM) (ImcGen.exprImCode.get(arrExpr.array))).addr;
        ImcExpr index = ImcGen.exprImCode.get(arrExpr.index);
        ImcCONST arrSize = new ImcCONST(SemAn.isOfType.get(arrExpr).size());
        ImcBINOP indexValue = new ImcBINOP(ImcBINOP.Oper.MUL, index, arrSize);
        ImcMEM arrayAdr = new ImcMEM(new ImcBINOP(ImcBINOP.Oper.ADD, array, indexValue));
        ImcGen.exprImCode.put(arrExpr, arrayAdr);
        return null;
    }

    @Override
    public ImcExpr visit(AbsNewExpr newExpr, Stack<Frame> visArg) {
        Vector<ImcExpr> args = new Vector<ImcExpr>();
        args.add(new ImcCONST(0));
        ImcCONST typeSize = new ImcCONST(SemAn.isOfType.get(newExpr).size());
        args.add(typeSize);
        ImcGen.exprImCode.put(newExpr, new ImcCALL(new Label("new"), args));
        return null;
    }

    @Override
    public ImcExpr visit(AbsDelExpr delExpr, Stack<Frame> visArg) {
        Vector<ImcExpr> args = new Vector<ImcExpr>();
        args.add(new ImcCONST(0));
        ImcExpr expr = ImcGen.exprImCode.get(delExpr.expr);
        args.add(expr);
        ImcGen.exprImCode.put(delExpr, new ImcCALL(new Label("del"), args));
        return null;
    }


    @Override
    public ImcExpr visit(AbsCastExpr castExpr, Stack<Frame> visArg) {
        castExpr.expr.accept(this, visArg);
        ImcExpr expr = ImcGen.exprImCode.get(castExpr.expr);
        ImcGen.exprImCode.put(castExpr, expr);
        return null;
    }

}
