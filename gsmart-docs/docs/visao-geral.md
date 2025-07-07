# Visão Geral do Projeto GSmart

## 1. Introdução

O **GSmart** é uma aplicação de desktop desenvolvida em Java com a biblioteca Swing. Ele funciona como uma ferramenta de ETL (Extração, Transformação e Carga) projetada para criar e gerenciar pipelines de dados de forma visual e intuitiva.

O objetivo principal do sistema é conectar-se a diversas fontes de dados, como a plataforma de IoT **ThingsBoard** e bancos de dados **PostgreSQL**, processar as informações coletadas para gerar insights e, em seguida, enviar os dados processados para plataformas de Business Intelligence, como o **Microsoft Power BI**.

## 2. Arquitetura e Fluxo de Dados

O projeto segue uma arquitetura multicamadas que separa a interface do utilizador, a lógica de controlo e o acesso aos dados. O diagrama abaixo ilustra o fluxo principal.

```mermaid
graph TD
    subgraph "Interface do Utilizador"
        A[/"Utilizador na GSmartGui"/]
    end

    subgraph "Núcleo da Aplicação"
        B(PipelineManager)
        C{DataPipeline <br> (Thread)}
        F(GSmartListener)
    end
    
    subgraph "Fontes de Dados"
        D(IDataSource)
        D1[ThingsBoardSource]
        D2[DatabaseSource]
    end
    
    subgraph "Lógica de Negócio"
        E(Controllers)
        E1[GeradorDeInsights]
        E2[PrevisaoFalhas]
        E3[Manutencao]
    end
    
    subgraph "Serviços Externos"
        G[ExportacaoDadosPWBI]
        H((Power BI))
    end

    %% Fluxo Principal com numeração
    A -- "1. Configura" --> B
    B -- "2. Inicia" --> C
    C -- "3. Busca Dados" --> D
    D -- Implementado por --> D1 & D2
    D -- "4. Retorna Dados" --> C
    C -- "5. Processa" --> E
    E -- Utiliza --> E1 & E2 & E3
    C -- "7. Notifica Eventos" --> F
    F -- "8. Atualiza UI" --> A
    C -- "6. Envia para" --> G
    G -- "API" --> H

    %% Estilos para clareza
    style C fill:#ffdead,stroke:#333,stroke-width:4px
    style A fill:#e6e6fa,stroke:#333,stroke-width:2px
    style H fill:#90ee90,stroke:#333,stroke-width:2px