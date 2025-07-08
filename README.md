GSmart - IoT Data Processor
GSmart is a desktop application developed in Java (Swing), designed to act as an ETL (Extract, Transform, Load) tool. The system allows for the creation and management of robust data pipelines, connecting to various data sources to process information and forward it to Business Intelligence platforms.

🚀 Key Features
Multiple Data Sources: Natively connect to the ThingsBoard IoT platform or to mirror databases via PostgreSQL.

Real-Time Processing: Pipelines run on separate threads to process data without freezing the user interface.

Embedded Business Logic: Generate insights, predict failures, and analyze costs with integrated controller modules.

Power BI Export: Send processed data directly to a streaming dataset in Microsoft Power BI.

Intuitive GUI: Configure and manage all your pipelines through a user-friendly interface built with Swing.

Configuration Persistence: Saves the last used settings for URLs and data sources for an improved user experience.

🏛️ Architecture
The project follows a multi-layered architecture that separates the user interface, control logic, and data access.

Snippet de código

graph TD
subgraph "User Interface"
A[/"User in GSmartGui"/]
end

    subgraph "Application Core"
        B(PipelineManager)
        C{DataPipeline <br> (Thread)}
    end
    
    subgraph "Data Sources (IDataSource)"
        D1[ThingsBoardSource]
        D2[DatabaseSource]
    end
    
    subgraph "Business Logic (Controllers)"
        E1[GeradorDeInsights]
        E2[PrevisaoFalhas]
    end
    
    subgraph "External Services"
        G[ExportacaoDadosPWBI]
        H((Power BI))
    end

    A -- 1. Configure --> B
    B -- 2. Launch --> C
    C -- 3. Fetch Data --> D1 & D2
    C -- 4. Process with --> E1 & E2
    C -- 5. Send to --> G
    G -- API --> H
🛠️ How to Build and Run
This project uses Apache Maven to manage dependencies and the build process.

Prerequisites
Java JDK 17 or higher.

Apache Maven.

(Optional) Access to the data sources (ThingsBoard or PostgreSQL).

Build Steps
Clone the repository:

Bash

git clone [YOUR_REPOSITORY_URL]
cd [PROJECT_FOLDER_NAME]
Build with Maven:
Run the following command in the project root. It will compile the code, resolve dependencies, and create an executable JAR in the target/ folder.

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