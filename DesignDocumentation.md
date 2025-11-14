# 설계 문서 - ScoreManager & DifficultyManager

## 1. 개요

본 문서는 팩맨 게임의 점수 관리 및 난이도 관리 시스템 설계에 대해 설명합니다. Singleton 패턴을 활용하여 중앙 집중식 관리 시스템을 구현했습니다.

## 2. 설계 목표

### 2.1 기능 요구사항
- 게임 점수의 중앙 집중식 관리
- 하이스코어 영속성 보장 (파일 저장/로드)
- 점수 기반 자동 난이도 조정
- 기존 게임 기능 유지 및 확장

### 2.2 설계 원칙
- **Single Responsibility Principle (SRP)**: 각 매니저는 단일 책임만 수행
- **Open/Closed Principle (OCP)**: 확장에는 열려있고 수정에는 닫혀있음
- **Dependency Inversion Principle (DIP)**: 고수준 모듈이 저수준 모듈에 의존하지 않음

## 3. 설계 패턴

### 3.1 Singleton Pattern

#### 3.1.1 선택 이유
```
문제: 게임 전체에서 점수와 난이도를 일관성 있게 관리해야 함
해결: Singleton 패턴을 사용하여 전역적으로 접근 가능한 단일 인스턴스 보장
```

#### 3.1.2 구현 방식
- **Thread-safe Lazy Initialization**: `synchronized` 키워드로 동시성 제어
- **Private Constructor**: 외부에서 직접 인스턴스 생성 불가
- **Static getInstance()**: 전역 접근점 제공

```java
public class ScoreManager {
    private static ScoreManager instance;

    private ScoreManager() {
        // Private constructor
    }

    public static synchronized ScoreManager getInstance() {
        if (instance == null) {
            instance = new ScoreManager();
        }
        return instance;
    }
}
```

#### 3.1.3 장점
1. **전역 접근**: 어디서든 동일한 인스턴스에 접근 가능
2. **메모리 효율성**: 단일 인스턴스로 메모리 절약
3. **일관성 보장**: 점수와 난이도 데이터의 일관성 유지
4. **제어된 초기화**: 지연 초기화로 리소스 최적화

#### 3.1.4 단점 및 해결방안
| 단점 | 해결방안 |
|------|----------|
| 테스트 어려움 | Reflection을 사용한 싱글톤 리셋 메서드 제공 |
| 멀티스레드 이슈 | `synchronized` 키워드로 동기화 |
| 강한 결합 | 인터페이스 추출로 확장 가능성 보장 |

### 3.2 Value Object Pattern (DifficultyLevel)

#### 3.2.1 선택 이유
```
문제: 난이도 레벨의 여러 속성(속도, 임계값 등)을 효과적으로 관리
해결: Value Object로 불변 객체 생성하여 데이터 무결성 보장
```

#### 3.2.2 특징
- **불변성(Immutability)**: 모든 필드가 `final`로 선언
- **equals/hashCode 구현**: 레벨 비교 가능
- **캡슐화**: 레벨 관련 모든 속성을 하나의 객체로 관리

## 4. 클래스 설계

### 4.1 ScoreManager

#### 4.1.1 책임
- 현재 점수 관리 및 갱신
- 하이스코어 파일 저장/로드 (`highscore.txt`)
- 점수 초기화 (게임 재시작)

#### 4.1.2 주요 메서드
| 메서드 | 설명 | 반환 타입 |
|--------|------|-----------|
| `getInstance()` | 싱글톤 인스턴스 반환 | ScoreManager |
| `addScore(int)` | 점수 추가 및 하이스코어 갱신 | void |
| `getCurrentScore()` | 현재 점수 조회 | int |
| `getHighScore()` | 하이스코어 조회 | int |
| `reset()` | 현재 점수 초기화 | void |
| `loadHighScore()` | 파일에서 하이스코어 로드 | int |
| `saveHighScore()` | 파일에 하이스코어 저장 | void |

#### 4.1.3 설계 결정사항
1. **파일 I/O 통합**: 별도 클래스로 분리하지 않고 ScoreManager 내부에서 처리
   - 이유: 점수 관리와 영속성이 밀접하게 관련됨
   - 장점: 응집도 향상, 간단한 구조

2. **자동 하이스코어 갱신**: `addScore()` 호출 시 자동으로 확인 및 저장
   - 이유: 사용자가 별도로 저장 호출할 필요 없음
   - 장점: 사용 편의성, 데이터 손실 방지

### 4.2 DifficultyManager

#### 4.2.1 책임
- 난이도 레벨 관리
- 점수 기반 자동 레벨업 처리
- 레벨별 게임 속성 제공

