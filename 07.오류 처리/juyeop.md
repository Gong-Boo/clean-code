## 오류 처리
- 잘못될 가능성은 늘 존재한다.
- 책임은 바로 우리 프로그래머에게 있다.
- 여기저기 흩어진 오류 처리 코드 때문에 실제 코드가 하는 일을 파악하기가 어렵다.
- 오류 처리 코드로 인해 프로그램 논리를 이해하기 어려워진다면 깨끗한 코드라 부르기 어렵다.

---

### 1. 오류 코드보다 예외를 사용하라
Before: 오류 코드 방식
``` kotlin
val jongWoo = getJongWoo()

if (jongWoo != UNDIFINED) {
  if (jongWoo.getHeadSize() == CANT_MEASURE) {
    println("종우 CANT_MEASURE")
  } else {
    if (jongWoo.getDrinkAlcoholLevel() == BASIC_LEVEL) {
      println("종우 BASIC_LEVEL")
    }
  }
} else {
  println("종우 UNDIFINED")
}
```
- 예외를 지원하지 않는 언어는 오류 플래그를 설정하거나 호출자에게 오류 코드를 반환하는 방법이 전부였다.
- 함수를 호출한 즉시 오류를 확인해야 해서 호출자 코드가 복잡해진다.

After: 예외 처리 방식
``` kotlin
try {
  jongWoo.getHeadSize()
  jongWoo.getDrinkAlcoholLevel()
} catch(e: UNDIFINED) {
  println("종우 undifined")
} catch(e: CANT_MEASURE) {
  println("머리 크기 측정 불가")
} catch(e: BASIC_LEVEL) {
  println("종우 주량 초짜")
}
```
- 오류가 발생했을 때 예외를 던지면 호출자 코드가 더 깔끔해지고 논리와 오류 처리 코드가 섞이지 않는다.

---

### 2. Try-Catch-Finally 문부터 작성하라
- try-catch-finally 문에서 try 블록에 들어가는 코드를 실행하면 어느 시점에서든 실행이 중단된 후 catch 블록으로 넘어갈 수 있다.
- try 블록에서 무슨 일이 생기든지 catch 블록은 프로그램 상태를 일관성 있게 유지해야 한다.

#### 논의 해보면 좋을 질문 🙋‍♂️
- catch 블록을 정의할 때 Exception 클래스를 사용하면 좋을까 아니면 세부적인 Exception 클래스를 사용하면 좋을까?

1번
``` kotlin
try {
  val stream = FileInputStream("")
  strea.close()
} catch(e: Exception) {
  println(e)
}
```

2번
``` kotlin
try {
  val stream = FileInputStream("")
  strea.close()
} catch(e: FileNotFoundException) {
  println(e)
}
```

---

### 3. 미확인 예외를 사용하라
- 확인된 예외는 메서드를 선언할 때 메서드가 반환할 예외를 모두 열거했다.
- 확인된 오류가 치르는 비용에 상응하는 이익을 제공하는지 따져봐야 한다.
- 확인된 예외는 OCP(Open Closed Principle 개방 폐쇄의 원칙)를 위반한다.
- 최하위 단계에서 최상위 단계까지 연쇄적인 수정이 일어난다.
- throws 경로에 위치하는 모든 함수가 최하위 함수에서 던지는 예외를 알아야 하므로 캡슐화가 깨진다.
- 오류를 원거리에서 처리하기 위해 예외를 사용한다는 사실을 감안한다면 이처럼 확인된 예외가 캡슐화를 깨버리는 현상은 참으로 유감스럽다.
- 일반적인 애플리케이션은 의존성이라는 비용이 이익보다 크다.

