# 점진적인 개선
> 명령형 인수 구문분석기 사례 연구

<img width="500" src="https://user-images.githubusercontent.com/50200481/209465840-3f5430b6-ad47-4987-89dc-3ec6dda64ff7.png">

- **Args 클래스**
  - 생성자에 **형식 문자열**과 **인수 문자열**을 넘겨 Args 인스턴스를 생성
  - 첫번째 매개변수: 형식 또는 스키마를 지정하는 "l,p#,d*"
    - -l: Boolean
    - -p: Int
    - -d: String 
  - 두 번째 매개변수: main으로 넘어온 명령행 인수 배열 자체
