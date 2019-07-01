/**
 * @author sliva
 */
package compiler.data.imcode;

import compiler.data.imcode.visitor.*;

public class ImcMOVE extends ImcStmt {

	public final ImcExpr dst;

	public final ImcExpr src;

	public ImcMOVE(ImcExpr dst, ImcExpr src) {
		this.dst = dst;
		this.src = src;
	}

	@Override
	public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
		return visitor.visit(this, accArg);
	}

}
