package org.ois.core.utils;

import org.testng.Assert;
import org.testng.annotations.Test;

public class VersionTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructorWithNullVersion() {
        new Version(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConstructorWithBlankVersion() {
        new Version("");
    }

    @Test
    public void testConstructorWithValidVersion() {
        Version version = new Version("1.0.0");
        Assert.assertEquals(version.toString(), "1.0.0");
    }

    @Test
    public void testIsValidWithValidVersion() {
        Version version = new Version("1.0.0");
        Assert.assertTrue(version.isValid());
    }

    @Test
    public void testIsValidWithNotFoundVersion() {
        Assert.assertFalse(Version.NOT_FOUND.isValid());
    }

    @Test
    public void testIsAtLeastWithEqualVersion() {
        Version version1 = new Version("1.0.0");
        Version version2 = new Version("1.0.0");
        Assert.assertTrue(version1.isAtLeast(version2));
    }

    @Test
    public void testIsAtLeastWithGreaterVersion() {
        Version version1 = new Version("1.1.0");
        Version version2 = new Version("1.0.0");
        Assert.assertTrue(version1.isAtLeast(version2));
    }

    @Test
    public void testIsAtLeastWithLesserVersion() {
        Version version1 = new Version("1.0.0");
        Version version2 = new Version("1.1.0");
        Assert.assertFalse(version1.isAtLeast(version2));
    }

    @Test
    public void testIsAtLeastWithNullVersion() {
        Version version = new Version("1.0.0");
        Assert.assertTrue(version.isAtLeast(null));
    }

    @Test
    public void testCompareTokensWithEqualTokens() {
        int comparison = new Version("1.0.0").compareTokens("1", "1");
        Assert.assertEquals(comparison, 0);
    }

    @Test
    public void testCompareTokensWithLessToken() {
        int comparison = new Version("1.0.0").compareTokens("1", "2");
        Assert.assertTrue(comparison < 0);
    }

    @Test
    public void testCompareTokensWithGreaterToken() {
        int comparison = new Version("1.0.0").compareTokens("2", "1");
        Assert.assertTrue(comparison > 0);
    }

    @Test
    public void testIsNumericWithValidNumericString() {
        Assert.assertTrue(new Version("1.0.0").isNumeric("123"));
    }

    @Test
    public void testIsNumericWithInvalidNumericString() {
        Assert.assertFalse(new Version("1.0.0").isNumeric("abc"));
    }

    @Test
    public void testIsAlphaNumericWithValidAlphanumericString() {
        Assert.assertTrue(new Version("1.0.0").isAlphaNumeric("abc123"));
    }

    @Test
    public void testIsAlphaNumericWithInvalidAlphanumericString() {
        Assert.assertFalse(new Version("1.0.0").isAlphaNumeric("abc!123"));
    }

    @Test
    public void testCompareNumerals() {
        Assert.assertTrue(new Version("1.0.0").compareNumerals("2", "1") > 0);
    }

    @Test
    public void testEqualsWithSameObject() {
        Version version = new Version("1.0.0");
        Assert.assertTrue(version.equals(version));
    }

    @Test
    public void testEqualsWithDifferentObject() {
        Version version1 = new Version("1.0.0");
        Version version2 = new Version("1.0.0");
        Assert.assertTrue(version1.equals(version2));
    }

    @Test
    public void testHashCode() {
        Version version1 = new Version("1.0.0");
        Version version2 = new Version("1.0.0");
        Assert.assertEquals(version1.hashCode(), version2.hashCode());
    }

    @Test
    public void testToString() {
        Version version = new Version("1.0.0");
        Assert.assertEquals(version.toString(), "1.0.0");
    }
}
