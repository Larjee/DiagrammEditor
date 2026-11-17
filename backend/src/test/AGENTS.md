# AGENTS.md — Backend tests

Unit tests and Cucumber scenarios for the Spring Boot backend. **No Spring context, no mocks**: tests drive `DiagramEditor` directly.

## Purpose

Every implemented use case from the project doc (UC-1…UC-13) is covered twice: a JUnit 5 unit test class and a Cucumber feature written in Russian. Only backend behaviour is tested; flows that live in the UI only (A1 "the user cancels") are not.

## Architecture

```
backend/src/test/
├── java/ru/nsu/diagrameditor/
│   ├── TestEditors.java           — the only place tests build a DiagramEditor
│   ├── CucumberTest.java          — JUnit Platform Suite that runs resources/features
│   ├── service/<Action>Test.java  — unit tests, one class per use case
│   └── steps/DiagramSteps.java    — all Cucumber step definitions
└── resources/features/
    └── ucN_<action>.feature       — one feature per use case, in Russian
```

## Unit tests

- One class per use case in `service`, named after the action: `CreateDiagramTest` (UC-1), `AddBlockTest` (UC-2).
- Cover the main flow and every alternative flow the backend handles, one test method per flow; the name describes behaviour (`rejectsUnavailableSchema`).
- Build the editor with `TestEditors.create()` in `@BeforeEach`. Never call `new DiagramEditor(...)` in a test: when its constructor changes, only `TestEditors` is edited.
- Plain JUnit 5 `Assertions`. No `@SpringBootTest`, no Mockito; component sets and schemas come from `InMemoryResourceRepository` (`uml-class`, `flowchart`).
- Assert errors by the concrete `EditorException` subclass (`assertThrows(SchemaViolationException.class, ...)`) and check that a rejected action left the state unchanged.
- Separate arrange / act / assert with blank lines.

## Cucumber scenarios

- File `resources/features/ucN_<action>.feature`; first line `# language: ru`, then `Функция: UC-N <name from the doc>`.
- One `Сценарий` per flow; alternative flows start with the doc's id: `Сценарий: A2. ...`.
- Steps are plain Russian sentences with `Дано` / `Когда` / `Тогда` / `И`; parameters are quoted strings (`"uml-class"`) or numbers.
- Reuse existing phrases from `DiagramSteps` before adding new ones; keep the wording identical so one step definition serves every feature.
- A rejected action is checked by its HTTP status: `Тогда редактор отказывает с ошибкой 422`.

## Step definitions

- All steps live in `steps/DiagramSteps`: there is no DI container, so state can be shared only inside one class. Cucumber creates a new instance per scenario.
- Annotations come from `io.cucumber.java.ru` (`@Дано`, `@Когда`, `@Тогда`); parameters use Cucumber expressions `{string}`, `{int}`.
- `Когда` steps that may fail go through `attempt(...)`, which stores the `EditorException`; `Тогда` steps assert on it.
- Method names are in English.

## Adding a use case

1. Every new error is a subclass of `EditorException` with its HTTP status; `ApiExceptionHandler` needs no changes.
2. Add `service/<Action>Test` with the main and alternative flows.
3. Add `features/ucN_<action>.feature` and only the missing steps in `DiagramSteps`.
4. If the change breaks tests of other use cases (e.g. a new `DiagramEditor` dependency), fix them in the same commit.
5. Tests are committed by the author of the use case, in a separate commit: `UC-N: unit tests for <action>`.
6. Before merging, merge `main` into the branch and run `gradle test` in `backend/`.

## Constraints

- **Cucumber stays at 7.18.1**: the last release built against JUnit 5.10, which Spring Boot 3.3 pins. Newer 7.x needs JUnit 5.11+, 8.x needs JUnit 6.
- Feature files and Java sources are UTF-8 (Cyrillic in step text and annotations).
- No Gradle wrapper is committed yet: run tests with a local Gradle or from IntelliJ.
