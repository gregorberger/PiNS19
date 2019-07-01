package compiler.phases.imcgen;

import compiler.data.abstree.*;
import compiler.data.abstree.visitor.AbsFullVisitor;
import compiler.data.chunk.CodeChunk;
import compiler.data.chunk.DataChunk;
import compiler.data.imcode.*;
import compiler.data.layout.Frame;
import compiler.data.layout.Label;
import compiler.data.layout.Temp;
import compiler.phases.chunks.Chunks;
import compiler.phases.frames.Frames;

import java.util.Stack;
import java.util.Vector;

/**
 * Intermediate code generator.
 *
 * @author sliva
 */
public class CodeGenerator extends AbsFullVisitor<Object, Stack<Frame>> {

    private ExprGenerator exprGenerator = new ExprGenerator();
    private StmtGenerator stmtGenerator = new StmtGenerator();


    @Override
    public Object visit(AbsSource source, Stack<Frame> visArg) {
        source.decls.accept(this, visArg);
        return null;
    }



    @Override
    public Object visit(AbsAtomExpr atomExpr, Stack<Frame> visArg) {
        exprGenerator.visit(atomExpr, visArg);
        return null;
    }

    @Override
    public Object visit(AbsBinExpr binExpr, Stack<Frame> visArg) {
        binExpr.fstExpr.accept(this, visArg);
        binExpr.sndExpr.accept(this, visArg);

        return exprGenerator.visit(binExpr, visArg);
    }


    @Override
    public Object visit(AbsVarName name, Stack<Frame> visArg) {
        return exprGenerator.visit(name, visArg);
    }

    @Override
    public Object visit(AbsUnExpr unExpr, Stack<Frame> visArg) {
        unExpr.subExpr.accept(this, visArg);
        return exprGenerator.visit(unExpr, visArg);
    }

    @Override
    public Object visit(AbsFunName funName, Stack<Frame> visArg) {
        funName.args.accept(this, visArg);
        return exprGenerator.visit(funName, visArg);
    }

    @Override
    public Object visit(AbsExprStmt exprStmt, Stack<Frame> visArg) {
        exprStmt.expr.accept(this, visArg);
        return stmtGenerator.visit(exprStmt, visArg);
    }


    @Override
    public Object visit(AbsAssignStmt assignStmt, Stack<Frame> visArg) {
        assignStmt.src.accept(this, visArg);
        assignStmt.dst.accept(this, visArg);
        return stmtGenerator.visit(assignStmt, visArg);
    }

    @Override
    public Object visit(AbsIfStmt ifStmt, Stack<Frame> visArg) {
        ifStmt.cond.accept(this, visArg);
        ifStmt.elseStmts.accept(this, visArg);
        ifStmt.thenStmts.accept(this, visArg);
        return stmtGenerator.visit(ifStmt, visArg);
    }

    @Override
    public Object visit(AbsWhileStmt whileStmt, Stack<Frame> visArg) {
        whileStmt.cond.accept(this, visArg);
        whileStmt.stmts.accept(this, visArg);
        return stmtGenerator.visit(whileStmt, visArg);
    }


    @Override
    public Object visit(AbsFunDef funDef, Stack<Frame> visArg) {
        visArg.push(Frames.frames.get(funDef));
        funDef.value.accept(this, visArg);
        super.visit(funDef, visArg);
        //ImcMOVE move = new ImcMOVE(new ImcTEMP(visArg.peek().RV), (ImcExpr) ImcGen.exprImCode.get(funDef.value));
        //ImcGen.stmtImCode.put(funDef, move);
        visArg.pop();
        return null;
    }

    @Override
    public Object visit(AbsArrExpr arrExpr, Stack<Frame> visArg) {
        arrExpr.array.accept(this, visArg);
        arrExpr.index.accept(this, visArg);
        return exprGenerator.visit(arrExpr, visArg);
    }

    @Override
    public Object visit(AbsNewExpr newExpr, Stack<Frame> visArg) {
        newExpr.type.accept(this, visArg);
        return exprGenerator.visit(newExpr, visArg);
    }

    @Override
    public Object visit(AbsDelExpr delExpr, Stack<Frame> visArg) {
        delExpr.expr.accept(this, visArg);
        return exprGenerator.visit(delExpr, visArg);
    }

    @Override
    public Object visit(AbsBlockExpr blockExpr, Stack<Frame> visArg){
        blockExpr.decls.accept(this, visArg);
        ImcStmt stmts = (ImcStmt) blockExpr.stmts.accept(this, visArg);
        blockExpr.expr.accept(this, visArg);
        ImcExpr expr = ImcGen.exprImCode.get(blockExpr.expr);
        ImcGen.exprImCode.put(blockExpr, new ImcSEXPR(stmts, expr));
        return null;
    }


    @Override
    public Object visit(AbsStmts stmts, Stack<Frame> visArg) {
        Vector<ImcStmt> allStmts = new Vector<>();
        for(AbsStmt stmt : stmts.stmts()){
            stmt.accept(this, visArg);
            allStmts.add(ImcGen.stmtImCode.get(stmt));
        }
        return new ImcSTMTS(allStmts);
    }

    @Override
    public Object visit(AbsCastExpr cast, Stack<Frame> visArg) {
        cast.expr.accept(this, visArg);
        cast.type.accept(this, visArg);
        return exprGenerator.visit(cast, visArg);
    }




    //    @Override
//    public Object visit(AbsArrType arrType, Stack<Frame> visArg) {
//        arrType.elemType.accept(this, visArg);
//        return null;
//    }

}
