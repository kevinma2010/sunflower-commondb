package com.mlongbo.sunflower.commondb;

/**
 * @author malongbo
 */
public final class OptionFailedException extends Exception {
    public OptionFailedException() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public OptionFailedException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public OptionFailedException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public OptionFailedException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
