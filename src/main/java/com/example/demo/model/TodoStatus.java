package com.example.demo.model;

public enum TodoStatus {
    ACTIVE("ACTIVE", "ふつう"),
    DELETE_REQUESTED("DELETE_REQUESTED", "おねがい中"),
    DELETED("DELETED", "けした");

    private final String code;
    private final String label;

    TodoStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static TodoStatus fromCode(String raw) {
        if (raw == null) {
            return ACTIVE;
        }
        for (TodoStatus s : values()) {
            if (s.code.equalsIgnoreCase(raw.trim())) {
                return s;
            }
        }
        return ACTIVE;
    }
}
