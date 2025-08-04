# Classe: `GSmartGui`

**Pacote:** `com.gsmart`

## Descrição Geral

Classe principal da interface gráfica (GUI) e ponto de controlo central da aplicação GSmart.  
  
Esta janela orquestra toda a interação com o utilizador, permitindo a configuração  
e gestão completa dos pipelines de dados. As suas principais responsabilidades incluem:  
<ul>  
<li>Configurar a fonte de dados (ThingsBoard ou Base de Dados).</li>  
<li>Gerir a seleção e transformação de métricas a serem processadas.</li>  
<li>Permitir a criação e edição de Regras de Alerta (notificações críticas).</li>  
<li>Permitir a criação e edição de Regras de Alarme (insights inteligentes).</li>  
<li>Iniciar, parar e monitorizar as tarefas de pipeline através do `PipelineManager`.</li>  
</ul>

- **Ver Também:** com.gsmart.pipeline.PipelineManager


## Métodos da Classe

---

### `private void showReconnectionLog()`

Exibe a janela de logs de reconexão.  
Se a janela ainda não existir, uma nova é criada. Se já existir,  
ela é trazida para a frente e o seu conteúdo é recarregado.

---

### `private void loadConfiguration()`

Carrega as configurações da última sessão a partir do ficheiro gsmart.properties.  
Isto inclui URLs e a última fonte de dados selecionada, melhorando a experiência do utilizador.

---

### `private void saveConfiguration()`

Salva as configurações atuais (URLs, etc.) no ficheiro gsmart.properties.  
Este método é chamado automaticamente quando a janela da aplicação é fechada.

---

### `private void launchPipeline()`

Orquestra o lançamento de uma nova tarefa de pipeline.  
Recolhe todas as configurações da interface (fonte de dados, métricas, regras de alerta e alarme),  
cria um objeto `PipelineConfiguration` e entrega-o ao `PipelineManager` para execução.

---

### `private void stopAllPipelines()`

Delega ao PipelineManager a tarefa de parar todas as pipelines em execução,  
geralmente após uma confirmação do utilizador.

---

### `private void showTaskManager()`

Exibe a "Central de Monitoramento".  
Se a janela ainda não existir, uma nova é criada. Se já existir,  
é simplesmente trazida para a frente.

---

### `private void toggleSourceFields()`

Alterna a visibilidade dos painéis de configuração de fonte de dados (ThingsBoard ou Base de Dados)  
com base na seleção do utilizador no JComboBox principal.

---

### `private String getThingsboardUrl()`

Obtém e valida a URL do servidor ThingsBoard a partir do campo de texto correspondente.  
Lança uma IllegalStateException se o campo estiver vazio.

- **Retorna:** A URL do ThingsBoard como uma String.


---

### `private void connectToThingsboard()`

Tenta estabelecer uma conexão com o servidor ThingsBoard.  
Se bem-sucedido, ativa os seletores de perfil de dispositivo e carrega os perfis disponíveis.  
Utiliza um `SwingWorker` para não bloquear a interface durante a conexão.

---

### `private void connectToDatabase()`

Tenta estabelecer uma conexão com a base de dados configurada via JDBC.  
Se bem-sucedido, ativa o seletor de tabelas e carrega as tabelas disponíveis.  
Utiliza um `SwingWorker` para não bloquear a interface durante a conexão.

---

### `private void loadAvailableKeys()`

Carrega as chaves de telemetria (ThingsBoard) ou os nomes das colunas (Base de Dados)  
da fonte de dados selecionada e popula a tabela de métricas na interface.  
Utiliza um `SwingWorker` para executar a operação em segundo plano.

---

### `private void handleKeysLoaded(SwingWorker<List<String>, Void> worker)`

Processa o resultado do SwingWorker que busca as métricas/colunas.  
Popula a tabela de métricas com os dados recebidos ou exibe uma mensagem de erro.

- **Parâmetro:** `worker` - O SwingWorker que completou a sua execução.


---

### `private void setLoadButtonReady()`

Restaura o estado do botão "Carregar Métricas", reativando-o e  
redefinindo o seu texto para o estado inicial.

---

### `private IDataSource createSelectedDataSource(List<String> originalKeys) throws Exception`

Cria e retorna uma instância da fonte de dados (IDataSource) apropriada  
com base na seleção do utilizador na interface.

- **Parâmetro:** `originalKeys` - A lista de métricas/colunas que a fonte de dados deve buscar.
- **Retorna:** Uma instância de ThingsBoardSource ou DatabaseSource.
- **`@throws`**: Se a conexão com a fonte de dados falhar ou a configuração for inválida.


---

### `private String showDropdownDialog(List<String> options, String title, String message)`

Exibe um diálogo de seleção (JOptionPane) com uma lista de opções.

- **Parâmetro:** `options` - A lista de strings a serem exibidas no dropdown.
- **Parâmetro:** `title` - O título da janela de diálogo.
- **Parâmetro:** `message` - A mensagem a ser exibida ao utilizador.
- **Retorna:** A string selecionada pelo utilizador ou null se o diálogo for cancelado.


---

### `private void loadDeviceProfiles()`

Carrega a lista de Perfis de Dispositivo do servidor ThingsBoard e popula  
o JComboBox correspondente.

---

### `private void loadDevicesByProfile()`

Carrega a lista de Dispositivos associados a um Perfil de Dispositivo específico  
do servidor ThingsBoard e popula o JComboBox correspondente.

---

### `private void applyRolePermissions(JPanel ruleButtonsPanel, JPanel insightButtonsPanel)`

Aplica permissões à interface com base no perfil do utilizador.  
Desativa funcionalidades de edição e criação para perfis que não sejam "ADMINISTRATOR".

---

### `private void showUserManagementWindow()`

Cria e exibe a janela de gestão de utilizadores.

