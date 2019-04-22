package kppk.jpx.module;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is here just to minimize changes in {@link ModuleDescriptor} which was copied from jdk 9.
 */
final class Sets {


    static <E> Set<E> of(E e1) {
        HashSet<E> set = new HashSet<>(1);
        set.add(e1);
        return Collections.unmodifiableSet(set);
    }

    /**
     * This is here just to fulfill ModuleDescriptor requirements and to keep changes minimal.
     */
    static <E> Set<E> copyOf(Collection<? extends E> coll) {
        return new HashSet<>(coll);
    }

}
