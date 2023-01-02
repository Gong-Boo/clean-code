package refactoring;


import junit.framework.Assert;

public class ComparisonCompactor10 {

    private static final String ELLIPSIS = "...";
    private static final String DELTA_END = "]";
    private static final String DELTA_START = "[";

    private int contextLength;
    private String expected;
    private String actual;

    /**
     * Step10. index -> length
     * 0에서 시작하지 않고 1에서부터 시작하므로 length가 합당함
     */
    /*-
    private int prefixIndex;
    private int suffixIndex;
     */
    private int prefixLength;
    private int suffixLength;

    private String compactExpected;
    private String compactActual;

    public ComparisonCompactor10(int contextLength, String expected, String actual) {
        this.contextLength = contextLength;
        this.expected = expected;
        this.actual = actual;
    }

    public String formatCompactedComparison(String message) {
        if (canBeCompacted()) {
            compactExpectedAndActual();
            return Assert.format(message, compactExpected, compactActual);
        } else {
            return Assert.format(message, expected, actual);
        }
    }

    private void compactExpectedAndActual() {
        findCommonPrefixAndSuffix();
        compactExpected = compactString(expected);
        compactActual = compactString(actual);
    }

    private boolean canBeCompacted() {
        return expected != null && actual != null && !areStringsEqual();
    }

    private String compactString(String source) {
        return computeCommonPrefix() +
                DELTA_END +
                source.substring(prefixLength, source.length() - suffixLength) +
                DELTA_END +
                computeCommonSuffix();
    }

    private void findCommonPrefixAndSuffix() {
        findCommonPrefix();
        suffixLength = 0;
        for (; suffixOverlapsPrefix(suffixLength); suffixLength++) {
            if (charFromEnd(expected, suffixLength) != charFromEnd(actual, suffixLength)) {
                break;
            }
        }
    }

    private char charFromEnd(String s, int i) {
        return s.charAt(s.length() - i);
    }

    private boolean suffixOverlapsPrefix(int suffixLength) {
        return actual.length() - suffixLength < prefixLength || expected.length() - suffixLength < prefixLength;
    }

    private void findCommonPrefix() {
        prefixLength = 0;
        int end = Math.min(expected.length(), actual.length());
        for (; prefixLength < end; prefixLength++) {
            if (expected.charAt(prefixLength) != actual.charAt(prefixLength)) {
                break;
            }
        }
    }

    private String computeCommonPrefix() {
        return (prefixLength > contextLength ? ELLIPSIS : "") + expected.substring(Math.max(0, prefixLength - contextLength), prefixLength);
    }

    private String computeCommonSuffix() {
        int end = Math.min(expected.length() - suffixLength + contextLength, expected.length());
        return expected.substring(expected.length() - suffixLength, end) +
                (expected.length() - suffixLength < expected.length() - contextLength ? ELLIPSIS : "");
    }

    private boolean areStringsEqual() {
        return expected.equals(actual);
    }
}
