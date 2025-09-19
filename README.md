# GSmart - Intelligent Data Processing Engine ⚙️

![Java Version](https://img.shields.io/badge/Java-21%2B-blue?style=for-the-badge&logo=openjdk)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux-green?style=for-the-badge&logo=windows)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=for-the-badge)

## 📖 About The Project

**GSmart** is a powerful ETL (Extract, Transform, Load) desktop application built with Java Swing. It serves as an intelligent bridge between raw data sources and actionable business intelligence, providing an intuitive graphical interface to create, manage, and monitor robust real-time data pipelines without writing any code.

The application is designed to fetch data from IoT platforms and databases, process it through a **fully configurable dual-rule engine**, and export the results to modern BI tools while sending proactive notifications throughout the process.


![Screenshot of GSmartGui.java](Site/img/Gsmart-logo.png)

---

## ✨ Key Features

* **Flexible Data Sources:** Natively connect to the **ThingsBoard** IoT platform API or relational databases like **PostgreSQL** via JDBC to collect real-time telemetry and logs.
* **Dual Intelligent Rule Engine:**
    * **Alert Engine:** Create custom rules that trigger critical and immediate notifications via **MQTT** and **Telegram** when conditions (`>`, `<`, `==`, `Between`) are met.
    * **Alarm Engine (Insights):** Define rules to generate proactive intelligence and observations (e.g., "High energy consumption"), which are displayed in the application and sent to a separate MQTT topic.
* **BI Tool Integration:** Send processed and transformed data directly to dashboards in **Power BI** (via Push URL) or to Eventstreams in **Microsoft Fabric** (via Connection String), ready for analysis.
* **User and Access Management:** Control application access with a secure login system and a user management panel. Define **Administrator** and **Operator** profiles with different permissions.
* **Centralized Monitoring:** View and manage all active pipelines from a monitoring center, tracking the status, runtime, and logs of each process in real-time.
* **Concurrent Processing:** Each pipeline runs in a separate thread to ensure a responsive user interface during data processing.
* **Persistent Configuration:** All your settings, including URLs, BI endpoints, and rules, are saved locally for a faster workflow.

---

## 🏛️ Architecture and Data Flow

The project follows a modular architecture that separates the interface, orchestration, and data processing. The diagram below illustrates the data flow.

```mermaid
graph TD
    subgraph "User Interface (GSmartGui)"
        A["<br>fa:fa-user User<br>Configures Rules"]
        F["<br>fa:fa-desktop Monitoring Window<br>Receives Alarms"]
    end

    subgraph "Application Core"
        B["<br>fa:fa-cogs PipelineManager<br>Orchestrator"]
        C{"<br>fa:fa-sync-alt DataPipeline<br>(Worker Thread)"}
    end

    subgraph "Sources & Destinations"
        D["<br>fa:fa-database Data Sources<br>(IDataSource)"]
        H["<br>fa:fa-chart-bar Power BI<br>(Destination)"]
        I["<br>fa:fa-paper-plane Notifications<br>(MQTT, Telegram)"]
    end

    %% Flow
    A -- "1. Start Pipeline" --> B
    B -- "2. Launch Task" --> C
    C -- "3. Fetch Data" --> D
    C -- "4. Evaluate Rules" --o E1["fa:fa-bolt Alerts"] & E2["fa:fa-lightbulb Alarms"]
    E1 -- "5a. Critical Notification" --> I
    E2 -- "5b. Proactive Insight" --> I
    E2 -- "5c. Display on GUI" --> F
    C -- "6. Send Data" --> H

    %% Styles
    style C fill:#ffdead,stroke:#333,stroke-width:4px
    style A fill:#e6e6fa,stroke:#333,stroke-width:2px
    style H fill:#add8e6,stroke:#333,stroke-width:2px
    style I fill:#90ee90,stroke:#333,stroke-width:2px 
   ``` 
    
## 🛠️ Tech Stack

| Category | Technology / Library |
| :--- | :--- |
| **Main Language** | Java (JDK 21+) |
| **GUI Framework**| Java Swing |
| **Build System** | Apache Maven |
| **Dependencies** | Docker, Docker Compose, Mosquitto MQTT |
| **Key Libraries** | OkHttp, Gson, Logback, SLF4J, jBCrypt, Paho MQTT Client, Azure Event Hubs, exp4j, JavaParser, PostgreSQL JDBC Driver |

---

## 🚀 Getting Started

### Prerequisites (End-User)

Before installing GSmart, ensure your system has the following dependencies installed and running:

* **Docker & Docker Compose:** To run the MQTT broker.
* **Mosquitto MQTT:** The broker used for alert notifications.
* **Java (JDK):** Version 21 or higher.

*The GSmart installer will attempt to install these dependencies for you if they are not detected.*

### Installation

Download the appropriate installer for your operating system from our latest release.

* **On Windows:**
    1.  Download the `.exe` installer.
    2.  Run the installer with administrator privileges.
    3.  Follow the on-screen instructions. A shortcut will be created on your Desktop and in the Start Menu.
* **On Linux (Mint / Ubuntu / Debian):**
    1.  Download the `.deb` package.
    2.  Open a terminal in the download folder.
    3.  Run the command: `sudo apt install ./package-name.deb`.
    4.  `apt` will handle all dependencies automatically. After installation, look for "GSmart" in your applications menu.

---

## ⚙️ How to Build and Run (Developer)

This project is managed by Apache Maven.

### Prerequisites

* Java JDK 17 or higher
* Apache Maven configured in your system's environment variables

### Build Steps

1.  Clone the repository:
    ```bash
    git clone [YOUR_REPOSITORY_URL]
    cd GSmart
    ```

2.  Build with Maven:
    ```bash
    mvn clean package
    ```
    This will compile the code, resolve dependencies, and create an executable JAR in the `target/` folder.

### Running the Application

After the build, the main JAR will be available. Run it with the following command:

```bash
java -jar target/GSmart-Processador-gui.jar
```
## 📄 Documentation

For more detailed information, please consult our official documentation.

* **GSmart Documentation:** `https://gsmart-site.netlify.app/`
* **Streaming Replication Documentation:** `https://gsmart-site.netlify.app/`

The complete project documentation, including the API reference, can also be viewed locally via MkDocs.

---

## ⚖️ License

This project is licensed under the MIT License. See the LICENSE file for more details.

---

## 👥 Author

**GPSERS** - *Intelligent Solutions*
    