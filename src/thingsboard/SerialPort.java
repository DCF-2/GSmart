package thingsboard;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SerialPort {

    // Simulação do acesso à lista de portas COM disponíveis
    public static SerialPort[] getCommPorts() {
        // Para fins de exemplo, retornamos um array com uma porta simulada
        return new SerialPort[]{new SerialPort("COM1"), new SerialPort("COM2")};
    }

    // Simulação da obtenção de uma porta específica por nome
    public static SerialPort getCommPort(String portName) {
        // Retorna uma nova instância de SerialPort com o nome fornecido
        return new SerialPort(portName);
    }

    private String portName;
    private InputStream inputStream;
    private OutputStream outputStream;

    // Construtor da classe SerialPort
    public SerialPort(String portName) {
        this.portName = portName;
    }

    // Método para abrir a porta serial
    public void openPort() throws IOException {
        // Simulação de abertura de porta
        System.out.println("Abrindo a porta: " + portName);
        // Normalmente você usaria uma biblioteca como RXTX ou JavaComm aqui para conectar à porta
        inputStream = System.in;  // Simulação do fluxo de entrada
        outputStream = System.out; // Simulação do fluxo de saída
    }

    // Método para verificar o número de bytes disponíveis na porta serial
    public int bytesAvailable() throws IOException {
        // Simulação de bytes disponíveis
        return inputStream.available();
    }

    // Método para ler dados da porta serial
    public int readBytes(byte[] readBuffer, int length) throws IOException {
        // Simulação de leitura de bytes da porta
        int bytesRead = inputStream.read(readBuffer, 0, length);
        return bytesRead;
    }

    // Método para fechar a porta serial
    public void closePort() throws IOException {
        // Simulação de fechamento de porta
        System.out.println("Fechando a porta: " + portName);
        // Fechar fluxos de entrada e saída
        if (inputStream != null) inputStream.close();
        if (outputStream != null) outputStream.close();
    }

    // Método para obter o nome da porta do sistema
    public String getSystemPortName() {
        return portName;
    }

    // Método para enviar dados pela porta serial
    public void writeBytes(byte[] data) throws IOException {
        // Simulação de envio de dados pela porta
        outputStream.write(data);
    }

    // Método para listar todas as portas COM no sistema (apenas simulado aqui)
    public static List<String> listAvailablePorts() {
        List<String> availablePorts = new ArrayList<>();
        availablePorts.add("COM1");
        availablePorts.add("COM2");
        return availablePorts;
    }
}
