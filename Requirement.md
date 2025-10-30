# 팩맨 게임 개선 과제 요구사항

## 📋 과제 개요
- 기존 팩맨 게임의 기능 확장 및 설계 개선
- 설계패턴 및 객체지향 설계 개념 적극 활용
- 기존 게임 기능은 여전히 동작해야 함
- JUnit 테스트케이스 작성 및 문서화 필수

---

## 🎯 추가할 기능

### 1. 폭탄 시스템 💣

#### 기본 사양
- **새로운 엔티티**: Bomb
- **상태 전환**: 3 → 2 → 1 → 폭발
- **초기 타이머**: 5초 후 폭발
- **난이도 연동**: 레벨이 오를수록 폭발 시간 단축 (5초 → 4초 → 3초 → ... → 1초)

#### 생성 규칙
- 난이도에 따라 스폰 빈도 증가
- 맵 상에 랜덤하게 생성

#### 충돌 효과
| 대상 | 효과 |
|------|------|
| Pacman + 폭발 상태 폭탄 | Game Over (즉시 사망) |
| Ghost + 폭발 상태 폭탄 | EatenMode로 전환 (유령 상태) |
| Pacman + 폭발 전 폭탄 | 효과 없음 |

---

### 2. 난이도 시스템 📈

#### 난이도 증가 조건
- 점수 기준 자동 레벨업
- 예시: 1000점마다 레벨 1씩 증가

#### 난이도별 변화
- **폭탄 스폰 빈도** ↑
- **폭탄 폭발 속도** ↑ (5초 → 1초)
- **유령 속도** ↑ (선택사항)

---

### 3. 파워업 아이템 시스템 ⚡

#### 추가할 아이템 (최소 3종)
1. **SpeedBoost** - Pacman 이동 속도 증가 (일정 시간)
2. **Shield** - 폭탄 공격 무적 (일정 시간)
3. **DoubleScore** - 획득 점수 2배 (일정 시간)

#### 생성 규칙
- 맵 상의 특정 위치에 배치
- 먹으면 효과 발동 및 사라짐

---

### 4. 게임 재시작 & 점수 대쉬보드 🎮

#### 게임 오버 후 기능
- **Restart 버튼** - 게임 재시작
- **점수 대쉬보드** - 현재 점수 및 하이스코어 표시
- **종료 버튼** - 게임 종료

#### 점수 관리
- 하이스코어 파일로 저장/로드
- 게임 기록 유지 (최고 점수, 플레이 횟수 등)

---

## 🏗️ 적용할 디자인 패턴

### 패턴별 적용 계획

| 패턴 | 적용 위치 | 목적 | 난이도 |
|------|---------|------|--------|
| **State** | 폭탄 상태 관리 | BombState3 → BombState2 → BombState1 → ExplodedState | ⭐⭐⭐ |
| **Decorator** | 파워업 아이템 | SpeedBoostDecorator, ShieldDecorator, DoubleScoreDecorator | ⭐⭐⭐⭐ |
| **Singleton** | 게임 관리자 | ScoreManager, DifficultyManager | ⭐⭐⭐⭐⭐ |
| **Factory** | 폭탄 생성 | BombFactory (난이도별 타이머 설정) | ⭐⭐⭐⭐ |
| **Template Method** | 난이도 적용 로직 | DifficultyLevel 추상 클래스 | ⭐⭐⭐ |
| **Facade** | UI 메뉴 시스템 | GameMenuFacade (시작/재시작/점수판 통합) | ⭐⭐⭐ |

---

## 📐 Template Method 패턴 상세 설계

### 옵션 A: 난이도별 게임 설정 템플릿 (권장)

```java
abstract class DifficultyLevel {
    // Template Method
    public final void applyDifficulty() {
        setBombSpawnRate();      // 폭탄 스폰 간격 설정
        setBombExplosionSpeed(); // 폭탄 폭발 시간 설정
        setGhostSpeed();         // 유령 속도 설정
        applySpecialRules();     // Hook method (선택)
    }

    protected abstract void setBombSpawnRate();
    protected abstract void setBombExplosionSpeed();
    protected abstract void setGhostSpeed();
    protected void applySpecialRules() {} // Hook
}

class EasyLevel extends DifficultyLevel { ... }
class MediumLevel extends DifficultyLevel { ... }
class HardLevel extends DifficultyLevel { ... }
class ExtremeLevel extends DifficultyLevel { ... }
```

