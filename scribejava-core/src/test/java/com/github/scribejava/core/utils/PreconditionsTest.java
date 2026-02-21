package com.github.scribejava.core.utils;

import org.junit.function.ThrowingRunnable;

import static org.junit.Assert.assertThrows;

public class PreconditionsTest {

    private static final String ERROR_MSG = "";

    public void shouldThrowExceptionForNullObjects() {
        assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                Preconditions.checkNotNull(null, ERROR_MSG);
            }
        });
    }

    public void shouldThrowExceptionForNullStrings() {
        assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                Preconditions.checkEmptyString(null, ERROR_MSG);
            }
        });
    }

    public void shouldThrowExceptionForEmptyStrings() {
        assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                Preconditions.checkEmptyString("", ERROR_MSG);
            }
        });
    }

    public void shouldThrowExceptionForSpacesOnlyStrings() {
        assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                Preconditions.checkEmptyString("               ", ERROR_MSG);
            }
        });
    }
}
