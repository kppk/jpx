package kppk.jpx.cli;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class for arguments iteration.
 */
final class Arguments {
    private final List<String> arguments;
    private int currentIndex;

    Arguments(String... arguments) {
        this.arguments = Stream.of(arguments)
                .filter(s -> s != null && s.trim().length() > 0)
                .collect(Collectors.toList());
    }

    boolean hasNext() {
        return currentIndex < arguments.size();
    }

    String next() {
        return arguments.get(currentIndex++);
    }

    String peek() {
        return arguments.get(currentIndex);
    }

    boolean isEmpty() {
        return arguments.size() == 0;
    }
}
