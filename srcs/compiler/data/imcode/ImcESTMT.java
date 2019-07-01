/**
 * @author sliva
 */
package compiler.data.imcode;

import compiler.data.imcode.visitor.*;

public class ImcESTMT extends ImcStmt {

	public final ImcExpr expr;

	public ImcESTMT(ImcExpr expr) {
		this.expr = expr;
	}

	@Override
	public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
		return visitor.visit(this, accArg);
	}

}
