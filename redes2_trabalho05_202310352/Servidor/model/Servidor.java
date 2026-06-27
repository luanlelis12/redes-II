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
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Servidor extends Thread {

  private final int PORTA_UDP = 8080;
  private final int PORTA_TCP = 8081;

  private InetAddress ipServidor;
  private DatagramSocket endpointServidor;
  private static Semaphore mutex = new Semaphore(1);

  private Map<String, ArrayList<Usuario>> grupos = new HashMap<>();
  private Map<String, Usuario> usuariosOnline = new HashMap<>();

  public Servidor() {
    try {
      ipServidor = InetAddress.getLocalHost();
      endpointServidor = new DatagramSocket(PORTA_UDP);
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
          System.out.println("SERVIDOR TCP - esperando receber alguma conexao...");
          Socket conexao = servidor.accept();
          new Thread(() -> {
            try {
              System.out.println("SERVIDOR TCP - estabelecendo conexao com ip = " + conexao.getInetAddress() + ".");

              ObjectOutputStream saida = new ObjectOutputStream(conexao.getOutputStream());
              saida.flush();
              ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream());

              String apduRecebida = ((String) entrada.readObject()).trim();
              System.out.println("SERVIDOR TCP - APDU recebida: " + apduRecebida + ".");

              processarApdu(apduRecebida, conexao.getInetAddress(), saida);
            } catch (IOException | ClassNotFoundException e) {
              System.out.println("SERVIDOR TCP - ERRO: Nao foi possivel receber a APDU do cliente!");
            }
          }).start();
        }
      } catch (Exception e) {
        System.out.println("SERVIDOR TCP - ERRO: Nao foi possivel iniciar o socket TCP!");
      }
    }).start();

    try {
      while (true) {
        byte[] dadosEntrada = new byte[1024];

        DatagramPacket pacoteRecebido = new DatagramPacket(dadosEntrada, dadosEntrada.length);
        System.out.println("SERVIDOR UDP - esperando receber algum pacote...");

        endpointServidor.receive(pacoteRecebido);
        System.out.println("SERVIDOR UDP - recebendo pacote do ip = " + pacoteRecebido.getAddress() + ".");

        String apduRecebida = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength()).trim();

        // ======= INTERCEPTA O BROADCAST DE DESCOBERTA =======
        if (apduRecebida.equals("DISCOVER")) {
          byte[] resposta = "DISCOVER_OK".getBytes();
          // Responde diretamente para a porta e IP de quem gritou na rede
          DatagramPacket pacoteResposta = new DatagramPacket(resposta, resposta.length, pacoteRecebido.getAddress(),
              pacoteRecebido.getPort());
          endpointServidor.send(pacoteResposta);
          continue;
        }
        // ====================================================

        new Thread(() -> {
          System.out.println("SERVIDOR UDP - APDU recebida: " + apduRecebida + ".");
          processarApdu(apduRecebida, pacoteRecebido.getAddress(), null);
        }).start();
      } // fim do while
    } catch (Exception e) {
      System.out.println("SERVIDOR UDP - ERRO: Nao foi possivel iniciar o socket UDP!");
    } // fim do try-catch
  }

  /*
   * Metodo: processarApdu
   * Funcao: Pegar a APDU recebida e determinar qual eh o comando realizar
   * Parametros: apduRecebida = APDU enviada pelo servidor
   * Retorno: void
   */
  public void processarApdu(String apduRecebida, InetAddress ipCliente, ObjectOutputStream saida) {
    String[] partes = dividirApdu(apduRecebida);
    switch (partes[0]) {
      case "JOIN":
        try {
          String grupo = partes[1];
          String nome = partes[2];
          mutex.acquire();
          inserirNoGrupo(grupo, nome, ipCliente);
          mutex.release();
        } catch (Exception e) {
          System.out.println("SERVIDOR - ERRO: Nao foi possivel processar a APDU JOIN.");
        }
        break;
      case "LEAVE":
        try {
          String grupo = partes[1];
          String nome = partes[2];
          mutex.acquire();
          sairDoGrupo(grupo, nome);
          mutex.release();
        } catch (Exception e) {
          System.out.println("SERVIDOR - ERRO: Nao foi possivel processar a APDU LEAVE.");
        }
        break;
      case "SEND":
        try {
          String grupo = partes[1];
          String nome = partes[2];
          String mensagem = partes[3];
          mutex.acquire();
          enviarMensagem(mensagem, grupo, nome);
          mutex.release();
        } catch (Exception e) {
          System.out.println("SERVIDOR - ERRO: Nao foi possivel processar a APDU SEND.");
        }
        break;
      case "SENDPVT":
        try {
          String usuario = partes[1];
          String nome = partes[2];
          String mensagem = partes[3];
          mutex.acquire();
          enviarMensagem(mensagem, usuario, nome);
          mutex.release();
        } catch (Exception e) {
          System.out.println("SERVIDOR - ERRO: Nao foi possivel processar a APDU SENDPVT.");
        }
        break;
      case "LOGIN":
        try {
          String nome = partes[1];
          mutex.acquire();
          logarUsuario(nome, ipCliente, saida);
        } catch (Exception e) {
          System.out.println("SERVIDOR - ERRO: Nao foi possivel processar a APDU LOGIN.");
          e.getStackTrace();
        }
        break;

      case "LOGOUT":
        try {
          String nome = partes[1];
          mutex.acquire();

          if (usuariosOnline.containsKey(nome)) {
            usuariosOnline.remove(nome);
          }

          mutex.release();
          System.out.println("SERVIDOR TCP - Usuario " + nome + " deslogado.");

        } catch (Exception e) {
          System.out.println("SERVIDOR - ERRO: Nao foi possivel processar a APDU LOGOUT.");
          e.printStackTrace();
          mutex.release();
        }
        break;

      default:
        break;
    }
  } // fim do metodo processarApdu

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

  public void logarUsuario(String nome, InetAddress ipCliente, ObjectOutputStream saida) {
    try {
      boolean nomeEmUso = usuariosOnline.containsKey(nome);

      boolean aprovado = !nomeEmUso;

      if (aprovado) {
        Usuario novoUsuario = new Usuario(ipCliente, nome, PORTA_TCP);
        usuariosOnline.put(nome, novoUsuario);
      }

      mutex.release();

      if (saida != null) {
        saida.writeObject(aprovado ? "LOGIN_OK" : "LOGIN_ERROR");
        saida.flush();
      }
      System.out.println("SERVIDOR TCP - Validando LOGIN de " + nome + " (Aprovado: " + aprovado + ").");

    } catch (Exception e) {
      System.out.println("SERVIDOR - ERRO Crítico no logarUsuario:");
      e.printStackTrace();
      mutex.release();
    }
  }

  public void inserirNoGrupo(String nomeGrupo, String nomeUsuario, InetAddress ipCliente) {
    Usuario novoUsuario = new Usuario(ipCliente, nomeUsuario, PORTA_TCP);
    if (grupos.containsKey(nomeGrupo)) {
      if (grupos.get(nomeGrupo).contains(novoUsuario)) {
        System.out.println("SERVIDOR TCP - " + nomeUsuario + " ja esta no grupo " + nomeGrupo + ".");
      } else {
        System.out.println("SERVIDOR TCP - Adicionando " + nomeUsuario + " no grupo " + nomeGrupo + ".");
        grupos.get(nomeGrupo).add(novoUsuario);
      }
    } else {
      System.out
          .println("SERVIDOR TCP - Criando novo grupo " + nomeGrupo + " adicionando usuario " + nomeUsuario + ".");
      ArrayList<Usuario> listaUsuario = new ArrayList<>();
      listaUsuario.add(novoUsuario);
      grupos.put(nomeGrupo, listaUsuario);
    }
  }

  public void sairDoGrupo(String nomeGrupo, String nomeUsuario) {
    ArrayList<Usuario> listaDeUsuarios = grupos.get(nomeGrupo);
    for (Usuario usuario : listaDeUsuarios) {
      if (usuario.getNome().equals(nomeUsuario)) {
        grupos.get(nomeGrupo).remove(usuario);
        System.out.println("SERVIDOR TCP - Removendo o usuario " + nomeUsuario + " do grupo " + nomeGrupo + ".");
        break;
      }
    }
    if (grupos.get(nomeGrupo).isEmpty()) {
      System.out.println("SERVIDOR TCP - O grupo " + nomeGrupo + " possui zero usuarios apagando grupo.");
      grupos.remove(nomeGrupo);
    }
  }

  public void enviarMensagem(String mensagem, String nomeGrupo, String nomeUsuario) {
    ArrayList<Usuario> listaDeUsuarios = grupos.get(nomeGrupo);
    for (Usuario usuario : listaDeUsuarios) {
      if (!(usuario.getNome().equals(nomeUsuario))) {
        try {
          byte[] dadosEnviados = new byte[1024];

          String apdu = new String("SEND~~" + nomeGrupo + "~~" + nomeUsuario + "~~" + mensagem + "\n");
          dadosEnviados = apdu.getBytes();

          DatagramPacket datagramaEnviado = new DatagramPacket(dadosEnviados, dadosEnviados.length, usuario.getIp(),
              PORTA_UDP);
          System.out.println(
              "SERVIDOR UDP - enviando mensagem para usuario " + usuario.getNome() + " ip = " + usuario.getIp() + ".");
          endpointServidor.send(datagramaEnviado);
        } catch (Exception e) {
          System.out.println("SERVIDOR UDP - ERRO: Nao foi possivel enviar a mensagem!");
        }
      }
    }
  }

  public void enviarMensagemPrivado(String mensagem, String nomeUsuarioDestino, String nomeUsuario) {
    Usuario usuarioDestino = usuariosOnline.get(nomeUsuarioDestino);
    try {
      byte[] dadosEnviados = new byte[1024];

      String apdu = new String("SENDPVT~~" + nomeUsuarioDestino + "~~" + nomeUsuario + "~~" + mensagem + "\n");
      dadosEnviados = apdu.getBytes();

      DatagramPacket datagramaEnviado = new DatagramPacket(dadosEnviados, dadosEnviados.length, usuarioDestino.getIp(),
          PORTA_UDP);
      System.out.println(
          "SERVIDOR UDP - enviando mensagem para usuario " + usuarioDestino.getNome() + " ip = "
              + usuarioDestino.getIp() + ".");
      endpointServidor.send(datagramaEnviado);
    } catch (Exception e) {
      System.out.println("SERVIDOR UDP - ERRO: Nao foi possivel enviar a mensagem privada!");
    }
  }

}