# JUnit 들여다보기

### JUnit 프레임워크
- Com-parisonCompactor모듈은 문자열 비교오류를 파악할때 유용하다
- 두 문자열을 받아 차이를 반환한다.
- 보이스카우트 규칙에 따르면 처음 왔을때보다 더 깨끗하게 해놓고 떠나야 한다.

#### Com-parisonCompactor 리펙토링
- 접두어 f 모두 제거
- 조건문 캡슐화
- findCommonSuffix를 살펴보면 숨겨진 시각적인 결합(Hidden Temporal Coupling)이 존재한다
- findCommon함수들 이름 변경하기
- findCommon함수들 정리하기
- suffixLength 수정
- CompactString 구조 다듬기

### 결론
- 코드를 리펙토링 하다보면 원래했던 변경을 되돌리는 경우가 흔하다
- 세상에 개선이 불필요한 모듈은 없다. 
- 코드를 처음보다 더 깨끗하게 만드는 책임은 우리 모두에게 있다 
