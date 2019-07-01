/**
 * @author sliva
 */
package compiler.phases.seman;

import compiler.common.report.*;
import compiler.data.abstree.*;
import compiler.data.abstree.visitor.*;
import compiler.data.type.SemType;

/**
 * Determines which value expression can denote an address.
 *
 * @author sliva
 */
public class AddrResolver extends AbsFullVisitor<Boolean, Object> {

    public Boolean visit(AbsVarName varName, Object visArg) {
        SemAn.isAddr.put(varName, Boolean.TRUE);
        return null;
    }

    @Override
    public Boolean visit(AbsFunName funName, Object visArg) {
        funName.args.accept(this, visArg);
        SemAn.isAddr.put(funName, Boolean.TRUE);
        return null;
    }

    @Override
    public Boolean visit(AbsAssignStmt assignStmt, Object visArg) {
        assignStmt.src.accept(this, visArg);
        SemAn.isAddr.put(assignStmt.dst, Boolean.TRUE);
        return null;
    }

    @Override
    public Boolean visit(AbsArrExpr arrExpr, Object visArg) {
        arrExpr.array.accept(this, visArg);
        SemAn.isAddr.put(arrExpr, Boolean.TRUE);
        return null;
    }


    @Override
    public Boolean visit(AbsUnExpr unExpr, Object visArg) {
        unExpr.subExpr.accept(this, visArg);
        if (unExpr.oper == AbsUnExpr.Oper.DATA || unExpr.oper == AbsUnExpr.Oper.ADDR) {
            SemAn.isAddr.put(unExpr, Boolean.TRUE);
        } else {
            SemAn.isAddr.put(unExpr, Boolean.FALSE);
        }
        return null;
    }
}
