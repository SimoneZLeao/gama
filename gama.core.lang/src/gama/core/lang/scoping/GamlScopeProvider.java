/*********************************************************************************************
 *
 * 'GamlScopeProvider.java, in plugin gama.core.lang, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 *
 *
 **********************************************************************************************/
package gama.core.lang.scoping;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.ISelectable;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import gama.common.util.Collector;

/**
 * This class contains custom scoping description.
 *
 * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#scoping on how and when to use it
 *
 */
// @Singleton
public class GamlScopeProvider extends org.eclipse.xtext.scoping.impl.SimpleLocalScopeProvider {

	private class MultimapBasedSelectable implements ISelectable {
		List<IEObjectDescription> descriptions;
		private final Multimap<QualifiedName, IEObjectDescription> nameToObjects;

		public MultimapBasedSelectable(final List<IEObjectDescription> descriptions) {
			this.descriptions = descriptions == null || descriptions.isEmpty() ? null : descriptions;
			this.nameToObjects = this.descriptions == null ? null : LinkedHashMultimap.create();
			if (this.descriptions != null) {
				for (final IEObjectDescription description : this.descriptions) {
					nameToObjects.put(description.getName(), description);
				}
			}

		}

		@Override
		public boolean isEmpty() {
			return descriptions == null;
		}

		@Override
		public Iterable<IEObjectDescription> getExportedObjectsByType(final EClass type) {
			if (descriptions == null)
				return Collections.emptyList();
			return Iterables.filter(descriptions, input -> EcoreUtil2.isAssignableFrom(type, input.getEClass()));
		}

		@Override
		public Iterable<IEObjectDescription> getExportedObjectsByObject(final EObject object) {
			if (descriptions == null)
				return Collections.emptyList();
			final URI uri = EcoreUtil2.getPlatformResourceOrNormalizedURI(object);
			return Iterables.filter(descriptions, input -> {
				if (input.getEObjectOrProxy() == object)
					return true;
				if (uri.equals(input.getEObjectURI()))
					return true;
				return false;
			});
		}

		@Override
		public Iterable<IEObjectDescription> getExportedObjects(final EClass type, final QualifiedName name,
				final boolean ignoreCase) {
			if (nameToObjects == null)
				return Collections.emptyList();
			if (nameToObjects.containsKey(name)) {
				for (final IEObjectDescription desc : nameToObjects.get(name)) {
					if (EcoreUtil2.isAssignableFrom(type, desc.getEClass()))
						return Collections.singleton(desc);
				}
			}
			return Collections.emptyList();
		}

		@Override
		public Iterable<IEObjectDescription> getExportedObjects() {
			return descriptions == null ? Collections.EMPTY_LIST : descriptions;
		}

	}

	@Override
	protected ISelectable getAllDescriptions(final Resource resource) {
		try (final Collector.AsList<IEObjectDescription> descriptions = Collector.newList()) {
			final Iterator<EObject> iterator = resource.getAllContents();
			while (iterator.hasNext()) {
				final EObject from = iterator.next();
				final QualifiedName qualifiedName = getNameProvider().apply(from);
				if (qualifiedName != null) {
					descriptions.add(new EObjectDescription(qualifiedName, from, null));
				}
			}
			return new MultimapBasedSelectable(descriptions.items());
		}

	}
}