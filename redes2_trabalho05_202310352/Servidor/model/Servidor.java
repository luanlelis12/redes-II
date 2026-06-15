/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 12/06/2026
* Ultima alteracao.: 
* Nome.............: Servidor.java
* Funcao...........: 
*******************************************************************/

package Servidor.model;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Servidor {
  final int PORTA_UDP = 8080;
  final int PORTA_TCP = 8081;

  public Servidor() {

    try {
      DatagramSocket conexaoServidor = new DatagramSocket(PORTA_UDP);
      byte[] dadosEntrada = new byte[1024];

      DatagramPacket pacoteRecebido = new DatagramPacket(dadosEntrada, dadosEntrada.length);
      conexaoServidor.receive(pacoteRecebido);
      
      String mensagemRecebida = new String(pacoteRecebido.getData());
      System.out.println(mensagemRecebida);
    } catch (Exception e) {
      // TODO: handle exception
    }

  }

}
