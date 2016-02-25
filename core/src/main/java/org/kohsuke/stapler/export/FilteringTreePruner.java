package org.kohsuke.stapler.export;

/**
 * Decorates a base {@link TreePruner} by also refusing all the properties
 * that are present in the given {@link Model}.
 *
 * @author Kohsuke Kawaguchi
 */
class FilteringTreePruner extends TreePruner {
    private final Model model;
    private final TreePruner base;

    FilteringTreePruner(Model model, TreePruner base) {
        this.model = model;
        this.base = base;
    }

    @Override
    public TreePruner accept(Object node, Property prop) {
        if (model.hasPropertyNamed(prop.name))
            return null;
        return base.accept(node,prop);
    }
}
