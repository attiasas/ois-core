package org.ois.core.utils;

import java.io.Serializable;
import java.util.Objects;

public class Version implements Serializable {
    public static final String SNAPSHOT = "SNAPSHOT";
    public static final Version NOT_FOUND = new Version("0.0.0");

    private final String[] tokens;
    private final String version;

    public Version(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("Provide a valid version");
        }
        this.version = version;
        this.tokens = version.split("\\.");
    }

    public boolean isValid() {
        return !NOT_FOUND.equals(this.version);
    }

    public boolean isAtLeast(Version atLeast) {
        if (atLeast == null) {
            return true;
        }
        // compare token by token
        for (int i = 0; i < atLeast.tokens.length; i++) {
            String atLeastToken = atLeast.tokens[i].trim();
            if (this.tokens.length < (i + 1)) {
                // the current token index of atLeast is greater than this versions length, than at least is greater
                return false;
            }

            int comparison = compareTokens(this.tokens[i].trim(), atLeastToken);
            if (comparison < 0) {
                // the current token of this version is less than atLeast current token
                return false;
            }
            if (comparison > 0) {
                // the current token of this version is greater than atLeast current token
                return true;
            }
        }
        return true;
    }

    private int compareTokens(String toCheck, String atLeastToken) {
        boolean toCheckIsBlank = toCheck == null || toCheck.isBlank();
        boolean atLeastTokenIsBlank = atLeastToken == null || atLeastToken.isBlank();
        if (toCheckIsBlank && atLeastTokenIsBlank) {
            return 0;
        } else if (!toCheckIsBlank && atLeastTokenIsBlank) {
            return 1;
        } else if (toCheckIsBlank) {
            return -1;
        }

        if (isNumeric(atLeastToken)) {
            return compareToCheckToNumericAtLeast(toCheck, atLeastToken);
        }
        if (isAlphaNumeric(atLeastToken)) {
            String atLeastTokenFirstNumerals = getTokenFirstNumerals(atLeastToken);
            if (!atLeastTokenFirstNumerals.isBlank()) {
                return compareToCheckToNumericAtLeast(toCheck, atLeastTokenFirstNumerals);
            }
            if (isNumeric(toCheck)) {
                return -1;
            }
        }
        int comparison = toCheck.compareTo(atLeastToken);
        if (comparison == 0) {
            boolean toCheckIsSnapshot = toCheck.contains(SNAPSHOT);
            boolean atLeastIsSnapshot = atLeastToken.contains(SNAPSHOT);
            if (toCheckIsSnapshot && !atLeastIsSnapshot) {
                return 1;
            } else if (!toCheckIsSnapshot && atLeastIsSnapshot) {
                return -1;
            }
        }

        return comparison;
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    private boolean isAlphaNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }

        return true;
    }

    private int compareToCheckToNumericAtLeast(String toCheck, String atLeast) {
        if (isNumeric(toCheck)) {
            return compareNumerals(toCheck, atLeast);
        }
        if (isAlphaNumeric(toCheck)) {
            return compareAlphaNumericToCheckToNumericAtLeast(toCheck, atLeast);
        }
        return 1;
    }

    private int compareAlphaNumericToCheckToNumericAtLeast(String toCheck, String atLeast) {
        String toCheckFirstNumerals = getTokenFirstNumerals(toCheck);
        if (toCheckFirstNumerals.isBlank()) {
            return 1;
        }
        return compareNumerals(toCheckFirstNumerals, atLeast);
    }

    private int compareNumerals(String toCheck, String atLeast) {
        return (Integer.valueOf(toCheck).compareTo(Integer.valueOf(atLeast)));
    }

    private String getTokenFirstNumerals(String token) {
        char[] chars = token.toCharArray();
        StringBuilder numerals = new StringBuilder();
        for (char c : chars) {
            if (!Character.isDigit(chars[0])) {
                break;
            }
            numerals.append(c);
        }
        return numerals.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof String) {
            return version.equals(o);
        }
        if (getClass() != o.getClass()) return false;
        Version version1 = (Version) o;
        return version.equals(version1.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public String toString() {
        return version;
    }
}
