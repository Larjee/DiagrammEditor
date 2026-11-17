# AGENTS.md — Backend tests

Cucumber scenarios in Russian that drive the backend through its REST API (MockMvc, full Spring context). **No JUnit unit tests**: every check goes through an endpoint and its JSON response.

## Purpose

Every implemented use case from the project doc (UC-1…UC-13) has a Cucumber feature: scenarios send HTTP requests to the endpoints and check status codes and JSON. Only backend behaviour is tested; flows that live in the UI only (A1 "the user cancels") are not.

## Architecture

```
backend/src/test/
├── java/ru/nsu/diagrameditor/
│   ├── CucumberTest.java                        — JUnit Platform Suite that runs resources/features
│   └── steps/
│       ├── CucumberSpringConfiguration.java     — @CucumberContextConfiguration + @SpringBootTest + @AutoConfigureMockMvc
│       └── DiagramSteps.java                    — all step definitions
└── resources/features/
    └── ucN_<action>.feature                     — one feature per use case, in Russian
```

## Scenarios

- File `resources/features/ucN_<action>.feature`; first line `# language: ru`, then `Функция: UC-N <name from the doc>`.
- One `Сценарий` per flow: the main flow, every alternative flow the backend handles, free mode where strict mode refuses something. Alternative flows start with the doc's id: `Сценарий: A2. ...`.
- Steps are plain Russian sentences with `Дано` / `Когда` / `Тогда` / `И`; parameters are quoted strings (`"uml-class"`) or numbers.
- Blocks get a name in the scenario (`на диаграмме блок "class" под именем "Заказ"`); steps map names to the ids returned by the API. An unknown name is sent as is, which is how a scenario refers to a missing component.
- Reuse existing phrases from `DiagramSteps` before adding new ones; keep the wording identical so one step definition serves every feature.
- A refused action is checked by its HTTP status: `Тогда редактор отказывает с ошибкой 422`. After a refusal, also check that the diagram did not change.

## Step definitions

- All steps live in `steps/DiagramSteps`; Spring autowires `MockMvc`, `ObjectMapper` and `DiagramEditor` into it. Cucumber creates a new instance per scenario, the Spring context is shared.
- `@Before` closes the open diagram: `DiagramEditor` is a singleton and would otherwise carry state between scenarios. This is the only direct use of `DiagramEditor` in tests.
- Request bodies are the API's own request records (`CreateDiagramRequest`, `AddBlockRequest`, …) serialized with `ObjectMapper`.
- `Дано` steps assert their request succeeded; `Когда` steps only store the response; `Тогда` steps read the state through `GET /api/diagram` (JsonPath) and never inspect Java objects.
- Annotations come from `io.cucumber.java.ru`; parameters use Cucumber expressions `{string}`, `{int}`. Method names are in English.

## Adding a use case

1. Every new error is a subclass of `EditorException` with its HTTP status; `ApiExceptionHandler` needs no changes.
2. Whatever the scenarios need to check must be visible in `GET /api/diagram`.
3. Add `features/ucN_<action>.feature` and only the missing steps in `DiagramSteps`.
4. If the change breaks scenarios of other use cases, fix them in the same commit.
5. Scenarios go in a separate commit by the author of the use case: `UC-N: Cucumber scenarios`.
6. Before merging, merge `main` into the branch and run `gradle test` in `backend/`.

## Constraints

- **Cucumber stays at 7.18.1**: the last release built against JUnit 5.10, which Spring Boot 3.3 pins. Newer 7.x needs JUnit 5.11+, 8.x needs JUnit 6.
- Feature files and Java sources are UTF-8 (Cyrillic in step text and annotations).
- No Gradle wrapper is committed yet: run tests with a local Gradle or from IntelliJ.
