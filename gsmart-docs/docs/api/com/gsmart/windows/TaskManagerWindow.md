# Classe: TaskManagerWindow

**Pacote:** `com.gsmart.windows`

## Descrição Geral

Uma janela (JFrame) que funciona como a "Central de Monitoramento".
Ela exibe uma tabela (JTable) com todas as tarefas de pipeline ativas,
mostrando informações cruciais como seu status e tempo de execução.

Esta classe se comunica com o {@link PipelineManager} para receber atualizações
em tempo real e permite que o usuário interaja com as tarefas (visualizar,
parar ou reiniciar) diretamente pela interface.

## Métodos

