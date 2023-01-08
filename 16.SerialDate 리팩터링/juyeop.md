## SerialDate 리팩터링
- 비판이 있어야만 발전도 가능하다.

### SerialDate란?
- 날짜를 표현하는 자바 클래스다.
- 이미 자바에서는 날짜 관련 클래스(Calendar, Date)가 있지만 하루 중 시각, 시간대에 무관하게 특정 날짜를 표현하기 위해 만들어졌다.
- 즉, 시간 기반 클래스가 아닌 순수 날짜 클래스이다.

### 첫째, 돌려보자

#### 1. 모든 테스트 케이스를 점검하지 않는다.
- SerialDate에서 실행 가능한 문장 185개 중 단위 테스트가 실행하는 문장은 91개에 불과했다. (대략 50%)
- monthCodeToQuarter 코드를 전혀 실행하지 않는다.
```java
/**
 * Returns the quarter for the specified month.
 *
 * @param code  the month code (1-12).
 *
 * @return the quarter that the month belongs to.
 */
public static int monthCodeToQuarter(final int code) {

    switch(code) {
        case JANUARY:
        case FEBRUARY:
        case MARCH: return 1;
        case APRIL:
        case MAY:
        case JUNE: return 2;
        case JULY:
        case AUGUST:
        case SEPTEMBER: return 3;
        case OCTOBER:
        case NOVEMBER:
        case DECEMBER: return 4;
        default: throw new IllegalArgumentException(
        "SerialDate.monthCodeToQuarter: invalid month code.");
    }

}
```

- stringToWeekdayCode라는 메서드는 대소문자 구분 없이 모두 통과해야 한다고 생각한다.
  + 기존의 equals를 equalsIgnoreCase로 바꿨다.

``` java
/**
 * Converts the supplied string to a day of the week.
 *
 * @param s  a string representing the day of the week.
 *
 * @return <code>-1</code> if the string is not convertable, the day of
 *         the week otherwise.
 */
public static int stringToWeekdayCode(String s) {

    final String[] shortWeekdayNames
    = DATE_FORMAT_SYMBOLS.getShortWeekdays();
    final String[] weekDayNames = DATE_FORMAT_SYMBOLS.getWeekdays();

    int result = -1;
    s = s.trim();
    for (int i = 0; i < weekDayNames.length; i++) {
        if (s.equalsIgnoreCase(shortWeekdayNames[i])) {
            result = i;
            break;
        }
        if (s.equalsIgnoreCase(weekDayNames[i])) {
            result = i;
            break;
        }
    }
    return result;

}
```

- 'tues'와 'thurs'라는 약어도 지원해야 할지가 분명치 않다.
   + 스터디 질문: 각자의 의견은?

- 테스트 통과하지 않는 이슈 수정
  + monthCode를 1 ~ 12 사이 값을 안 넣었을 때는 result 값으로 -1을 넘기도록 수정
  + 대소문자 상관없이 처리 되도록 equalsIgnoreCase 사용

``` java
/**
 * Converts a string to a month code.
 * <P>
 * This method will return one of the constants JANUARY, FEBRUARY, ...,
 * DECEMBER that corresponds to the string.  If the string is not
 * recognised, this method returns -1.
 *
 * @param s  the string to parse.
 *
 * @return <code>-1</code> if the string is not parseable, the month of the
 *         year otherwise.
 */
public static int stringToMonthCode(String s) {

    final String[] shortMonthNames = DATE_FORMAT_SYMBOLS.getShortMonths();
    final String[] monthNames = DATE_FORMAT_SYMBOLS.getMonths();

    int result = -1;
    s = s.trim();

    // first try parsing the string as an integer (1-12)...
    try {
        result = Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
        // suppress
    }

    // now search through the month names...
    if ((result < 1) || (result > 12)) {
        result = -1;
        for (int i = 0; i < monthNames.length; i++) {
            if (s.equalsIgnoreCase(shortMonthNames[i])) {
                result = i + 1;
                break;
            }
            if (s.equalsIgnoreCase(monthNames[i])) {
                result = i + 1;
                break;
            }
        }
    }

    return result;

}
```

