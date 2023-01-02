# Junit 들여다보기

## Junit 프레임워크

- ComparisonCompactor는 두 문자열을 받아 차이를 반환
- 예) ABCDE, ABXDE를 받아 <...B[X]D...>를 반환

> /src/main/ComparisonCompactorTest.java
- 위 테스트 케이스로 original.ComparisonCompactor 모듈에 대한 코드 커버리지 분석을 수행했더니 100%가 나옴
- 테스트 케이스가 모든 행, 모든 if문, 모든 for문을 실행한다는 의미임

> /src/main/original/ComparisonCompactor.java (원본)
- 코드는 잘 분리되었고, 표현력이 적절하며, 구조가 단순한 전반적으로 훌륭한 모듈
- 저자들이 모듈을 아주 좋은 상태로 남겨두었지만, 보이스카우트 규칙에 따라 처음보다 더 깨끗하게 해놓고 떠나야 함
- 어떻게 개선할 수 있을까?
