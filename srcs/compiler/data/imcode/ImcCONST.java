/**
 * @author sliva
 */
package compiler.data.imcode;

import compiler.data.imcode.visitor.*;

public class ImcCONST extends ImcExpr {

	public final long value;

	public ImcCONST(long value) {
		this.value = value;
	}

	@Override
	public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
		return visitor.visit(this, accArg);
	}

}
