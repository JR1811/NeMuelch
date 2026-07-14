package net.shirojr.nemuelch.util.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class CollectionsHelper {
    private CollectionsHelper() {
    }

    public static <T> void modifyIfPresent(Collection<T> entries, Predicate<T> criteria, UnaryOperator<T> modification) {
        if (entries instanceof List<?> rawList) {
            @SuppressWarnings("unchecked")
            List<T> orderedEntries = (List<T>) rawList;
            for (int i = 0; i < rawList.size(); i++) {
                T entry = orderedEntries.get(i);
                if (!criteria.test(entry)) continue;
                T newEntry = modification.apply(entry);
                orderedEntries.set(i, newEntry);
            }
        } else {
            Iterator<T> iterator = entries.iterator();
            List<T> replacements = new ArrayList<>();
            while (iterator.hasNext()) {
                T entry = iterator.next();
                if (!criteria.test(entry)) continue;
                T newEntry = modification.apply(entry);
                iterator.remove();
                replacements.add(newEntry);
            }
            entries.addAll(replacements);
        }
    }
}
