/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 28 03 2026
* Nome.............: Roteador.java
* Funcao...........: Executa as operacoes do roteador como enviar e processar os pacotes
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
  private volatile boolean rodando = true;
  private BackboneController controller;
  private BlockingQueue<Pacote> bufferPacotes = new LinkedBlockingQueue<>();
  private double[] coordenadaXY;
  private ImageView imageView;

  private Map<Integer, Integer> tabelaProximoSalto = new HashMap<>();

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
      controller.exibirGengar(idRoteador);
      System.out.println("Roteador " + idRoteador + ": Pacote chegou ao destino!!");
    } else {

      enviarPacote(pacote);

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

  /*
   * Metodo: enviarPacote
   * Funcao: envia o pacote entre os roteadores
   * Parametros: Pacote recebido pelo roteador
   * Retorno: void
   */
  public void enviarPacote(Pacote pacote) {
    int idDestino = pacote.getIdRoteadorDestino();
    Integer idProximoSalto = tabelaProximoSalto.get(idDestino);

    // Se a rota existir na tabela
    if (idProximoSalto != null) {

      // Procura a aresta especifica para esse vizinho
      for (Aresta conexao : conexoes) {
        if (conexao.getDestino().getIdRoteador() == idProximoSalto) {
          Roteador vizinho = conexao.getDestino();
          System.out.println(
              "Roteador " + idRoteador + " enviou para " + vizinho.getIdRoteador() + " | Destino Final: " + idDestino);

          Pacote copia = new Pacote(this.idRoteador, idDestino);
          controller.atualizarContadorPacotes(Pacote.getContadorPacotes());
          controller.exibirPacote(copia, this, vizinho);
          break; // Sai do for pois ja enviou
        }
      }
    } else {
      System.out.println("Roteador " + idRoteador + ": Rota desconhecida!");
    }
  } // fim do metodo enviarPacote

  public void calcularDijkstra(ArrayList<Roteador> todosRoteadores) {
    Map<Integer, Integer> tabelaCustos = new HashMap<>();
    ArrayList<Roteador> naoVisitados = new ArrayList<>(todosRoteadores);

    // 1. Inicializa todos os custos como "Infinito", exceto a si mesmo
    for (Roteador r : todosRoteadores) {
      tabelaCustos.put(r.getIdRoteador(), Integer.MAX_VALUE);
    }
    tabelaCustos.put(this.idRoteador, 0);

    // 2. Loop de exploracao
    while (!naoVisitados.isEmpty()) {
      Roteador atual = null;
      int menorCusto = Integer.MAX_VALUE;

      // Procura o roteador mais proximo que ainda nao foi visitado
      for (Roteador r : naoVisitados) {
        int custo = tabelaCustos.get(r.getIdRoteador());
        if (custo < menorCusto) {
          menorCusto = custo;
          atual = r;
        }
      }

      // Se nao achar ninguem ou todos os restantes estiverem isolados, para
      if (atual == null || menorCusto == Integer.MAX_VALUE)
        break;

      naoVisitados.remove(atual);

      // 3. Avalia as conexoes do roteador atual
      for (Aresta aresta : atual.getConexoes()) {
        Roteador vizinho = aresta.getDestino();

        if (naoVisitados.contains(vizinho)) {
          int novoCusto = tabelaCustos.get(atual.getIdRoteador()) + aresta.getPeso();

          // Relaxamento: Achou um caminho mais rapido?
          if (novoCusto < tabelaCustos.get(vizinho.getIdRoteador())) {
            tabelaCustos.put(vizinho.getIdRoteador(), novoCusto);

            // Atualiza o proximo salto
            if (atual.getIdRoteador() == this.idRoteador) {
              tabelaProximoSalto.put(vizinho.getIdRoteador(), vizinho.getIdRoteador());
            } else {
              tabelaProximoSalto.put(vizinho.getIdRoteador(), tabelaProximoSalto.get(atual.getIdRoteador()));
            }
          }
        }
      }
    }
  }

  /*
   * Metodo: receberPacote
   * Funcao: adiciona o pacote no buffer
   * Parametros: Pacote recebido pelo roteador
   * Retorno: void
   */
  public void receberPacote(Pacote p) {
    bufferPacotes.add(p);
  } // fim do metodo receberPacote

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

  public Map<Integer, Integer> getTabelaProximoSalto() {
    return tabelaProximoSalto;
  }

  public void setTabelaProximoSalto(Map<Integer, Integer> tabelaProximoSalto) {
    this.tabelaProximoSalto = tabelaProximoSalto;
  }

  public ArrayList<Aresta> getConexoes() {
    return conexoes;
  }

  public void setConexoes(ArrayList<Aresta> conexoes) {
    this.conexoes = conexoes;
  }

}
