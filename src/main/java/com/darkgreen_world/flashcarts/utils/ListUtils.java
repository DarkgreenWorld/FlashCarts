package com.darkgreen_world.flashcarts.utils;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;

public abstract class ListUtils {

    public static <T> void biIterate(List<T> iterable, BiConsumer<T, @Nullable T> consumer) {
        var iterator = iterable.iterator();
        T current = iterator.hasNext() ? iterator.next() : null;
        T next;
        for (int j = 0; j < iterable.size(); j++) {
            next = iterator.hasNext() ? iterator.next() : null;
            if (current == null) break;
            consumer.accept(current, next);
            current = next;
        }
    }
}
