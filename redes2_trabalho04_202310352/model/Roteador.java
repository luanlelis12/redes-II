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
import java.util.Random;

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
  private Timer timerPing;

  // Memória do Estado de Enlace
  private int meuNumeroSequencia = 0;
  private Map<Integer, Integer> ultimosLSPsVistos = new HashMap<>(); // ID do Gerador -> Ultimo Num Sequencia
  private Map<Integer, Map<Integer, Integer>> topologiaGlobal = new HashMap<>(); // ID do Gerador -> (Vizinho ->
                                                                                 // Latencia)

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
    iniciarMonitoramentoPing();
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
    if (timerPing != null) {
      timerPing.cancel();
      timerPing = null;
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
    iniciarMonitoramentoPing();
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
    } else if (pacote instanceof PacoteLSP) {
      tratarLSP((PacoteLSP) pacote);
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
        inundarLSP();
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
   * Metodo: tratarLSP
   * Funcao: Recebe um LSP, verifica a novidade e repassa (inunda) se for novo
   */
  public void tratarLSP(PacoteLSP pacote) {
    int gerador = pacote.getIdRoteadorGerador();
    int seq = pacote.getNumeroSequencia();
    int quemMeEnviou = pacote.getIdRoteadorOrigem(); // De onde veio o repasse agora

    // Descobre qual foi o último número de sequência que vimos deste gerador (-1 se
    // for inédito)
    int ultimoSeqVisto = ultimosLSPsVistos.getOrDefault(gerador, -1);

    // Regra de Contenção da Inundação: Se o pacote for NOVO e ainda tiver TTL
    if (seq > ultimoSeqVisto && pacote.getTtl() > 0) {

      ultimosLSPsVistos.put(gerador, seq); // Atualiza memória de sequência
      topologiaGlobal.put(gerador, pacote.getEnlaces()); // Atualiza o Mapa do Mundo!

      System.out.println(
          "Roteador " + idRoteador + " ACEITOU LSP do R" + gerador + " (via R" + quemMeEnviou + "). Repassando...");

      pacote.setTtl(pacote.getTtl() - 1); // Envelhece o pacote

      // REPASSA PARA TODOS OS VIZINHOS (EXCETO PARA QUEM ACABOU DE MANDAR)
      for (Aresta conexao : conexoes) {
        Roteador vizinho = conexao.getDestino();
        if (vizinho.getIdRoteador() != quemMeEnviou) {
          PacoteLSP repasse = new PacoteLSP(this.idRoteador, vizinho.getIdRoteador(), gerador, seq, pacote.getTtl(),
              pacote.getEnlaces());
          transmitirParaVizinho(repasse, vizinho);
        }
      }

      // NOVIDADE: A topologia mudou, então é hora de recalcular as rotas!
      recalcularRotasDijkstra();

    } else {
      // Se for <= ao que já temos, é pacote circulando em loop. Joga no lixo!
      System.out.println("Roteador " + idRoteador + " DESCARTOU LSP antigo/duplicado do R" + gerador);
    }
  }

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
   * Metodo: inundarLSP
   * Funcao: Cria um Pacote de Estado de Enlace (LSP) e envia para todos os
   * vizinhos
   */
  public void inundarLSP() {
    meuNumeroSequencia++; // Aumenta a sequência para mostrar que é uma fofoca nova
    Map<Integer, Integer> meusEnlaces = new HashMap<>();

    // Coleta as latências oficiais dos vizinhos
    for (Aresta aresta : conexoes) {
      meusEnlaces.put(aresta.getDestino().getIdRoteador(), aresta.getLatenciaAnunciada());
    }

    // Salva a própria informação na sua visão da topologia
    topologiaGlobal.put(this.idRoteador, meusEnlaces);
    ultimosLSPsVistos.put(this.idRoteador, meuNumeroSequencia);

    System.out.println("Roteador " + idRoteador + " GEROU LSP (Seq: " + meuNumeroSequencia + "). Inundando a rede!");

    // Inunda para todos os vizinhos
    for (Aresta conexao : conexoes) {
      Roteador vizinho = conexao.getDestino();
      PacoteLSP pacote = new PacoteLSP(this.idRoteador, vizinho.getIdRoteador(), this.idRoteador, meuNumeroSequencia,
          10, meusEnlaces);
      transmitirParaVizinho(pacote, vizinho);
    }
  }

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
   * Funcao: Adicionar roteador vizinho e a peso da conexao
   * Parametros: Vizinho = roteador que esta interligado; peso = custo do caminho
   * Retorno: void
   */
  public void addVizinho(Roteador vizinho, int peso) {
    this.conexoes.add(new Aresta(vizinho, peso));
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
            inundarLSP();
          } // fim do if
        } catch (Exception e) {
          System.out.println("Erro no Timer do Roteador " + idRoteador + " (ignorado).");
        } // fim do try-catch
      } // fim do run
    }, 10000, 10000);
  } // fim do metodo iniciarEnvioPeriodico
  /*
   * Metodo: gerarPingAleatorio
   * Funcao: Simula a oscilacao de trafego alterando a latencia e verifica se deve
   * alertar a rede
   */

  public void gerarPingAleatorio() {
    Random gerador = new Random();
    boolean teveMudancaSignificativa = false;

    for (Aresta conexao : conexoes) {
      int pingOficialDaRede = conexao.getLatenciaAnunciada();

      // Gera uma nova latencia aleatoria entre 1 e 500 ms
      int novaLatencia = gerador.nextInt(500) + 1;

      // Atualiza a latencia no cabo
      conexao.setLatencia(novaLatencia);

      // Manda a tela atualizar a conexao
      controller.atualizarLatenciaVisual(this.idRoteador, conexao.getDestino().getIdRoteador(), novaLatencia);

      int diferenca = Math.abs(novaLatencia - pingOficialDaRede);

      if (pingOficialDaRede == 0 || diferenca > 50) {
        // Atualiza a memoria do roteador com o novo valor oficial que ele vai anunciar
        conexao.setLatenciaAnunciada(novaLatencia);
        teveMudancaSignificativa = true;
      }

      System.out.println("Roteador " + idRoteador + " pingou vizinho R" + conexao.getDestino().getIdRoteador() + " -> "
          + novaLatencia + "ms");
    } // fim do for

    // 4. Se a diferenca acumulada estourou o limite de 50ms, inunda a rede
    if (teveMudancaSignificativa && ecosRecebidos >= conexoes.size()) {
      System.out.println("Roteador " + idRoteador + ": Mudanca drastica de ping detetada! Avisando a rede...");

      inundarLSP();
    }
  } // fim do metodo gerarPingAleatorio

  /*
   * Metodo: iniciarMonitoramentoPing
   * Funcao: Configura um cronometro para sortear novas latencias a cada 15
   * segundos
   */
  public void iniciarMonitoramentoPing() {
    if (timerPing != null)
      timerPing.cancel();

    timerPing = new Timer();
    timerPing.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        try {
          // So comeca a medir a latencia aleatoria depois que descobrir quem sao os
          // vizinhos
          if (ecosRecebidos >= conexoes.size() && conexoes.size() > 0) {
            gerarPingAleatorio();
          } // fim do if
        } catch (Exception e) {
          System.out.println("Erro no Timer de Ping do Roteador " + idRoteador);
        } // fim do try-catch
      } // fim do run
    }, 15000, 15000); // Roda a cada 15 segundos
  } // fim do metodo iniciarMonitoramentoPing

  /*
   * Metodo: recalcularRotasDijkstra
   * Funcao: Usa a topologiaGlobal (mapa do mundo) para calcular o menor caminho
   * para todos os destinos
   */
  public void recalcularRotasDijkstra() {
    // Se o mapa ainda estiver vazio, não faz nada
    if (topologiaGlobal.isEmpty())
      return;

    Map<Integer, Integer> distancias = new HashMap<>();
    Map<Integer, Integer> predecessores = new HashMap<>();
    ArrayList<Integer> naoVisitados = new ArrayList<>();

    // 1. Inicialização
    for (Integer idNode : topologiaGlobal.keySet()) {
      distancias.put(idNode, 999999); // Simula o "Infinito"
      predecessores.put(idNode, -1);
      naoVisitados.add(idNode);
    }
    // A distância para si próprio é zero
    distancias.put(this.idRoteador, 0);

    // 2. Algoritmo Principal de Dijkstra
    while (!naoVisitados.isEmpty()) {
      // Procura o nó não visitado com a menor distância atual
      int u = -1;
      int menorDistancia = 999999;
      for (int id : naoVisitados) {
        if (distancias.get(id) < menorDistancia) {
          menorDistancia = distancias.get(id);
          u = id;
        }
      }

      // Se não encontrou mais ninguém alcançável, para
      if (u == -1)
        break;

      naoVisitados.remove(Integer.valueOf(u));

      // Avalia os vizinhos de 'u' olhando para a topologia global
      Map<Integer, Integer> vizinhosDeU = topologiaGlobal.get(u);
      if (vizinhosDeU != null) {
        for (Map.Entry<Integer, Integer> vizinho : vizinhosDeU.entrySet()) {
          int v = vizinho.getKey();
          int peso = vizinho.getValue();

          if (naoVisitados.contains(v)) {
            int novaDistancia = distancias.get(u) + peso;
            if (novaDistancia < distancias.get(v)) {
              distancias.put(v, novaDistancia);
              predecessores.put(v, u); // Guarda de onde viemos para chegar a 'v'
            }
          }
        }
      }
    } // Fim do Dijkstra

    // 3. Reconstruir a Tabela de Roteamento Real
    synchronized (tabelaRoteamento) {
      tabelaRoteamento.clear();
      tabelaRoteamento.put(this.idRoteador, new Aresta(this, 0)); // Rota local

      for (Integer destino : topologiaGlobal.keySet()) {
        if (destino == this.idRoteador || distancias.get(destino) == 999999)
          continue;

        // Fazer o caminho inverso para descobrir o "Próximo Salto" (Next Hop)
        int passoAtual = destino;
        int nextHop = passoAtual;

        while (predecessores.get(passoAtual) != this.idRoteador && predecessores.get(passoAtual) != -1) {
          passoAtual = predecessores.get(passoAtual);
          nextHop = passoAtual;
        }

        // Procurar o objeto Roteador real correspondente a esse nextHop nas conexões
        // físicas
        Roteador instanciaNextHop = null;
        for (Aresta c : conexoes) {
          if (c.getDestino().getIdRoteador() == nextHop) {
            instanciaNextHop = c.getDestino();
            break;
          }
        }

        // Adiciona à Tabela de Roteamento oficial que o JavaFX lê!
        if (instanciaNextHop != null) {
          tabelaRoteamento.put(destino, new Aresta(instanciaNextHop, distancias.get(destino)));
        }
      }
    }

    // 4. Manda o JavaFX atualizar o painel lateral
    controller.atualizarTabelasNaTela();
  } // fim do metodo recalcularRotasDijkstra

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
