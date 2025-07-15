# Visão Geral do Projeto GSmart v5.0

## 1. Introdução

O **GSmart** é uma aplicação de desktop desenvolvida em Java com a biblioteca Swing. Na sua versão 5.0, ele evoluiu de uma simples ferramenta de ETL para uma **plataforma de monitorização e inteligência de dados**.

O seu objetivo principal é conectar-se a fontes de dados como a plataforma de IoT **ThingsBoard** ou bases de dados, e processar essa informação através de um **motor de regras duplo e configurável**, permitindo a geração de **Alertas** críticos e **Alarmes** proativos. Os dados tratados e os eventos gerados podem ser enviados para o **Microsoft Power BI** e para sistemas externos via **MQTT**.

## 2. Arquitetura e Componentes

O projeto segue uma arquitetura modular que separa a interface do utilizador, a gestão de pipelines e o processamento de regras. O diagrama abaixo ilustra o fluxo principal da aplicação na sua versão atual.


```mermaid
graph TD
    subgraph "Interface do Utilizador (GSmartGui)"
        A["<br>fa:fa-user Utilizador<br>Configura Regras"]
        F["<br>fa:fa-desktop Janela de Monitorização<br>Recebe Alarmes"]
    end

    subgraph "Núcleo da Aplicação"
        B["<br>fa:fa-cogs PipelineManager<br>Orquestrador"]
        C{"<br>fa:fa-sync-alt DataPipeline<br>(Worker Thread)"}
    end

    subgraph "Fontes e Destinos"
        D["<br>fa:fa-database Fontes de Dados<br>(IDataSource)"]
        H["<br>fa:fa-chart-bar Power BI<br>(Destino)"]
        I["<br>fa:fa-paper-plane Node-RED / MQTT<br>(Destino)"]
    end

    %% Fluxo
    A -- "1. Inicia Pipeline" --> B
    B -- "2. Lança Tarefa" --> C
    C -- "3. Busca Dados" --> D
    C -- "4. Avalia Regras" --o E1["fa:fa-bolt Alertas"] & E2["fa:fa-lightbulb Alarmes"]
    E1 -- "5a. Notificação Crítica" --> I
    E2 -- "5b. Insight Proativo" --> I
    E2 -- "5c. Exibe na GUI" --> F
    C -- "6. Envia Dados" --> H

    %% Estilos
    style C fill:#ffdead,stroke:#333,stroke-width:4px
    style A fill:#e6e6fa,stroke:#333,stroke-width:2px
    style H fill:#add8e6,stroke:#333,stroke-width:2px
    style I fill:#90ee90,stroke:#333,stroke-width:2px
```