# Classe: DataPipeline

**Pacote:** `com.gsmart.pipeline`

## Descrição Geral

Representa a lógica de execução de um único pipeline de dados.
Esta classe opera como um "worker" que roda em sua própria thread. Suas
responsabilidades principais são:
1. Executar um loop de vida contínuo até que a parada seja solicitada.
2. Buscar dados de uma {@link IDataSource}.
3. Processar os dados brutos, aplicando expressões matemáticas das {@link MetricConfig}.
4. Executar a lógica de negócio (insights, manutenção, previsão) se ativada.
5. Enviar os dados processados para a URL de push do Power BI.
6. Gerenciar seu próprio ciclo de vida, incluindo um robusto mecanismo de
tratamento de falhas e reconexão automática com delay progressivo.

## Métodos

---

### `public void triggerManualReconnect()`

Sinaliza para a thread da pipeline que uma reconexão manual e imediata deve ser tentada.
Este método interrompe o sono atual da thread para forçar uma verificação imediata do gatilho.

---

### `public void requestStop()`

Solicita que a pipeline pare sua execução de forma graciosa.
A flag de parada será verificada no final do ciclo atual ou durante um loop de reconexão.

---

### `public void run()`

Ponto de entrada principal para a execução da pipeline na sua thread.
Contém o loop de vida da pipeline, que consiste em um bloco de operação normal
e um bloco de tratamento de falhas e reconexão. O loop só termina quando o método
{@link #requestStop()} é chamado e a flag de parada é acionada.

