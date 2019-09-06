/*******************************************************************************************************
 *
 * gama.kernel.batch.StoppingCriterion.java, in plugin gama.core,
 * is part of the source code of the GAMA modeling and simulation platform (v. 1.8)
 * 
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/
package gama.common.interfaces.batch;

import java.util.Map;

public interface StoppingCriterion {

	public boolean stopSearchProcess(Map<String, Object> parameters);
}