package main.java.com.gsmart.services;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Classe de serviço responsável por enviar mensagens para a API do Telegram.
 *
 * Esta classe utilitária fornece um método estático para encapsular a lógica
 * de realizar requisições HTTP para o endpoint de envio de mensagens do Telegram,
 * tratando da formatação da URL e do corpo da requisição.
 */
public class TelegramService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);
    private static final OkHttpClient httpClient = new OkHttpClient();

    /**
     * Envia uma mensagem de texto para um chat específico do Telegram através de um bot.
     *
     * @param botToken O token de autenticação do bot, fornecido pelo BotFather.
     * @param chatId O identificador único do chat para onde a mensagem será enviada.
     * @param mensagem O texto da mensagem a ser enviada.
     */
    public static void enviarMensagem(String botToken, String chatId, String mensagem) {
        if (botToken == null || botToken.trim().isEmpty() || chatId == null || chatId.trim().isEmpty()) {
            logger.warn("Token do Bot ou Chat ID do Telegram não foram configurados. A mensagem não será enviada.");
            return;
        }

        try {
            // A API do Telegram permite o envio via GET ou POST. POST com JSON é mais robusto.
            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

            // Cria o corpo da requisição em formato JSON
            String jsonBody = String.format("{\"chat_id\":\"%s\", \"text\":\"%s\"}",
                    chatId,
                    // Escapa o texto da mensagem para ser um JSON válido
                    mensagem.replace("\"", "\\\"").replace("\n", "\\n"));

            RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            logger.info("Enviando mensagem para o Telegram. Chat ID: {}", chatId);

            // Executa a chamada de forma assíncrona para não bloquear a pipeline
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    logger.error("Falha ao enviar mensagem para o Telegram.", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            logger.error("Falha ao enviar mensagem para o Telegram. Código: {}, Resposta: {}", response.code(), responseBody.string());
                        } else {
                            logger.info("Mensagem enviada com sucesso para o Telegram.");
                        }
                    }
                }
            });

        } catch (Exception e) {
            logger.error("Erro inesperado ao construir a requisição para o Telegram.", e);
        }
    }
}