#### 4.2.2 주요 메서드
| 메서드 | 설명 | 반환 타입 |
|--------|------|-----------|
| `getInstance()` | 싱글톤 인스턴스 반환 | DifficultyManager |
| `updateLevel(int)` | 점수 기반 레벨 업데이트 | boolean |
| `getCurrentLevel()` | 현재 DifficultyLevel 반환 | DifficultyLevel |
| `getCurrentLevelNumber()` | 현재 레벨 번호 반환 | int |
| `getScoreToNextLevel(int)` | 다음 레벨까지 필요 점수 | int |
| `reset()` | 레벨 초기화 | void |

#### 4.2.3 레벨 설계

```
레벨 1 (0점):     유령 속도 1.0x - Tutorial
레벨 2 (1000점):  유령 속도 1.1x - Beginner
레벨 3 (2000점):  유령 속도 1.2x - Intermediate
레벨 4 (3000점):  유령 속도 1.3x - Advanced
레벨 5 (4000점):  유령 속도 1.5x - Expert
레벨 6 (5000점+): 유령 속도 1.7x - Master
```

#### 4.2.4 설계 결정사항
1. **레벨당 1000점**: 균형잡힌 난이도 상승 곡선
   - 이유: 너무 빠르거나 느린 레벨업 방지
   - 확장성: `SCORE_PER_LEVEL` 상수로 쉽게 조정 가능

2. **점진적 속도 증가**: 레벨이 오를수록 유령 속도 증가
   - 이유: 게임 난이도의 자연스러운 상승
   - 향후 확장: 다른 속성(팩맨 속도, 점수 배율 등) 추가 가능

### 4.3 DifficultyLevel

#### 4.3.1 책임
- 레벨별 속성 저장 (불변 객체)

#### 4.3.2 필드
```java
private final int level;                    // 레벨 번호
private final int scoreThreshold;           // 이 레벨에 도달하기 위한 점수
private final double ghostSpeedMultiplier;  // 유령 속도 배율
private final double pacmanSpeedMultiplier; // 팩맨 속도 배율
private final String description;           // 레벨 설명
```

## 5. 통합 설계

### 5.1 기존 시스템과의 통합

#### 5.1.1 UIPanel 통합
**변경 전**:
```java
private int score = 0;  // 직접 관리
```

**변경 후**:
```java
private ScoreManager scoreManager;
private DifficultyManager difficultyManager;

public void updateScore(int incrScore) {
    scoreManager.addScore(incrScore);
    difficultyManager.updateLevel(scoreManager.getCurrentScore());
    // UI 업데이트
}
```

**이점**:
- 중앙 집중식 점수 관리
- 자동 레벨 업데이트
- 레벨 정보 UI 표시

#### 5.1.2 Game 통합
**변경 사항**:
- Game Over 시 ScoreManager와 DifficultyManager 정보 출력
- 최종 점수, 하이스코어, 최종 레벨 표시

### 5.2 Observer 패턴 유지
기존 게임의 Observer 패턴은 그대로 유지:
```
Pacman (Subject) → UIPanel (Observer)
                 → Game (Observer)
```

새로운 매니저들은 Observer 패턴을 통해 간접적으로 업데이트됨

## 6. 테스트 설계

### 6.1 ScoreManagerTest

#### 6.1.1 테스트 범주
| 범주 | 테스트 케이스 | 목적 |
|------|---------------|------|
| 싱글톤 | `testSingletonInstance` | 인스턴스 동일성 검증 |
| 점수 관리 | `testAddScore`, `testAddMultipleScores` | 점수 누적 기능 |
| 초기화 | `testReset`, `testInitialScore` | 리셋 기능 검증 |
| 하이스코어 | `testHighScoreUpdate`, `testHighScorePersistence` | 저장/로드 기능 |
| 엣지 케이스 | `testNegativeScore`, `testLargeScore` | 경계값 테스트 |

#### 6.1.2 특이사항
- **Reflection 사용**: 각 테스트마다 싱글톤 리셋하여 독립성 보장
- **파일 정리**: `@After`에서 테스트 파일 삭제

### 6.2 DifficultyManagerTest

#### 6.2.1 테스트 범주
| 범주 | 테스트 케이스 | 목적 |
|------|---------------|------|
| 싱글톤 | `testSingletonInstance` | 인스턴스 동일성 검증 |
| 레벨업 | `testLevelUpAt1000Points`, `testNoLevelUpBelowThreshold` | 레벨업 로직 |
| 레벨 속성 | `testGhostSpeedIncreaseWithLevel` | 난이도 증가 검증 |
| 다음 레벨 | `testGetScoreToNextLevel` | 진행도 계산 |
| 초기화 | `testReset` | 리셋 기능 |

#### 6.2.2 테스트 전략
- **경계값 테스트**: 999점, 1000점 등에서 레벨 전환 확인
- **순서 검증**: 레벨 임계값이 오름차순인지 확인
- **최대 레벨**: 최고 레벨에서의 동작 검증

