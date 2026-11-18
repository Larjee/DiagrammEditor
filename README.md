# Diagram Editor

Desktop editor for block diagrams (UML, flowcharts). NSU CS3 project.

Backend: Java 21, Spring Boot (`backend/`).

Component sets and schemas are JSON files in the data folder (`editor.data-dir`, default `~/DiagramEditor`):
`component-sets/<name>.json` and `schemas/<name>.json`. The built-in `uml-class` and `flowchart` are copied
there on the first start; new files are picked up without a restart.
