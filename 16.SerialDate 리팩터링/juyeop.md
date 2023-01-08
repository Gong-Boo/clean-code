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
