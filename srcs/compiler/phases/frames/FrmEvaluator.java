/**
 * @author sliva
 */
package compiler.phases.frames;

import compiler.data.abstree.*;
import compiler.data.abstree.visitor.*;
import compiler.data.type.*;
import compiler.data.layout.*;
import compiler.phases.seman.*;

/**
 * Computing function frames and accesses.
 *
 * @author sliva
 */
public class FrmEvaluator extends AbsFullVisitor<Object, FrmEvaluator.Context> {

    /**
     * The context {@link FrmEvaluator} uses while computing function frames and
     * variable accesses.
     *
     * @author sliva
     */
    protected abstract class Context {
    }

    /**
     * Functional context, i.e., used when traversing function and building a new
     * frame, parameter acceses and variable acceses.
     *
     * @author sliva
     */
    private class FunContext extends Context {
        public int depth = 0;
        public long locsSize = 0;
        public long argsSize = 0;
        public long parsSize = new SemPtrType(new SemVoidType()).size();
    }

    @Override
    public Object visit(AbsSource source, Context visArg) {
        FunContext context = new FunContext();
        context.depth = 0;
        return super.visit(source, context);
    }

    @Override
    public Object visit(AbsVarDecl varDecl, Context visArg) {
        FunContext context = (FunContext) visArg;
        SemType type = SemAn.isType.get(varDecl.type);
        if(context.depth != 0){
            context.locsSize += type.size();
            Frames.accesses.put(varDecl, new RelAccess(type.size(), -context.locsSize, context.depth+1));
            return  super.visit(varDecl, new FunContext());
        }
        Frames.accesses.put(varDecl, new AbsAccess(type.size(), new Label(varDecl.name)));
        return null;
    }

    @Override
    public Object visit(AbsFunDef decl, FrmEvaluator.Context visArg){
        FrmEvaluator.FunContext context = new FrmEvaluator.FunContext();
        context.depth = ((FunContext) visArg).depth + 1;
        context.argsSize += new SemPtrType(new SemVoidType()).size();
        super.visit(decl, context);
        Label label;
        label = context.depth == 1 ? new Label(decl.name) : new Label();
        Frames.frames.put(decl, new Frame(label, context.depth, context.locsSize, context.argsSize));
        return null;
    }

    @Override
    public Object visit(AbsFunDecl decl, FrmEvaluator.Context visArg){
        FunContext context = (FunContext) visArg;
        Label label;
        label = context.depth == 0 ? new Label(decl.name) : new Label();
        context.argsSize += new SemPtrType(new SemVoidType()).size();
        Frames.frames.put(decl, new Frame(label, ((FunContext) visArg).depth + 1, 0, 0));
        return null;
    }

    @Override
    public Object visit(AbsParDecl parDecl, FrmEvaluator.Context visArg){
        SemType type = SemAn.isType.get(parDecl.type);
        FunContext context = ((FunContext) visArg);
        Frames.accesses.put(parDecl, new RelAccess(type.size(), context.parsSize, context.depth+1));
        context.parsSize += type.size();
        return null;
    }

    @Override
    public Object visit(AbsFunName funName, FrmEvaluator.Context visArg){
        FunContext context = (FunContext) visArg;
        long size = (Long) funName.args.accept(this, visArg);
        if (size > context.argsSize){
            context.argsSize = size;
        }
        return null;
    }

    @Override
    public Object visit(AbsArgs args, Context visArgs){
        long size = new SemPtrType(new SemVoidType()).size();
        for (AbsExpr expr : args.args()){
            expr.accept(this, visArgs);
            size += SemAn.isOfType.get(expr).size();
        }
        return size;
    }

}