## 7. 확장 가능성

### 7.1 향후 개선 사항

#### 7.1.1 점수 시스템
```java
// 콤보 시스템 추가 가능
public void addScore(int score, int combo) {
    int totalScore = score * combo;
    addScore(totalScore);
}
```

#### 7.1.2 난이도 시스템
```java
// 커스텀 난이도 설정 추가
public void addCustomLevel(DifficultyLevel level) {
    levels.add(level);
}

// 유령별 개별 속도 조정
public void setGhostSpeed(GhostType type, double multiplier) {
    // 구현
}
```

#### 7.1.3 데이터베이스 연동
현재는 파일 기반이지만, 향후 데이터베이스로 확장 가능:
```java
interface ScoreRepository {
    int loadHighScore();
    void saveHighScore(int score);
}

class FileScoreRepository implements ScoreRepository { ... }
class DatabaseScoreRepository implements ScoreRepository { ... }
```

### 7.2 디자인 패턴 확장

#### 7.2.1 Strategy Pattern
난이도별로 다른 유령 행동 전략 적용:
```java
public interface DifficultyStrategy {
    void applyTo(Ghost ghost);
}

class EasyStrategy implements DifficultyStrategy { ... }
class HardStrategy implements DifficultyStrategy { ... }
```

#### 7.2.2 Factory Pattern
레벨별 게임 요소 생성:
```java
public interface LevelFactory {
    Ghost createGhost(DifficultyLevel level);
    PacGum createPacGum(DifficultyLevel level);
}
```

## 8. UML 다이어그램

### 8.1 클래스 다이어그램 (간략)

```
┌─────────────────────┐
│   ScoreManager      │
├─────────────────────┤
│ - instance: static  │
│ - currentScore: int │
│ - highScore: int    │
├─────────────────────┤
│ + getInstance()     │
│ + addScore(int)     │
│ + getCurrentScore() │
│ + getHighScore()    │
│ + reset()           │
└─────────────────────┘
           △
           │ uses
           │
┌─────────────────────┐
│      UIPanel        │
├─────────────────────┤
│ - scoreManager      │
│ - difficultyManager │
├─────────────────────┤
│ + updateScore(int)  │
└─────────────────────┘
           │ uses
           ▽
┌─────────────────────┐
│  DifficultyManager  │
├─────────────────────┤
│ - instance: static  │
│ - levels: List      │
│ - currentLevelIndex │
├─────────────────────┤
│ + getInstance()     │
│ + updateLevel(int)  │
│ + getCurrentLevel() │
│ + reset()           │
└─────────────────────┘
           │ creates
           ▽
┌─────────────────────┐
│  DifficultyLevel    │
├─────────────────────┤
│ - level: int        │
│ - scoreThreshold    │
│ - ghostSpeed: double│
├─────────────────────┤
│ + getLevel()        │
│ + getGhostSpeed()   │
└─────────────────────┘
```

### 8.2 시퀀스 다이어그램 - 점수 추가 및 레벨업

```
Player    UIPanel    ScoreManager    DifficultyManager
  │          │             │                 │
  │ eatPacGum│             │                 │
  ├─────────>│             │                 │
  │          │ addScore(10)│                 │
  │          ├────────────>│                 │
  │          │             │ save if high    │
  │          │             ├────────────────>│
  │          │ updateLevel(score)            │
  │          ├───────────────────────────────>│
  │          │             │         levelUp?│
  │          │<────────────────────────────────┤
  │          │ updateUI    │                 │
  │<─────────┤             │                 │
```

## 9. 결론

### 9.1 설계의 강점
1. **Singleton 패턴**: 전역 접근성과 일관성 보장
2. **캡슐화**: 점수와 난이도 로직이 잘 분리됨
3. **확장성**: 새로운 레벨이나 점수 규칙 추가 용이
4. **테스트 가능성**: 포괄적인 단위 테스트로 품질 보증
5. **기존 코드 유지**: 기존 기능을 깨지 않고 확장

### 9.2 학습 포인트
- **디자인 패턴 실전 적용**: Singleton의 장단점 이해
- **리팩토링 기술**: 기존 코드를 깨지 않고 개선
- **테스트 주도**: JUnit을 활용한 신뢰성 확보
- **문서화 중요성**: 설계 의도와 이유를 명확히 기록

### 9.3 개선 제안
1. 인터페이스 추출로 더 느슨한 결합 구현
2. 설정 파일로 레벨 정의 외부화
3. 이벤트 시스템으로 레벨업 알림 개선
4. 통계 시스템 추가 (평균 점수, 플레이 시간 등)

---

**문서 작성일**: 2025년 10월 30일
**버전**: 1.0
**작성자**: Claude Code
