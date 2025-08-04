# Classe: `TelegramService`

**Pacote:** `com.gsmart.services`

## Descrição Geral

Classe de serviço responsável por enviar mensagens para a API do Telegram.  
  
Esta classe utilitária fornece um método estático para encapsular a lógica  
de realizar requisições HTTP para o endpoint de envio de mensagens do Telegram,  
tratando da formatação da URL e do corpo da requisição.

## Métodos da Classe

---

### `public static void enviarMensagem(String botToken, String chatId, String mensagem)`

Envia uma mensagem de texto para um chat específico do Telegram através de um bot.

- **Parâmetro:** `botToken` - O token de autenticação do bot, fornecido pelo BotFather.
- **Parâmetro:** `chatId` - O identificador único do chat para onde a mensagem será enviada.
- **Parâmetro:** `mensagem` - O texto da mensagem a ser enviada.


