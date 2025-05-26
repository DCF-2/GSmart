package thingsboard;

import com.fazecast.jSerialComm.*;

public class JSerialBasico {

    static MonitorSerial Serial1 = new MonitorSerial();

    public static void main(String[] args) {
        // Obtendo as portas seriais disponíveis no sistema
        SerialPort[] ports = SerialPort.getCommPorts(); // Renomeado para `ports`, sem a duplicação
        if (ports == null || ports.length == 0) {
            System.out.println("Nenhuma porta serial encontrada ou erro ao acessar.");
            return;
        }

        // Exibindo os nomes das portas seriais encontradas
        System.out.println("Portas seriais encontradas:");
        for (SerialPort p : ports) {
            System.out.println(p.getSystemPortName()); // Exibe apenas o nome da porta
        }

        // Inicializando o MonitorSerial (se necessário)
        Serial1.start();
    }
}
