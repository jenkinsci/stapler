package org.kohsuke.stapler.export;

import java.util.Map;
import java.util.TreeMap;

/**
 * Tree pruner which operates according to a textual description of what tree leaves should be included.
 */
public final class NamedPathPruner extends TreePruner {
    
    static class Tree {
        final Map<String,Tree> children = new TreeMap<String,Tree>();
        public @Override String toString() {return children.toString();}
    }

    // Simple recursive descent parser:
    static Tree parse(String spec) throws IllegalArgumentException {
        Reader r = new Reader(spec);
        Tree t = new Tree();
        list(r, t);
        r.expect(Token.EOF);
        return t;
    }
    private static void list(Reader r, Tree t) throws IllegalArgumentException {
        node(r, t);
        if (r.accept(Token.COMMA)) {
            list(r, t);
        }
    }
    private static void node(Reader r, Tree t) throws IllegalArgumentException {
        Object actual = r.peek();
        if (actual instanceof Token) {
            throw new IllegalArgumentException("expected name at " + r.pos);
        }
        r.advance();
        Tree subtree = new Tree();
        t.children.put((String) actual, subtree);
        if (r.accept(Token.LBRACE)) {
            list(r, subtree);
            r.expect(Token.RBRACE);
        }
    }
    private enum Token {COMMA, LBRACE, RBRACE, EOF}
    private static class Reader {
        private final String text;
        int pos, next;
        Reader(String text) {
            this.text = text;
            pos = 0;
        }
        Object peek() {
            if (pos == text.length()) {
                return Token.EOF;
            }
            switch (text.charAt(pos)) {
            case ',':
                next = pos + 1;
                return Token.COMMA;
            case '[':
                next = pos + 1;
                return Token.LBRACE;
            case ']':
                next = pos + 1;
                return Token.RBRACE;
            default:
                next = text.length();
                for (char c : new char[] {',', '[', ']'}) {
                    int x = text.indexOf(c, pos);
                    if (x != -1 && x < next) {
                        next = x;
                    }
                }
                return text.substring(pos, next);
            }
        }
        void advance() {
            pos = next;
        }
        void expect(Token tok) throws IllegalArgumentException {
            Object actual = peek();
            if (actual != tok) {
                throw new IllegalArgumentException("expected " + tok + " at " + pos);
            }
            advance();
        }
        boolean accept(Token tok) {
            if (peek() == tok) {
                advance();
                return true;
            } else {
                return false;
            }
        }
    }
    
    private final Tree tree;

    /**
     * Constructs a pruner by parsing a textual specification.
     * This lists the properties which should be included at each level of the hierarchy.
     * Properties are separated by commas and nested objects are inside square braces.
     * For example, {@code a,b[c,d]} will emit the top-level property {@code a} but
     * none of its children, and the top-level property {@code b} and only those
     * of its children named {@code c} and {@code d}.
     * @param spec textual specification of tree
     * @throws IllegalArgumentException if the syntax is incorrect
     */
    public NamedPathPruner(String spec) throws IllegalArgumentException {
        this(parse(spec));
    }
    
    private NamedPathPruner(Tree tree) {
        this.tree = tree;
    }

    public @Override TreePruner accept(Object node, Property prop) {
        Tree subtree = tree.children.get(prop.name);
        return subtree != null ? new NamedPathPruner(subtree) : null;
    }

}
