package org.kohsuke.stapler;

public class WrongTypeException extends IllegalArgumentException {
    public WrongTypeException(String s) {
        super(s);
    }
}
