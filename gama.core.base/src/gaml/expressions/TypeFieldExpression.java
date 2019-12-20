/*******************************************************************************************************
 *
 * gaml.expressions.TypeFieldExpression.java, in plugin gama.core, is part of the source code of the GAMA
 * modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.expressions;

import gama.common.util.TextBuilder;
import gama.processor.annotations.GamlAnnotations.variable;
import gama.processor.annotations.GamlAnnotations.vars;
import gama.runtime.scope.IScope;
import gaml.descriptions.IDescription;
import gaml.prototypes.OperatorProto;

public class TypeFieldExpression extends UnaryOperator {

	public TypeFieldExpression(final OperatorProto proto, final IDescription context, final IExpression expr) {
		super(proto, context, expr);
	}

	@Override
	public TypeFieldExpression resolveAgainst(final IScope scope) {
		return new TypeFieldExpression(prototype, null, child.resolveAgainst(scope));
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		try (TextBuilder sb = TextBuilder.create()) {
			parenthesize(sb, child);
			sb.append(".").append(getName());
			return sb.toString();
		}
	}

	@Override
	public String toString() {
		if (child == null) { return prototype.signature.toString() + "." + getName(); }
		return child.serialize(false) + "." + getName();
	}

	@Override
	public String getDocumentation() {
		try (TextBuilder sb = TextBuilder.create()) {
			if (child != null) {
				sb.append("Defined on objects of type " + child.getGamlType().getTitle());
			}
			final vars annot = prototype.getSupport().getAnnotation(vars.class);
			if (annot != null) {
				final variable[] allVars = annot.value();
				for (final variable v : allVars) {
					if (v.name().equals(getName())) {
						if (v.doc().length > 0) {
							sb.append("<br/>");
							sb.append(v.doc()[0].value());
						}
					}
				}
			}
			return sb.toString();
		}
	}

	@Override
	public String getTitle() {
		return "field <b>" + getName() + "</b> of type " + getGamlType().getTitle();
	}

	// @Override
	// public void collectMetaInformation(final GamlProperties meta) {
	// meta.put(GamlProperties.ATTRIBUTES, getName());
	// }

}