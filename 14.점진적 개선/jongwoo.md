# 점진적 개선

## Args
- Args의 사용법
  - Args 생성자에 인수 문자열과 형식 문자열을 넘겨 Args 인스턴스를 생성
  - Args인스턴스에 인수값을 질의

``` 
val arg = Args("l, p#, d*", args)
val logging = arg.getBoolean('l')
val port = arg.getInt('p')
val directory = arg.getString('d')
```

- 첫째 매개변수는 형식 또는 스키마를 지정하는 "l, p#, d*"이다.
- 첫번째 l은 불린 인수다.
- 두번째 p# 은 정수 인수다.
- 세번째 d* 은 문자열 인수다.

## Args 구현
- 장황한 언어인 자바를 사용한 탓에 단순한 개념을 구현하는데 코드가 너무많이 필요하다.
- 새로운 인수 유형을 추가하는데 방법이 명백하다.
- 오류 메시지가 필요할 경우, ArgsException.ErrorCode를 만들고 오류 메시지를 추가하면 끝이다.

## 어떻게 짰냐고?
- 깨끗한 코드를 짜려면 지저분한 코드를 짠뒤에 정리를 해야한다.
- 작문할때 초안부터 작성한뒤, 고치고 또 고쳐야 최선의 결과물인 최종안이 탄생되는것과 비슷한 논리이다.
- 대다수 신참 프로그래머는 코드를 짤때 '돌아가는'코드가 나오면 바로 다음 업무로 가버린다. 이는 프로그래머로써 자살행위나 다름없다.

## Args 1차 초안
- 명백한 미완성코드
  - 인스턴스 변수 개수
  - "TILT"와 같은 희한한 문자열
  - HashSet과 TreeSet, try-catch-catch블록
- 처음부터 이런식으로 짜려는 의도는 없었다고 한다. 하지만 진행하다 보니 어느순간 프로그램은 저자의 손을 벗어났다.
- 심지어 후에는 String과 Integer라는 인수 유형을 추가해줬을 뿐인데 코드가 엄청나게 지저분해졌다. 유지와 부수가 적당히 수월했던 코드가 버그와 결함이
 숨어있을 지도 모른다는 상당히 의심스러운 코드로 뒤바뀌어버렸다.

## 그래서 멈췄다
- 추가할 인수 유형이 적어도 2개는 더있었지만 그러면 코드가 훨씬 더 나빠지리라는 사실이 자명했다.
- 코드 구조를 유지보수하기 좋은 상태로 만들려면 지금이 적기라 판단했다.
- 새 인수 유형을 추가하려면 주요 지점 3곳에다 코드를 추가해야한다.
  - 인수 유형에 해당하는 HashMap을 선택하기 위해 스키마 요소의 구문을 분석.
  - 명령행 인수에서 인수 유형을 분석해 진짜 유형으로 변환
  - getXXX 메서드를 구현해 호출자레게 진짜 유형을 반환

- 인수유형을 다양하지만 모두가 유사한 메서드를 제공하므로 클래스 하나가 적합하다 판단, 그래서 ArgumentMarshaler라는 개념이 탄생

## 점진적으로 개선하다
- 프로그램을 망치는 가장 좋은 행위는 개선이라는 이름 아래 구조를 크게 뒤집는 행위이다.
- 그래서 TDD(test-Driven Development, 테스트 주도 개발)라는 기법을 사용한다.
- TDD는 언제 어느때라도 시스템이 돌아가야 한다는 원칙을 따른다.
- TDD는 시스템을 망가뜨리는 변경을 허용하지 않는다.
- 시스템 구조를 개선할 때마다 조금씩 ArgumentMarshaler 개념에 가까워졌다.

#### ArgumentMarshaler 추가
- Boolean인수를 지정하는 HashMap에서 Boolean 인수 유형을 ArgumentMarshaler 유형으로 바꿨다.
- getBoolean함수에서 falseIfNull를 제거했고, falseIfNull함수 자체도 삭제했다.
- 함수를 두 행으로 쪼갠 후 ArgumentMarshaler를 argumentMarshaler라는 독자적인 변수에 저장했다.
- 변수이름 am으로 줄인 후 null점검

#### String 인수 추가
- String인수를 추가하는 과정은 HashMap을 변경한 후 parse, get, set함수를 고치는 과정으로 boolean인수와 유사했다.

#### ArgumentMarshaler 파생클래스 생성 후 코드 분리
- int 인수 기능을 ArgumentMarshaler로 옮김
- setBoolean함수를 BooleanArgumentMarshaler 로 옮긴 후 함수가 올바로 호출되는지 확인
- ArgumentMarshaler 클래스에 추상 메서드 set 제작
- BooleanArgumentMarshaler 클래스에 set 메서드 제작
- setBoolean 호출을 set 호출로 변경

#### 필요없는 Map제거 (ArgumentMarshaler로 맵을 만들어 원래 맵을 교체한뒤 관련 메서드 변경)
#### set 함수를 해당 ArgumentMarshaler 파생클래스로 내리기
#### set 함수를 모두 추상 메서드로 호출
#### 모든 예외를 하나로 모아 ArgsException 클래스를 만든 후 독자 모듈로 옮기기

## 결론
- 단순히 돌아가는 코드에 만족하는 프로그래머는 전문가 정신이 부족하다
- 나쁜코드보다 더 오랫동안 더 심각하게 개발 프로젝트에 악영향을 미치는 요인도 없다.
- 나쁜코드를 깨끗한 코드로 개선하기 위해서는 비용이 엄청나게 많이 든다.
- 코드는 언제나 최대한 깔끔하고 단순하게 정리하자. 절대로 썩어가게 방치하면 안된다