- getFollowingDayOfWeek 메서드 버그 수정
  + 2004년 12월 25일이 토요일인 상태에서 getFollowingDayOfWeek 메서드를 호출하면 2005년 1월 1일 토요일이 나와야 하는데 2004년 12월 25일 토요일이 그대로 반환된다.
  + 경계 조건 오류로 > 를 >=로 수정했다.
  + 근데 변경내역 주석에서는 이전에 해당 버그를 고쳤다고 말하고 있다.

``` java
/**
 * Returns the earliest date that falls on the specified day-of-the-week
 * and is AFTER the base date.
 *
 * @param targetWeekday  a code for the target day-of-the-week.
 * @param base  the base date.
 *
 * @return the earliest date that falls on the specified day-of-the-week
 *         and is AFTER the base date.
 */
public static SerialDate getFollowingDayOfWeek(final int targetWeekday,
final SerialDate base) {

    // check arguments...
    if (!SerialDate.isValidWeekdayCode(targetWeekday)) {
        throw new IllegalArgumentException(
                "Invalid day-of-the-week code."
                );
    }

    // find the date...
    final int adjust;
    final int baseDOW = base.getDayOfWeek();
    if (baseDOW >= targetWeekday) {
        adjust = 7 + Math.min(0, targetWeekday - baseDOW);
    }
    else {
        adjust = Math.max(0, targetWeekday - baseDOW);
    }

    return SerialDate.addDays(adjust, base);
}
```

``` java
 * 12-Nov-2001 : IBD requires setDescription() method, now that NotableDate
 *               class is gone (DG);  Changed getPreviousDayOfWeek(),
 *               getFollowingDayOfWeek() and getNearestDayOfWeek() to correct
 *               bugs (DG);
```

- getTestNearestDayOfWeek 메서드를 테스트하는 코드는 처음부터 이렇게 길지 않았다.
  + 처음 구현한 테스트 케이스가 실패하는 바람에 계속 추가하게 되었다.
  + 주석으로 처리한 코드를 살펴보면 실패하는 패턴이 보인다.
  + 알고리즘은 가장 가까운 날짜가 미래면 실패한다. (경계 조건 오류)
  + 알고리즘을 새롭게 수정했다.

``` java
/**
 * Returns the date that falls on the specified day-of-the-week and is
 * CLOSEST to the base date.
 *
 * @param targetDOW  a code for the target day-of-the-week.
 * @param base  the base date.
 *
 * @return the date that falls on the specified day-of-the-week and is
 *         CLOSEST to the base date.
 */
public static SerialDate getNearestDayOfWeek(final int targetDOW,
final SerialDate base) {

    // check arguments...
    if (!SerialDate.isValidWeekdayCode(targetDOW)) {
        throw new IllegalArgumentException(
                "Invalid day-of-the-week code."
                );
    }

    // find the date...
    int delta = targetDOW - base.getDayOfWeek();
    int positiveDelta = delta + 7;
    int adjust = positiveDelta % 7;
    
    if (adjust > 3)
        adjust -= 7;
    
    return SerialDate.addDays(adjust, base);

}
```

- weekInMonthToString과 relativeToString 메서드에서 오류 문자열을 반환하는 대신 IllegalArgumentException을 던지도록 수정했다.

``` java
/**
 * Returns a string corresponding to the week-in-the-month code.
 * <P>
 * Need to find a better approach.
 *
 * @param count  an integer code representing the week-in-the-month.
 *
 * @return a string corresponding to the week-in-the-month code.
 */
public static String weekInMonthToString(final int count) throws IllegalArgumentException {

    switch (count) {
        case SerialDate.FIRST_WEEK_IN_MONTH : return "First";
        case SerialDate.SECOND_WEEK_IN_MONTH : return "Second";
        case SerialDate.THIRD_WEEK_IN_MONTH : return "Third";
        case SerialDate.FOURTH_WEEK_IN_MONTH : return "Fourth";
        case SerialDate.LAST_WEEK_IN_MONTH : return "Last";
        default :
        throw new IllegalArgumentException("SerialDate.weekInMonthToString(): invalid code.");
    }

}
```

