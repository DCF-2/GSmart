# Classe: GSmartListener

**Pacote:** `com.gsmart.resources`

## Descrição Geral

Define uma interface de "ouvinte" para comunicação entre a lógica de pipeline  
e a interface do utilizador (UI).  
  
As classes que implementam esta interface podem receber notificações em tempo real  
sobre eventos importantes que ocorrem dentro de uma `DataPipeline`, como  
mudanças de estado, perda de conexão ou a geração de novos insights.

## Métodos da Classe

---

### ` abstract void onInsight(String message, String type)`

Chamado quando um novo insight é gerado pela lógica de negócio.

- **Parâmetro:** `message` - A mensagem do insight a ser exibida.
- **Parâmetro:** `type` - O tipo de insight (ex: "CUSTO", "MANUTENCAO", "FALHA").


---

### ` abstract void onAlert(String title, String message)`

Chamado quando um alerta crítico que requer atenção do utilizador é gerado.

- **Parâmetro:** `title` - O título do alerta.
- **Parâmetro:** `message` - A mensagem detalhada do alerta.


---

### ` abstract void onStatusUpdate(TaskStatus status)`

Chamado sempre que o estado de uma tarefa de pipeline muda.

- **Parâmetro:** `status` - O novo `TaskStatus` da tarefa.


---

### ` abstract void onConnectionLost(String errorMessage)`

Chamado quando a pipeline perde a sua conexão com a fonte de dados.

- **Parâmetro:** `errorMessage` - A mensagem de erro que causou a perda de conexão.


---

### ` abstract void onReconnectionAttempt(long delayInSeconds)`

Chamado quando a pipeline inicia uma nova tentativa de reconexão.

- **Parâmetro:** `delayInSeconds` - O tempo em segundos que a pipeline aguardará antes da próxima tentativa.


---

### ` abstract void onConnectionRestored()`

Chamado quando a conexão com a fonte de dados é restaurada com sucesso.

