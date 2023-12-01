[![Gradle Kotlin CI](https://github.com/modicio/modicio/actions/workflows/gradle.yml/badge.svg)](https://github.com/modicio/modicio/actions/workflows/gradle.yml)
[![codecov](https://codecov.io/gh/modicio/modicio/branch/main/graph/badge.svg?token=TY4TLWUGSG)](https://codecov.io/gh/modicio/modicio)

# modicio

🚀 **Welcome to the new modicio microservice!**

> If you are looking for the older Scala framweork version, check the provided tags or have a look at [this commit](https://github.com/modicio/modicio/tree/f3830a20ab10dca00aa847e62274e225eb571a1e).

⚡**Unfortunately, we currently have heavy work in progress, so the wiki is outdated and still referes to the older scala version linked above.**

📖 The newest documentation is found [here](https://modicio.github.io/modicio-docs/).

## Getting Started (for Contributors)

This list of steps is recommended and testet. Other development tools etc. might work aas well.

1. Clone this repository
2. Install OpenJDK 21 and have it active on your PATH (``java --version`` in the terminal must return 21)
3. Prepare IntelliJ IDE with Spring and Kotlin Plugins
4. Import modicio as gradle project in your IDE
5. Prepare a working Docker setup.
    - Install Docker Desktop
    - Load the ``postgres:latest`` image and start a container
    - Do not forget to forward port 5432 to system 5432 (5432:5432)
    - Create an empty database called ``modiciodb``, where the default user *postgres* has full rights.
6. In the ``applications.properties`` set the database url to ``jdbc:postgresql://localhost:5432/modiciodb`` if the local docker setup is used. If the databse runs directly on the system, this step is not required. **Do not push this change to remote because the CI will fail with this database url!** 
7. Optional: Connect modiciodb as a datasource in IntelliJ (may require ultimate version), otherwise pgadmin works as well for administration.
8. Install Gradle 8.4 directly on your system. The one integrated in IntelliJ is fine, but advanced commands should be tested on the system terminal.

### Useful Commands:

Exectued on the system terminal in the project root directory.

``gradle test`` runs all tests and creates coverage reports (requires active postgres connection). Note that every test resets the database.

``gradle dokkaHtml`` renders the doc, as visible (here)[https://modicio.github.io/modicio-docs/] 

``gradle bootRun`` starts the server (requires active postgres connection)
