/*******************************************************************************************************
 *
 * gaml.descriptions.LabelExpressionDescription.java, in plugin gama.core, is part of the source code of the
 * GAMA modeling and simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gaml.descriptions;

import java.util.Collection;
import java.util.Set;

import gama.common.util.Collector;
import gama.common.util.StringUtils;
import gama.common.util.TextBuilder;
import gama.runtime.exceptions.GamaRuntimeException;
import gama.runtime.scope.IScope;
import gaml.expressions.IExpression;
import gaml.types.IType;
import gaml.types.Types;

/**
 * The class LabelExpressionDescription.
 *
 * @author drogoul
 * @since 31 mars 2012
 *
 */
public class LabelExpressionDescription extends BasicExpressionDescription implements IExpression {

	@Override
	public IType<?> getDenotedType(final IDescription context) {
		return context.getTypeNamed(value);
	}

	final String value;

	private LabelExpressionDescription(final String label) {
		super((IExpression) null);
		value = StringUtils.unescapeJava(label);
	}

	@Override
	public IExpressionDescription cleanCopy() {
		// TODO Check that it does not cause any problem
		return this;
	}

	@Override
	public IExpressionDescription compileAsLabel() {
		return this;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return value;
		// return StringUtils.toGamlString(value);
	}

	@Override
	public IExpression getExpression() {
		if (expression == null) {
			expression = this;
		}
		return expression;
	}

	@Override
	public IExpression compile(final IDescription context) {
		return getExpression();
	}

	@Override
	public boolean equalsString(final String o) {
		return value.equals(o);
	}

	// @Override
	// public void setTarget(final EObject newTarget) {
	// super.setTarget(newTarget);
	// }

	@Override
	public Set<String> getStrings(final IDescription context, final boolean skills) {
		// Assuming of the form [aaa, bbb]
		try (Collector.AsSet<String> result = Collector.newSet()) {
			try (TextBuilder sb = TextBuilder.create()) {
				for (final char c : value.toCharArray()) {
					switch (c) {
						case '[':
						case ' ':
							break;
						case ']':
						case ',': {
							result.add(sb.toString());
							sb.setLength(0);
							break;
						}
						default:
							sb.append(c);
					}
				}
				return result.items();
			}
		}
	}

	public static IExpressionDescription create(final String s) {
		return new LabelExpressionDescription(s);
	}

	@Override
	public String getDocumentation() {
		return "Constant string: " + getName();
	}

	@Override
	public String getTitle() {
		return "constant string '" + getName() + "'";
	}

	@Override
	public String getDefiningPlugin() {
		return null;
	}

	@Override
	public String getName() {
		return value;
	}

	@Override
	public void setName(final String newName) {}

	@Override
	public IType<String> getGamlType() {
		return Types.STRING;
	}

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		return value;
	}

	@Override
	public boolean isConst() {
		return true;
	}

	@Override
	public String literalValue() {
		return value;
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	// @Override
	// public void collectMetaInformation(final GamlProperties meta) {}

	@Override
	public void collectUsedVarsOf(final SpeciesDescription species, final Collection<VariableDescription> result) {}

}