
# Java Mainrepo Project Structure & Contribution Guidelines

Welcome to the  **SWE Java Monorepo** !

This document explains the  **directory structure** ,  **module ownership** , and the  **rules to follow before raising a Pull Request (PR)** .

---

## Repository Structure

All Java modules live under the `/java` directory.

```
Java-Core-Communicator-SWE/
└─ java/
   ├─ pom.xml
   ├─ config/
   │  └─ checkstyle/
   │     ├─ checkstyle.xml
   │     └─ suppressions.xml
   │
   ├─ module-networking/
   │  └─ src/
   │     ├─ main/java/com/swe/networking/...
   │     ├─ main/resources/
   │     ├─ test/java/com/swe/networking/...
   │     └─ test/resources/
   │
   ├─ module-ux/
   │  └─ src/
   │     ├─ main/java/com/swe/ux/...
   │     ├─ main/resources/
   │     ├─ test/java/com/swe/ux/...
   │     └─ test/resources/
   │
   ├─ module-chat/
   │  └─ src/
   │     ├─ main/java/com/swe/chat/...
   │     ├─ main/resources/
   │     ├─ test/java/com/swe/chat/...
   │     └─ test/resources/
   │
   ├─ module-canvas/
   │  └─ src/
   │     ├─ main/java/com/swe/canvas/...
   │     ├─ main/resources/
   │     ├─ test/java/com/swe/canvas/...
   │     └─ test/resources/
   │
   ├─ module-screen-video/
   │  └─ src/
   │     ├─ main/java/com/swe/screenvideo/...
   │     ├─ main/resources/
   │     ├─ test/java/com/swe/screenvideo/...
   │     └─ test/resources/
   │
   ├─ module-ai-insights/
   │  └─ src/
   │     ├─ main/java/com/swe/aiinsights/...
   │     ├─ main/resources/
   │     ├─ test/java/com/swe/aiinsights/...
   │     └─ test/resources/
   │
   ├─ module-cloud/
   │  └─ src/
   │     ├─ main/java/com/swe/cloud/...
   │     ├─ main/resources/
   │     ├─ test/java/com/swe/cloud/...
   │     └─ test/resources/
   │
   ├─ module-controller/
   │  └─ src/
   │     ├─ main/java/com/swe/controller/...
   │     ├─ main/resources/
   │     ├─ test/java/com/swe/controller/...
   │     └─ test/resources/
   │
   ├─ module-app/
   │  └─ src/
   │     ├─ main/java/com/swe/app/Application.java
   │     ├─ main/resources/
   │     ├─ test/java/com/swe/app/...
   │     └─ test/resources/
   │
   └─ module-integration-tests/
          └─ src/
             ├─ test/java/com/swe/it/...
             └─ test/resources/
```

---

## What You Need to Focus On

When contributing to the repo, you only need to work inside **your assigned module directory** under `/java/`.

Each module is self-contained and follows a consistent Maven layout:

```
src/
 ├─ main/java/...     → your Java source files  
 ├─ main/resources/   → configs, properties, or assets  
 ├─ test/java/...     → unit and integration tests  
 └─ test/resources/   → test-related assets  
```

---

## Before Raising a Pull Request

Please ensure the following **three conditions are met** before creating a PR:

1. **Your code is inside the correct module folder**
   * e.g. `module-networking/src/main/java/com/swe/networking/...`
   * Do *not* create new folders or modify the repo structure.
2. **All Checkstyle rules pass successfully**
   * The Checkstyle configuration is located at

     ```
     java/config/checkstyle/checkstyle.xml
     java/config/checkstyle/suppressions.xml
     ```
   * You can verify locally via Maven:

     ```bash
     mvn clean verify
     ```

     or

     ```bash
     mvn checkstyle:check
     ```
   * Fix all reported style issues before committing.
3. **At least one test case exists and passes**
   * Each module must have at least one working test under `/test/java/...`.
   * Example:
     ```java
     @Test
     void sampleTest() {
         assertEquals(2, 1 + 1);
     }
     ```
   * Run:
     ```bash
     mvn test
     ```

Only when **all three criteria** are satisfied, raise a PR to the main branch.

---

## Module Ownership

| Module            | Path                               | Example Package         | Owner |
| :---------------- | :--------------------------------- | :---------------------- | :---- |
| Networking        | `/java/module-networking`        | `com.swe.networking`  | —    |
| UX                | `/java/module-ux`                | `com.swe.ux`          | —    |
| Chat              | `/java/module-chat`              | `com.swe.chat`        | —    |
| Canvas            | `/java/module-canvas`            | `com.swe.canvas`      | —    |
| Screen Video      | `/java/module-screen-video`      | `com.swe.screenvideo` | —    |
| AI & Insights     | `/java/module-ai-insights`       | `com.swe.aiinsights`  | —    |
| Cloud             | `/java/module-cloud`             | `com.swe.cloud`       | —    |
| Controller        | `/java/module-controller`        | `com.swe.controller`  | —    |
| Application       | `/java/module-app`               | `com.swe.app`         | —    |
| Integration Tests | `/java/module-integration-tests` | `com.swe.it`          | —    |

(Add names in the Owner column as teams are assigned.)

---

## Build Commands (Quick Reference)

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Run checkstyle
mvn checkstyle:check

# Package module
mvn package

# Full verification before PR
mvn clean verify
```

---

## Notes

* **Never commit `.class` files or build directories.**
* **Respect package naming** (all lowercase, under `com.swe.<module>`).
* **Document your classes** using Javadoc where applicable.
* **Keep commits atomic and meaningful.**

---

### Tip

If you’re unsure whether your module builds correctly, run:

```bash
cd java
mvn clean verify -pl module-yourmodule -am
```

This will build only your module and its dependencies.

---

### TL;DR

✔ Work only inside your module

✔ Pass Checkstyle

✔ Add & pass at least one test

✔ Then raise a PR 

---
