package net.ochibo.custom;

import java.util.Arrays;

public class StoredItemsComponent {
    public final byte[] data;

    public StoredItemsComponent(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoredItemsComponent that = (StoredItemsComponent) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
