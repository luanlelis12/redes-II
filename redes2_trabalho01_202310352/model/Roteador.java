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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import controller.BackboneController;
import javafx.scene.image.ImageView;

public class Roteador extends Thread {
  private int idRoteador;
  private int algoritmo;
  private boolean rodando = true;
  private BackboneController controller;
  private BlockingQueue<Pacote> bufferPacotes = new LinkedBlockingQueue<>();
  private double[] coordenadaXY;
  private ImageView imageView;

  private Map<Integer, Integer> memoriaSequencia = new HashMap<>();

  private ArrayList<Aresta> conexoes = new ArrayList<>();

  public Roteador(int idRoteador, BackboneController controller) {
    this.controller = controller;
    this.idRoteador = idRoteador;
    System.out.println("Roteador " + idRoteador + ": criado com sucesso!");
  }

  @Override
  public void run() {
    System.out.println("Roteador " + idRoteador + ": ligado e aguardando pacotes...");
    while (rodando) {
      try {
        Pacote pacoteAtual = bufferPacotes.take();

        processaPacote(pacoteAtual);

      } catch (InterruptedException e) {
        System.out.println("Roteador " + idRoteador + ": desligado.");
        rodando = false;
      } // fim do try-catch
    } // fim do while
  }

  /*
   * Metodo: desligar
   * Funcao: define a flag como falsa para desligar o roteador
   * Parametros:
   * Retorno: void
   */
  public void desligar() {
    this.rodando = false;
  } // fim do metodo desligar

  /*
   * Metodo: ligar
   * Funcao: define a flag como verdadeira para ligar o roteador
   * Parametros:
   * Retorno: void
   */
  public void ligar() {
    this.rodando = true;
  } // fim do metodo ligar

  /*
   * Metodo: processaPacote
   * Funcao: processa o pacote e verifica se eh o roteador atual eh o destino dele
   * ou se vai continuar enviando o pacote
   * Parametros: Pacote recebido pelo roteador
   * Retorno: void
   */
  public void processaPacote(Pacote pacote) {
    System.out.println("Roteador " + idRoteador + ": Esta processando um pacote. Fila = " + bufferPacotes.size());
    checaEstaAtivo();
    if (pacote.getIdRoteadorDestino() == idRoteador) {
      controller.atualizarContadorPacotesChegados();
      System.out.println("Roteador " + idRoteador + ": Pacote chegou ao destino!!");
    } else {
      switch (algoritmo) {
        case 1:
          enviarPacote(pacote);
          break;
        case 2:
          enviarPacote(pacote);
          break;
        case 3:
          if (pacote.getTtl() <= 0) { // verifica o ttl
            System.out.println("Roteador " + idRoteador + ": Pacote descartado (TTL=0)");
          } else {
            enviarPacote(pacote);
          } // fim do if
          break;
        case 4:
          int criador = pacote.getIdRoteadorCriador();
          int seqPacote = pacote.getNumeroSequencia();

          int maiorSeqConhecida = memoriaSequencia.getOrDefault(criador, 0);

          if (pacote.getTtl() <= 0) { // verifica o ttl
            System.out.println("Roteador " + idRoteador + ": Pacote descartado (TTL=0)");
          } else if (seqPacote > maiorSeqConhecida) {
            memoriaSequencia.put(criador, seqPacote);
            System.out.println("Roteador " + idRoteador + ": aceitou Pacote Novo (Seq: " + seqPacote + ")");

            enviarPacote(pacote);
          } else {
            System.out.println(
                "Roteador " + idRoteador + ": DESCARTOU pacote repetido (Seq: " + seqPacote + " ja processado).");
          } // fim do if
          break;

        default:
          System.out.println("Erro: Algoritmo de roteamento nao selecionado");
          break;
      } // fim do switch-case
    } // fim do if
  } // fim do metodo processaPacote

