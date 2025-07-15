# Visão Geral do Projeto GSmart v5.0

## 1. Introdução

O **GSmart** é uma aplicação de desktop desenvolvida em Java com a biblioteca Swing. Na sua versão 5.0, ele evoluiu de uma simples ferramenta de ETL para uma **plataforma de monitorização e inteligência de dados**.

O seu objetivo principal é conectar-se a fontes de dados como a plataforma de IoT **ThingsBoard** ou bases de dados, e processar essa informação através de um **motor de regras duplo e configurável**, permitindo a geração de **Alertas** críticos e **Alarmes** proativos. Os dados tratados e os eventos gerados podem ser enviados para o **Microsoft Power BI** e para sistemas externos via **MQTT**.

## 2. Arquitetura e Componentes

O projeto segue uma arquitetura modular que separa a interface do utilizador, a gestão de pipelines e o processamento de regras. O diagrama abaixo ilustra o fluxo principal da aplicação na sua versão atual.

# **Mermaid**
```
graph TD
    subgraph "Interface do Utilizador (GSmartGui)"
        A[/"Utilizador configura Métricas, Alertas e Alarmes"/]
        F(Janela de Monitorização)
    end

    subgraph "Núcleo da Aplicação"
        B(PipelineManager)
        C{DataPipeline <br> (Thread)}
    end
    
    subgraph "Fontes de Dados"
        D(IDataSource)
        D1[ThingsBoardSource]
        D2[DatabaseSource]
    end
    
    subgraph "Motor de Regras Configurável"
        E(Avaliação de Regras)
        E1[Regras de Alerta]
        E2[Regras de Alarme]
    end
    
    subgraph "Serviços Externos"
        G[Power BI]
        H(Node-RED / Broker MQTT)
    end

    %% Fluxo Principal
    A -- "1. Inicia Pipeline com Configs" --> B
    B -- "2. Lança Tarefa" --> C
    C -- "3. Busca Dados" --> D
    D -- " " --> D1 & D2
    D -- "4. Retorna Dados" --> C
    C -- "5. Avalia Regras" --> E
    E -- "Utiliza" --> E1 & E2
    E -- "6a. Dispara Alerta Crítico" --> H
    E -- "6b. Gera Alarme Proativo" --> H & F
    C -- "7. Envia Dados" --> G
    
    %% Estilos
    style C fill:#ffdead,stroke:#333,stroke-width:4px
    style A fill:#e6e6fa,stroke:#333,stroke-width:2px
    style H fill:#90ee90,stroke:#333,stroke-width:2px
    style G fill:#add8e6,stroke:#333,stroke-width:2px 
```