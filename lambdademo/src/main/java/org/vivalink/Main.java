package org.vivalink;

import com.amazonaws.AbortedException;

import java.io.IOException;

public class Main {


    public static void set() {
        throw new RetriableException("aa");
    }

    public static void main(String[] args) {
        set();
        try {
            throw new RetriableException("aa");
        } catch (RuntimeException e){
            System.out.println(e.getMessage());
        }

    }
}

class RetriableException extends RuntimeException{
    public RetriableException(String message) {
        super(message);
    }

    public RetriableException(String message, Throwable cause) {
        super(message, cause);
    }
}