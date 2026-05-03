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
import java.util.Timer;
import java.util.TimerTask;

import controller.BackboneController;
import javafx.scene.image.ImageView;

public class Roteador extends Thread {
  private int idRoteador;
  private ImageView imageView;
  private double[] coordenadaXY;
  private int ecosRecebidos = 0;
  private BackboneController controller;
  private volatile boolean rodando = true;
  private Timer timerAtualizacao;

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
    iniciarEnvioPeriodico();
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
    if (timerAtualizacao != null) {
      timerAtualizacao.cancel();
      timerAtualizacao = null;
    }
  } // fim do metodo desligar

  /*
   * Metodo: ligar
   * Funcao: define a flag como verdadeira para ligar o roteador
   * Parametros:
   * Retorno: void
   */
  public void ligar() {
    this.rodando = true;
    iniciarEnvioPeriodico();
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
   * Parametros: pacote = pacote echo recebido
   * Retorno: void
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
        synchronized (tabelaRoteamento) {
          tabelaRoteamento.put(idVizinho, new Aresta(instanciaVizinho, latencia));
        }
        controller.atualizarTabelasNaTela();
        System.out.println("Roteador " + idRoteador + " registrou vizinho R" + idVizinho + " na tabela!");
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

  /*
   * Metodo: tratarVetor
   * Funcao: recebe a tabela do vizinho e atualiza a tabela caso tenha um caminho
   * melhor
   * Parametros: pacote = Pacote com a tabela
   * Retorno: void
   */
  public void tratarVetor(PacoteVetor pacote) {
    int idVizinho = pacote.getIdRoteadorOrigem();
    Map<Integer, Integer> vetorDoVizinho = pacote.getVetorDistancias();

    Aresta rotaParaVizinho = tabelaRoteamento.get(idVizinho);
    if (rotaParaVizinho == null)
      return;

    int meuCustoAteVizinho = rotaParaVizinho.getLatencia();
    Roteador instanciaVizinho = rotaParaVizinho.getDestino();

    boolean mudouAlgumaCoisa = false;

    for (Map.Entry<Integer, Integer> entrada : vetorDoVizinho.entrySet()) {
      int destinoFinal = entrada.getKey();
      int custoDoVizinhoAoDestino = entrada.getValue();
      int custoPossivel = meuCustoAteVizinho + custoDoVizinhoAoDestino;
      Aresta minhaRotaAtual = tabelaRoteamento.get(destinoFinal);

      boolean isMesmoVizinho = minhaRotaAtual != null && minhaRotaAtual.getDestino().getIdRoteador() == idVizinho;

      if (minhaRotaAtual == null || custoPossivel < minhaRotaAtual.getLatencia()
          || (isMesmoVizinho && custoPossivel != minhaRotaAtual.getLatencia())) {
        synchronized (tabelaRoteamento) {
          tabelaRoteamento.put(destinoFinal, new Aresta(instanciaVizinho, custoPossivel));
        } // fim do synchronized
        mudouAlgumaCoisa = true;
      } // fim do if
    } // fim do for

    if (mudouAlgumaCoisa) {
      controller.atualizarTabelasNaTela();
      enviarVetorParaVizinhos();
    } // fim do if
  } // fim do metodo tratarVetor

  /*
   * Metodo: tratarDados
   * Funcao: processa o pacote e verifica se eh o roteador atual eh o destino dele
   * Parametros: Pacote recebido pelo roteador
   * Retorno: void
   */
  public void tratarDados(Pacote pacote) {
    controller.atualizarCustoTotalDoCaminho(Pacote.getCustoTotalDeEnvio());
    if (pacote.getIdRoteadorDestino() == idRoteador) {
      controller.exibirGengar(idRoteador);
      System.out.println("Roteador " + idRoteador + ": Pacote chegou ao destino!!\nCusto total de envio: "
          + Pacote.getCustoTotalDeEnvio());
    } else {
      enviarPacoteDados(pacote);
    } // fim do if
  } // fim do metodo tratarDados

  /*
   * Metodo: enviarVetorParaVizinhos
   * Funcao: Pega a tabela de roteamento e envia uma copia para TODOS os vizinhos
   * diretos
   * Parametros:
   * Retorno: void
   */
  public void enviarVetorParaVizinhos() {
    Map<Integer, Integer> tabela = new HashMap<>();
    Map<Integer, Aresta> copiaSegura = getCopiaSeguraTabelaRoteamento();

    // Monta o vetor (Destino -> Custo)
    for (Map.Entry<Integer, Aresta> entrada : copiaSegura.entrySet()) {
      tabela.put(entrada.getKey(), entrada.getValue().getLatencia());
    } // fim do for

    // Manda para todos os vizinhos
    for (Aresta conexao : conexoes) {
      Roteador vizinho = conexao.getDestino();
      PacoteVetor pacoteVetor = new PacoteVetor(this.idRoteador, vizinho.getIdRoteador(), tabela);

      System.out
          .println("Roteador " + idRoteador + ": enviando tabela para roteador vizinho " + vizinho.getIdRoteador());
      transmitirParaVizinho(pacoteVetor, vizinho);
    } // fim do for
  } // fim do metodo enviarVetorParaVizinhos

  /*
   * Metodo: enviarPacoteDados
   * Funcao: Consulta a tabela de roteamento para encaminhar o pacote de usuario
   * Parametros: pacote = pacote recebido
   * Retorno: void
   */
  public void enviarPacoteDados(Pacote pacote) {
    int idDestino = pacote.getIdRoteadorDestino();
    Aresta rota = tabelaRoteamento.get(idDestino);

    if (rota != null && rota.getDestino() != null && rota.getLatencia() < 9999) {
      Roteador vizinho = rota.getDestino();
      boolean caboExiste = false;
      int latencia = 0;
      for (Aresta conexao : conexoes) {
        if (conexao.getDestino().getIdRoteador() == vizinho.getIdRoteador()) {
          latencia = conexao.getLatencia();
          caboExiste = true;
          break;
        } // fim do if
      } // fim do for
      if (caboExiste) {
        System.out.println("Roteador " + idRoteador + ": Enviando dados para roteador " + vizinho.getIdRoteador()
            + " | Destino Final: roteador " + idDestino);
        Pacote.setCustoTotalDeEnvio(Pacote.getCustoTotalDeEnvio() + latencia);
        transmitirParaVizinho(pacote, vizinho);
      } else {
        System.out.println("Roteador " + idRoteador + ": Tentou enviar, mas o cabo para " + vizinho.getIdRoteador()
            + " foi cortado! Pacote descartado.");
      } // fim do if
    } else {
      System.out.println(
          "Roteador " + idRoteador + ": Rota para " + idDestino + " desconhecida ou inalcançável! Descartando pacote.");
    } // fim do if
  } // fim do metodo enviarPacoteDados

  /*
   * Metodo: transmitirParaVizinho
   * Funcao: inicializa a animacao de envio de pacote
   * Parametros: pacote = pacote para enviar, vizinho = roteador vizinho que
   * recebera o pacote
   * Retorno: void
   */
  private void transmitirParaVizinho(Pacote pacote, Roteador vizinho) {
    controller.exibirPacote(pacote, this, vizinho);
  } // fim do metodo transmitirParaVizinho

  /*
   * Metodo: iniciarDescobertaDeVizinhos
   * Funcao: Envia um Echo Request para todas as portas fisicas (conexoes)
   * Parametros:
   * Retorno: void
   */
  public void iniciarDescobertaDeVizinhos() {
    System.out.println("Roteador " + idRoteador + ": iniciando descoberta (Echo Request)...");

    for (Aresta conexao : conexoes) {
      Roteador vizinho = conexao.getDestino();
      PacoteEcho request = new PacoteEcho(this.idRoteador, vizinho.getIdRoteador(), conexao.getLatencia());
      transmitirParaVizinho(request, vizinho);
    } // fim do for
  } // fim do metodo iniciarDescobertaDeVizinhos

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

  /*
   * Metodo: getCopiaSeguraTabelaRoteamento
   * Funcao: Retorna um clone da tabela para evitar problema na tabela
   */
  public Map<Integer, Aresta> getCopiaSeguraTabelaRoteamento() {
    synchronized (tabelaRoteamento) {
      return new HashMap<>(tabelaRoteamento);
    } // fim do synchronized
  } // fim do metodo getCopiaSeguraTabelaRoteamento

  /*
   * Metodo: iniciarEnvioPeriodico
   * Funcao: Configura um cronometro para reenviar a tabela de rotas a cada 10
   * segundos
   */
  public void iniciarEnvioPeriodico() {
    // Mata o timer anterior caso ainda exista
    if (timerAtualizacao != null)
      timerAtualizacao.cancel();

    timerAtualizacao = new Timer();
    timerAtualizacao.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        try {
          if (ecosRecebidos >= conexoes.size() && conexoes.size() > 0) {
            System.out.println("Roteador " + idRoteador + ": Disparando atualizacao periodica de rotas!");
            enviarVetorParaVizinhos();
          } // fim do if
        } catch (Exception e) {
          System.out.println("Erro no Timer do Roteador " + idRoteador + " (ignorado).");
        } // fim do try-catch
      } // fim do run
    }, 10000, 10000);
  } // fim do metodo iniciarEnvioPeriodico

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
