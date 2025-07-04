# Classe: GSmartGui

**Pacote:** `com.gsmart`

## Descrição Geral

A classe principal da interface gráfica (GUI) para a aplicação GSmart.
Esta janela (JFrame) serve como o painel de controle central, permitindo ao usuário:
<ol>
<li>Selecionar e configurar a fonte de dados (ThingsBoard ou Banco de Dados).</li>
<li>Conectar-se à fonte para carregar metadados como perfis, dispositivos e tabelas.</li>
<li>Carregar, selecionar e configurar as métricas a serem monitoradas.</li>
<li>Configurar o destino dos dados (URL de push do Power BI).</li>
<li>Lançar, monitorar e parar os pipelines de dados através do {@link PipelineManager}.</li>
</ol>
A classe gerencia o estado da UI, lida com eventos do usuário e usa {@link SwingWorker}
para operações de longa duração (rede/IO) para não congelar a interface.

## Métodos

---

### `private void launchPipeline()`

Valida as configurações da UI e lança um novo pipeline.
Coleta todas as informações dos campos (URL do Power BI, métricas selecionadas),
cria um objeto {@link PipelineConfiguration} e o submete ao {@link PipelineManager}
para iniciar a execução em segundo plano.

---

### `private void connectToThingsboard()`

Inicia uma tentativa de conexão com a fonte de dados (ThingsBoard/Banco de Dados)
em uma thread de trabalho em segundo plano usando {@link SwingWorker}.
Atualiza a UI com o status da conexão (sucesso ou falha) sem congelar a aplicação.

---

### `private void connectToDatabase()`

Inicia uma tentativa de conexão com a fonte de dados (ThingsBoard/Banco de Dados)
em uma thread de trabalho em segundo plano usando {@link SwingWorker}.
Atualiza a UI com o status da conexão (sucesso ou falha) sem congelar a aplicação.

---

### `private void loadAvailableKeys()`

Busca as "chaves" (métricas de telemetria ou colunas de tabela) disponíveis na fonte
de dados atualmente configurada. Utiliza um {@link SwingWorker} para a operação de rede
e, em caso de sucesso, preenche a tabela de métricas com os resultados.

---

### `private IDataSource createSelectedDataSource(List<String> originalKeys) throws Exception`

Cria e retorna uma instância concreta de {@link IDataSource} com base na seleção
atual do usuário na interface gráfica.
Este método valida se todas as informações necessárias (URLs, dispositivo, tabela, etc.)
estão presentes antes de instanciar o objeto da fonte de dados.

