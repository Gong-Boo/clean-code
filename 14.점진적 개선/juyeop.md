## 점진적 개선
### Args 사용법
- 첫째 매개변수는 형식 또는 스키마를 지정한다.
  + -l은 부울
  + -p는 정수
  + -d는 문자열
- 둘째 매개변수는 main으로 넘어온 명령행 인수 배열 자체다.
- 인수 값을 가져오려면 `getBoolean`, `getInteger`, `getString` 등과 같은 메서드를 사용한다.

```kotlin
val arg = Args("l,p#,d*", args)
args.getBoolean('l')
args.getInt('p')
args.getString('d')
```

### Args 소감
- 자바는 정적 타입 언어라서 타입 시스템을 만족하려면 많은 단어가 필요하다.
- 루비, 파이썬, 스몰토크 등과 같은 언어를 사용했다면 프로그램이 훨씬 작아졌으리라.
  + 엉클밥 형이 루비로 작성한 버전 (https://github.com/unclebob/rubyargs/tree/master)
- 날짜 인수나 복소수 인수 등 새로운 인수 유형을 추가하는 방법이 명백하다.
- ArgumentMarshaler에서 새 클래스를 파생해서 구현하고 Args 클래스에서 getXXX 함수를 추가한 후 parseSchemaElement 함수에 else if 문만 추가하면 끝이다.

### Args 어떻게 짰느냐고?
- 더욱 중요하게는 여러분이 깨끗하고 우아한 프로그램을 한 방에 뚝딱 내놓으리라 기대하지 않는다.
- 프로그램은 과학보다 공예에 더 가깝다는 사실이다.
  + 나는 과학이 60%, 공예가 40% 라고 생각한다.
  + 다른 분들의 의견은 어떠신지? (스터디 질문)
- 깨끗한 코드를 짜려면 먼저 지저분한 코드를 짠 뒤에 정리해야 한다.
- 일단 프로그램이 돌아가면 다음 업무로 넘어간다, 돌아가는 프로그램은 그 상태가 어떻든 그대로 버려둔다.
- 경험이 풍부한 전문 프로그래머라면 이런 행동이 전문가로서 자살 행위라는 사실을 잘 안다.

### Args 1차 초안
- 'TILT'와 같은 희한한 문자열, HashSets와 TreeSets, try-catch-catch 블록 등 모두가 지저분한 코드에 기여하는 요인이다.
  + 나는 앞에서 봤던 Args 완성본보다 Args 1차 초안이 훨씬 코드 흐름을 따라가기 쉬웠던 것 같다.
  + 추상화가 덜 되어 있고 절차적으로 작성되어서 그런가?
  + 다른 분들의 의견은 어떠신지? (스터디 질문)

- 함수 이름이나 변수 이름을 선택한 방식, 어설프지만 나름대로 구조가 있다는 사실 등이 내노력의 증거다.
- 하지만 어느 순간 프로그램은 내 손을 벗어났다.
- 다만, Boolean 인수만 지원하던 초기 버전에서는 이정도 까지는 아니었다.
- 유지와 보수가 적당히 수월했던 코드가 버그와 결함이 숨어있을지도 모른다는 상당히 의심스러운 코드로 뒤바뀌어버렸다.

### 그래서 멈췄다
- 추가할 인수 유형이 적어도 두 개는 더 있었는데 그러면 코드가 훨씬 나빠지리라는 사실이 자명했다.
- 계속 밀어붙이면 프로그램은 어떻게든 완성하겠지만 그랫다가는 너무 커서 손대기 어려운 골칫거리가 생겨날 참이었다.
- 새 인수 유형을 추가하려면 주요 지점 세 곳에다 코드를 추가해야 한다는 사실을 이미 깨달았다.
  + 첫째, 인수 유형에 해당하는 HashMap을 선택하기 위해 스키마 요소의 구문을 분석한다.
  + 둘째, 명령행 인수에서 인수 유형을 분석해 진짜 유형으로 변환한다.
  + 셋째, getXXX 메서드를 구현해 호출자에게 진짜 유형을 반환한다.
- 인수 유형은 다양하지만 모두가 유사한 메서드를 제공하므로 클래스 하나가 적합하다 판단했다, 그래서 ArgumentMarshaler 개념 탄생 🎉

### 점진적으로 개선하다
- 프로그램을 망치는 가장 좋은 방법 중 하나는 개선이라는 이름 아래 구조를 크게 뒤집는 행위다.
- 개선 전과 똑같이 프로그램을 돌리기가 아주 어렵기 때문이다.
- TDD는 시스템을 망가뜨리는 변경을 허용하지 않는다.
- 변경을 가한 후에도 시스템이 변경 전과 똑같이 돌아가야 한다는 말이다.
  + 코드를 수정하고 기존에 작성해둔 테스트 케이스를 돌리면서 코드의 안정성을 확보한다.
  + 테스트 코드가 왜 중요한지 한번 더 느꼈다...

#### 1. ArgumentMarshaler 클래스 추가
- ArgumentMarshaler를 상속하는 BooleanArgumentMarshaler, StringArgumentMarshaler, IntegerArgumentMarshaler 구현
- booleanArgs의 HashMap value 타입을 ArgumentMarshaler로 교체
  + 컴파일 에러나는 곳 수정
    * parseBooleanSchemaElement
    * setBooleanArg
    * getBoolean

  + 이미 위 3곳에서 컴파일 에러가 날 것이라고 예상을 했었음
- getBoolean 함수에서 NPE 발생 가능성이 있으므로 수정

#### 2. String, Int 타입 역시 ArgumentMarshaler로 교체
- Boolean 타입 변경했던 것과 동일하게 진행
- 코드 수정시 테스트 케이스를 즉시 돌려 오류를 바로바로 수정함
- 바로 BooleanArgumentMarsahler, StringArgumentMarshaler, IntegerArgumentMarshaler에 코드 구현을 하지 않고 ArgumentMarshaler에 코드를 구현을 하는 이유는 프로그램 구조를 조금씩 변경하는 동안에 기존 시스템의 동작을 더욱 안전하게 유지하면서 리팩터링을 하기 위해서이다.

#### 3. ArgumentMarshaler 구현체로 코드 이동
- ArgumentMarshaler 클래스에 모든 구현을 다했던 코드를 타입별 ArgumentMarshaler 구현체로 코드를 이동시킨다.
- ArgumentMarshaler 클래스를 abstract로 변경시킴
- value 변수, set 함수, get 함수를 타입별 ArgumentMarshaler 구현체로 코드 이동
- 기존 코드를 BooleanArgumentMarshaler 클래스에 있는 get, set을 사용할 수 있도록 코드 수정한 후 기존에 ArgumentMarshaler에 있던 booleanValue, getBoolean 함수를 제거한다.
  + 신규 코드로 모두 변경하고 이상이 없을 때 기존 코드를 제거한다.
- String, Int 역시 동일하게 수정 진행

#### 4. 타입별 Map 만들었던 것 하나로 통합
- 기존에 타입별로 만든 Map을 바로 지우면 시스템이 너무 많이 깨지기 때문에 하나로 통합할 Map을 먼저 만들고, 기존 코드를 통합할 Map으로 모두 수정한 후, 기존 타입별 Map을 제거한다.
  + 이래야만 더욱 시스템이 안전하다.
- Boolean 부터하고 String, Int 순서대로 동일하게 진행

#### 5. Args 클래스의 setXXX 함수를 타입별 ArgumentMarshaler 구현체로 이동
- Args 클래스에서 setArgument 메서드에서 타입을 일일히 분석하고 setXXX 함수로 이동시켜 값을 세팅하는 코드가 너무 아쉽다.
- 이를 Args 없애기 위해서는 타입별 ArgumentMarshaler 구현체에서 역할을 이동시켜야 한다.
  + 기존에 setArgument 메서드에서 타입을 분리하던 코드도 제거해도 된다.
    * Argumentmarshaler에서 만들어둔 추상 메서드를 타입별 ArgumentMarshaler 구현체에서 구현하고 있기 때문이다.
    * set 함수를 호출하면 각 타입별로 저절로 가겠죠?
- 그렇게 하려면 Args 클래스의 args, currentArgument 필드를 넘겨야 하는데 인수를 두개 넘기면 지저분해진다.
- 따라서 args 배열을 list로 변환 후 Iterator 형식으로 전달하면 기존 args, currentArgument 필드를 한개로 통합시킬 수 있다.
  + hasNext, next 함수들이 이런 역할을 한다.
  + 이런 생각을 어떻게 할 수 있을까... 다양한 자료구조를 사용해보고 상황에 따라 적합한걸 생각해내야 하는구나
- 리팩터링을 하다보면 코드를 넣었다가 뺐다 하는 사례가 아주 흔하다.
- 단계적으로 조금씩 변경하며 매번 테스트를 돌려야 하므로 코드를 여기저기 옮길 일이 많아진다.
- 큰 목표 하나를 이루기 위해 자잘한 단계를 수없이 거친다.

#### 6. ArgumentMarshaler 클래스를 인터페이스로 변환
- 기존 구현체들 코드에서 extends를 implement로 바꿔주면 되겠죠

#### 7. ArgsException 클래스 생성 후 하나로 통합
- ArgsException 모듈은 잡다한 오류 지원 코드가 들어갈 합당하고 당연한 장소다.
- Exception 사용하던 코드를 ArgsException으로 수정

#### 8. 기존에 작성된 테스트 코드들 한번씩 싹 돌리기
- 리팩터링 후 시스템이 안전한지 확인해야징!

### 리팩터링한 구조에서 새로운 타입 유형 추가
1. Args 클래스의 parseSchemaElement 함수에 타입 유형을 나누는 Case 추가
2. ArgumentMarshaler 클래스를 구현하는 유형 클래스 추가
3. 신규 ErrorCode 추가
4. Args 클래스에 getXXX 함수 추가
5. 신규 타입에 대한 테스트 코드 수정 및 추가 작성

### 리팩터링 후 최종 소감
- 기존에 Args 클래스에 있던 모든 로직을 ArgumentMarshaler, ArgsException 클래스로 모두 역할에 맡게 옮겼다.
- 소프트웨어 설계는 분할만 잘해도 품질이 크게 높아진다.
- 관심사를 분리하면 코드를 이해하고 보수하기 훨씬 더 쉬워진다.

### 결론
- 단순히 돌아가는 코드에 만족하는 프로그래머는 전문가 정신이 부족하다.
- 나쁜 코드보다 더 오랫동안 더 심각하게 개발 프로젝트에 악영향을 미치는 요인도 없다.
- 코드가 썩어가며 모듈은 서로서로 얽히고설켜 뒤엉키고 숨겨진 의존성이 수도 없이 생긴다.
- 코드는 언제나 최대한 깔끔하고 단순하게 정리하자.
