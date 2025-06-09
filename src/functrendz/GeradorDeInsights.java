package functrendz;
import static functrendz.Manutencao.StatusManutencao;
// functrendz/GeradorDeInsights.java
/**
 * Classe final do projeto, responsável por gerar insights acionáveis
 * a partir da correlação dos dados de produção e eficiência.
 */
public class GeradorDeInsights {

    private static final double LIMIAR_EFICIENCIA_MATERIAL = 97.0; // %
    private static final double LIMIAR_TEMPERATURA_ALTA = 78.0;   // Celsius
    private static final double LIMIAR_FATOR_POTENCIA_BAIXO = 0.90;

    /**
     * Analisa o estado atual do sistema e imprime insights relevantes.
     * @param eficienciaMaterial Eficiência de material atual (%)
     * @param fatorPotencia Fator de potência atual
     * @param temperatura Temperatura atual
     * @param statusManutencao Status retornado pela classe de Manutenção
     * @param falhaPrevista Booleano da classe de Previsão de Falhas
     */
    public static void gerarInsightsDoCiclo(double eficienciaMaterial, double fatorPotencia, double temperatura,
                                            StatusManutencao statusManutencao, boolean falhaPrevista) {

        boolean insightGerado = false;

        // Regra 1: Causa-Raiz para Baixa Eficiência
        if (eficienciaMaterial < LIMIAR_EFICIENCIA_MATERIAL && temperatura > LIMIAR_TEMPERATURA_ALTA) {
            System.out.println("-> INSIGHT DE PROCESSO: A eficiência de material está abaixo do ideal. Isso pode estar ligado à alta temperatura de operação, causando maior degradação da massa.");
            insightGerado = true;
        }

        // Regra 2: Oportunidade de Custo
        if (fatorPotencia < LIMIAR_FATOR_POTENCIA_BAIXO) {
            System.out.println("-> INSIGHT DE CUSTO: O fator de potência está baixo. A correção pode reduzir custos com energia elétrica e multas da concessionária.");
            insightGerado = true;
        }

        // Regra 3: Confirmação de Problema
        if (statusManutencao == StatusManutencao.ALERTA_TEMPERATURA && eficienciaMaterial < LIMIAR_EFICIENCIA_MATERIAL) {
            System.out.println("-> INSIGHT DE MANUTENÇÃO: O alerta de alta temperatura coincide com a queda na eficiência. A manutenção é urgente para evitar perdas de produção.");
            insightGerado = true;
        }

        // Regra 4: Causa-Raiz da Previsão de Falha
        if (falhaPrevista) {
            System.out.println("-> INSIGHT DE FALHA: Previsão de falha acionada! Dados indicam que a principal causa é o desvio contínuo nos padrões de temperatura e potência. Inspecionar sistema de refrigeração e alimentação elétrica.");
            insightGerado = true;
        }

        if (!insightGerado) {
            System.out.println("-> Nenhum insight crítico gerado. O processo está operando dentro dos parâmetros esperados.");
        }
    }
}