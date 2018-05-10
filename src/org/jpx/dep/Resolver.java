package org.jpx.dep;

import org.jpx.model.Dep;
import org.jpx.model.Manifest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TODO: Document this
 */
public interface Resolver {

    List<Resolver> RESOLVERS = Collections.unmodifiableList(Arrays.asList(
            PathResolver.INSTANCE
    ));

    Manifest resolve(Manifest parent, Dep dep);

    boolean canResolve(Dep dep);

    static Resolver thatResolves(Dep dep) {
        return RESOLVERS.stream()
                .filter(resolver -> resolver.canResolve(dep))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Don't know which resolver to use"));
    }
}
