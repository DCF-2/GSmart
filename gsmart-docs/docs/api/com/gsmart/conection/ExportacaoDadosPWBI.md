# Classe: `ExportacaoDadosPWBI`

**Pacote:** `com.gsmart.conection`

## Descrição Geral

Classe utilitária responsável por enviar dados para a API de Push do Microsoft Power BI.  
  
Esta classe contém um método estático que formata o objeto de dados num payload JSON,  
conforme esperado pela API, e realiza uma requisição POST para o endpoint especificado,  
tratando a resposta do servidor.

## Métodos da Classe

---

### `public static void sendDataToPowerBI(JsonObject dataObject, String powerBiApiUrl) throws Exception`

Envia um único objeto de dados para um endpoint da API de Push do Power BI.  
  
Este método encapsula a lógica de conexão HTTP, a formatação do corpo da requisição  
e o tratamento da resposta do servidor para garantir que os dados foram recebidos com sucesso.

- **Parâmetro:** `dataObject` - O objeto `JsonObject` contendo os dados a serem enviados.
- **Parâmetro:** `powerBiApiUrl` - A URL completa do conjunto de dados de streaming do Power BI.
- **`@throws`**: se a URL do Power BI for nula, vazia ou inválida.
- **`@throws`**: se ocorrer um erro na comunicação com a API do Power BI (ex: código de resposta não-2xx).


