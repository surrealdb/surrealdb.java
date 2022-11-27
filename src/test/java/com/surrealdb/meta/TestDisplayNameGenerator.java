package com.surrealdb.meta;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayNameGenerator.Standard;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class TestDisplayNameGenerator extends Standard {

    @Override
    public String generateDisplayNameForClass(Class<?> testClass) {
        String className = testClass.getSimpleName();
        String[] split = className.split("_");

        if (split.length == 1) {
            return className;
        }

        if (split.length == 2) {
            String testedClassName = split[0];
            String feature = toSentence(split[1].replace("Test", "")).toUpperCase();
            return String.format("%s - %s", testedClassName, feature);
        }

        return "(invalid test class name) " + className;
    }

    @Override
    public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
        return String.format("[%s]", toSentence(nestedClass.getSimpleName()).toUpperCase());
    }

    @Override
    public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
        String[] split = testMethod.getName().split("_");

        if (split.length == 3) {
            String method = split[0];
            String when = toSentence(split[1]);
            String result = toSentence(split[2]);

            return String.format("%s(): %s → %s", method, when, result);
        }

        if (split.length == 4) {
            String clazz = split[0];
            String method = split[1];
            String and = toSentence(split[2]);
            String result = toSentence(split[3]);

            return String.format("%s.%s(): %s → %s", clazz, method, and, result);
        }

        return toSentence(testMethod.getName());
    }

    private @NotNull String toSentence(@NotNull String testMethod) {
        String[] words = testMethod.split("(?=[A-Z])");
        return String.join(" ", words).toLowerCase();
    }
}