``` java
/**
 * Returns a string representing the supplied 'relative'.
 * <P>
 * Need to find a better approach.
 *
 * @param relative  a constant representing the 'relative'.
 *
 * @return a string representing the supplied 'relative'.
 */
public static String relativeToString(final int relative) throws IllegalArgumentException {

    switch (relative) {
        case SerialDate.PRECEDING : return "Preceding";
        case SerialDate.NEAREST : return "Nearest";
        case SerialDate.FOLLOWING : return "Following";
        default : throw new IllegalArgumentException("ERROR : Relative To String");
    }

}
```

- 지금까지 느낀점
  + 작성한 테스트 코드를 기반으로 코드 오류를 찾아내고 있다.
    * 만약 테스트 코드가 없었다면 위에서 살펴봤던 오류들을 찾을 수 있었을까?
    * 테스트 코드의 중요성을 한번 더 알게된다.

  + 테스트 코드를 작성하면서 전체적인 코드 구조를 확인할 수 있다.
    * 기존 코드를 알아야지 테스트 코드를 작성할 수 있다.
    * 따라서 기존 코드를 확인하므로 개선점도 찾을 수 있기에 리팩터링 때 어떤걸 개선해야 할지 찾을 수 있다.

  + 리팩터링을 시작하기 전 순서
    * 테스트 커버리지가 100%가 될 때까지 테스트 코드 작성
    * 테스트 코드 실행 과정에서 발생한 오류 대응
    * 구조적으로 개선이 필요한 부분 기록 후 리팩터링 과정에서 개선

  + 스터디 질문: 각자의 느낀점은?

### 둘째, 고쳐보자
- 코드를 고칠 때마다 JCommon 단위 테스트와 엉클밥 형이 짠 단위 테스트를 실행했다.

#### 변경 이력 제거
- 법적인 정보는 필요하므로 라이선스 정보와 저작권을 보존한다.
- 변경이력은 Git에서 해주므로 제거한다.
- 불필요한 주석은 거짓말과 잘못된 정보가 쌓이기 좋은 곳이다.

#### import문 수정
- java.text.*와 java.util.*로 줄인다.
- 스터디 질문: *를 쓰는 것보다 실제 사용하는 것만 import 하는게 좋지 않나?

#### 한 소스코드에서 여러 언어? 말 안돼
- 자바, 영어, JavaDoc, HTML 4가지를 사용하고 있다.
- HTML 태그를 사용해서 줄맞춤을 잘하더라도 JavaDoc으로 수정하면 이상하게 변환될 수 있다.
- 따라서 차리 pre 태그를 사용하는 것이 좋다. (JavaDoc으로 변환하더라도 형식 그대로 유지됨)

#### 클래스 이름이 왜 SerialDate 일까?
- Serializable을 파생하니까? NO
  + 나는 이거 때문인줄 알았다ㅋ
- 클래스 이름이 SerialDate인 이유는 일련번호를 사용해 클래스를 구현했기 때문이다.
- 여기서는 1899년 12월 30일 기준으로 경과한 날짜 수를 사용한다.
  + SERIAL_LOWER_BOUND: 2 (1899/12/30 부터 2일 경과됨 의미)
  + SERIAL_UPPER_BOUND: 2958465 (1899/12/30 부터 2958465일 경과됨 의미)
  + 스터디 질문: 왜 1899년 12월 30일 일까?
- 일련번호는 날짜보다 제품 식별 번호에 더 적합해서 상대 오프셋 또는 서수가 더 적합하다고 생각한다.
- SerialDate는 추상 클래스인데 정작 이름은 내부 구현을 암시할 수 있도록 한다.
  + 구현을 암시할 필요가 없다. (숨기는 편이 좋다.)
  + Date, Day는 많이 쓰므로 DayDate로 앞으로 부르겠다.
  + 스터디 질문: 추상 클래스이더라도 어떤 역할을 하는지는 알 수 있도록 이름을 지어야 하지 않을까?

