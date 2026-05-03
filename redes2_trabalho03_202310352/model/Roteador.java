/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 03 05 2026
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
  private ImageView imageView;
  private double[] coordenadaXY;
  private int ecosRecebidos = 0;
  private BackboneController controller;
  private volatile boolean rodando = true;

  private Map<Integer, Aresta> tabelaRoteamento = new HashMap<>();
  private BlockingQueue<Pacote> bufferPacotes = new LinkedBlockingQueue<>();
  private ArrayList<Aresta> conexoes = new ArrayList<>();

  public Roteador(int idRoteador, BackboneController controller) {
    this.controller = controller;
    this.idRoteador = idRoteador;
    tabelaRoteamento.put(idRoteador, new Aresta(this, 0));
    System.out.println("Roteador " + idRoteador + ": criado com sucesso!");
  }

  @Override
  public void run() {
    iniciarDescobertaDeVizinhos();
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
   * Funcao: processa o pacote e o encaminha para ser tratado de maneira a
   * depender de seu tipo
   * Parametros: Pacote recebido pelo roteador
   * Retorno: void
   */
  public void processaPacote(Pacote pacote) {
    System.out.println("Roteador " + idRoteador + ": Esta processando um pacote. Fila = " + bufferPacotes.size());
    checaEstaAtivo();
    if (pacote instanceof PacoteEcho) {
      tratarEcho((PacoteEcho) pacote);
    } else if (pacote instanceof PacoteVetor) {
      tratarVetor((PacoteVetor) pacote);
    } else {
      tratarDados(pacote);
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
   * Metodo: tratarEcho
   * Funcao: Avalia o pacote Echo e direciona para a rotina correta
   */
  public void tratarEcho(PacoteEcho pacote) {
    if (pacote.isReply()) {
      int idVizinho = pacote.getIdRoteadorOrigem();
      int latencia = pacote.getLatenciaDaAresta();

      Roteador instanciaVizinho = null;
      for (Aresta conexao : conexoes) {
        if (conexao.getDestino().getIdRoteador() == idVizinho) {
          instanciaVizinho = conexao.getDestino();
          break;
        } // fim do if
      } // fim do for

      if (instanciaVizinho != null) {
        tabelaRoteamento.put(idVizinho, new Aresta(instanciaVizinho, latencia));

        System.out.println("Roteador " + idRoteador + " registrou vizinho R" + idVizinho + " na tabela!");
        controller.enviarParaLog(
            "  -> R" + idRoteador + " confirmou conexao com R" + idVizinho + " (Latencia: " + latencia + ")", "NORMAL");
      } // fim do if
      
      ecosRecebidos++;

      // Verifica se ja recebeu resposta de TODOS os vizinhos
      if (ecosRecebidos == conexoes.size()) {
        System.out.println("Roteador " + idRoteador + " terminou o Echo. Iniciando Vetor de Distancia!");
        enviarVetorParaVizinhos();
      } // fim do if
    } else {
      int idOrigem = pacote.getIdRoteadorOrigem();

      for (Aresta conexao : conexoes) {
        if (conexao.getDestino().getIdRoteador() == idOrigem) {

          PacoteEcho reply = new PacoteEcho(this.idRoteador, idOrigem, pacote.getLatenciaDaAresta());
          reply.setReply(true);

          System.out.println("Roteador " + idRoteador + " enviando REPLY para R" + idOrigem);
          transmitirParaVizinho(reply, conexao.getDestino());
          break;
        } // fim do if
      } // fim do for
    } // fim do if
  } // fim do metodo tratarEcho

  public void tratarVetor(PacoteVetor pacote) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'tratarVetor'");
  }

  /*
   * Metodo: tratarDados
   * Funcao: processa o pacote e verifica se eh o roteador atual eh o destino dele
   * Parametros: Pacote recebido pelo roteador
   * Retorno: void
   */
  public void tratarDados(Pacote pacote) {
    if (pacote.getIdRoteadorDestino() == idRoteador) {
      controller.exibirGengar(idRoteador);
      System.out.println("Roteador " + idRoteador + ": Pacote chegou ao destino!!\nCusto total de envio: "
          + Pacote.getCustoTotalDeEnvio());
      controller.atualizarCustoTotalDoCaminho(Pacote.getCustoTotalDeEnvio());
    } else {
      enviarPacoteDados(pacote);
    } // fim do if
  } // fim do metodo tratarDados

  /*
   * Metodo: enviarVetorParaVizinhos
   * Funcao: Pega a tabela de roteamento e envia uma copia para TODOS os vizinhos
   * diretos
   */
  public void enviarVetorParaVizinhos() {
    System.out.println("teste");
    for(Map.Entry<Integer, Aresta> entrada : tabelaRoteamento.entrySet()) {
      System.out.println("Roteador "+idRoteador+" | Vizinho de busca: " + entrada.getKey() + ", vizinho para qual enviar: " + entrada.getValue().getDestino().getIdRoteador() + ", latencia: "+entrada.getValue().getLatencia());
    }
    // Map<Integer, Integer> tabela = new HashMap<>();
    // for (Map.Entry<Integer, Aresta> entrada : tabelaRoteamento.entrySet()) {
    //   tabela.put(entrada.getKey(), entrada.getValue().getLatencia());
    // } // fim do for

    // for (Aresta conexao : conexoes) {
    //   Roteador vizinho = conexao.getDestino();

    //   PacoteVetor pacoteVetor = new PacoteVetor(this.idRoteador, vizinho.getIdRoteador(), tabela);

    //   System.out.println("Roteador " + idRoteador + " enviando VETOR (Fofoca) para vizinho " + vizinho.getIdRoteador());
    //   transmitirParaVizinho(pacoteVetor, vizinho);
    // } // fim do for
  } // fim do metodo enviarVetorParaVizinhos

  /*
   * Metodo: enviarPacoteDados
   * Funcao: Consulta a tabela de roteamento para encaminhar o pacote de usuario
   */
  public void enviarPacoteDados(Pacote pacote) {
    int idDestino = pacote.getIdRoteadorDestino();
    Aresta rota = tabelaRoteamento.get(idDestino);

    // SEGURANÇA: Só tenta enviar se a rota existe na tabela e tem um próximo salto
    // válido
    if (rota != null && rota.getDestino() != null) {
      Roteador vizinho = rota.getDestino();

      System.out.println("Roteador " + idRoteador + " roteando DADOS para " + vizinho.getIdRoteador()
          + " | Destino Final: " + idDestino);

      Pacote.setCustoTotalDeEnvio(Pacote.getCustoTotalDeEnvio() + rota.getLatencia());
      transmitirParaVizinho(pacote, vizinho);

    } else {
      System.out.println("Roteador " + idRoteador + ": Rota para " + idDestino + " desconhecida! Descartando pacote.");
    } // fim do if
  } // fim do metodo enviarPacoteDados

  /*
   * Metodo: transmitirParaVizinho
   * Funcao: inicializa a animacao de envio de pacote
   */
  private void transmitirParaVizinho(Pacote pacote, Roteador vizinho) {
    controller.exibirPacote(pacote, this, vizinho);
  } // fim do metodo transmitirParaVizinho

  /*
   * Metodo: iniciarDescobertaDeVizinhos
   * Funcao: Envia um Echo Request para todas as portas fisicas (conexoes)
   */
  public void iniciarDescobertaDeVizinhos() {
    System.out.println("Roteador " + idRoteador + " iniciando descoberta (Echo Request)...");
    controller.enviarParaLog("[Roteador " + idRoteador + "] Iniciou a descoberta de vizinhos.", "TITULO");

    for (Aresta conexao : conexoes) {
      Roteador vizinho = conexao.getDestino();

      PacoteEcho request = new PacoteEcho(this.idRoteador, vizinho.getIdRoteador(), conexao.getLatencia());

      transmitirParaVizinho(request, vizinho);
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
   * Funcao: Adicionar roteador vizinho e a latencia de envio
   * Parametros: Vizinho = roteador que esta interligado; latencia = tempo de
   * retardo
   * Retorno: void
   */
  public void addVizinho(Roteador vizinho, int latencia) {
    this.conexoes.add(new Aresta(vizinho, latencia));
  } // fim do addVizinho

  public int getIdRoteador() {
    return idRoteador;
  }

  public void setIdRoteador(int idRoteador) {
    this.idRoteador = idRoteador;
  }

  public ImageView getImageView() {
    return imageView;
  }

  public void setImageView(ImageView imageView) {
    this.imageView = imageView;
  }

  public double[] getCoordenadaXY() {
    return coordenadaXY;
  }

  public void setCoordenadaXY(double[] coordenadaXY) {
    this.coordenadaXY = coordenadaXY;
  }

  public int getEcosRecebidos() {
    return ecosRecebidos;
  }

  public void setEcosRecebidos(int ecosRecebidos) {
    this.ecosRecebidos = ecosRecebidos;
  }

  public BackboneController getController() {
    return controller;
  }

  public void setController(BackboneController controller) {
    this.controller = controller;
  }

  public boolean isRodando() {
    return rodando;
  }

  public void setRodando(boolean rodando) {
    this.rodando = rodando;
  }

  public Map<Integer, Aresta> getTabelaRoteamento() {
    return tabelaRoteamento;
  }

  public void setTabelaRoteamento(Map<Integer, Aresta> tabelaRoteamento) {
    this.tabelaRoteamento = tabelaRoteamento;
  }

  public BlockingQueue<Pacote> getBufferPacotes() {
    return bufferPacotes;
  }

  public void setBufferPacotes(BlockingQueue<Pacote> bufferPacotes) {
    this.bufferPacotes = bufferPacotes;
  }

  public ArrayList<Aresta> getConexoes() {
    return conexoes;
  }

  public void setConexoes(ArrayList<Aresta> conexoes) {
    this.conexoes = conexoes;
  }

}
