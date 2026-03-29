package com.my.bookstore.exception;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AlreadyExistExceptionTest {
    private static Class<?> aClass;

    @BeforeAll
    static void init() throws ClassNotFoundException {
        aClass = Class.forName("com.my.bookstore.exception.AlreadyExistException");
    }

    @Test
    @DisplayName("AlreadyExistException extends RuntimeException")
    void testExtends(){
        assertEquals(RuntimeException.class, aClass.getSuperclass(),
                "AlreadyExistException should extends RuntimeException");
    }
}
