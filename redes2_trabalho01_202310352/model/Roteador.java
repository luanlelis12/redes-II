/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 
* Nome.............: Roteador.java
* Funcao...........: 
*************************************************************** */
package model;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import controller.BackboneController;

public class Roteador extends Thread {
  private int idRoteador;
  private int algoritmo;
  private boolean estaRodando = true;
  private BackboneController controller;
  private BlockingQueue<Pacote> bufferPacotes = new LinkedBlockingQueue<>();
  private ArrayList<Aresta> conexoes = new ArrayList<>();

  public Roteador(int idRoteador, BackboneController controller) {
    this.controller = controller;
    this.idRoteador = idRoteador;
    System.out.println("Roteador " + idRoteador + ": criado com sucesso!");
  }

  @Override
  public void run() {
    System.out.println("Roteador " + idRoteador + ": ligado e aguardando pacotes...");

    while (estaRodando) {
      try {
        Pacote pacoteAtual = bufferPacotes.take();

        processaPacote(pacoteAtual);

      } catch (InterruptedException e) {
        System.out.println("Roteador " + idRoteador + ": desligado.");
        estaRodando = false;
      } // fim do try-catch
    } // fim do while
  }

  public void processaPacote(Pacote pacote) {
    System.out.println("Roteador " + idRoteador + ": Esta processando um pacote. Fila = " + bufferPacotes.size());
    checaEstaAtivo();
    if (pacote.getIdRoteadorDestino() == idRoteador) {
      controller.atualizarContadorPacotesChegados();
      System.out.println("Roteador " + idRoteador + ": Pacote chegou ao destino!!");
    } else {
      if (pacote.getTtl() <= 0 & (algoritmo == 3 | algoritmo == 4)) {
        System.out.println("Roteador " + idRoteador + ": Pacote descartado (TTL=0)");
      } else {
        enviarPacote(pacote);
      } // fim do if
    }

  }

  /****************************************************************
   * Metodo: checaEstaAtivo
   * Funcao: metodo checa se a thread foi desativada (analiando o valor de
   * 'estaAtivo'),
   * caso positivo interrompe a thread atual
   ****************************************************************/
  public void checaEstaAtivo() {
    if (!estaRodando) {
      Thread.currentThread().interrupt();
    }
  }

  public void enviarPacote(Pacote pacote) {
    switch (algoritmo) {
      case 1:
        roteamentoV1(pacote);
        break;
      case 2:
        roteamentoV2(pacote);
        break;
      case 3:
        roteamentoV3(pacote);
        break;
      case 4:
        roteamentoV4(pacote);
        break;

      default:
        System.out.println("Erro: Algoritmo de roteamento nao selecionado");
        break;
    }
  }

  public void roteamentoV1(Pacote pacote) {
    try {
      for (Aresta vizinho : conexoes) {
        System.out.println(
            "Roteador " + idRoteador + ": Pacote enviado para Roteador " + vizinho.getDestino().getIdRoteador());
        Pacote copia = new Pacote(pacote.getIdRoteadorOrigem(), pacote.getIdRoteadorDestino());
        controller.atualizarContadorPacotes(Pacote.getContadorPacotes());
        Thread.sleep(500); // Meio segundo de delay
        controller.exibirPacote(pacote, this, vizinho.getDestino());
        vizinho.getDestino().receberPacote(copia);
      } // fim do for
    } catch (InterruptedException e) {
      System.out.println("Roteador " + idRoteador + ": Falhou em enviar pacote.");
    } // fim do try-catch
  } // fim do metodo roteamentoV1

  public void roteamentoV2(Pacote pacote) {

  }

  public void roteamentoV3(Pacote pacote) {
    // try {
    // for (Aresta vizinho : conexoes) {
    // System.out.println("Roteador " + idRoteador + ": Pacote enviado para Roteador
    // " + vizinho.getDestino().getIdRoteador());
    // Pacote copia = new Pacote(pacote.getIdRoteadorOrigem(),
    // pacote.getIdRoteadorDestino(), pacote.getTtl() - 1);
    // Thread.sleep(500); // Meio segundo de delay
    // controller.atualizarContadorPacotes(Pacote.getContadorPacotes());
    // vizinho.getDestino().receberPacote(copia);
    // } // fim do for
    // } catch (InterruptedException e) {
    // System.out.println("Roteador " + idRoteador + ": Falhou em enviar pacote.");
    // } // fim do try-catch
  }

  public void roteamentoV4(Pacote pacote) {

  }

  public void receberPacote(Pacote p) {
    bufferPacotes.add(p);
  }

  /*
   * Metodo: addVizinho
   * Funcao: Adicionar roteador vizinho e o peso da aresta
   * Parametros: Vizinho = roteador que esta interligado; peso = valor numero para
   * medir a conexao
   * Retorno: void
   */
  public void addVizinho(Roteador vizinho, int peso) {
    this.conexoes.add(new Aresta(vizinho, peso));
  } // fim do addVizinho

  public int getIdRoteador() {
    return idRoteador;
  }

  public ArrayList<Aresta> getConexoes() {
    return conexoes;
  }

  public void setAlgoritmo(int algoritmo) {
    this.algoritmo = algoritmo;
  }

}
