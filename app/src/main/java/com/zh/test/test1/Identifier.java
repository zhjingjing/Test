package com.zh.test.test1;

/**
 * Created by jing on 16/3/10.
 */
public class Identifier {
    public final String type;
    public final String value;

    public Identifier(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Identifier)) return false;

        Identifier that = (Identifier) o;
        return Objects.equals(type, that.type) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return (type == null ? 0 : type.hashCode()) ^ (value == null ? 0 : type.hashCode());
    }

    @Override
    public String toString() {
        return type + "/" + value;
    }
}
