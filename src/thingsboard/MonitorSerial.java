package thingsboard;

import com.fazecast.jSerialComm.*;

public class MonitorSerial extends Thread {
    private String texto;
    private String msg = "Erro";

    // Método getter para a variável msg
    public String getMsg() {
        return msg;
    }

    @Override
    public void run() {
        // Obtendo a porta serial pela qual será realizada a leitura
        SerialPort comPort = SerialPort.getCommPort("tnt1");

        // Tentando abrir a porta serial
        /*if (!comPort.openPort()) {
            System.out.println("Erro ao abrir a porta serial.");
            return;
        }*/

        try {
            while (true) {
                // Espera até que haja bytes disponíveis na porta serial
                while (comPort.bytesAvailable() < 14) {
                    Thread.sleep(20);
                }

                byte[] readBuffer = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(readBuffer, readBuffer.length);

                System.out.println("Read " + numRead + " bytes.");

                // Converte os bytes lidos para uma string
                msg = new String(readBuffer, "UTF-8");

                if (numRead == 14) {
                    // Substring entre '<' e '>', se o número de bytes lidos for 14
                    msg = msg.substring(msg.indexOf('<'), msg.indexOf('>') + 1);
                } else {
                    // Caso o número de bytes lidos não seja 14, imprime o erro
                    System.out.println("->" + msg);
                    msg = "Erro";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Garante que a porta seja fechada no final
            /* comPort.closePort();*/
        }
    }
}