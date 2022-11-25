package com.surrealdb.meta;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayNameGenerator.Standard;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class TestDisplayNameGenerator extends Standard {

    @Override
    public String generateDisplayNameForClass(Class<?> testClass) {
        return super.generateDisplayNameForClass(testClass);
    }

    @Override
    public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
        return toSentence(nestedClass.getSimpleName());
    }

    @Override
    public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
        String[] split = testMethod.getName().split("_");

        if (split.length != 3) {
            return "(invalid test name) " + toSentence(testMethod.getName());
        }

        String method = toMethodString(split[0]);
        String when = toSentence(split[1]);
        String result = toSentence(split[2]);

        return String.format("%s: %s → %s", method, when, result);
    }

    private @NotNull String toMethodString(@NotNull String name) {
        return String.format("%s()", name);
    }

    private @NotNull String toSentence(@NotNull String testMethod) {
        String[] words = testMethod.split("(?=[A-Z])");
        return String.join(" ", words).toLowerCase();
    }
}
