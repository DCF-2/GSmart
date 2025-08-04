# Classe: `MqttService`

**Pacote:** `com.gsmart.services`

## Descrição Geral

Classe de serviço responsável por publicar mensagens num broker MQTT.  
  
Utiliza a biblioteca Eclipse Paho para se conectar a um broker, publicar  
uma mensagem num tópico específico e desconectar-se de forma segura.

## Métodos da Classe

---

### `public static void publish(String brokerUrl, String topic, String message)`

Conecta-se a um broker MQTT, publica uma mensagem num tópico e desconecta-se.

- **Parâmetro:** `brokerUrl` - O endereço do broker MQTT (ex: "tcp://localhost:1883").
- **Parâmetro:** `topic` - O tópico MQTT no qual a mensagem será publicada.
- **Parâmetro:** `message` - O conteúdo da mensagem a ser publicada.


