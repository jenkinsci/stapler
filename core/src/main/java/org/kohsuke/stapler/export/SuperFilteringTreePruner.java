package org.kohsuke.stapler.export;

/**
 * @author Kohsuke Kawaguchi
 */
class SuperFilteringTreePruner extends TreePruner {
    private final Model model;
    private final TreePruner base;

    SuperFilteringTreePruner(Model model, TreePruner base) {
        this.model = model;
        this.base = base;
    }

    @Override
    public TreePruner accept(Object node, Property prop) {
        if (model.hasPropertyNamedInAncestor(prop.name))
            return null;
        return base.accept(node,prop);
    }
}
