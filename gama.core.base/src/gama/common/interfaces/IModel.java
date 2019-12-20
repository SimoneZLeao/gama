/*******************************************************************************************************
 *
 * gama.kernel.model.IModel.java, in plugin gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package gama.common.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import gama.common.interfaces.experiment.IExperimentPlan;
import gaml.descriptions.ModelDescription;
import gaml.descriptions.SpeciesDescription;
import gaml.species.ISpecies;
import gaml.statements.test.TestStatement;

/**
 * Written by drogoul Modified on 29 d�c. 2010
 *
 * @todo Description
 *
 */
public interface IModel extends ISpecies {

	ISpecies getSpecies(String speciesName);

	ISpecies getSpecies(String speciesName, SpeciesDescription specDes);

	IExperimentPlan getExperiment(final String s);

	String getWorkingPath();

	String getFilePath();

	String getProjectPath();

	Map<String, ISpecies> getAllSpecies();

	Collection<String> getImportedPaths();

	List<TestStatement> getAllTests();

	@Override
	default URI getURI() {
		final ModelDescription md = (ModelDescription) getDescription();
		if (md == null) { return null; }
		final EObject o = md.getUnderlyingElement();
		if (o == null) { return null; }
		final Resource r = o.eResource();
		if (r == null) { return null; }
		return r.getURI();
	}

}