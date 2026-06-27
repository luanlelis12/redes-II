/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 12/06/2026
* Ultima alteracao.: 
* Nome.............: Cliente.java
* Funcao...........: 
*******************************************************************/

package model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import controller.clienteController;

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
      e.printStackTrace();
    }
  }

  @Override
  public synchronized void start() {
    new Thread(() -> {
      try {
        while (true) {
          byte[] dadosEntrada = new byte[1024];

          DatagramPacket pacoteRecebido = new DatagramPacket(dadosEntrada, dadosEntrada.length);
          System.out.println("CLIENTE - esperando uma mensagem...");
          endpointCliente.receive(pacoteRecebido);

          String apduRecebida = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength()).trim();
          System.out.println("CLIENTE - Recebeu apdu " + apduRecebida);
          new Thread(() -> {
            processarApdu(apduRecebida);
          }).start();
        }
      } catch (Exception e) {
        System.out.println("CLIENTE - ERRO: Nao foi possivel receber a mensagem!");
        e.printStackTrace();
      }
    }).start();
  }

  /*
   * Metodo: entrarGrupo
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  private void processarApdu(String apduRecebida) {
    System.out.println("oi");
    ArrayList<String> list = new ArrayList<>();
    int indice = 0;
    boolean ehFlag = true;
    for (int i = 0; i < apduRecebida.length(); i++) {
      if (apduRecebida.charAt(i) == '{') {
        i++;
      } else if (apduRecebida.charAt(i) == '~') {
        list.add(apduRecebida.substring(indice, i).trim());
        i++;
        indice = i + 1;
      }
    }
    list.add(apduRecebida.substring(indice, apduRecebida.length()));
    int resultSize = list.size();
    String[] result = new String[resultSize];
    String[] partes = list.subList(0, resultSize).toArray(result);
    clienteController.receberMensagem(partes[3], partes[2], partes[1]);
  } // fim do processo

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

      System.out.println("CLIENTE - Enviando APDU JOIN para o servidor");
      saida.writeObject(apdu);
      saida.flush();
      socketCliente.close();
    } catch (Exception e) {
      System.out.println("CLIENTE - ERRO: Nao foi possivel entrar no grupo!");
      e.printStackTrace();
    } // fim try-catch
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

      System.out.println("CLIENTE - Enviando APDU LEAVE para o servidor");
      saida.writeObject(apdu);
      saida.flush();
      socketCliente.close();
    } catch (Exception e) {
      System.out.println("CLIENTE - ERRO: Nao foi possivel sair do grupo!");
      e.printStackTrace();
    } // fim try-catch
  } // fim do metodo sairGrupo

  /*
   * Metodo: enviarMensagemPrivado
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  public void enviarMensagemPrivado(String usuario, String mensagem) {
    try {
      byte[] dadosEnviados = new byte[1024];

      String apdu = new String("SENDPVT~~" + usuario + "~~" + nome + "~~" + mensagem + "\n");
      dadosEnviados = apdu.getBytes();

      System.out.println("CLIENTE - Enviando APDU SENDPVT para o servidor");
      DatagramPacket datagramaEnviado = new DatagramPacket(dadosEnviados, dadosEnviados.length, ipServidor, PORTA_UDP);
      endpointCliente.send(datagramaEnviado);
    } catch (Exception e) {
      System.out.println("CLIENTE - ERRO: Nao foi possivel enviar a mensagem no privado!");
      e.printStackTrace();
    } // fim try-catch
  } // fim do metodo enviarMensagemPrivado

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

      System.out.println("CLIENTE - Enviando APDU SEND para o servidor");
      DatagramPacket datagramaEnviado = new DatagramPacket(dadosEnviados, dadosEnviados.length, ipServidor, PORTA_UDP);
      endpointCliente.send(datagramaEnviado);
    } catch (Exception e) {
      System.out.println("CLIENTE - ERRO: Nao foi possivel enviar a mensagem!");
      e.printStackTrace();
    } // fim try-catch
  } // fim do metodo enviarMensagem

  /*
   * Metodo: fazerLogin
   * Funcao: Conecta via TCP e pergunta se o nome ja esta em uso
   */
  public boolean fazerLogin() {
    try {
      Socket socketCliente = new Socket(ipServidor, PORTA_TCP);

      // Cria a saida primeiro
      ObjectOutputStream saida = new ObjectOutputStream(socketCliente.getOutputStream());
      saida.flush();
      ObjectInputStream entrada = new ObjectInputStream(socketCliente.getInputStream());

      // Envia a APDU de checagem
      saida.writeObject("LOGIN~~" + this.nome);
      saida.flush();

      // Fica travado aqui esperando o servidor responder (LOGIN_OK ou LOGIN_ERROR)
      String resposta = (String) entrada.readObject();
      socketCliente.close();

      return resposta.equals("LOGIN_OK");
    } catch (Exception e) {
      System.out.println("CLIENTE - ERRO: Nao foi possivel comunicar com o servidor!");
      e.printStackTrace();
      return false;
    }
  }

  // Já aproveite e crie o método de Logout para enviar quando a aplicação fechar!
  public void fazerLogout() {
    try {
      Socket socketCliente = new Socket(ipServidor, PORTA_TCP);
      ObjectOutputStream saida = new ObjectOutputStream(socketCliente.getOutputStream());
      saida.writeObject("LOGOUT~~" + this.nome);
      saida.flush();
      socketCliente.close();
    } catch (Exception e) {
      System.out.println("CLIENTE - ERRO: Nao foi possivel fazer logout!");
      e.printStackTrace();
    }
  }

}
