

# Java Core Communicator SWE — Project Structure & Contribution Guidelines
 
Welcome to the **SWE Java Monorepo**!
This document defines the **directory structure**, **branching conventions**, **module ownership**, **testing & checkstyle rules**, and the **workflow every contributor must follow before merging code**.

---

## 📁 Repository Overview

All Java components live under `/java`.
Each module has its **own child POM** (`pom.xml`) and is managed by the parent aggregator at `/java/pom.xml`.

```
Java-Core-Communicator-SWE/
└── java/
    ├── pom.xml                       ← Parent aggregator POM
    ├── config/
    │   └── checkstyle/
    │       ├── checkstyle.xml
    │       └── suppressions.xml
    │
    ├── module-networking/
    │   ├── pom.xml
    │   └── src/
    │       ├── main/java/com/swe/networking/...
    │       ├── main/resources/
    │       ├── test/java/com/swe/networking/...
    │       └── test/resources/
    │
    ├── module-ux/
    │   ├── pom.xml
    │   └── src/...
    │
    ├── module-chat/
    │   ├── pom.xml
    │   └── src/...
    │
    ├── module-canvas/
    │   ├── pom.xml
    │   └── src/...
    │
    ├── module-screen-video/
    │   ├── pom.xml
    │   └── src/...
    │
    ├── module-ai-insights/
    │   ├── pom.xml
    │   └── src/...
    │
    ├── module-cloud/
    │   ├── pom.xml
    │   └── src/...
    │
    ├── module-controller/
    │   ├── pom.xml
    │   └── src/...
    │
    ├── module-app/
    │   ├── pom.xml
    │   └── src/...
    │
    └── module-integration-tests/
        ├── pom.xml
        └── src/test/java/com/swe/it/...
```

Every module follows a **standard Maven structure**:

```
src/
 ├─ main/java/...     → Java source files
 ├─ main/resources/   → configs, assets
 ├─ test/java/...     → unit/integration tests
 └─ test/resources/   → test assets
```

---

## 🌱 Branching & Collaboration Model

### 🧭 Branches

| Branch                 | Purpose                                                                         |
| :--------------------- | :------------------------------------------------------------------------------ |
| **`main`**             | Stable, production-ready code. No direct commits.                               |
| **`dev`**              | Active development branch. All merges go here after review.                     |
| **`module-*`** | Temporary branches for individual module work. Developers commit and test here. |

**✅ Only Team Leads have push access to `dev`.**
All other commits must be done via PRs (pull requests) from feature branches.

The **CI/CD pipeline** automatically runs Checkstyle + JUnit tests for all modules when PRs are raised to `dev`.

After stable builds, `main` is synced periodically from `dev`.

---

## 🔗 Commit Policy (Azure DevOps Work Item Integration)

Every commit **must reference an Azure Board work item** so that all work stays traceable.

Use this format:

```
<type>: <short summary> AB#<work-item-id>
```

### Examples

```bash
git commit -m "feat: add UDP packet handler to networking module AB#124"
git commit -m "fix: resolve null pointer in AI Insights module AB#207"
git commit -m "test: implement controller test suite AB#311"
```

> **Note:**
> This automatically links the commit and PR to the corresponding Azure work item.

---

## ✅ Pre-PR Checklist

Before creating a Pull Request (PR) to `dev`, ensure **all of the following** are satisfied:

### 1️⃣ Code is inside the correct module

* Example: `module-networking/src/main/java/com/swe/networking/...`
* Do **not** modify the repo’s folder structure or add new top-level directories.

### 2️⃣ Checkstyle passes successfully

The shared Checkstyle configuration is located at:

```
java/config/checkstyle/checkstyle.xml
java/config/checkstyle/suppressions.xml
```

Run locally before committing:

```bash
mvn checkstyle:check
```

If violations appear, fix them.
To auto-format (where possible):

```bash
mvn spotless:apply
```

### 3️⃣ Minimum 10 JUnit Tests per module

* Each module **must contain at least 10 valid test cases** under `/test/java/...`.
* Example test:

  ```java
  @Test
  void sampleAdditionTest() {
      assertEquals(2, 1 + 1);
  }
  ```
* Run all tests locally:

  ```bash
  mvn test
  ```
* The Azure pipeline executes these same tests before merging.

Only when all **Checkstyle, tests, and builds pass**, you may raise a PR to `dev`.

---

## 👥 Module Ownership

| Module            | Path                             | Package Example       | Owner       |
| :---------------- | :------------------------------- | :-------------------- | :---------- |
| Networking        | `/java/module-networking`        | `com.swe.networking`  | Logan       |
| UX                | `/java/module-ux`                | `com.swe.ux`          | Vaibhav     |
| Chat              | `/java/module-chat`              | `com.swe.chat`        | Akshay      |
| Canvas            | `/java/module-canvas`            | `com.swe.canvas`      | Sri Krishna |
| Screen Video      | `/java/module-screen-video`      | `com.swe.screenvideo` | Priyanshu   |
| AI & Insights     | `/java/module-ai-insights`       | `com.swe.aiinsights`  | Nandhana    |
| Cloud             | `/java/module-cloud`             | `com.swe.cloud`       | Sai Kiran   |
| Controller        | `/java/module-controller`        | `com.swe.controller`  | Nishant     |
| Application       | `/java/module-app`               | `com.swe.app`         | —           |
| Integration Tests | `/java/module-integration-tests` | `com.swe.it`          | —           |

---

## 🧪 Build & Validation Commands

```bash
# Clean and compile project
mvn clean compile

# Run all unit & integration tests
mvn test

# Run Checkstyle validation
mvn checkstyle:check

# Package a module
mvn package

# Full verification before PR
mvn clean verify

# Run for only one module + dependencies
mvn clean verify -pl module-yourmodule -am
```

---

## ⚙️ Developer Guidelines

* **Do not** commit compiled code (`.class`, `.jar`, or `/target` folders).
* **Follow package naming conventions**: `com.swe.<module>`.
* **Add Javadoc** for public classes and methods.
* **Write atomic, meaningful commits.**
* **Every commit must have a linked work item (`AB#<id>`).**
* **All merges go through Pull Requests → `dev`.**

---

## 🧭 Continuous Integration (CI/CD)

* The **Azure Pipeline** automatically:

  * Runs **Checkstyle** on every module.
  * Executes all **JUnit tests** (minimum 10 per module expected).
  * Fails the build if **any violation or test failure** occurs.

✅ A successful PR → `dev` implies your code passes both Checkstyle and all tests.

---

## ⚡ TL;DR

✔ Work only inside your module
✔ Add **≥10 tests** and ensure all pass
✔ Run `mvn checkstyle:check` and fix issues
✔ Commit using `AB#<work-item>`
✔ Push via PR to **`dev` only** (lead-approved)
✔ `main` is updated only after stable builds

---

