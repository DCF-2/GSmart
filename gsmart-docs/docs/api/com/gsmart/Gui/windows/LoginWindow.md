# Classe: `LoginWindow`

**Pacote:** `com.gsmart.Gui.windows`

## Descrição Geral

Apresenta a janela de login inicial da aplicação GSmart.  
  
Esta classe é responsável por obter as credenciais do utilizador e validá-las.  
Em caso de sucesso na autenticação, ela fecha-se e abre a janela principal  
da aplicação, a `GSmartGui`.  
  
Atualmente, a validação é feita com credenciais fixas no código, mas a  
estrutura permite a sua substituição por um mecanismo de autenticação mais robusto.

## Métodos da Classe

---

### `private void performLogin(ActionEvent e)`

Valida o login usando o DatabaseManager e abre a aplicação principal se for bem-sucedido.

---

### `private void showMainApplication(String userRole)`

Inicializa e exibe a janela principal da aplicação, passando o perfil do utilizador.

- **Parâmetro:** `userRole` - O perfil do utilizador que fez o login (ex: "ADMINISTRATOR").


