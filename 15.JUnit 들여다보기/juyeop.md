## JUnit 들여다보기

### JUnit 프레임워크
- ComparisonCompactor는 두 문자열을 받아 차이를 반환한다.
- 예를 들어, ABCDE와 ABXDE를 받아 <...B[X]D...>를 반환한다.

### ComparisonCompactorTest 클래스
- ComparisonCompactor 모듈에 대한 코드 커버리지 분석을 수행했더니 100%가 나왔다.
- 테스트 케이스가 모든 행, 모든 if 문, 모든 for 문을 실행한다는 의미다.

### ComparsionCompactor 클래스 리팩터링
- 보이스카우트 규칙에 따르면 우리는 처음 왔을 때보다 더 깨끗하게 해놓고 떠나야 한다.

#### 1. 멤버 변수 앞에 붙인 접두어 f 제거

#### 2. 캡슐화되지 않은 조건문을 메서드로 캡슐화
``` java
private boolean shouldNotCompact() {
  return expected == null || actual == null || areStringsEqual();
}
````

부정문은 긍정문보다 이해하기 약간 더 어렵다.  
그러므로 첫 문장 if를 긍정으로 만들어 조건문을 반전한다.

``` java
private boolean canBeCompacted() {
  return expected != null && actual != null && !areStringsEqual();
}
```

#### 3. compact 메서드명 변경
- 문자열을 압축하는 함수라지만 실제로 canBeCompacted가 false이면 압축하지 않는다.
- 그러므로 오류 점검이라는 부가 단계가 숨겨진다.
- 게다가 함수는 단순히 압축된 문자열이 아니라 형식이 갖춰진 문자열을 반환한다.

``` java
public String formatCompactedComparison(String message) {}
```

#### 4. 실제 압축하는 코드를 따로 분리
- 압축 코드를 compactExpectedAndActual이라는 메서드로 분리시키나 형식을 맞추는 작업은 formatCompactedComparison에게 전적으로 맡긴다.

``` java
private String compactExpected;
private String compactActual;

public String formatCompactedComparison(String message) {
  if (canBecompacted()) {
    compactExpectedAndActual();
    return Assert.format(message, compactExpected, compactActual);
  } else {
    return Assert.format(message, expected, actual);
  }
}

private void compactExpectedAndActual() {
  findCommonPrefix();
  findCommonSuffix();
  compactExpected = compactString(expected);
  compactActual = compactString(actual);
}
```

#### 5. findCommonXXX 메서드 사용방식 통일화
- compactString 함수와 동일하게 findCommonXXX 함수도 값을 반환하도록 수정

``` java
private void compactExpectedAndActual() {
  prefixIndex = findCommonPrefix();
  suffixIndex = findCommonSuffix();
  compactExpected = compactString(expected);
  compactActual = compactString(actual);
}
```

덕분에 기존에 prefix, suffix 멤버변수도 prefixIndex, suffixIndex로 변경됨

#### 6. findCommonSuffix 메서드의 숨겨진 시간적인 결합 문제 해결
- findCommonSuffix는 prefixIndex에 의존함
- findCommonSuffix에 prefixIndex를 파라미터로 전달하는 방법도 있으나 자의적이다
- findCommonSuffix를 findCommonPrefixAndSuffix로 바꾸고 내부에서 findCommonPrefix를 호출하도록 한다

``` java
private fun findCommonPrefixAndSuffix() {
  findCommonPrefix();
  ...
}

// 신규 추가
private char charFromEnd(String s, int i) {
  ...
}

// 신규 추가
private boolean suffixOverlapsPrefix(int suffixLength) {
  ...
}
```

실제로 suffixIndex는 0에서 시작하지 않는다.  
1에서 시작하므로 진정한 길이가 아니다.














