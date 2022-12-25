# 점진적인 개선

> 명령행 인수의 구문을 분석할 필요가 있을 때, 편리한 유틸리티가 없어서 직접 작성할 것을 결심   
> 새로 작성할 유틸리티를 Args라고 명명

<img width="500" src="https://user-images.githubusercontent.com/50200481/209465840-3f5430b6-ad47-4987-89dc-3ec6dda64ff7.png">

- **Args 클래스**
  - 생성자에 **형식 문자열**과 **인수 문자열**을 넘겨 Args 인스턴스를 생성
  - 첫번째 매개변수: 형식 또는 스키마를 지정하는 "l,p#,d*"
    - -l: Boolean
    - -p: Integer
    - -d: String 
  - 두 번째 매개변수: main으로 넘어온 명령행 인수 배열 자체
  - 생성자에서 ```ArgsException```이 발생하지 않는다면, 명령행 인수의 구문을 성공적으로 분석했으며 Args 인스턴스에 질의를 던져도 좋다는 의미
  - 인수 값을 가져오기 위해서는 ```getBoolean```, ```getInteger```, ```getString```등과 같은 메서드를 사용
  - 형식 문자열이나 명령행 인수 자체에 문제가 있다면 ```ArgsException```이 발생
