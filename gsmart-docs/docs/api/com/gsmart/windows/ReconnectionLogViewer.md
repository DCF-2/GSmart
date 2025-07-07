# Classe: ReconnectionLogViewer

**Pacote:** `com.gsmart.windows`

## Descrição Geral

Uma janela de UI dedicada a exibir os logs de reconexão da aplicação.  
  
Esta classe foca-se em ler o ficheiro de log específico de reconexões  
(ex: reconnection.log) e apresentar o seu conteúdo. Isto permite isolar  
e analisar facilmente os eventos de perda e restabelecimento de conexão  
das pipelines, facilitando a depuração de problemas de rede.

## Métodos da Classe

---

### `public void loadLogFile()`

Carrega e colore o conteúdo do arquivo de log.

---

### `private void appendColoredText(String text, Color color)`

Adiciona uma string com uma cor específica ao JTextPane.

---

### `private Color getColorForLine(String line)`

Determina a cor com base no conteúdo da linha do log.

