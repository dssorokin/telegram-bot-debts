package com.telegram.debt;

import lombok.Data;

@Data
public class NoSuchUserException extends Exception {

    private long userId;

    public NoSuchUserException(long userId) {
        super();
        this.userId = userId;
    }
}
