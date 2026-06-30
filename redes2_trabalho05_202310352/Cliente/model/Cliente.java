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
import java.util.Arrays;

import controller.clienteController;

public class Cliente extends Thread {

  private final int PORTA_UDP = 8080;
  private final int PORTA_TCP = 8081;

  private final String GRUPO = "grupo";
  private final String PRIVADO = "priv";

  private String nomeCliente;
  private InetAddress ipCliente;
  private InetAddress ipServidor;
  private DatagramSocket endpointCliente;

  public Cliente(String nomeCliente, String ipServidor) {
    try {
      this.nomeCliente = nomeCliente;
      ipCliente = InetAddress.getLocalHost();
      this.ipServidor = InetAddress.getByName(ipServidor);
      endpointCliente = new DatagramSocket(PORTA_UDP);
      System.out.println("CLIENTE estabelecido: nome = " + nomeCliente + " / ip = " + ipCliente);
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

          if (apduRecebida.equals("DISCOVER"))
            continue;

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
    String[] partes = dividirApdu(apduRecebida);
    switch (partes[0]) {
      case "SEND":
        try {
          String grupoDestino = partes[1];
          String usuarioRemetente = partes[2];
          String mensagem = partes[3];
          clienteController.receberMensagem(mensagem, grupoDestino, usuarioRemetente, GRUPO);
        } catch (Exception e) {
          System.out.println("CLIENTE - ERRO: Nao foi possivel processar a APDU SEND.");
        }
        break;
      case "SENDPVT":
        try {
          String usuarioDestino = partes[1];
          String usuarioRemetente = partes[2];
          String mensagem = partes[3];
          clienteController.receberMensagem(mensagem, usuarioDestino, usuarioRemetente, PRIVADO);
        } catch (Exception e) {
          System.out.println("CLIENTE - ERRO: Nao foi possivel processar a APDU SENDPVT.");
        }
        break;
      case "LISTCVS":
        try {
          ArrayList<String> grupos = new ArrayList<>();
          // Se o tamanho for maior que 1, significa que existem grupos na lista
          if (partes.length > 1) {
            grupos = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(partes, 1, partes.length)));
          }
          // Envia a lista para o controlador principal abrir a tela
          clienteController.exibirListaConversas(grupos, GRUPO);
        } catch (Exception e) {
          System.out.println("CLIENTE - ERRO: Nao foi possivel processar a APDU LISTCVS.");
        }
        break;
      case "LISTMEMBERS":
        try {
          ArrayList<String> membros = new ArrayList<>();
          if (partes.length > 1) {
            membros = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(partes, 1, partes.length)));
          }
          // Envia a lista para o controlador principal abrir a tela
          clienteController.exibirListaConversas(membros, PRIVADO);
        } catch (Exception e) {
          System.out.println("CLIENTE - ERRO: Nao foi possivel processar a APDU LISTMEMBERS.");
        }
        break;

      default:
        break;
    }
  } // fim do processo

  public String[] dividirApdu(String apdu) {
    ArrayList<String> list = new ArrayList<>();
    int indice = 0;
    for (int i = 0; i < apdu.length(); i++) {
      if (apdu.charAt(i) == '{') {
        i++;
      } else if (apdu.charAt(i) == '~') {
        list.add(apdu.substring(indice, i).trim());
        i++;
        indice = i + 1;
      }
    }
    list.add(apdu.substring(indice, apdu.length()));
    int resultSize = list.size();
    String[] result = new String[resultSize];
    return list.subList(0, resultSize).toArray(result);
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

      String apdu = new String("JOIN~~" + grupo + "~~" + nomeCliente + "\n");

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

      String apdu = new String("LEAVE~~" + grupo + "~~" + nomeCliente + "\n");

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
  public void enviarMensagemPrivado(String usuarioDestino, String mensagem) {
    try {
      byte[] dadosEnviados = new byte[1024];

      String apdu = new String("SENDPVT~~" + usuarioDestino + "~~" + nomeCliente + "~~" + mensagem + "\n");
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

      String apdu = new String("SEND~~" + grupo + "~~" + nomeCliente + "~~" + mensagem + "\n");
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
      saida.writeObject("LOGIN~~" + this.nomeCliente);
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

  public void fazerLogout() {
    try {
      Socket socketCliente = new Socket(ipServidor, PORTA_TCP);
      ObjectOutputStream saida = new ObjectOutputStream(socketCliente.getOutputStream());
      saida.writeObject("LOGOUT~~" + this.nomeCliente);
      saida.flush();
      socketCliente.close();
    } catch (Exception e) {
      System.out.println("CLIENTE - ERRO: Nao foi possivel fazer logout!");
      e.printStackTrace();
    }
  }

  public void solicitarListaGrupos() {
    try {
      byte[] dadosEnviados = new byte[1024];
      String apdu = new String("LISTCVS~~" + nomeCliente + "\n");
      dadosEnviados = apdu.getBytes();

      System.out.println("CLIENTE - Solicitando lista de grupos ao servidor...");
      DatagramPacket datagramaEnviado = new DatagramPacket(dadosEnviados, dadosEnviados.length, ipServidor, PORTA_UDP);
      endpointCliente.send(datagramaEnviado);
    } catch (Exception e) {
      System.out.println("CLIENTE - ERRO: Nao foi possivel solicitar grupos!");
    }
  }

  public void solicitarListaMembros(String grupo) {
    try {
      byte[] dadosEnviados = new byte[1024];

      String apdu = new String("LISTMEMBERS~~" + grupo + "~~" + nomeCliente + "\n");
      dadosEnviados = apdu.getBytes();

      System.out.println("CLIENTE - Enviando APDU SEND para o servidor");
      DatagramPacket datagramaEnviado = new DatagramPacket(dadosEnviados, dadosEnviados.length, ipServidor, PORTA_UDP);
      endpointCliente.send(datagramaEnviado);
    } catch (Exception e) {
      System.out.println("CLIENTE - ERRO: Nao foi possivel enviar a mensagem!");
      e.printStackTrace();
    } // fim try-catch
  }

  public void desligarCliente() {
    endpointCliente.close();
  }

  public int getPORTA_UDP() {
    return PORTA_UDP;
  }

  public int getPORTA_TCP() {
    return PORTA_TCP;
  }

  public String getGRUPO() {
    return GRUPO;
  }

  public String getPRIVADO() {
    return PRIVADO;
  }

  public String getNomeCliente() {
    return nomeCliente;
  }

  public void setNomeCliente(String nomeCliente) {
    this.nomeCliente = nomeCliente;
  }

  public InetAddress getIpCliente() {
    return ipCliente;
  }

  public void setIpCliente(InetAddress ipCliente) {
    this.ipCliente = ipCliente;
  }

  public InetAddress getIpServidor() {
    return ipServidor;
  }

  public void setIpServidor(InetAddress ipServidor) {
    this.ipServidor = ipServidor;
  }

  public DatagramSocket getEndpointCliente() {
    return endpointCliente;
  }

  public void setEndpointCliente(DatagramSocket endpointCliente) {
    this.endpointCliente = endpointCliente;
  }

}
