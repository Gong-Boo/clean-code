# 객체와 자료 구조
> Clean Code Chapter 06.


## 자료 추상화
| 직교좌표계 | 극좌표계 |
| --- | --- |
| <img width="150" src="https://user-images.githubusercontent.com/50200481/201666113-4d8d688a-0e5b-4587-9bee-801b477b3d28.png"> | <img width="150" src="https://user-images.githubusercontent.com/50200481/201665657-193c3ce7-2114-4319-9015-cb55643be281.png"> |

<img width="350" src="https://user-images.githubusercontent.com/50200481/201663471-fea7b477-72b4-4331-b3a2-3f4c0e76c8c0.png">

> 코드 이미지 출처: Clean Code 도서

- 목록 6-1: 구현을 외부에 노출
  - 확실히 직교좌표를 사용함
  - 개별적으로 좌표값을 읽고 설정하게 강제함
  - private로 선언하고 set, get 함수를 제공하는 것보다 추상 인터페이스를 제공하여 사용자가 구현을 모른 채 조작할 수 있어야 진정한 클래스임
  
- 목록 6-2: 구현을 완전히 숨김
  - 직교좌표인지 극좌표지인 알 수 없으나 자료구조를 명백히 표현 
  - 메세드가 접급 정책을 강제 하여, 좌표를 읽을 때 각 값을 개별적으로 읽어여 하나, 설정할 때는 한꺼번에 설정함

---

<img width="350" src="https://user-images.githubusercontent.com/50200481/201668050-523c1a91-8768-4238-8f2f-f609b49f90fd.png">

> 코드 이미지 출처: Clean Code 도서

- 목록 6-3: 구체적인 개념
  - 자동차 연료 상태를 구체적인 숫자 값을 알려줌 

- 목록 6-4: 추상적인 개념
  - 자동차 연료 상태를 백분율이라는 추상적인 개념으로 알려줌

---


#### 결론

- 자료를 세세하게 공개하기보다는 추상적인 개념으로 표현하도록
- 인터페이스나 set, get 함수만으로는 추상화가 이뤄지지 않음


