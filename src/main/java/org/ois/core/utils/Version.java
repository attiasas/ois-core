package org.ois.core.utils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a version in the format of "major.minor.patch".
 * Provides methods to compare versions and check their validity.
 */
public class Version implements Serializable {
    /** Constant representing a snapshot version. */
    public static final String SNAPSHOT = "SNAPSHOT";
    /** Constant representing a version that is not found. */
    public static final Version NOT_FOUND = new Version("0.0.0");
    /** The tokens representing the individual version components. */
    private final String[] tokens;
    /** The full version string. */
    private final String version;

    /**
     * Constructs a Version object with the given version string.
     *
     * @param version the version string to be parsed (must not be null or blank).
     * @throws IllegalArgumentException if the version string is null or blank.
     */
    public Version(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("Provide a valid version");
        }
        this.version = version;
        this.tokens = version.split("\\.");
    }

    /**
     * Checks if the version is valid.
     *
     * @return true if the version is not equal to {@link #NOT_FOUND}, false otherwise.
     */
    public boolean isValid() {
        return !NOT_FOUND.equals(this.version);
    }

    /**
     * Compares this version to another version to check if it is at least that version.
     *
     * @param atLeast the version to compare against (can be null, which means always true).
     * @return true if this version is at least the given version, false otherwise.
     */
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

    /**
     * Compares two version tokens to determine their order.
     *
     * @param toCheck      the token of this version to compare.
     * @param atLeastToken the token of the other version to compare against.
     * @return a negative integer, zero, or a positive integer as this token
     *         is less than, equal to, or greater than the specified token.
     */
    int compareTokens(String toCheck, String atLeastToken) {
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

    /**
     * Checks if the given string is numeric.
     *
     * @param str the string to check.
     * @return true if the string is numeric, false otherwise.
     */
    boolean isNumeric(String str) {
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

    /**
     * Checks if the given string is alphanumeric.
     *
     * @param str the string to check.
     * @return true if the string is alphanumeric, false otherwise.
     */
    boolean isAlphaNumeric(String str) {
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

    /**
     * Compares this version's numeric token to a specified numeric token.
     *
     * @param toCheck the token of this version to check.
     * @param atLeast the token to compare against.
     * @return a negative integer, zero, or a positive integer based on the comparison.
     */
    private int compareToCheckToNumericAtLeast(String toCheck, String atLeast) {
        if (isNumeric(toCheck)) {
            return compareNumerals(toCheck, atLeast);
        }
        if (isAlphaNumeric(toCheck)) {
            return compareAlphaNumericToCheckToNumericAtLeast(toCheck, atLeast);
        }
        return 1;
    }

    /**
     * Compares an alphanumeric token to a specified numeric token.
     *
     * @param toCheck the token of this version to check.
     * @param atLeast the token to compare against.
     * @return a negative integer, zero, or a positive integer based on the comparison.
     */
    private int compareAlphaNumericToCheckToNumericAtLeast(String toCheck, String atLeast) {
        String toCheckFirstNumerals = getTokenFirstNumerals(toCheck);
        if (toCheckFirstNumerals.isBlank()) {
            return 1;
        }
        return compareNumerals(toCheckFirstNumerals, atLeast);
    }

    /**
     * Compares two numeral strings.
     *
     * @param toCheck the numeral string to compare.
     * @param atLeast the other numeral string to compare against.
     * @return a negative integer, zero, or a positive integer based on the comparison.
     */
    int compareNumerals(String toCheck, String atLeast) {
        return (Integer.valueOf(toCheck).compareTo(Integer.valueOf(atLeast)));
    }

    /**
     * Extracts the leading numeric characters from a token.
     *
     * @param token the token to extract from.
     * @return a string representing the leading numerals.
     */
    String getTokenFirstNumerals(String token) {
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
