# Classe: `ConfigManager`

**Pacote:** `com.gsmart.config`

## Descrição Geral

Gere a persistência das configurações da aplicação.  
  
Esta classe é responsável por carregar as configurações do utilizador da última  
sessão (como URLs, nomes de utilizador, etc.) de um ficheiro `.properties`  
quando a aplicação inicia, e por salvar as configurações atuais quando a  
aplicação é fechada.  
  
Isto garante uma melhor experiência do utilizador, que não precisa de reintroduzir  
os mesmos dados a cada execução.

## Métodos da Classe

---

### `public Properties loadProperties()`

Carrega as propriedades do arquivo de configuração.  
Se o arquivo não existir, retorna um objeto de propriedades vazio.

- **Retorna:** Um objeto Properties com as configurações carregadas.


---

### `public void saveProperties(Properties props)`

Salva as propriedades no arquivo de configuração.

- **Parâmetro:** `props` - O objeto Properties contendo as configurações a serem salvas.


---

### `public void saveRules(List<AlertRule> alertRules, List<InsightRule> insightRules)`

Salva as listas de regras de Alerta e Alarme em ficheiros serializados.

- **Parâmetro:** `alertRules` - A lista de regras de alerta a ser guardada.
- **Parâmetro:** `insightRules` - A lista de regras de alarme a ser guardada.


---

### `public List<AlertRule> loadAlertRules()`

Carrega a lista de regras de Alerta a partir de um ficheiro serializado.

- **Retorna:** Uma lista de AlertRule. Se o ficheiro não for encontrado, retorna uma lista vazia.


---

### `public List<InsightRule> loadInsightRules()`

Carrega a lista de regras de Alarme a partir de um ficheiro serializado.

- **Retorna:** Uma lista de InsightRule. Se o ficheiro não for encontrado, retorna uma lista vazia.


