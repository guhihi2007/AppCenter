package cn.lt.framework.exception;

/**
 * Created by wenchao on 2016/1/19.
 */
public class LTRunntimeException extends RuntimeException {
    /**
     * Constructs a new {@code RuntimeException} that includes the current stack
     * trace.
     */
    public LTRunntimeException() {
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public LTRunntimeException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace,
     * the specified detail message and the specified cause.
     *
     * @param detailMessage
     *            the detail message for this exception.
     * @param throwable
     *            the cause of this exception.
     */
    public LTRunntimeException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified cause.
     *
     * @param throwable
     *            the cause of this exception.
     */
    public LTRunntimeException(Throwable throwable) {
        super(throwable);
    }
}
