/**
 * @author sliva
 */
package compiler.data.imcode;

import compiler.data.imcode.visitor.*;

public class ImcUNOP extends ImcExpr {

    public enum Oper {
        NOT, NEG,
//        NOT, ADD, SUB, ADDR, DATA,
    }

    public final Oper oper;

    public final ImcExpr subExpr;

    public ImcUNOP(Oper oper, ImcExpr subExpr) {
        this.oper = oper;
        this.subExpr = subExpr;
    }

    @Override
    public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
        return visitor.visit(this, accArg);
    }

}
