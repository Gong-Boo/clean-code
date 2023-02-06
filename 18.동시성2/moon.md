# 동시성II

## 클라이언트/서버 예제

 - 서버는 소켓을 열어놓고 클라이언트가 연결하기를 기다린다. 클라이언트는 소켓에 연결해 요청을 보낸다.

### 서버
``` java 
ServerSocket serverSocket = new ServerSocket(8009);

while (keepProcessing)
  try {
    Socket socket = serverSocket.accept();
    process(socket);
  } catch (Exception e) {
    handle(e);
  }
}
```

### 클라이언트
``` java
private void connectSendReceive(int i) {
  try {
    Socket socket = new Socket("localhost", PORT);
    MessageUtils.sendMessage(socket, Integer.toString(i));
    MessageUtils.getMessage(socket); socket.close
  } catch (Excption e) {
    e.printStackTrace();
  }
}
```
### 테스트
``` java
@Test(timeout = 10000)
public void shouldRunInUnder10Seconds() throws Exception {
  Thread[] threads = createThreads();
  startAllThreads(threads);
  waitForAllThreadsToFinish(threads);
}
```
- 이 테스트 케이는 테스트가 10,000밀리초 내에 끝나는지를 검사한다.
- 시스템이 이련의 클라이언트 요청을 10초 내에 처리해야 한다는 의미다.
- 서버가 각 클라이언트 요청을 적절한 시간 내에 처리하면 시스템은 테스트를 통과한다.
- 테스트를 실패한다면 먼저 애플리케이션이 어디서 시간을 보내는지 알아야한다. 가능성은 두 가지다.

- 1. I/O - 소켓 사용, 데이터베이스 연결, 가상 메모리 스와핑 기다리기 등에 시간을 보낸다.
- 2. 프로세서 - 수치 계산, 정규 표현식 처리, 가비지 컬렉션 등에 시간을 보낸다.

- 대개 시스템은 둘 다 하느라 시간을 보내지만, 특정 연산을 살펴보면 대개 하나가 지배적이다.
- 만약 프로그램이 주로 프로세서 연산에 시간을 보낸다면, 새로운 하드웨어를 추가해 성능을 높여 테스트를 통과하는 방식이 적합하다.
- 프로세서 연산에 시간을 보내는 프로그램은 스레드를 늘인다고 빨라지지 않는다. CPU사이클은 한계가 있기 때문이다.

- 반면 프로그램이 주로 I/O 연산에 시간을 보낸다면 동시성이 성능을 높여주기도 한다.
- 시스템 한쪽이 I/O를 기다리는 동안에 다른 쪽이 뭔가를 처리해 노는 CPU를 효과적으로 활용할 수 있다.

### 스레드 추가하기
- 여기서 성능 테스트가 실패했다고 가정하자. 그렇다면 다음처럼 스레드를 추가한다.
``` java
void process(final Socket socket) {
  if (socket == null)
    return;
  
  Runnable clientHandler = new Runnable() {
    public void run() {
      try {
        String message = MessageUtils.getMessage(socket);
        MessageUtils.sendMessage(socket, "Processed: " + message);
        closeIgnoringException(socket);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };
  Thread clientConnection = new Thread(clientHandler);
  clientConnection.start();
}
```

### 서버 살펴보기
- 위에서 고친 서버는 대략 1초 만에 성능 테스트를 완료한다. 하지만 다른 새로운 문제를 일으킨다.
- 너무 많은 사용자가 한꺼번에 몰린다면 시스템이 동작을 멈출지도 모른다.
- 하지만 동작 문제는 잠시 미뤄두자. 새로 고친 서버는 깨끗한 코드와 구조라는 관점에서도 문제가 있다. 서버 코드가 지는 책임이 몇 개일까?

- 1. 소켓 연결 관리
- 2. 클라이언트 처리
- 3. 스레드 정책
- 4. 서버 종료 정책

- 이 모든 책임은 process 함수가 진다. 확실히 분할할 필요가 있다.
- 서버 프로그램은 고칠 이유가 여럿이다. 즉, 단일 책임 원칙을 위반한다.
- 다음은 분할한 서버 코드다.

``` java
public void run() {
  while (keepProcessing) {
    try {
      ClientConnection clientConnection = connectionManager.awaitClient();
      ClientRequestProcessor requestProcessor = new ClientRequestProcessor(clientConnection);
      clientScheduler.schedule(requestProcessor);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  connectionManager.shutdown();
}
```

- 이제 스레드와 관련한 코드는 모두 한 곳에 위치한다. 그러니 서버에 동시성 문제가 생긴다면 한곳만 보면 된다.
- 또한 스레드를 제어하는 동시성 정책을 바꾸기도 쉬워진다.

