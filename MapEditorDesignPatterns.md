# Pacman Map Editor - 디자인 패턴 적용 문서

## 📋 프로젝트 개요
Pacman 게임의 커스텀 맵을 생성할 수 있는 독립적인 맵 에디터 도구입니다.
56×63 그리드의 맵을 시각적으로 편집하고 CSV 파일로 저장할 수 있습니다.

---

## 🏗️ 적용된 디자인 패턴

### 1. Observer Pattern (관찰자 패턴)
**목적**: 맵 데이터 변경 시 여러 UI 컴포넌트를 자동으로 업데이트

**구현 위치**:
- **Subject**: `MapData.java` - 맵 데이터를 관리하고 변경 사항을 통지
- **Observer Interface**: `MapObserver.java` - 관찰자 인터페이스 정의
- **Concrete Observers**:
  - `MapGridPanel.java` - 그리드 재렌더링
  - `EntityCounterPanel.java` - 엔티티 개수 업데이트

**적용 이유**:
- 데이터와 UI의 느슨한 결합(Loose Coupling) 달성
- 여러 뷰가 동일한 모델을 관찰하며 동기화
- 새로운 Observer 추가가 쉬움 (Open-Closed Principle)

**코드 예시**:
```java
// MapData에서 엔티티 배치 시 모든 Observer에게 통지
public boolean placeEntity(int x, int y, EntityType entityType) {
    // ... 엔티티 배치 로직 ...
    notifyEntityPlaced(x, y, entityType);
    notifyEntityCountChanged(entityType);
    notifyValidationStateChanged(isMapValid());
    return true;
}
```

---

### 2. State Pattern (상태 패턴)
**목적**: 에디터의 상태(선택, 배치, 삭제)에 따라 동작을 다르게 처리

**구현 위치**:
- **State Interface**: `EditorState.java` - 상태 인터페이스
- **Context**: `EditorStateContext.java` - 상태 관리 컨텍스트
- **Concrete States**:
  - `IdleState.java` - 대기 상태
  - `PlacementState.java` - 엔티티 배치 상태
  - `EraseState.java` - 삭제 상태

**적용 이유**:
- 복잡한 조건문(if-else) 제거
- 각 상태별 로직을 독립적으로 관리
- 새로운 상태 추가가 용이 (예: 드래그 선택 상태)

**코드 예시**:
```java
// PlacementState에서 마우스 클릭 처리
@Override
public void handleMouseClick(int gridX, int gridY, int button) {
    if (button == MouseEvent.BUTTON1 && canPlaceAtCurrentPosition) {
        mapData.placeEntity(gridX, gridY, selectedEntityType);
    }
}
```

---

### 3. Command Pattern (명령 패턴)
**목적**: 모든 액션을 객체로 캡슐화하여 Undo/Redo 기능 구현

**구현 위치**:
- **Command Interface**: `Command.java` - 명령 인터페이스
- **Concrete Commands**:
  - `PlaceEntityCommand.java` - 엔티티 배치 명령
  - `RemoveEntityCommand.java` - 엔티티 제거 명령
- **Invoker**: `CommandManager.java` - 명령 실행 및 히스토리 관리

**적용 이유**:
- 실행 취소/재실행 기능의 깔끔한 구현
- 액션 로깅 및 매크로 기능 확장 가능
- 요청과 실행의 분리

**코드 예시**:
```java
// PlaceEntityCommand의 undo 구현
@Override
public boolean undo() {
    if (previousEntityType == EntityType.EMPTY) {
        return mapData.removeEntity(x, y);
    } else {
        return mapData.placeEntity(x, y, previousEntityType);
    }
}
```

---

### 4. Singleton Pattern (싱글톤 패턴)
**목적**: 에디터 전역 상태를 단일 인스턴스로 관리

**구현 위치**:
- `MapEditorManager.java` - 맵 에디터의 중앙 관리자

**적용 이유**:
- 전체 애플리케이션에서 일관된 상태 유지
- 메모리 효율성 (단일 인스턴스)
- 기존 프로젝트 패턴과 일관성 (ScoreManager, DifficultyManager)

**코드 예시**:
```java
public class MapEditorManager {
    private static MapEditorManager instance;

    public static synchronized MapEditorManager getInstance() {
        if (instance == null) {
            instance = new MapEditorManager();
        }
        return instance;
    }
}
```

