/*******************************************************************************************************
 *
 * gaml.compilation.ast.SyntacticTopLevelElement.java, in plugin gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.core.lang.gaml.ast;

import org.eclipse.emf.ecore.EObject;

import gaml.compilation.interfaces.ISyntacticElement;
import gaml.compilation.interfaces.ISyntacticElement.SyntacticVisitor;
import gaml.statements.Facets;

/**
 * The Class SyntacticTopLevelElement.
 */
public class SyntacticTopLevelElement extends SyntacticSpeciesElement {

	/**
	 * Instantiates a new syntactic top level element.
	 *
	 * @param keyword the keyword
	 * @param facets the facets
	 * @param statement the statement
	 */
	SyntacticTopLevelElement(final String keyword, final Facets facets, final EObject statement) {
		super(keyword, facets, statement);
	}

	/* (non-Javadoc)
	 * @see gaml.compilation.ast.AbstractSyntacticElement#visitGrids(gaml.compilation.ast.ISyntacticElement.SyntacticVisitor)
	 */
	@Override
	public void visitGrids(final SyntacticVisitor visitor) {
		visitAllChildren(visitor, GRID_FILTER);
	}

}