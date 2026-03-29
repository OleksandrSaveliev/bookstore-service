package com.my.bookstore.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientModelTest {

    private static List<Field> allFields;
    private static List<Constructor<?>> allConstructors;

    @BeforeAll
    static void setup() throws ClassNotFoundException {
        final Class<?> clazz = Class.forName(com.my.bookstore.model.Constants.CLIENT_TYPE);
        allFields = Arrays.asList(clazz.getDeclaredFields());
        allConstructors = Arrays.asList(clazz.getConstructors());
    }

    @Test
    @DisplayName("Count constructors")
    void checkCountConstructors() {
        assertEquals(com.my.bookstore.model.Constants.Client.CLASS_COUNT_CONSTRUCTORS, allConstructors.size());
    }

    @Test
    @DisplayName("Modifiers constructors can be public")
    void checkModifiersConstructors() {
        var actual = allConstructors.stream()
                .allMatch(constructor -> Modifier.isPublic(constructor.getModifiers()));
        assertTrue(actual);
    }

    @Test
    @DisplayName(com.my.bookstore.model.Constants.Client.CLASS_NAME + " has default constructor")
    void checkDefaultConstructor() {
        var count = allConstructors.stream()
                .filter(constructor -> constructor.getParameterCount() == 0)
                .count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName(com.my.bookstore.model.Constants.Client.CLASS_NAME + " has constructor with " + com.my.bookstore.model.Constants.Client.PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS + " parameters")
    void checkConstructorWithParameter() {
        var count = allConstructors.stream()
                .filter(constructor -> constructor.getParameterCount() == com.my.bookstore.model.Constants.Client.PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS)
                .count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("Check parameter type in constructor with parameter")
    void checkParameterTypeForConstructorWithParameter() {
        final var constructor = allConstructors.stream()
                .filter(c -> c.getParameterCount() == com.my.bookstore.model.Constants.Client.PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No constructor with parameters"));

        final var parameters = Arrays.asList(constructor.getParameters());

        parameters.stream()
                .filter(p -> p.getType().getTypeName().equals(com.my.bookstore.model.Constants.BIG_DECIMAL_TYPE))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No parameter with type " + com.my.bookstore.model.Constants.BIG_DECIMAL_TYPE));

        assertEquals(com.my.bookstore.model.Constants.Client.PARAMETERS_IN_CONSTRUCTOR_WITH_PARAMETERS, parameters.size());
    }

    @Test
    @DisplayName("Count fields")
    void checkCountFields() {
        assertEquals(com.my.bookstore.model.Constants.Client.CLASS_COUNT_FIELDS, allFields.size());
    }

    @Test
    @DisplayName("Modifiers fields can be private")
    void checkModifiersFields() {
        var count = allFields.stream()
                .filter(f -> Modifier.isPrivate(f.getModifiers()))
                .count();

        assertEquals(com.my.bookstore.model.Constants.Client.CLASS_COUNT_FIELDS, count);
    }

    @DisplayName("Check field type and field name")
    @ParameterizedTest
    @CsvSource({
            "java.math.BigDecimal, balance, 1"
    })
    void checkNameFieldType(String fieldType, String fieldName, long expected) {
        var count = allFields.stream()
                .filter(f -> f.getType().getTypeName().equals(fieldType)
                        & f.getName().equals(fieldName))
                .count();
        assertEquals(expected, count);
    }


}
