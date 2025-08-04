# Classe: `CsvExportService`

**Pacote:** `com.gsmart.services`

## Descrição Geral

Serviço responsável por exportar dados de telemetria para ficheiros CSV.  
  
Esta classe gere a criação, escrita e rotação de ficheiros de log de telemetria,  
garantindo que apenas os 10 registos mais recentes (correspondentes a 200 horas  
de dados) são mantidos, implementando uma lógica de pilha (buffer circular).

## Métodos da Classe

---

### `public void exportData(List<Map<String, Object>> telemetryData)`

Método principal que recebe os dados de telemetria e os escreve num novo ficheiro CSV.  
Antes de escrever, ele gere a rotação dos ficheiros para garantir que o limite não é excedido.

- **Parâmetro:** `telemetryData` - Uma lista de mapas, onde cada mapa representa uma linha de dados.


---

### `private void manageFileRotation()`

Verifica o número de ficheiros no diretório de exportação.  
Se o número de ficheiros exceder o limite, o ficheiro mais antigo é removido.

