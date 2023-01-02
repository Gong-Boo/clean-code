package refactoring;


import junit.framework.Assert;

public class ComparisonCompactor9 {

    private static final String ELLIPSIS = "...";
    private static final String DELTA_END = "]";
    private static final String DELTA_START = "[";

    private int contextLength;
    private String expected;
    private String actual;
    private int prefixIndex;
    private int suffixIndex;

    private String compactExpected;
    private String compactActual;

    public ComparisonCompactor9(int contextLength, String expected, String actual) {
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

    /**
     * Step9. 이유가 분명하지 않으니 다른 방법 고안
     * prefixIndex를 전달하는 방식은 다소 자의적임
     * 호출 순서는 확실히 정해지지만, prefixIndex가 필요한 이유를 설명하지 못함
     */
    private void compactExpectedAndActual() {
        /*-
        prefixIndex = findCommonPrefix();
        suffixIndex = findCommonSuffix(prefixIndex);
         */
        findCommonPrefixAndSuffix();
        compactExpected = compactString(expected);
        compactActual = compactString(actual);
    }

    private boolean canBeCompacted() {
        return expected != null && actual != null && !areStringsEqual();
    }

    private String compactString(String source) {
        String result = DELTA_START + source.substring(prefixIndex, source.length() - suffixIndex + 1) + DELTA_END;
        if (prefixIndex > 0) {
            result = computeCommonPrefix() + result;
        }
        if (suffixIndex > 0) {
            result = result + computeCommonSuffix();
        }
        return result;
    }

    private void findCommonPrefixAndSuffix() {
        findCommonPrefix();
        int suffixLength = 1;
        for (; suffixOverlapsPrefix(suffixLength); suffixLength++) {
            if (charFromEnd(expected, suffixLength) != charFromEnd(actual, suffixLength)) {
                break;
            }
        }
        suffixIndex = suffixLength;
    }

    private char charFromEnd(String s, int i) {
        return s.charAt(s.length() - i);
    }

    private boolean suffixOverlapsPrefix(int suffixLength) {
        return actual.length() - suffixLength < prefixIndex || expected.length() - suffixLength < prefixIndex;
    }

    private void findCommonPrefix() {
        prefixIndex = 0;
        int end = Math.min(expected.length(), actual.length());
        for (; prefixIndex < end; prefixIndex++) {
            if (expected.charAt(prefixIndex) != actual.charAt(prefixIndex)) {
                break;
            }
        }
    }

    /*-
    private int findCommonPrefix() {
        int prefixIndex = 0;
        int end = Math.min(expected.length(), actual.length());
        for (; prefixIndex < end; prefixIndex++) {
            if (expected.charAt(prefixIndex) != actual.charAt(prefixIndex)) {
                break;
            }
        }
        return prefixIndex;
    }
     */

    /*
    private int findCommonSuffix(int prefixIndex) {
        int expectedSuffix = expected.length() - 1;
        int actualSuffix = actual.length() - 1;
        for (; actualSuffix >= prefixIndex && expectedSuffix >= prefixIndex; actualSuffix--, expectedSuffix--) {
            if (expected.charAt(expectedSuffix) != actual.charAt(actualSuffix)) {
                break;
            }
        }
        return expected.length() - expectedSuffix;
    }
     */

    private String computeCommonPrefix() {
        return (prefixIndex > contextLength ? ELLIPSIS : "") + expected.substring(Math.max(0, prefixIndex - contextLength), prefixIndex);
    }

    private String computeCommonSuffix() {
        int end = Math.min(expected.length() - suffixIndex + 1 + contextLength, expected.length());
        return expected.substring(expected.length() - suffixIndex + 1, end) + (expected.length() - suffixIndex + 1 < expected.length() - contextLength ? ELLIPSIS : "");
    }

    private boolean areStringsEqual() {
        return expected.equals(actual);
    }
}
