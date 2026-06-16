/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 12/06/2026
* Ultima alteracao.: 
* Nome.............: Servidor.java
* Funcao...........: 
*******************************************************************/

package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor extends Thread {

  private final int PORTA_UDP = 8080;
  private final int PORTA_TCP = 8081;

  private InetAddress ipServidor;
  private DatagramSocket endpointServidor;

  public Servidor() {
    try {
      ipServidor = InetAddress.getLocalHost();
      endpointServidor = new DatagramSocket(PORTA_UDP, ipServidor);
      System.out.println("SERVIDOR estabelecido: ip = " + ipServidor);
    } catch (Exception e) {
      System.out.println("ERRO: Nao foi possivel iniciar o servidor!");
    }
  }

  @Override
  public synchronized void start() {

    new Thread(() -> {
      try {
        ServerSocket servidor = new ServerSocket(PORTA_TCP);
        while (true) {
          System.out.println("O SERVIDOR esta esperando receber alguma conexao...");
          Socket conexao = servidor.accept();
          new Thread(() -> {
            ObjectInputStream entrada;
            try {
              entrada = new ObjectInputStream(conexao.getInputStream());
              String apduRecebida = (String) entrada.readObject();
              System.out.println("APDU recebida: " + apduRecebida);
              processarApdu(apduRecebida);
            } catch (IOException | ClassNotFoundException e) {
              System.out.println("ERRO: Nao foi possivel receber a APDU do cliente!");
            }
          }).start();
        }
      } catch (Exception e) {
        System.out.println("ERRO: Nao foi possivel iniciar o socket TCP!");
      }
    }).start();

    try {
      while (true) {
        byte[] dadosEntrada = new byte[1024];

        DatagramPacket pacoteRecebido = new DatagramPacket(dadosEntrada, dadosEntrada.length);
        System.out.println("O SERVIDOR esta esperando receber algum pacote...");
        endpointServidor.receive(pacoteRecebido);

        String apduRecebida = new String(pacoteRecebido.getData());
        new Thread(() -> {
          System.out.println("APDU recebida: " + apduRecebida);
          processarApdu(apduRecebida);
        }).start();
      }
    } catch (Exception e) {
      System.out.println("ERRO: Nao foi possivel iniciar o socket UDP!");
    }
  }

  /*
   * Metodo: processarApdu
   * Funcao: Pegar a APDU recebida e determinar qual eh o comando realizar
   * Parametros: apduRecebida = APDU enviada pelo servidor
   * Retorno: void
   */
  public void processarApdu(String apduRecebida) {
    String[] partes = apduRecebida.split("~~");
    switch (partes[0]) {
      case "JOIN":

        break;
      case "LEAVE":

        break;
      case "SEND":
        byte[] dadosEnviados = new byte[1024];

        dadosEnviados = apduRecebida.getBytes();

        // DatagramPacket datagramaEnviado = new DatagramPacket(dadosEnviados, dadosEnviados.length, ipCliente, PORTA_UDP);
        // endpointServidor.send(datagramaEnviado);
        break;
      case "SENDPVT":

        break;

      default:
        break;
    }
  } // fim do metodo processarApdu

}
