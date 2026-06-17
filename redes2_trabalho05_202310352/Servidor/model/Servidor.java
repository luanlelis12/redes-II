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
          System.out.println("SERVIDOR - esperando receber alguma conexao...");
          Socket conexao = servidor.accept();
          System.out.println("SERVIDOR - estabelecendo conexao com ip = " + conexao.getInetAddress() + ".");
          new Thread(() -> {
            ObjectInputStream entrada;
            try {
              entrada = new ObjectInputStream(conexao.getInputStream());
              String apduRecebida = (String) entrada.readObject();
              System.out.println("APDU recebida: " + apduRecebida);
              processarApdu(apduRecebida, conexao.getInetAddress());
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
        System.out.println("SERVIDOR - esperando receber algum pacote...");
        endpointServidor.receive(pacoteRecebido);
        System.out.println("SERVIDOR - recebendo pacote do ip = "+ pacoteRecebido.getAddress() +".");

        String apduRecebida = new String(pacoteRecebido.getData());
        new Thread(() -> {
          System.out.println("APDU recebida: " + apduRecebida);
          processarApdu(apduRecebida, pacoteRecebido.getAddress());
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
  public void processarApdu(String apduRecebida, InetAddress ipCliente) {
    String[] partes = apduRecebida.split("~~");
    switch (partes[0]) {
      case "JOIN":
        try {
          String grupo = partes[1];
          String nome = partes[2];
          mutex.acquire();
          inserirNoGrupo(grupo, nome, ipCliente);
          mutex.release();
        } catch (Exception e) {
          // TODO: handle exception
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
          // TODO: handle exception
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
          // TODO: handle exception
        }
        break;
      case "SENDPVT":

        break;

      default:
        break;
    }
  } // fim do metodo processarApdu

  private void inserirNoGrupo(String nomeGrupo, String nomeUsuario, InetAddress ipCliente) {
    Usuario novoUsuario = new Usuario(ipCliente, nomeUsuario, PORTA_TCP);
    if (grupos.containsKey(nomeGrupo)) {
      if (grupos.get(nomeGrupo).contains(novoUsuario)) {
        System.out.println("SERVIDOR - " + nomeUsuario + " ja esta no grupo " + nomeGrupo + ".");
      } else {
        System.out.println("SERVIDOR - Adicionando " + nomeUsuario + " no grupo " + nomeGrupo + ".");
        grupos.get(nomeGrupo).add(novoUsuario);
      }
    } else {
      System.out.println("SERVIDOR - Criando novo grupo " + nomeGrupo + " adicionando usuario " + nomeUsuario + ".");
      ArrayList<Usuario> listaUsuario = new ArrayList<>();
      listaUsuario.add(novoUsuario);
      grupos.put(nomeGrupo, listaUsuario);
    }
  }

  private void sairDoGrupo(String nomeGrupo, String nomeUsuario) {
    ArrayList<Usuario> listaDeUsuarios = grupos.get(nomeGrupo);
    for (Usuario usuario : listaDeUsuarios) {
      if (usuario.getNome() == nomeUsuario) {
        grupos.get(nomeGrupo).remove(usuario);
        System.out.println("SERVIDOR - Removendo o usuario " + nomeUsuario);
      }
    }

  }

  private void enviarMensagem(String mensagem, String nomeGrupo, String nomeUsuario) {
    ArrayList<Usuario> listaDeUsuarios = grupos.get(nomeGrupo);
    for (Usuario usuario : listaDeUsuarios) {
      if (usuario.getNome() != nomeUsuario) {
        try {
          byte[] dadosEnviados = new byte[1024];

          String apdu = new String("SEND~~" + nomeGrupo + "~~" + nomeUsuario + "~~" + mensagem + "\n");
          dadosEnviados = apdu.getBytes();

          DatagramPacket datagramaEnviado = new DatagramPacket(dadosEnviados, dadosEnviados.length, usuario.getIp(),
              PORTA_UDP);
          System.out.println("SERVIDOR - enviando mensagem para usuario " + usuario.getNome());
          endpointServidor.send(datagramaEnviado);
        } catch (Exception e) {
          System.out.println("ERRO: Nao foi possivel enviar a mensagem!");
        }
      }
    }
  }

}

class Usuario {
  InetAddress ip;
  String nome;
  int porta;

  public Usuario(InetAddress ip, String nome, int porta) {
    this.ip = ip;
    this.nome = nome;
    this.porta = porta;
  }

  public InetAddress getIp() {
    return ip;
  }

  public void setIp(InetAddress ip) {
    this.ip = ip;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public int getPorta() {
    return porta;
  }

  public void setPorta(int porta) {
    this.porta = porta;
  }

}