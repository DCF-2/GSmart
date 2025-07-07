# Classe: ConfigManager

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

- **`@return`**: Um objeto Properties com as configurações carregadas.


---

### `public void saveProperties(Properties props)`

Salva as propriedades no arquivo de configuração.

- **Parâmetro:** `props` - O objeto Properties contendo as configurações a serem salvas.


