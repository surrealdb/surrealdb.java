package meta.tests;

import org.junit.jupiter.api.Test;

public abstract class GsonAdaptorTest {

    @Test
    protected abstract void gson_toJson_whenProvidedWithJavaObject_returnsAProperlySerializedJsonRepresentation();

    @Test
    protected abstract void gson_fromJson_whenProvidedWithASerializedObject_returnsAnEqualJavaObject();
}
