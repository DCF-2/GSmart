package functrendz;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FuncTrendZ {

    // Estrutura para armazenar os dados históricos de consumo
    private static List<Double> historicoConsumo = new ArrayList<>();
    private static Instant inicioExecucao = Instant.now();
    private static final double TARIFA_ENERGIA = 0.50; // Tarifa em R$/kWh

    // Configurações de manutenção
    private static int ciclosBaixoFator = 0;
    private static int ciclosAltaTemperatura = 0;
    private static final double LIMITE_FATOR_POTENCIA = 0.6;
    private static final double LIMITE_TEMPERATURA = 75.0;

    // Estrutura para prever falhas
    private static final List<Double> historicoTemperatura = new ArrayList<>();
    private static final List<Double> historicoFatorPotencia = new ArrayList<>();
    private static final List<Double> historicoPotenciaAtiva = new ArrayList<>();
    private static final double LIMITE_DESVIO_PADRAO = 1.5;

    /**
     * Registra o consumo com base na potência e no tempo de execução.
     * @param potencia Potência do dispositivo em watts.
     */
    public static void registrarConsumo(double potencia) {
        Instant agora = Instant.now();
        Duration duracao = Duration.between(inicioExecucao, agora);
        double segundos = duracao.toMillis() / 1000.0; // Tempo em segundos

        // Calcular o consumo em kWh
        double consumo = (potencia / 1000) * (segundos / 3600.0); // Segundos convertidos para horas

        // Atualizar o histórico
        historicoConsumo.add(consumo);
        inicioExecucao = agora;

        // Exibir consumo atualizado
        System.out.printf("Consumo registrado: %.6f kWh%n", consumo);
    }

    /**
     * Calcula o custo baseado no consumo total registrado.
     * @return Custo total em reais.
     */
    public static double calcularCusto() {
        double consumoTotal = historicoConsumo.stream().mapToDouble(Double::doubleValue).sum();
        return consumoTotal * TARIFA_ENERGIA;
    }

    /**
     * Recupera o histórico de consumo.
     * @return Lista de consumos registrados em kWh.
     */
    public static List<Double> getHistoricoConsumo() {
        return historicoConsumo;
    }

    /**
     * Reseta o histórico de consumo (usado para testes ou reinicialização).
     */
    public static void resetarHistorico() {
        historicoConsumo.clear();
        inicioExecucao = Instant.now();
        System.out.println("Histórico de consumo resetado.\n");
    }

    /**
     * Verifica a necessidade de manutenção com base no fator de potência e temperatura.
     * @param fatorPotencia Fator de potência atual.
     * @param temperatura Temperatura atual em graus Celsius.
     */
    public static void verificarManutencao(double fatorPotencia, double temperatura) {
        // Verificação do fator de potência
        if (fatorPotencia < LIMITE_FATOR_POTENCIA) {
            ciclosBaixoFator++;
        } else {
            ciclosBaixoFator = 0;
        }

        if (ciclosBaixoFator >= 3) {
            System.out.println("Alerta: Baixo fator de potência por 3 ciclos consecutivos. Agendar manutenção!");
        }

        // Verificação da temperatura
        if (temperatura > LIMITE_TEMPERATURA) {
            ciclosAltaTemperatura++;
        } else {
            ciclosAltaTemperatura = 0;
        }

        if (ciclosAltaTemperatura >= 5) {
            System.out.println("Alerta: Alta temperatura por 5 ciclos consecutivos. Agendar manutenção!");
        }
    }

    /**
     * Registra métricas críticas para prever falhas.
     */
    public static void registrarMetricas(double temperatura, double fatorPotencia, double potenciaAtiva) {
        historicoTemperatura.add(temperatura);
        historicoFatorPotencia.add(fatorPotencia);
        historicoPotenciaAtiva.add(potenciaAtiva);

        // Limitar o tamanho do histórico
        if (historicoTemperatura.size() > 100) historicoTemperatura.remove(0);
        if (historicoFatorPotencia.size() > 100) historicoFatorPotencia.remove(0);
        if (historicoPotenciaAtiva.size() > 100) historicoPotenciaAtiva.remove(0);
    }

    /**
     * Analisa o histórico e prevê possíveis falhas.
     *
     * @return
     */

    public static boolean preverFalhas() {
        // Verificar se há dados suficientes para análise
        if (historicoTemperatura.size() < 3) {
            System.out.println("Dados insuficientes para análise. Aguardando mais ciclos.");
            return false;
        }

        // Calcular média e desvio padrão para os últimos 3 registros
        List<Double> ultimasTemperaturas = historicoTemperatura.subList(historicoTemperatura.size() - 3, historicoTemperatura.size());
        List<Double> ultimosFatoresPotencia = historicoFatorPotencia.subList(historicoFatorPotencia.size() - 3, historicoFatorPotencia.size());
        List<Double> ultimasPotenciasAtivas = historicoPotenciaAtiva.subList(historicoPotenciaAtiva.size() - 3, historicoPotenciaAtiva.size());

        double mediaTemperatura = calcularMedia(ultimasTemperaturas);
        double desvioTemperatura = calcularDesvioPadrao(ultimasTemperaturas, mediaTemperatura);
        double mediaFatorPotencia = calcularMedia(ultimosFatoresPotencia);
        double desvioFatorPotencia = calcularDesvioPadrao(ultimosFatoresPotencia, mediaFatorPotencia);
        double mediaPotenciaAtiva = calcularMedia(ultimasPotenciasAtivas);
        double desvioPotenciaAtiva = calcularDesvioPadrao(ultimasPotenciasAtivas, mediaPotenciaAtiva);

        // Exibir as médias calculadas
        System.out.printf("Média das Temperaturas: %.2f, Desvio Padrão: %.2f%n", mediaTemperatura, desvioTemperatura);
        System.out.printf("Média dos Fatores de Potência: %.2f, Desvio Padrão: %.2f%n", mediaFatorPotencia, desvioFatorPotencia);
        System.out.printf("Média das Potências Ativas: %.2f, Desvio Padrão: %.2f%n", mediaPotenciaAtiva, desvioPotenciaAtiva);

        // Definir limites
        double limiteInferiorTemperatura = mediaTemperatura - 0.8 * desvioTemperatura;
        double limiteSuperiorTemperatura = mediaTemperatura + 0.8 * desvioTemperatura;
        double limiteInferiorFatorPotencia = mediaFatorPotencia - 0.8 * desvioFatorPotencia;
        double limiteSuperiorFatorPotencia = mediaFatorPotencia + 0.8 * desvioFatorPotencia;
        double limiteInferiorPotenciaAtiva = mediaPotenciaAtiva - 0.8 * desvioPotenciaAtiva;
        double limiteSuperiorPotenciaAtiva = mediaPotenciaAtiva + 0.8 * desvioPotenciaAtiva;

        // Verificar se os últimos valores estão fora dos limites
        double ultimaTemperatura = ultimasTemperaturas.get(ultimasTemperaturas.size() - 1);
        double ultimoFatorPotencia = ultimosFatoresPotencia.get(ultimosFatoresPotencia.size() - 1);
        double ultimaPotenciaAtiva = ultimasPotenciasAtivas.get(ultimasPotenciasAtivas.size() - 1);

        if (ultimaTemperatura < limiteInferiorTemperatura || ultimaTemperatura > limiteSuperiorTemperatura ||
                ultimoFatorPotencia < limiteInferiorFatorPotencia || ultimoFatorPotencia > limiteSuperiorFatorPotencia ||
                ultimaPotenciaAtiva < limiteInferiorPotenciaAtiva || ultimaPotenciaAtiva > limiteSuperiorPotenciaAtiva) {

            System.out.println("ALERTA: Falha detectada no sistema! Investigar imediatamente.");
            return true;
        }

        System.out.println("Nenhuma falha detectada no momento.");
        return false;
    }

    /**
     * Detecta anomalias em uma lista de dados usando desvio padrão.
     */
    private static boolean detectarAnomalia(List<Double> metricas) {
        if (metricas.size() <= 10) {
            //System.out.println("Dados insuficientes para análise. Aguardando mais ciclos.");
            return false;
        }

        double media = metricas.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double desvioPadrao = Math.sqrt(metricas.stream().mapToDouble(m -> Math.pow(m - media, 2)).average().orElse(0));

        double limiteSuperior = media + (LIMITE_DESVIO_PADRAO * desvioPadrao);
        double limiteInferior = media - (LIMITE_DESVIO_PADRAO * desvioPadrao);

        // Debug: Exibir limites
        System.out.printf("Média: %.2f, Desvio Padrão: %.2f, Limites: [%.2f, %.2f]%n",
                media, desvioPadrao, limiteInferior, limiteSuperior);

        // Verifica se os últimos 3 valores estão fora dos limites.
        int tamanho = metricas.size();
        for (int i = tamanho - 3; i < tamanho; i++) {
            double valor = metricas.get(i);
            if (valor < limiteInferior || valor > limiteSuperior) {
                System.out.printf("Anomalia detectada: Valor %.2f fora dos limites.%n", valor);
                return true;
            }
        }

        return false;
    }
    /**
     * Calcula a média de uma lista de valores.
     *
     * @param valores Lista de valores.
     * @return Média dos valores.
     */
    public static double calcularMedia(List<Double> valores) {
        if (valores == null || valores.isEmpty()) {
            throw new IllegalArgumentException("A lista de valores não pode ser nula ou vazia.");
        }
        double soma = 0.0;
        for (double valor : valores) {
            soma += valor;
        }
        return soma / valores.size();
    }

    /**
     * Calcula o desvio padrão de uma lista de valores.
     *
     * @param valores Lista de valores.
     * @param media   Média dos valores.
     * @return Desvio padrão dos valores.
     */
    public static double calcularDesvioPadrao(List<Double> valores, double media) {
        if (valores == null || valores.isEmpty()) {
            throw new IllegalArgumentException("A lista de valores não pode ser nula ou vazia.");
        }
        double somaQuadrados = 0.0;
        for (double valor : valores) {
            somaQuadrados += Math.pow(valor - media, 2);
        }
        return Math.sqrt(somaQuadrados / valores.size());
    }
}

