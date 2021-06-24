package com.telegram.debt.exceptions;

/**
 * @author dsorokin on 24.03.2021
 */
public class DebtException extends Exception {
    public DebtException(Exception e) {
        super(e);
    }

    public DebtException() {
        super();
    }
}