### 옵션 B: Entity 업데이트 라이프사이클

```java
abstract class Entity {
    // Template Method
    public final void updateEntity() {
        preUpdate();   // Hook: 전처리 (충돌 체크 등)
        doUpdate();    // 핵심 업데이트 로직
        postUpdate();  // Hook: 후처리 (애니메이션 등)
    }

    protected void preUpdate() {}
    protected abstract void doUpdate();
    protected void postUpdate() {}
}
```

---

## 🧪 테스트 요구사항

### JUnit 테스트 작성 대상

1. **State 패턴 테스트**
   - 폭탄 상태 전환 검증
   - 폭발 시 충돌 효과 검증

2. **Decorator 패턴 테스트**
   - 각 파워업 효과 검증
   - 효과 중첩 검증

3. **Singleton 패턴 테스트**
   - 인스턴스 유일성 검증
   - 점수 저장/로드 검증

4. **Factory 패턴 테스트**
   - 난이도별 폭탄 생성 검증

5. **Template Method 패턴 테스트**
   - 난이도별 설정 적용 검증

6. **Facade 패턴 테스트**
   - 메뉴 전환 검증

---

## 📝 문서화 요구사항

### 작성할 문서

1. **패턴 적용 정당성 문서** (PatternJustification.md)
   - 각 패턴을 선택한 이유
   - 다른 패턴 대신 선택한 이유
   - 적용 전후 비교

2. **설계 개선 문서** (DesignImprovement.md)
   - 기존 설계의 문제점
   - 개선 방안
   - 적용 결과

3. **테스트 문서** (TestReport.md)
   - 테스트 케이스 설명
   - 테스트 결과
   - 커버리지

---

## ✅ 구현 체크리스트

### Phase 1: 핵심 시스템 구축
- [ ] Singleton: ScoreManager 구현
- [ ] Singleton: DifficultyManager 구현
- [ ] Template Method: DifficultyLevel 추상 클래스 및 구체 클래스 구현

### Phase 2: 폭탄 시스템
- [ ] State: BombState 인터페이스/추상 클래스 구현
- [ ] State: BombState3, BombState2, BombState1, ExplodedState 구현
- [ ] Factory: BombFactory 구현
- [ ] Entity: Bomb 클래스 구현
- [ ] 충돌 처리: Pacman, Ghost와의 상호작용 구현

### Phase 3: 파워업 시스템
- [ ] Decorator: PacmanDecorator 추상 클래스 구현
- [ ] Decorator: SpeedBoostDecorator 구현
- [ ] Decorator: ShieldDecorator 구현
- [ ] Decorator: DoubleScoreDecorator 구현
- [ ] 새 아이템 엔티티 추가

### Phase 4: UI 시스템
- [ ] Facade: GameMenuFacade 구현
- [ ] UI: 재시작 버튼 추가
- [ ] UI: 점수 대쉬보드 추가
- [ ] 파일 I/O: 점수 저장/로드 구현

### Phase 5: 테스트 & 문서화
- [ ] JUnit 테스트 작성 (각 패턴별)
- [ ] PatternJustification.md 작성
- [ ] DesignImprovement.md 작성
- [ ] TestReport.md 작성
- [ ] README.md 업데이트

---

## 🎓 평가 기준

1. **설계 패턴 적용의 적절성** (30%)
   - 패턴 선택의 타당성
   - 올바른 구현

2. **기능 확장** (30%)
   - 요구사항 충족도
   - 기존 기능 유지

3. **테스트** (20%)
   - 테스트 커버리지
   - 테스트 케이스 품질

4. **문서화** (20%)
   - 설계 결정 설명
   - 코드 가독성
