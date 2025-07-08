Of course! A well-styled README.md is the front door to your project and makes a great impression. We can definitely make it more visually appealing using advanced Markdown features that work perfectly on GitHub and GitLab.

I've created a more modern and structured version below. It uses tables, more emojis for visual cues, and a clearer layout to guide the reader.

Just copy and paste this content into your README.md file.

GSmart - IoT Data Processing Pipeline 🚀
GSmart is a desktop ETL (Extract, Transform, Load) application built with Java Swing. It provides a visual interface to create, manage, and monitor robust data pipelines, connecting to various IoT sources and forwarding processed data to Business Intelligence platforms.

✨ Key Features
Feature

Description

Multi-Source Connectivity

Natively connect to the ThingsBoard IoT platform or mirror databases via PostgreSQL.

Real-Time Processing

Each pipeline runs on a separate thread to ensure a responsive UI while processing data.

Embedded Business Logic

Generate insights, predict failures, and analyze costs with integrated controller modules.

Power BI Export

Send processed data directly to a streaming dataset in Microsoft Power BI.

Intuitive GUI

A user-friendly interface to configure all pipelines, data sources, and metrics visually.

Configuration Persistence

Saves your last-used settings for URLs and data sources for a faster workflow.


Exportar para as Planilhas
🏛️ Architecture Overview
The project follows a multi-layered architecture that separates the UI, control logic, and data access. The diagram below illustrates the main data flow.

Snippet de código

graph TD
subgraph "Presentation Layer (UI)"
A["<br>fa:fa-user User<br>GSmartGui"]
F["<br>fa:fa-bell GSmartListener<br>Updates UI"]
end

    subgraph "Service Layer (Core)"
        B["<br>fa:fa-cogs PipelineManager<br>Orchestrator"]
        C{"<br>fa:fa-sync-alt DataPipeline<br>(Worker Thread)"}
    end

    subgraph "Data Access Layer"
        D["<br>fa:fa-database IDataSource<br>(Interface)"]
        D1["fa:fa-cloud ThingsBoardSource"]
        D2["fa:fa-server DatabaseSource"]
    end

    subgraph "Business Logic Layer"
        E["<br>fa:fa-brain Controllers<br>(Analysis)"]
    end

    subgraph "Integration Layer"
        G["<br>fa:fa-paper-plane ExportData<br>to Power BI"]
        H["<br>fa:fa-chart-bar Power BI<br>(Destination)"]
    end

    A -- "1. Configure" --> B
    B -- "2. Launch" --> C
    C -- "3. Fetch()" --> D
    D -.-> D1 & D2
    C -- "4. Process()" --> E
    C -- "6. Send()" --> G
    G --> H
    C -- "5. Notify()" --> F
    F --> A
🛠️ Tech Stack
Language: Java 17

Framework: Swing (for the GUI)

Build Tool: Apache Maven

Libraries:

OkHttp: For HTTP requests to the ThingsBoard API.

PostgreSQL JDBC Driver: For database connectivity.

Gson: For JSON parsing and manipulation.

SLF4J & Logback: For robust logging.

jSerialComm: For serial port communication.

exp4j: For evaluating mathematical expressions.

⚙️ How to Build and Run
This project is managed by Apache Maven.

Prerequisites
Java JDK 17 or higher.

Apache Maven configured in your system's PATH.

Build Steps
Clone the repository:

Bash

git clone [YOUR_REPOSITORY_URL]

cd GSmart

Build with Maven:
Run the following command in the project root. This will compile the code, resolve dependencies, and create an executable JAR in the target/ folder.

Bash

mvn clean package
Run the Application:
After the build is complete, the main JAR will be available. Run it with the following command:

Bash

java -jar target/GSmart-Processador-gui.jar

📖 Documentation
The complete project documentation, including the API reference, can be viewed by generating it locally.

Navigate to the documentation folder:

Bash

cd gsmart-docs
Start the local server:

Bash

mkdocs serve
Open your browser and go to http://127.0.0.1:8000.