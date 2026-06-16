/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 12/06/2026
* Ultima alteracao.: 
* Nome.............: Cliente.java
* Funcao...........: 
*******************************************************************/

package model;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class Cliente extends Thread {

  private final int PORTA_UDP = 8080;
  private final int PORTA_TCP = 8081;

  private String nome;
  private InetAddress ipCliente;
  private InetAddress ipServidor;
  private DatagramSocket endpointCliente;


  public Cliente(String nome, String ipServidor) {
    try {
      this.nome = nome;
      ipCliente = InetAddress.getLocalHost();
      this.ipServidor = InetAddress.getByName(ipServidor);
      endpointCliente = new DatagramSocket(PORTA_UDP);
      System.out.println("CLIENTE estabelecido: nome = " + nome + " / ip = " + ipCliente);
    } catch (Exception e) {
      System.out.println("ERRO: Nao foi possivel inicializar o cliente");
    }
  }

  @Override
  public synchronized void start() {
    new Thread(() -> {
      try {
        while (true) {
          byte[] dadosEntrada = new byte[1024];
          
          DatagramPacket pacoteRecebido = new DatagramPacket(dadosEntrada, dadosEntrada.length);
          System.out.println("O cliente esta esperando uma mensagem...");
          endpointCliente.receive(pacoteRecebido);

          String apduRecebida = new String(pacoteRecebido.getData());
          // processarApdu(apduRecebida);
        }
      } catch (Exception e) {
        System.out.println("ERRO: Nao foi possivel receber a mensagem!");
      }
    }).start();
  }

  /*
   * Metodo: entrarGrupo
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  public void entrarGrupo(String grupo) {
    try {
      Socket socketCliente = new Socket(ipServidor, PORTA_TCP);

      OutputStream saida1 = socketCliente.getOutputStream();
      ObjectOutputStream saida = new ObjectOutputStream(saida1);

      String apdu = new String("JOIN~~" + grupo + "~~" + nome + "\n");

      saida.writeObject(apdu);
      saida.flush();
      socketCliente.close();
    } catch (Exception e) {
      System.out.println("ERRO: Nao foi possivel entrar no grupo!");
    }
  } // fim do metodo entrarGrupo

  /*
   * Metodo: sairGrupo
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  public void sairGrupo(String grupo) {
    try {
      Socket socketCliente = new Socket(ipServidor, PORTA_TCP);

      OutputStream saida1 = socketCliente.getOutputStream();
      ObjectOutputStream saida = new ObjectOutputStream(saida1);

      String apdu = new String("LEAVE~~" + grupo + "~~" + nome + "\n");

      saida.writeObject(apdu);
      saida.flush();
      socketCliente.close();
    } catch (Exception e) {
      System.out.println("ERRO: Nao foi possivel sair do grupo!");
    }
  } // fim do metodo sairGrupo

  /*
   * Metodo: enviarMensagem
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  public void enviarMensagem(String grupo, String mensagem) {
    try {
      byte[] dadosEnviados = new byte[1024];

      String apdu = new String("SEND~~" + grupo + "~~" + nome + "~~" + mensagem + "\n");
      dadosEnviados = apdu.getBytes();

      DatagramPacket datagramaEnviado = new DatagramPacket(dadosEnviados, dadosEnviados.length, ipServidor, PORTA_UDP);
      endpointCliente.send(datagramaEnviado);
    } catch (Exception e) {
      System.out.println("ERRO: Nao foi possivel enviar a mensagem!");
    }
  } // fim do metodo enviarMensagem

}
