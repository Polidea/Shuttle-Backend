package com.polidea.shuttle.infrastructure.json;

public class NullableOptionalRequestField<T> implements OptionalRequestField<T> {

    private final T value;

    public NullableOptionalRequestField(T value) {
        this.value = value;
    }

    @Override
    public T value() {
        return value;
    }

}