![image](https://user-images.githubusercontent.com/49600974/202837155-3367f473-badf-400e-b5df-bd764e97e7ca.png)

- 확인된 예외와 미확인 예외의 차이는 컴파일에서 사전에 확인을 하는지 또는 확인 하지 않는지에 따라 구분된다.

확인된 예외를 throws로 처리하는 방식
``` java

public void a() throws FileNotFoundException {
  b();
}

public void b() throws FileNotFoundException {
  c();
}

public void c() throws FileNotFoundException {
  error();
}

public void error() throws FileNotFoundException {
  throw new FileNotFoundException("파일 찾을 수 없음");
}
```

확인된 예외를 catch로 처리하는 방식
``` java

public void a() {
  b();
}

public void b() {
  c();
}

public void c() {
  try {
    error();
  } catch(FileNotFoundException e) {
    println(e)
  }
}

public void error() throws FileNotFoundException {
  throw new FileNotFoundException("파일 찾을 수 없음");
}
```

---

### 4. 예외에 의미를 제공해라
- 예외를 던질 때 전후 상황을 전달하면 오류가 발생한 원인과 위치를 찾기가 쉬워진다.
- catch 블록에서 오류를 기록하도록 충분한 정보를 넘겨준다.

---

### 5. 호출자를 고려해 예외 클래스를 정의해라
- 오류를 분류하는 방법은 수없이 많다.
- 애플리케이션에서 오류를 정의할 때 프로그래머에게 가장 중요한 관심사는 오류를 잡아내는 방법이 되어야 한다.

Before: 예외 케이스 별로 catch 모두 정의
``` kotlin

val moon = getMoon()

try {
  moon.start()
} catch(e: UglyException) {
  println(e)
} catch(e: AngryException) {
  println(e)
} catch(e: LoveException) {
  println(e)
}
```

After: Wrapper 클래스 방식
``` kotlin
val moon = getMoon()

try {
  moon.start()
} catch(e: MoonException) {
  println(e)
}
```
- Wrapper 클래스를 만들어서 예외처리를 진행하면 외부 라이브러리와 프로그램 사이의 의존성이 크게 줄어든다.
- 외부 라이브러리의 설계 방식이 마음에 들지 않아도 우리가 사용하기 편하게 재정의하면 된다.
- 상황에 따라 예외 클래스 종류를 하나로 할지 여러개로 할지 구분해서 관리하면 된다.

---

### 6. 정상 흐름을 정의하라
before: catch 구문에서 추가적인 논리 작업을 하는 방식
``` kotlin
try {
  val jinDong = getJinDong()
  totalHeadSize += jinDong.getHeadSize()
} catch(e: OutOfMemory) {
  totalHeadSize += averageHeadSize
}
```
- 예외에서 하는 추가적인 논리가 이해를 어렵게 만든다.

after: 클래스나, 객체에서 논리를 처리하는 방식
``` kotlin
val jinDong = getJinDong()
totalHeadSize += jinDong.getHeadSize()
```
- getHeadSize 메서드 자체에서 기본 값을 제공하도록 수정한다.
- 이를 특수 사례 패턴이라 부르며 클래스를 만들거나 객체를 조작해 특수 사례를 처리하는 방식이다.
- 클라이언트 코드가 예외적인 상황을 처리하지 않고 클래스나 객체가 예외적인 상황을 캡슐화해서 처리하도록 한다.

---

### 7. null을 반환하지 마라
- 메서드에서 null을 반환하고픈 유혹이 든다면 그 대신 예외를 던지거나 특수 사례 객체를 반환한다.
- 사용하려는 외부 API가 null을 반환한다면 감싸기 메서드를 구현해 예외를 던지거나 특수 사례 객체를 반환하는 방식을 고려한다.
- Collection에서 null 대신 emptyList를 반환해라.

---

### 8. null을 전달하지 마라
- assert 문을 사용하는 방법도 있다.

``` java
public int test(int a, int b) {
    assert a != null : "a null 이자나"
    assert b != null : "b null 이자나"
    return a - b
}
```

- 애초에 null을 넘기지 못하도록 금지하는 정책이 합리적이다.

---

### 결론
- 깨끗한 코드는 읽기도 좋아야 하지만 안전성도 높아야 한다.
- 오류 처리를 프로그램 논리와 분리하면 튼튼하고 깨끗한 코드를 작성할 수 있고 독립적인 추론이 가능하며 코드 유지보수성도 높아진다.