  /*
   * Metodo: checaEstaAtivo
   * Funcao: metodo checa se a thread foi desativada (analiando o valor de
   * 'estaAtivo'),
   * caso positivo interrompe a thread atual
   */
  public void checaEstaAtivo() {
    if (!rodando) {
      Thread.currentThread().interrupt();
    } // fim do if
  } // fim do metodo checaEstaAtivo

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
    } // fim do switch-case
  } // fim do metodo enviarPacote

  public void roteamentoV1(Pacote pacote) {
    for (Aresta conexao : conexoes) {
      Roteador vizinho = conexao.getDestino();
      System.out.println(
          "Roteador " + idRoteador + ": Pacote enviado para Roteador " + vizinho.getIdRoteador());
      Pacote copia = new Pacote(this.idRoteador, pacote.getIdRoteadorDestino());
      controller.atualizarContadorPacotes(Pacote.getContadorPacotes());
      controller.exibirPacote(copia, this, vizinho);
    } // fim do for
  } // fim do metodo roteamentoV1

  public void roteamentoV2(Pacote pacote) {
    for (Aresta conexao : conexoes) {
      Roteador vizinho = conexao.getDestino();
      if (pacote.getIdRoteadorOrigemAnterior() != vizinho.getIdRoteador()) {
        System.out.println(
            "Roteador " + idRoteador + ": Pacote enviado para Roteador " + vizinho.getIdRoteador());
        Pacote copia = new Pacote(this.idRoteador, pacote.getIdRoteadorDestino());
        controller.atualizarContadorPacotes(Pacote.getContadorPacotes());
        controller.exibirPacote(copia, this, vizinho);
      } // fim do for
    } // fim do for
  } // fim do metodo roteamentoV2

  public void roteamentoV3(Pacote pacote) {
    for (Aresta conexao : conexoes) {
      Roteador vizinho = conexao.getDestino();
      if (pacote.getIdRoteadorOrigemAnterior() != vizinho.getIdRoteador()) {
        System.out.println(
            "Roteador " + idRoteador + ": Pacote enviado para Roteador " + vizinho.getIdRoteador());
        Pacote copia = new Pacote(this.idRoteador, pacote.getIdRoteadorDestino(), pacote.getTtl() - 1);
        controller.atualizarContadorPacotes(Pacote.getContadorPacotes());
        controller.exibirPacote(copia, this, vizinho);
      } // fim do for
    } // fim do for
  } // fim do metodo roteamentoV3

  public void roteamentoV4(Pacote pacote) {
    int criador = pacote.getIdRoteadorCriador();
    int seqPacote = pacote.getNumeroSequencia();

    for (Aresta conexao : conexoes) {
      Roteador vizinho = conexao.getDestino();
      if (pacote.getIdRoteadorOrigemAnterior() != vizinho.getIdRoteador()) {
        System.out.println(
            "Roteador " + idRoteador + ": Pacote enviado para Roteador " + vizinho.getIdRoteador());
        Pacote copia = new Pacote(this.idRoteador, pacote.getIdRoteadorDestino(), pacote.getTtl() - 1, criador, seqPacote);
        controller.atualizarContadorPacotes(Pacote.getContadorPacotes());
        controller.exibirPacote(copia, this, vizinho);
      } // fim do for
    } // fim do for
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

  public void setIdRoteador(int idRoteador) {
    this.idRoteador = idRoteador;
  }

  public int getAlgoritmo() {
    return algoritmo;
  }

  public void setAlgoritmo(int algoritmo) {
    this.algoritmo = algoritmo;
  }

  public boolean isRodando() {
    return rodando;
  }

  public void setRodando(boolean rodando) {
    this.rodando = rodando;
  }

  public BackboneController getController() {
    return controller;
  }

  public void setController(BackboneController controller) {
    this.controller = controller;
  }

  public BlockingQueue<Pacote> getBufferPacotes() {
    return bufferPacotes;
  }

  public void setBufferPacotes(BlockingQueue<Pacote> bufferPacotes) {
    this.bufferPacotes = bufferPacotes;
  }

  public double[] getCoordenadaXY() {
    return coordenadaXY;
  }

  public void setCoordenadaXY(double[] coordenadaXY) {
    this.coordenadaXY = Arrays.copyOf(coordenadaXY, coordenadaXY.length);
  }

  public ImageView getImageView() {
    return imageView;
  }

  public void setImageView(ImageView imageView) {
    this.imageView = imageView;
  }

  public Map<Integer, Integer> getMemoriaSequencia() {
    return memoriaSequencia;
  }

  public void setMemoriaSequencia(Map<Integer, Integer> memoriaSequencia) {
    this.memoriaSequencia = memoriaSequencia;
  }

  public ArrayList<Aresta> getConexoes() {
    return conexoes;
  }

  public void setConexoes(ArrayList<Aresta> conexoes) {
    this.conexoes = conexoes;
  }

}
