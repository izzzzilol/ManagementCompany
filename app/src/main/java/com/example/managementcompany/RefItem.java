package com.example.managementcompany;

/** Элемент справочника для выпадающего списка внешнего ключа. */
public final class RefItem {
    public final long id;
    public final String label;

    RefItem(long id, String label) {
        this.id = id;
        this.label = label;
    }
}
