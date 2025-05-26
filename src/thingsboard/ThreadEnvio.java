/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package thingsboard;



import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static thingsboard.SimuladordeDispositivo.IP_TB;

/**
 * @author tb01
 */
public class ThreadEnvio extends Thread {

    private int l, n;
    private int instanciadeEnvio;
    int inicio, fim;

    public void setInstanciadeEnvio(int instanciadeEnvio) {
        this.instanciadeEnvio = instanciadeEnvio;
    }

    public void setL(int l) {
        this.l = l;
    }

    public void setN(int n) {
        this.n = n;
    }

    void Thingsboard(String Token, String Parametro, double Valor) throws IOException {

        System.out.println("Instancia de Envio->" + instanciadeEnvio);
        System.out.println("Enviando para o Thingsboard->" + Token + "-" + Parametro + "=" + Valor);

        String command = "./ScriptTB.sh " + String.valueOf(Valor) + " " + Token + " " + IP_TB + " " + Parametro;

        Process process = Runtime.getRuntime().exec(command);
    }

    public void run() {

        String Msg = "";
        double Tensao, Corrente, PotenciaAtiva, PotenciaReativa, Fator_Potencia;
        String[] Parametro = {"Tensão", "Corrente", "PotenciaAtiva", "PotenciaReativa", "Fator_Potencia"};
        String[] DeviceToken = {
                "TUI84RgaDBMhrWP7HtFD", "YaNDHks2fDY3ktCx5UPX", "JT8JwxByYHiZMiozYTIY"
                // Adicione os outros tokens aqui
        };

        // A lógica de envio de dados pode ser implementada aqui
        for (String token : DeviceToken) {
            for (String parametro : Parametro) {
                // Aqui você pode simular a obtenção dos valores para enviar
                // Simulação de valores para a variável "Valor"
                double valor = Math.random() * 100;  // Apenas um valor aleatório para exemplo
                try {
                    Thingsboard(token, parametro, valor);
                } catch (IOException e) {
                    Logger.getLogger(ThreadEnvio.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }
}

