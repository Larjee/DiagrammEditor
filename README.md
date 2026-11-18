# Diagram Editor

Desktop editor for block diagrams (UML, flowcharts). NSU CS3 project.

Backend: Java 21, Spring Boot (`backend/`).

Component sets and schemas are JSON files in the data folder (`editor.data-dir`, default `~/DiagramEditor`):
`component-sets/<name>.json` and `schemas/<name>.json`. The built-in `uml-class` and `flowchart` are copied
there on the first start; new files are picked up without a restart.

Frontend: Electron, React, TypeScript (`frontend/`, Node.js 22+).

```bash
cd frontend
npm install
npm run dev     # opens the window; talks to the backend at http://127.0.0.1:8080 (override with BACKEND_URL)
npm run build   # type check and build into frontend/out
```

The packaged app will start `backend.jar` itself on a free port; packaging is not set up yet.
