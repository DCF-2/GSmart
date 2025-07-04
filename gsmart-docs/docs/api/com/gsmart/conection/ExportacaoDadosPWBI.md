# Classe: ExportacaoDadosPWBI

**Pacote:** `com.gsmart.conection`

## Descrição Geral

Classe utilitária responsável por enviar dados para a API de Push do Microsoft Power BI.
Ela formata o objeto de dados em um payload JSON, conforme esperado pela API,
e realiza a requisição POST para o endpoint especificado.

## Métodos

---

### `public static void sendDataToPowerBI(JsonObject dataObject, String powerBiApiUrl) throws Exception`

Envia um único objeto de dados para um endpoint da API de Push do Power BI.
Este método encapsula a lógica de conexão HTTP, formatação do corpo da requisição
e tratamento da resposta do servidor.

