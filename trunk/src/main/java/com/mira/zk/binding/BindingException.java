package com.mira.zk.binding;

/**
 * Исключение, возникаюшее в ходе биндинга
 *
 * @author Мирзаян Арсен Валерьевич
 */
public class BindingException extends RuntimeException {

    public BindingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindingException() {
    }

    public BindingException(Throwable cause) {
        super(cause);
    }

    public BindingException(String message) {
        super(message);
    }
}
