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
      DatagramSocket endpointServidor = new DatagramSocket(PORTA_UDP, ipServidor);
      System.out.println("SERVIDOR estabelecido ip = " + ipServidor);
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
          Socket conexao = servidor.accept();
          new Thread(() -> {
            ObjectInputStream entrada;
            try {
              entrada = new ObjectInputStream(conexao.getInputStream());
              System.out.println((String) entrada.readObject());
              System.out.println("APDU recebida:" + entrada);
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
        endpointServidor.receive(pacoteRecebido);

        String apduRecebida = new String(pacoteRecebido.getData());
        new Thread(() -> {
          System.out.println("APDU recebida:" + apduRecebida);
        }).start();
      }
    } catch (Exception e) {
      System.out.println("ERRO: Nao foi possivel iniciar o socket UDP!");
    }

    super.start();
  }

  private void processarApdu(String apduRecebida) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'processarApdu'");
  }

}
