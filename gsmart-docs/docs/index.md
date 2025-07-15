# Bem-vindo à Documentação Técnica do GSmart v5.0

O **GSmart** é uma aplicação de desktop desenvolvida em Java (Swing) que atua como uma plataforma de monitorização e inteligência de dados, projetada para criar e gerenciar pipelines de forma visual e intuitiva.

O principal objetivo do sistema é conectar-se a fontes de dados de IoT, como o **ThingsBoard**, ou a bancos de dados espelho, para processar as informações em tempo real. Através de um **motor de regras duplo e totalmente configurável**, o GSmart é capaz de gerar **Alertas** críticos e **Alarmes** proativos, além de enviar os dados tratados para plataformas de Business Intelligence como o **Microsoft Power BI**.

## Principais Funcionalidades da v5.0

- **Múltiplas Fontes de Dados:** Conectividade nativa com a API do ThingsBoard e com bancos de dados relacionais via JDBC.
- **Motor de Alertas Configurável:** Permite ao utilizador criar regras personalizadas que, quando satisfeitas, disparam notificações críticas imediatas via MQTT (para automação, sistemas de notificação, etc.).
- **Motor de Alarmes Configurável:** Permite criar regras para gerar inteligência e observações proativas (ex: "O consumo de energia aumentou 15% esta semana"), que são exibidas na aplicação e podem ser enviadas para um tópico MQTT separado.
- **Processamento em Tempo Real:** As pipelines rodam em threads separadas para garantir que a interface do utilizador permaneça responsiva.
- **Exportação para Power BI:** Envia os dados processados diretamente para um conjunto de dados de streaming no Power BI.

## Como Navegar Nesta Documentação

Esta documentação está organizada para facilitar a consulta:

- **Introdução:** Esta página inicial, com uma visão geral do projeto.
- **Visão Geral do Projeto:** Uma página com mais detalhes sobre a arquitetura e o fluxo de dados.
- **Documentação da API:** A referência técnica completa, gerada a partir dos comentários Javadoc do código-fonte, detalhando todas as classes e métodos públicos.

Utilize o menu de navegação à esquerda para explorar as seções.