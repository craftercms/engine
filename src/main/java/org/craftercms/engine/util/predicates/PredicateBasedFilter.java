package org.craftercms.engine.util.predicates;

import java.util.List;

import org.apache.commons.collections4.Predicate;
import org.craftercms.core.service.Item;
import org.craftercms.core.service.ItemFilter;

/**
 * An implementation of Crafter's {@link org.craftercms.core.service.ItemFilter} that uses a predicate.
 *
 * @author avasquez
 */
public class PredicateBasedFilter implements ItemFilter {

    protected Predicate<Item> predicate;

    public PredicateBasedFilter(Predicate<Item> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean runBeforeProcessing() {
        return false;
    }

    @Override
    public boolean runAfterProcessing() {
        return true;
    }

    @Override
    public boolean accepts(Item item, List<Item> acceptedItems, List<Item> rejectedItems, boolean runningBeforeProcessing) {
        return predicate.evaluate(item);
    }

}
