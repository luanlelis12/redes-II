/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 31 05 2026
* Nome.............: Roteador.java
* Funcao...........: Executa as operacoes do roteador como enviar e processar os pacotes
*************************************************************** */
package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

import controller.BackboneController;
import javafx.scene.image.ImageView;

public class Roteador extends Thread {
  private int idRoteador;
  private ImageView imageView;
  private double[] coordenadaXY;
  private int ecosRecebidos = 0;
  private int vizinhosDescobertos = 0;
  private BackboneController controller;
  private volatile boolean rodando = true;
  private Timer timerAtualizacao;
  private Timer timerPing;

  // Memoria do Estado de Enlace
  private int meuNumeroSequencia = 0;
  private Map<Integer, Integer> ultimosLSPsVistos = new HashMap<>(); // ID do Gerador -> Ultimo Num Sequencia
  private Map<Integer, Map<Integer, Integer>> topologiaGlobal = new HashMap<>(); // ID do Gerador -> (Vizinho ->
                                                                                 // Latencia)

  private Map<Integer, Aresta> tabelaRoteamento = new HashMap<>();
  private BlockingQueue<Pacote> bufferPacotes = new LinkedBlockingQueue<>();
  private List<Aresta> conexoes = new CopyOnWriteArrayList<>(); // Lista segura contra quebra de threads

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
    } // fim do if
    if (timerPing != null) {
      timerPing.cancel();
      timerPing = null;
    } // fim do if
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
    checaEstaAtivo();
    if (pacote instanceof PacoteHello) {
      tratarHello((PacoteHello) pacote);
    } else if (pacote instanceof PacoteEcho) {
      tratarEcho((PacoteEcho) pacote);
    } else if (pacote instanceof PacoteLSP) {
      tratarLSP((PacoteLSP) pacote);
    } else {
      tratarDados(pacote);
    } // fim do if
  } // fim do metodo processaPacote

  /*
   * Metodo: checaEstaAtivo
   * Funcao: metodo checa se a thread foi desativada, caso positivo interrompe a
   * thread atual
   * Parametros:
   * Retorno: void
   */
  public void checaEstaAtivo() {
    if (!rodando) {
      Thread.currentThread().interrupt();
    } // fim do if
  } // fim do metodo checaEstaAtivo

  /*
   * Metodo: tratarHello
   * Funcao: Processa o pacote Hello para descobrir vizinhos e seus enderecos
   * Parametros: Pacote recebido
   * Retorno: void
   */
  public void tratarHello(PacoteHello pacote) {
    if (pacote.isReply()) { // Se for uma resposta (Reply)
      int idVizinho = pacote.getIdRoteadorOrigem();

      Roteador instanciaVizinho = null;
      for (Aresta conexao : conexoes) { // Procura o vizinho na lista de conexoes
        if (conexao.getDestino().getIdRoteador() == idVizinho) {
          instanciaVizinho = conexao.getDestino();
          break;
        } // fim do if
      } // fim do for

      // Se encontrou o vizinho
      if (instanciaVizinho != null) {
        synchronized (tabelaRoteamento) { // Bloqueia a tabela para atualizar com seguranca
          // Salva o vizinho com custo inicial zero
          tabelaRoteamento.put(idVizinho, new Aresta(instanciaVizinho, 0));
        } // fim do synchronized
        controller.atualizarTabelasNaTela();
        System.out.println("Roteador " + idRoteador + " descobriu o endereco do vizinho R" + idVizinho + " via Hello!");
      } // fim do if

      vizinhosDescobertos++;

      if (vizinhosDescobertos == conexoes.size()) { // Se ja descobriu todos os vizinhos
        System.out.println("Roteador " + idRoteador + " terminou a descoberta. Disparando Echo Requests!");

        // Inicia o teste de ping para cada vizinho
        for (Aresta conexao : conexoes) {
          PacoteEcho req = new PacoteEcho(this.idRoteador, conexao.getDestino().getIdRoteador());
          req.setReply(false);
          req.setLatenciaIda(new java.util.Random().nextInt(250) + 1);
          transmitirParaVizinho(req, conexao.getDestino());
        } // fim do for
      } // fim do if

    } else { // Se for um pedido de apresentacao (Request)
      int idOrigem = pacote.getIdRoteadorOrigem();

      for (Aresta conexao : conexoes) { // Procura quem enviou para devolver a resposta
        if (conexao.getDestino().getIdRoteador() == idOrigem) {
          PacoteHello reply = new PacoteHello(this.idRoteador, idOrigem, true);
          System.out
              .println("Roteador " + idRoteador + " recebeu apresentacao de R" + idOrigem + ". Enviando resposta...");
          transmitirParaVizinho(reply, conexao.getDestino());
          break;
        } // fim do if
      } // fim do for
    } // fim do if
  } // fim do metodo tratarHello

  /*
   * Metodo: tratarEcho
   * Funcao: Avalia o pacote Echo e direciona para a rotina correta
   * Parametros: pacote = pacote echo recebido
   * Retorno: void
   */
  public void tratarEcho(PacoteEcho pacote) {
    if (pacote.isReply()) { // Se for uma resposta (Reply)
      // Calcula o RTT (media entre ida e volta)
      int rtt = (pacote.getLatenciaIda() + pacote.getLatenciaVolta()) / 2;
      int idVizinho = pacote.getIdRoteadorOrigem();

      for (Aresta conexao : conexoes) {
        if (conexao.getDestino().getIdRoteador() == idVizinho) {
          int pingOficial = conexao.getLatenciaAnunciada();

          // Atualiza a latencia no cabo e na interface
          conexao.setLatencia(rtt);
          controller.atualizarLatenciaVisual(this.idRoteador, idVizinho, rtt);

          // Se a mudanca de ping for maior que 50ms, avisa a rede
          if (pingOficial == 0 || Math.abs(rtt - pingOficial) > 50) {
            conexao.setLatenciaAnunciada(rtt);
            System.out.println(
                "Roteador " + idRoteador + ": RTT para R" + idVizinho + " mudou para " + rtt + "ms. Avisando a rede!");
            inundarLSP();
          } // fim do if
          break;
        } // fim do if
      } // fim do for

      // Se for um pedido de teste (Request)
    } else {
      for (Aresta conexao : conexoes) {
        if (conexao.getDestino().getIdRoteador() == pacote.getIdRoteadorOrigem()) {
          PacoteEcho reply = new PacoteEcho(this.idRoteador, pacote.getIdRoteadorOrigem());
          reply.setReply(true);
          reply.setLatenciaIda(pacote.getLatenciaIda());
          reply.setLatenciaVolta(new java.util.Random().nextInt(250) + 1); // Sorteia o tempo de volta

          transmitirParaVizinho(reply, conexao.getDestino());
          break;
        } // fim do if
      } // fim do for
    } // fim do if
  } // fim do metodo tratarEcho

  /*
   * Metodo: tratarLSP
   * Funcao: Recebe um LSP, verifica a novidade e repassa (inunda) se for novo
   * Parametros: pacote = pacote LSP recebido
   * Retorno: void
   */
  public void tratarLSP(PacoteLSP pacote) {
    int gerador = pacote.getIdRoteadorGerador();
    int seq = pacote.getNumeroSequencia();
    int quemMeEnviou = pacote.getIdRoteadorOrigem(); // De onde o pacote veio agora

    // Busca a ultima versao recebida deste gerador (-1 se for a primeira vez)
    int ultimoSeqVisto = ultimosLSPsVistos.getOrDefault(gerador, -1);

    if (seq > ultimoSeqVisto && pacote.getTtl() > 0) { // Se for um pacote mais novo e ainda tiver tempo de vida (TTL)

      ultimosLSPsVistos.put(gerador, seq); // Atualiza a versao na memoria
      topologiaGlobal.put(gerador, pacote.getEnlaces()); // Atualiza o mapa da rede

      System.out.println(
          "Roteador " + idRoteador + " ACEITOU LSP do R" + gerador + " (via R" + quemMeEnviou + "). Repassando...");

      pacote.setTtl(pacote.getTtl() - 1); // Envelhece o pacote para evitar loop infinito

      // Repassa para os vizinhos
      for (Aresta conexao : conexoes) {
        Roteador vizinho = conexao.getDestino();

        // Nao devolve o pacote para quem acabou de enviar
        if (vizinho.getIdRoteador() != quemMeEnviou) {
          PacoteLSP repasse = new PacoteLSP(this.idRoteador, vizinho.getIdRoteador(), gerador, seq, pacote.getTtl(),
              pacote.getEnlaces());
          transmitirParaVizinho(repasse, vizinho);
        } // fim do if
      } // fim do for

      // Como o mapa mudou, recalcula as rotas usando Dijkstra
      recalcularRotasDijkstra();

    } else { // Se for um pacote velho ou duplicado
      System.out.println("Roteador " + idRoteador + " DESCARTOU LSP antigo/duplicado do R" + gerador);
    } // fim do if
  } // fim do metodo tratarLSP

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
   * Parametros:
   * Retorno: void
   */
  public void inundarLSP() {
    meuNumeroSequencia++; // Aumenta a sequencia para indicar pacote novo
    Map<Integer, Integer> meusEnlaces = new HashMap<>();

    // Pega a latencia atual de cada vizinho
    for (Aresta aresta : conexoes) {
      meusEnlaces.put(aresta.getDestino().getIdRoteador(), aresta.getLatenciaAnunciada());
    } // fim do for

    // Salva seus dados no mapa global
    topologiaGlobal.put(this.idRoteador, meusEnlaces);
    ultimosLSPsVistos.put(this.idRoteador, meuNumeroSequencia);

    System.out.println("Roteador " + idRoteador + " GEROU LSP (Seq: " + meuNumeroSequencia + "). Inundando a rede!");

    // Envia o pacote LSP para todos os vizinhos
    for (Aresta conexao : conexoes) {
      Roteador vizinho = conexao.getDestino();
      PacoteLSP pacote = new PacoteLSP(this.idRoteador, vizinho.getIdRoteador(), this.idRoteador, meuNumeroSequencia,
          10, meusEnlaces);
      transmitirParaVizinho(pacote, vizinho);
    } // fim do for
  } // fim do metodo inundarLSP

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
   * Funcao: Envia um Hello Request para todas as portas fisicas para conhecer os
   * vizinhos
   * Parametros:
   * Retorno: void
   */
  public void iniciarDescobertaDeVizinhos() {
    System.out.println("Roteador " + idRoteador + ": iniciando descoberta de vizinhos (Pacote Hello)...");
    for (Aresta conexao : conexoes) {
      Roteador vizinho = conexao.getDestino();
      PacoteHello request = new PacoteHello(this.idRoteador, vizinho.getIdRoteador(), false);
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
   * Metodo: iniciarEnvioPeriodico
   * Funcao: Configura um cronometro para reenviar a tabela de rotas a cada 10
   * segundos
   * Parametros:
   * Retorno: void
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
   * Parametros:
   * Retorno: void
   */
  public void gerarPingAleatorio() {
    Random gerador = new Random();
    boolean teveMudancaSignificativa = false; // Flag para marcar se o ping mudou muito

    // Percorre todas as conexoes (cabos) do roteador
    for (Aresta conexao : conexoes) {
      int pingOficialDaRede = conexao.getLatenciaAnunciada(); // Pega o ultimo ping avisado

      // Sorteia um novo ping entre 1 e 500ms
      int novaLatencia = gerador.nextInt(500) + 1;

      // Atualiza o valor do ping internamente
      conexao.setLatencia(novaLatencia);

      // Manda a interface grafica atualizar o numero na tela
      controller.atualizarLatenciaVisual(this.idRoteador, conexao.getDestino().getIdRoteador(), novaLatencia);

      // Calcula a diferenca entre o ping novo e o antigo
      int diferenca = Math.abs(novaLatencia - pingOficialDaRede);

      // Se for o primeiro ping ou a mudanca for maior que 50ms
      if (pingOficialDaRede == 0 || diferenca > 50) {
        // Salva o novo valor oficial
        conexao.setLatenciaAnunciada(novaLatencia);
        teveMudancaSignificativa = true; // Marca que houve uma mudanca drastica
      } // fim do if

      System.out.println("Roteador " + idRoteador + " pingou vizinho R" + conexao.getDestino().getIdRoteador() + " -> "
          + novaLatencia + "ms");
    } // fim do for

    // Se teve mudanca drastica e ja conhece todos os vizinhos
    if (teveMudancaSignificativa && ecosRecebidos >= conexoes.size()) {
      System.out.println("Roteador " + idRoteador + ": Mudanca drastica de ping detetada! Avisando a rede...");

      // Espalha a nova informacao para a rede inteira
      inundarLSP();
    } // fim do if
  } // fim do metodo gerarPingAleatorio

  /*
   * Metodo: iniciarMonitoramentoPing
   * Funcao: Configura um cronometro para sortear novas latencias a cada 15
   * segundos
   * Parametros:
   * Retorno: void
   */
  public void iniciarMonitoramentoPing() {
    // Cancela o timer antigo se existir
    if (timerPing != null)
      timerPing.cancel();

    timerPing = new Timer();
    // Configura para rodar repetidamente a cada 15 segundos
    timerPing.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        try {
          // So envia o ping se ja conheceu todos os vizinhos
          if (vizinhosDescobertos >= conexoes.size() && conexoes.size() > 0) {
            
            // Cria um ping para cada vizinho conectado
            for (Aresta conexao : conexoes) {
              PacoteEcho request = new PacoteEcho(idRoteador, conexao.getDestino().getIdRoteador());
              request.setReply(false); // Define que e um pedido (Request)
              request.setLatenciaIda(new java.util.Random().nextInt(250) + 1); // Sorteia o tempo de ida

              transmitirParaVizinho(request, conexao.getDestino()); // Envia o pacote
            } // fim do for
          } // fim do if 
        } catch (Exception e) {
          // Ignora erros para nao travar a thread
        } // fim do try-catch
      }
    }, 15000, 15000);
  } // fim do metodo iniciarMonitoramentoPing

  /*
   * Metodo: recalcularRotasDijkstra
   * Funcao: Usa o mapa global para calcular o menor caminho para todos os destinos
   * Parametros:
   * Retorno: void
   */
  public void recalcularRotasDijkstra() {
    // Se o mapa ainda estiver vazio, nao faz nada
    if (topologiaGlobal.isEmpty())
      return;

    Map<Integer, Integer> distancias = new HashMap<>();
    Map<Integer, Integer> predecessores = new HashMap<>();
    ArrayList<Integer> naoVisitados = new ArrayList<>();

    // Inicializa todas as distancias como infinito (999999)
    for (Integer idNode : topologiaGlobal.keySet()) {
      distancias.put(idNode, 999999); 
      predecessores.put(idNode, -1);
      naoVisitados.add(idNode);
    }
    // A distancia para si proprio e sempre zero
    distancias.put(this.idRoteador, 0);

    // Loop principal do Algoritmo de Dijkstra
    while (!naoVisitados.isEmpty()) {
      // Procura o roteador nao visitado com o menor custo atual
      int u = -1;
      int menorDistancia = 999999;
      for (int id : naoVisitados) {
        if (distancias.get(id) < menorDistancia) {
          menorDistancia = distancias.get(id);
          u = id;
        } // fim do if
      } // fim do for

      // Se nao encontrou caminhos validos, encerra a busca
      if (u == -1)
        break;

      // Marca o roteador atual como visitado
      naoVisitados.remove(Integer.valueOf(u));

      // Verifica os vizinhos olhando para a visao global da rede
      Map<Integer, Integer> vizinhosDeU = topologiaGlobal.get(u);
      if (vizinhosDeU != null) {
        for (Map.Entry<Integer, Integer> vizinho : vizinhosDeU.entrySet()) {
          int v = vizinho.getKey();
          int peso = vizinho.getValue();

          // Se o caminho novo for mais rapido, atualiza o custo e salva a rota
          if (naoVisitados.contains(v)) {
            int novaDistancia = distancias.get(u) + peso;
            if (novaDistancia < distancias.get(v)) {
              distancias.put(v, novaDistancia);
              predecessores.put(v, u); // Guarda de onde viemos para chegar em 'v'
            } // fim do if
          } // fim do if
        } // fim do for
      } // fim do if
    } // Fim do Dijkstra

    // Reconstroi a tabela de roteamento baseada nos resultados
    synchronized (tabelaRoteamento) {
      tabelaRoteamento.clear(); // Limpa as rotas antigas

      Aresta rotaLocal = new Aresta(this, 0);
      rotaLocal.setLatencia(0);
      tabelaRoteamento.put(this.idRoteador, rotaLocal); // Adiciona rota para si mesmo

      // Monta o caminho para cada destino possivel
      for (Integer destino : topologiaGlobal.keySet()) {
        // Ignora a si mesmo ou destinos inalcancaveis
        if (destino == this.idRoteador || distancias.get(destino) == 999999)
          continue;

        int passoAtual = destino;
        int nextHop = passoAtual;

        // Faz o caminho reverso para descobrir qual e o primeiro salto (next hop)
        while (predecessores.get(passoAtual) != this.idRoteador && predecessores.get(passoAtual) != -1) {
          passoAtual = predecessores.get(passoAtual);
          nextHop = passoAtual;
        } // fim do while

        // Busca o objeto real do vizinho correspondente nas conexoes fisicas
        Roteador instanciaNextHop = null;
        for (Aresta c : conexoes) {
          if (c.getDestino().getIdRoteador() == nextHop) {
            instanciaNextHop = c.getDestino();
            break;
          } // fim do if
        } // fim do for

        // Salva a nova rota finalizada na tabela
        if (instanciaNextHop != null) {
          Aresta novaRota = new Aresta(instanciaNextHop, distancias.get(destino));
          novaRota.setLatencia(distancias.get(destino)); // Sincroniza o custo para a interface
          tabelaRoteamento.put(destino, novaRota);
        } // fim do if
      } // fim do for
    } // fim do synchronized

    // Manda a interface grafica atualizar o painel lateral com os novos valores
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

  public List<Aresta> getConexoes() {
    return conexoes;
  }

  public void setConexoes(List<Aresta> conexoes) {
    this.conexoes = conexoes;
  }

  public Map<Integer, Aresta> getCopiaSeguraTabelaRoteamento() {
    synchronized (tabelaRoteamento) {
      return new HashMap<>(tabelaRoteamento);
    } // fim do synchronized
  }

}
