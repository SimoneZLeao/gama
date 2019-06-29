/*******************************************************************************************************
 *
 * msi.gama.common.util.JavaUtils.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and
 * simulation platform (v. 1.8)
 *
 * (c) 2007-2018 UMI 209 UMMISCO IRD/SU & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 *
 ********************************************************************************************************/
package msi.gama.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TLinkedHashSet;
import msi.gama.common.interfaces.ISkill;

/**
 * Written by drogoul Modified on 28 d�c. 2010
 *
 * Provides some utilities for dealing with reflection.
 *
 */
@SuppressWarnings ({ "unchecked", "rawtypes" })
public class JavaUtils {
	public final static PoolUtils.ObjectPool<Set> SET_POOL =
			PoolUtils.create("TLinkedHashSets", true, () -> new TLinkedHashSet(), c -> c.clear());
	public final static PoolUtils.ObjectPool<List> LIST_POOL =
			PoolUtils.create("ArrayLists", true, () -> new ArrayList(200), c -> c.clear());

	public final static TIntObjectHashMap<List<Class>> IMPLEMENTATION_CLASSES = new TIntObjectHashMap();
	private static Multimap<Class, Class> INTERFACES = HashMultimap.<Class, Class> create();
	private static Multimap<Class, Class> SUPERCLASSES = HashMultimap.<Class, Class> create();

	private static int keyOf(final Class base, final Iterable<Class<? extends ISkill>> others) {
		int result = base.hashCode();
		for (final Class other : others) {
			result += other.hashCode();
		}
		return result;
	}

	private static final Set<Class> allInterfacesOf(final Class c, final Set<Class> in) {
		if (c == null) { return Collections.EMPTY_SET; }
		if (!INTERFACES.containsKey(c)) {
			final Class[] interfaces = c.getInterfaces();
			for (final Class c1 : interfaces) {
				if (in.contains(c1)) {
					INTERFACES.put(c, c1);
					INTERFACES.putAll(c, allInterfacesOf(c1, in));
				}
			}
			INTERFACES.putAll(c, allInterfacesOf(c.getSuperclass(), in));
		}
		return (Set<Class>) INTERFACES.get(c);
	}

	private static final Set<Class> allSuperclassesOf(final Class c, final Set<Class> in) {
		if (c == null) { return null; }
		if (!SUPERCLASSES.containsKey(c)) {
			Class c2 = c.getSuperclass();
			while (c2 != null) {
				if (in.contains(c2)) {
					SUPERCLASSES.put(c, c2);
				}
				c2 = c2.getSuperclass();
			}
		}
		return (Set<Class>) SUPERCLASSES.get(c);
	}

	public static List<Class> collectImplementationClasses(final Class baseClass,
			final Iterable<Class<? extends ISkill>> skillClasses, final Set<Class> in) {
		final int key = keyOf(baseClass, skillClasses);
		if (!IMPLEMENTATION_CLASSES.containsKey(key)) {
			final Iterable<Class> basis =
					Iterables.concat(Collections.singleton(baseClass), skillClasses, allInterfacesOf(baseClass, in));
			final Iterable<Class> extensions =
					Iterables.concat(Iterables.transform(basis, each -> allSuperclassesOf(each, in)));
			final Set<Class> classes = Sets.newHashSet(Iterables.concat(basis, extensions));
			final ArrayList<Class> classes2 = new ArrayList(classes);
			Collections.sort(classes2, (o1, o2) -> {
				if (o1 == o2) { return 0; }
				if (o1.isAssignableFrom(o2)) { return -1; }
				if (o2.isAssignableFrom(o1)) { return 1; }
				if (o1.isInterface() && !o2.isInterface()) { return -1; }
				if (o2.isInterface() && !o1.isInterface()) { return 1; }
				return 1;
			});

			IMPLEMENTATION_CLASSES.put(key, classes2);
		}
		return IMPLEMENTATION_CLASSES.get(key);

	}

	public static <F> Iterator<F> iterator(final Object[] array) {
		if (array != null) { return (Iterator<F>) Iterators.forArray(array); }
		return new UnmodifiableIterator<F>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public F next() {
				return null;
			}
		};
	}

}