---

### 5. MVC Pattern (Model-View-Controller)
**목적**: 전체 애플리케이션 구조를 논리적으로 분리

**구현**:
- **Model**: `MapData`, `EntityType` - 데이터 및 비즈니스 로직
- **View**: `MapGridPanel`, `EntityPalettePanel`, `EntityCounterPanel` - UI 컴포넌트
- **Controller**: `MapEditorManager`, `EditorStateContext` - 사용자 입력 처리 및 조율

**적용 이유**:
- 관심사의 분리 (Separation of Concerns)
- 각 레이어를 독립적으로 테스트 가능
- UI와 비즈니스 로직의 독립성

---

## 📊 패턴 간 상호작용

```
사용자 클릭
    ↓
[State Pattern]
EditorStateContext가 현재 상태에 따라 처리
    ↓
[Command Pattern]
액션을 Command 객체로 생성
    ↓
[Singleton]
MapEditorManager가 Command 실행
    ↓
[Observer Pattern]
MapData가 변경되고 모든 Observer에게 통지
    ↓
[MVC]
View 컴포넌트들이 자동 업데이트
```

---

## 🎯 달성된 설계 목표

### 1. **확장성 (Extensibility)**
- 새로운 엔티티 타입 추가: `EntityType` enum에 추가만 하면 됨
- 새로운 에디터 상태 추가: `EditorState` 구현체 추가
- 새로운 UI 컴포넌트 추가: `MapObserver` 구현

### 2. **유지보수성 (Maintainability)**
- 각 패턴이 명확한 책임을 가짐
- 변경 사항이 캡슐화되어 있음
- 높은 응집도, 낮은 결합도

### 3. **재사용성 (Reusability)**
- Command 패턴의 명령들은 다른 컨텍스트에서도 사용 가능
- Observer 패턴은 다른 데이터 모델에도 적용 가능
- State 패턴의 상태들은 독립적으로 테스트 가능

### 4. **SOLID 원칙 준수**
- **S**ingle Responsibility: 각 클래스가 하나의 책임만 가짐
- **O**pen-Closed: 확장에는 열려있고 수정에는 닫혀있음
- **L**iskov Substitution: 인터페이스를 통한 다형성 활용
- **I**nterface Segregation: 적절한 크기의 인터페이스
- **D**ependency Inversion: 추상화에 의존

---

## 🚀 실행 방법

### 컴파일
```bash
cd pacman
javac -cp src/java src/java/mapeditor/*.java src/java/mapeditor/**/*.java
```

### 실행
```bash
java -cp src/java mapeditor.MapEditorLauncher
```

또는 IDE에서 `MapEditorLauncher.java`를 직접 실행

---

## 📝 주요 기능

1. **엔티티 배치**: 상단 팔레트에서 선택 후 그리드에 클릭
2. **Undo/Redo**: Ctrl+Z / Ctrl+Y 또는 버튼 클릭
3. **자동 검증**: 필수 엔티티 개수 실시간 확인
4. **자동 채우기**: 저장 시 빈 공간을 PacGum으로 자동 채움
5. **CSV 저장**: 게임과 호환되는 형식으로 저장

---

## 🔍 향후 개선 사항

1. **Strategy Pattern 확장**: 다양한 자동 채우기 알고리즘
2. **Factory Pattern 추가**: 엔티티 생성 로직 중앙화
3. **Memento Pattern**: 전체 맵 상태 저장/복원
4. **Decorator Pattern**: 엔티티 렌더링 커스터마이징
5. **Template Method Pattern**: 맵 검증 로직 확장

---

## 📌 결론

이 프로젝트는 5개의 주요 디자인 패턴을 적용하여 유지보수가 쉽고 확장 가능한 맵 에디터를 구현했습니다. 각 패턴이 특정 문제를 해결하며, 서로 조화롭게 작동하여 전체적으로 견고한 아키텍처를 형성합니다.

특히 Observer Pattern을 통한 UI 동기화, State Pattern을 통한 복잡도 관리, Command Pattern을 통한 Undo/Redo 구현은 실제 프로덕션 소프트웨어에서도 흔히 사용되는 검증된 설계입니다.