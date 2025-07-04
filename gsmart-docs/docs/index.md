# Bem-vindo à Documentação Técnica do GSmart

O **GSmart** é uma aplicação de desktop desenvolvida em Java (Swing) que atua como uma ferramenta de ETL (Extração, Transformação e Carga), projetada para criar e gerenciar pipelines de dados de forma visual e intuitiva.

O principal objetivo do sistema é conectar-se a fontes de dados de IoT, como o **ThingsBoard**, ou a bancos de dados espelho, processar as informações coletadas para gerar insights valiosos e, por fim, enviar os dados tratados para plataformas de Business Intelligence como o **Microsoft Power BI**.

## Principais Funcionalidades

- **Múltiplas Fontes de Dados:** Conectividade nativa com a API do ThingsBoard e com bancos de dados relacionais via JDBC.
- **Processamento em Tempo Real:** As pipelines rodam em threads separadas para garantir que a interface do usuário permaneça responsiva durante a coleta e o processamento dos dados.
- **Lógica de Negócio Embarcada:** Módulos para gerar insights sobre custos, prever falhas com base em telemetria e gerenciar alertas de manutenção.
- **Exportação para Power BI:** Envia os dados processados diretamente para um conjunto de dados de streaming no Power BI.

## Como Navegar Nesta Documentação

Esta documentação está organizada para facilitar a consulta:

- **Introdução:** Esta página inicial, com uma visão geral do projeto.
- **Documentação da API (JavaDoc):** A referência técnica completa, gerada a partir dos comentários Javadoc do código-fonte, detalhando todas as classes e métodos públicos.

Utilize o menu de navegação à esquerda para explorar as seções.