#### enum으로 변경한 가능한 코드 변경
- MonthConstants를 상속하면 MonthConstants.January 같은 코드를 사용하지 않아서 좋긴 하지만 이것 때문에 상속을 하기에는 아쉽다.
  + import 해서 사용하면 위 문제를 해결할 수 있다.
- 이제 달 정보를 int로 받던걸 Month 클래스로 받을 수 있다.
  + isValidMonthCode, monthCodeToQuarter 메서드도 이제 필요하지 않다.

``` java
public abstract class DayDate implements Comparable,
        Serializable,
        MonthConstants {

    public static enum Month {
        JANUARY(1),
        FEBRUARY(2),
        MARCH(3),
        APRIL(4),
        MAY(5),
        JUN(6),
        JULY(7),
        AUGUST(8),
        SEPTEMBER(9),
        OCTOBER(10),
        NOVEMBER(11),
        DECEMBER(12);

        public final int index;

        Month(int index) {
            this.index = index;
        }

        public static Month make(int monthIndex) {
            for (Month m : Month.values()) {
                if (m.index == monthIndex)
                    return m;
            }
            throw new IllegalArgumentException(("Invalid month index " + monthIndex));
        }
    }
}
```

#### serialVersionUID 변수 제거
- 이 변수 값을 변경하면 이전 소프트웨어 버전에서 직렬화한 DayDate를 더 이상 인식하지 못한다.
- serialVersionUID 변수를 선언하지 않으면 컴파일러가 자동으로 생성한다.
- serialVersionUID를 매번 변경하지 않아 생기는 괴상한 오류를 디버깅하느니 차라리 InvalidClassException이 발생하는 편이 나을 것 같다고 생각해서 serialVersionUID를 없애기로 결정했다.
  + 나는 엉클밥형 의견에 동의하지 않는다.
  + serialVersionUID를 제거하면 오류가 났을 때 더 명확하다는 장점은 있으나 모듈을 조금 수정하더라도 컴파일러가 변수 값을 자동으로 변경할 수 있으므로 이전 버전에서도 역직렬화를 했을 때 되어야 할텐데 되지 않으면 이 경우에는 어떻게 처리할건가?
  + 스터디 질문: 각자의 의견은?

#### 변수명 변경 및 위치 이동
- DayDate 클래스가 표현할 수 있는 최초 날짜와 최후 날짜를 의미한다.
- 변수명을 알맞게 수정했다.
- DayDate 클래스에는 해당 변수가 있을 이유가 없고 SpreadSheetDate에서만 이를 사용하므로 해당 파일로 옮겼다.

``` java
/** The serial number for 1 January 1900. */
public static final int EARLIEST_DATE_ORDINAL = 2;

/** The serial number for 31 December 9999. */
public static final int LATEST_DATE_ORDINAL = 2958465;
```

#### ABSTRACT FACTORY 패턴으로 인스턴스 생성 위치 하나로 통일
- DayDate 클래스를 다양한 곳에서 각지각색으로 생성하고 있어서 이를 관리하기가 어려웠다.
- 따라서 Factory 클래스를 만들어서 DayDate 인스턴스가 생성되는 것을 하나로 통일함
- 이때 기반 클래스는 파생 클래스를 모르도록 구조를 잡아야 함
- 코드는 352, 353쪽을 참고하자

#### final 키워드 제거
- 실질적인 가치는 없으면서 코드만 복잡하게 만든다고 판단했다.
- final 상수 등 몇 군데를 제외하고는 별다른 가치가 없으며 코드만 복잡하게 만든다.

### 결론
- 우리는 보이스카우트 규칙을 따랐다.
- 테스트 커버리지가 증가했으며, 버그 몇 개를 고쳤으며, 코드 크기가 줄었고, 코드가 명확해졌다.