### 클라이언트/서버 예제 결론
- 이 예제는 단일스레드 시스템을 다중 스레드 시스템으로 변환해 시스템 성능을 높이는 방법과 테스트 프레임워크에서 시스템 성능을 검증 하는 방법을 소개했다.
- 동시성과 관련한 코드를 몇몇 클래스로 집중해 단일 책임 원칙도 지켰다.
- 동시성은 그 자체가 복잡한 문제이므로 다중 스레드 프로그램에서는 단일 책임 원칙이 특히 중요하다.

## 가능한 실행 경로
- 다음 incrementValue 메서드를 살펴보자.
 
``` java
public class IdGenerator() {
  int lastIdUsed;
  
  public int incrementValue() {
    return ++lastIdUsed;
  }
}
```

- 정수 오버플로는 무시한다. 또한 스레드 하나가 IdGenerator 인스턴스 하나를 사용한다고 가정한다. 그렇다면 가능한 실행 경로는 단하나다. 결과도 단 하나다.
- 반환값은 lastIdUsed 값과 동일하다. 두 값 모두 메서드를 호출하기 전보다 1이 크다.
- lastIdUsed 초기값을 93으로 가정할 때 가능한 결과는 다음과 같다.
 
- 1. 스레드 1이 94를 얻고, 스레드 2가 95를 얻고, lastIdUsed가 95가 된다.
- 2. 스레드 1이 95를 얻고, 스레드 2가 94를 얻고, lastIdUsed가 95가 된다.
- 3. 스레드 1이 94를 얻고, 스레드 2가 94를 얻고, lastIdUsed가 94가 된다.

### 경로 수
- 가능한 경로 수는 9개에 해당한다.
- 루프나 분기가 없는 명령 N개를 T개가 차례로 실행한다면 경로수는  (NT)!/N!^T과 같다
- 우리가 예저로 사용한 자바 코드 한 줄은 N=8이고 T=2이다. 게계산하면 12870개다.
- 만약 lastIdUsed가 long정수라면 2704156개로 늘어난다.

``` java
public synchronized void incrementValue() {
  ++lastIdUsed;
}
```

- 위에 코드로 변경한다면 가능한 경로 수는 N!이다.

### 심층 분석
- 우리는 중단이 불가능한 연산을 원자적 연산으로 정의한다.
- 예를 들어, 다음 코드에서 lastid에 0을 할당하는 연산은 원자적이다.
- 자바 메모리 모델에 의하면 32비트 메모리에 값을 할당하는 연산은 중단이 불가능하기 때문이다.
- 하지만 lastId를 int에서 long으로 바꾼다면 JVM명세에 따라 원자적 연산이 아니다.
- JVM 명세에 따르면 64비트 값을 할당하는 연산은 32비트 값을 할당하는 연산 두 개로 나눠진다.
- 그러니까 첫 32비트 값을 할당한 직후에 그리고 둘째 32비트 값을 할당하기 직전에 다른 스레드가 끼어들어 두 32비트 값 중 하나를 변경할 수 있다는 의미다.
- 전처리 증가 연산자는 원자적 연산이 아니다.

- 프레임 : 모든 메서드 호출에는 프레임이 필요하다. 프레임은 반환 주소, 메서드로 넘어온 매개변수, 메서드가 정의하는 지역변수를 포함한다. 프레임은 호출 스택을 정의할 때 사용하는 표준 기법이다.
- 지역 변수 : 정적 메서드를 제외한 모든 메서드는 기본적으로 this 라는 지역 변수를 갖는다. this는 현재 객체다. 
- 피연산자 스택 : JVM의 지원하는 명령 대다수는 매개변수를 받는다. 피연산자 스택은 이런 매개변수를 저장하는 장소다. 피연산자가 스택은 표준 LIFO 자료 구조다.

- ALOAD 0, ICONST_0, PUTFIELDlastId는 확실히 원자적이다.
- 만약 위 명령 3개를 스레드 10개가 실행한다면 가능한 경로 수는 4.38679733629e+24개다.
- 하지만 여기서 가능한 결과는 단 하나이므로 경로 수는 중요하지 않다.
- 스레드 10개가 모두 상수 값을 할당하기 때문에 스레드가 서로 간섭을 하더라도 최종 결과는 마찬가지다.
- 하지만 getNextId 메서드에서 ++ 연산이 문제를 일으킨다.
- 이를 해결하기 위해서는 메서드를 synchronized로 선언하면 문제는 해결된다.

### 결론
- 스레드가 서로의 덮어쓰는 과정을 이해하기 위해 바이트 코드를 속속들이 이해할 필요는 없다.
- 여러 스레드가 서로 훼방을 놓는 시나리오가 어느정도 그려지는 정도면 충분하다.
- 그렇기에 메모리 모델을 이해하고 있어야 한다. ++연산은 분명히 원자적 연산이 아니다. 즉 다음을 알아야 한다.

1. 공유 객체/값이 있는 곳
2. 동시 읽기/수정 문제를 일으킬 소지가 있는 코드
3. 동시성 문제를 방지하는 벙법
