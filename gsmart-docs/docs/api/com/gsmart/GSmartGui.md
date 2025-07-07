# Classe: GSmartGui

**Pacote:** `com.gsmart`

## Descrição Geral

Classe principal da interface grafica (GUI) da aplicacao GSmart.  
  
Esta janela serve como o painel de controlo central, permitindo ao utilizador  
configurar e gerir os pipelines de dados. A classe e responsavel por toda a  
interacao com o utilizador e por orquestrar as operacoes de backend.

- **`@see`**: com.gsmart.pipeline.PipelineManager


## Métodos da Classe

---

### `private void launchPipeline()`

Valida a configuracao da UI e lanca um novo pipeline.  
  
Este metodo recolhe as informacoes dos campos da GUI, cria um  
objeto de configuracao e o submete ao PipelineManager para execucao.

---

### `private void connectToThingsboard()`

Inicia uma tentativa de conexão com a fonte de dados (ThingsBoard/Banco de Dados)  
em uma thread de trabalho em segundo plano usando SwingWorker.  
Atualiza a UI com o status da conexão (sucesso ou falha) sem congelar a aplicação.

---

### `private void connectToDatabase()`

Inicia uma tentativa de conexão com a fonte de dados (ThingsBoard/Banco de Dados)  
em uma thread de trabalho em segundo plano usando SwingWorker.  
Atualiza a UI com o status da conexão (sucesso ou falha) sem congelar a aplicação.

---

### `private void loadAvailableKeys()`

Busca as "chaves" (métricas de telemetria ou colunas de tabela) disponíveis na fonte  
de dados atualmente configurada. Utiliza um SwingWorker para a operação de rede  
e, em caso de sucesso, preenche a tabela de métricas com os resultados.

---

### `private IDataSource createSelectedDataSource(List<String> originalKeys) throws Exception`

Cria e retorna uma instância concreta de IDataSource com base na seleção  
atual do usuário na interface gráfica.  
Este método valida se todas as informações necessárias (URLs, dispositivo, tabela, etc.)  
estão presentes antes de instanciar o objeto da fonte de dados.

- **Parâmetro:** `originalKeys` - A lista de nomes originais das métricas a serem buscadas.
- **`@return`**: Uma instância de IDataSource pronta para ser usada pela pipeline.
- **`@throws`**: se a conexão com a fonte não puder ser estabelecida ou se a configuração estiver incompleta.


