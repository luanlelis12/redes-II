/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 12/06/2026
* Ultima alteracao.: 
* Nome.............: Cliente.java
* Funcao...........: 
*******************************************************************/

package model;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Cliente extends Thread {
  final int PORTA_UDP = 8080;
  final int PORTA_TCP = 8081;

  public Cliente() {


  }

  @Override
  public synchronized void start() {
    
    try {
      DatagramSocket conexaoCliente = new DatagramSocket();// conexao nao eh o melhor nome
      InetAddress enderecoIPServidor = InetAddress.getByName("10.227.119.130");

      byte[] dadosSaida = new byte[1024];
      String mensagemEnviada = new String("MUITO FACIL ;-)");// substituir por APDU+mensagem
      byte[] saida = mensagemEnviada.getBytes();

      DatagramPacket pacoteEnviado = new DatagramPacket(saida, saida.length,
          enderecoIPServidor, PORTA_UDP);

      
    } catch (Exception e) {
      // TODO: handle exception
    }

  }

}
