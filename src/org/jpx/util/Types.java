package org.jpx.util;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * TODO: Document this
 */
public final class Types {

    private Types() {
    }

    public static <T> T safeCast(Object o, Class<T> clazz) {
        if (o == null) {
            return null;
        }
        try {
            return clazz.isInstance(o) ? clazz.cast(o) : null;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("Invalid value type, expected %s, got %s",
                    clazz.getName(), o.getClass().getName()));
        }
    }

    public static <K, V> Map<K, V> castToMap(Object o, Class<K> kclazz, Class<V> vclass) {
        return (Map<K, V>) o;
    }


    public static void checkRequired(Consumer<List<Pair<String, ?>>> onMissing, Pair<String, ?>... pairs) {
        Objects.requireNonNull(onMissing, "onMissing");
        Objects.requireNonNull(pairs, "pairs");
        List<Pair<String, ?>> missing = Arrays.asList(pairs).stream()
                .filter(p -> p.getValue() == null)
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            onMissing.accept(missing);
        }
    }

    public static class Pair<K, V> extends AbstractMap.SimpleImmutableEntry<K, V> {

        public Pair(K key, V value) {
            super(key, value);
        }

        public Pair(Map.Entry<? extends K, ? extends V> entry) {
            super(entry);
        }
    }

    public static <K, V> Pair<K, V> pair(K key, V value) {
        return new Pair<K, V>(key, value);
    }
}
