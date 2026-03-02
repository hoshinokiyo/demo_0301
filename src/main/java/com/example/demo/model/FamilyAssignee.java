package com.example.demo.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum FamilyAssignee {
    FATHER("FATHER", "\u7236", new String[]{"FATHER", "\u7236"}),
    MOTHER("MOTHER", "\u6bcd", new String[]{"MOTHER", "\u6bcd"}),
    ME("ME", "\u50d5", new String[]{"ME", "\u50d5"}),
    SISTER("SISTER", "\u59c9", new String[]{"SISTER", "\u59c9", "\u304a\u59c9\u3061\u3083\u3093"});

    private final String code;
    private final String label;
    private final String[] aliases;

    FamilyAssignee(String code, String label, String[] aliases) {
        this.code = code;
        this.label = label;
        this.aliases = aliases;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public static Optional<FamilyAssignee> fromInput(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String normalized = raw.trim();
        if (normalized.isEmpty()) {
            return Optional.empty();
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(v -> v.code.equals(upper) || Arrays.stream(v.aliases).anyMatch(a -> a.equals(normalized)))
                .findFirst();
    }
}
