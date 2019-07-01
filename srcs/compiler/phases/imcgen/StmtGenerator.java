package compiler.phases.imcgen;

import compiler.common.report.Report;
import compiler.data.abstree.*;
import compiler.data.abstree.visitor.AbsVisitor;
import compiler.data.imcode.*;
import compiler.data.layout.Frame;
import compiler.data.layout.Label;
import compiler.phases.seman.SemAn;

import java.util.Stack;
import java.util.Vector;


/**
 * @author sliva
 */
public class StmtGenerator implements AbsVisitor<ImcStmt, Stack<Frame>> {

    @Override
    public ImcStmt visit(AbsExprStmt exprStmt, Stack<Frame> visArg) {
        ImcGen.stmtImCode.put(exprStmt, new ImcESTMT(ImcGen.exprImCode.get(exprStmt.expr)));
        return null;
    }


    @Override
    public ImcStmt visit(AbsAssignStmt assignStmt, Stack<Frame> visArg) {
        if (!(SemAn.isAddr.get(assignStmt.dst)))
            throw new Report.Error(assignStmt, "Assign destination must be an address.");
        ImcExpr dest = ImcGen.exprImCode.get(assignStmt.dst);
        ImcExpr src = ImcGen.exprImCode.get(assignStmt.src);
        ImcGen.stmtImCode.put(assignStmt, new ImcMOVE(dest, src));
        return null;
    }

    @Override
    public ImcStmt visit(AbsIfStmt ifStmt, Stack<Frame> visArg) {
        ImcExpr exprCond = ImcGen.exprImCode.get(ifStmt.cond);
        Label posLabel = new Label();
        Label negLabel = new Label();
        Vector<ImcStmt> stmts = new Vector<>(ifStmt.elseStmts.numStmts() + ifStmt.thenStmts.numStmts());

        stmts.add(new ImcCJUMP(exprCond, posLabel, negLabel));
        stmts.add(new ImcLABEL(posLabel));

        for (AbsStmt stmt : ifStmt.thenStmts.stmts()) {
            ImcStmt imcStmt = ImcGen.stmtImCode.get(stmt);
            stmts.add(imcStmt);
        }

        if (ifStmt.elseStmts.numStmts() != 0) {
            Label label = new Label();
            stmts.add(new ImcJUMP(label));
            stmts.add(new ImcLABEL(negLabel));

            for (AbsStmt stmt : ifStmt.elseStmts.stmts()) {
                ImcStmt imcStmt = ImcGen.stmtImCode.get(stmt);
                stmts.add(imcStmt);
            }
            stmts.add(new ImcLABEL(label));
        } else {
            stmts.add(new ImcLABEL(negLabel));
            for (AbsStmt stmt : ifStmt.elseStmts.stmts()) {
                ImcStmt imcStmt = ImcGen.stmtImCode.get(stmt);
                stmts.add(imcStmt);
            }
        }

        ImcGen.stmtImCode.put(ifStmt, new ImcSTMTS(stmts));
        return null;
    }

    @Override
    public ImcStmt visit(AbsWhileStmt whileStmt, Stack<Frame> visArg) {
        Vector<ImcStmt> stmts = new Vector<>(whileStmt.stmts.numStmts());
        Label label = new Label();
        stmts.add(new ImcLABEL(label));

        Label posLabel = new Label();
        Label negLabel = new Label();

        ImcExpr cond = ImcGen.exprImCode.get(whileStmt.cond);
        stmts.add(new ImcCJUMP(cond, posLabel, negLabel));
        stmts.add(new ImcLABEL(posLabel));

        for (AbsStmt stms : whileStmt.stmts.stmts()) {
            ImcStmt imcStmt = ImcGen.stmtImCode.get(stms);
            stmts.add(imcStmt);
        }

        stmts.add(new ImcJUMP(label));
        stmts.add(new ImcLABEL(negLabel));

        ImcGen.stmtImCode.put(whileStmt, new ImcSTMTS(stmts));
        return null;
    }
}
