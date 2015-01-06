package com.mlongbo.sunflower.commondb.exception;

/**
 * @author malongbo
 */
public final class QueryErrorException extends Exception {
    public QueryErrorException() {
        super();
    }

    public QueryErrorException(String message) {
        super(message);
    }

    public QueryErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryErrorException(Throwable cause) {
        super(cause);
    }

}
