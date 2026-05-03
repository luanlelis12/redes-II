/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 03 05 2026
* Nome.............: BackboneController.java
* Funcao...........: Controller para gerenciar entre tela do backbone do programa e os models 
*************************************************************** */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.scene.control.Alert;
import javafx.scene.Cursor;
import model.Aresta;
import model.Backbone;
import model.Pacote;
import model.PacoteEcho;
import model.PacoteVetor;
import model.Roteador;
import util.FxmlRotas;

public class BackboneController implements Initializable {
  // Elementos do javaFx
  @FXML
  private AnchorPane paneRoteadores;
  @FXML
  private AnchorPane paneConfiguracoes;
  @FXML
  private ChoiceBox<Integer> choiceOrigem;
  @FXML
  private ChoiceBox<Integer> choiceDestino;
  @FXML
  private Label labelCustoCaminho;
  @FXML
  private VBox paneTabelasLateral;
  @FXML
  private VBox vboxTabelas;
  @FXML
  private Button buttonIniciar;

  private Backbone rede = new Backbone();
  private int quantRoteadores = 0;
  private double anguloDosRoteadores = 0, centroDaTopologiaX, centroDaTopologiaY;
  private Paint corAnterior = Color.WHITE;
  private double strokeAnterior = 1;

  private final String ARQUIVO = "backbone.txt";

  private final Image IMAGEM_ROTEADOR = new Image("file:view/img/roteador.png"),
      IMAGEM_ROTEADOR_ORIGEM = new Image("file:view/img/roteadorOrigem.png"),
      IMAGEM_ROTEADOR_DESTINO = new Image("file:view/img/roteadorDestino.png"),
      IMAGEM_PACOTE = new Image("file:view/img/pacote.png"),
      IMAGEM_PACOTE_ECHO_REQUEST = new Image("file:view/img/pacoteEchoRequest.png"),
      IMAGEM_PACOTE_ECHO_REPLY = new Image("file:view/img/pacoteEchoReply.png"),
      IMAGEM_PACOTE_VETOR = new Image("file:view/img/pacoteVetor.png"),
      IMAGEM_HAUNTER = new Image("file:view/img/haunter.png"),
      IMAGEM_GENGAR = new Image("file:view/img/gengar.png");

  private final double RAIO = 250, RAIO_POKEMON = 180, LARGURA_ROTEADOR = IMAGEM_ROTEADOR.getWidth(),
      ALTURA_ROTEADOR = IMAGEM_ROTEADOR.getHeight(), LARGURA_HAUNTER = IMAGEM_HAUNTER.getWidth(),
      ALTURA_HAUNTER = IMAGEM_HAUNTER.getHeight(),
      LARGURA_GENGAR = IMAGEM_GENGAR.getWidth(), ALTURA_GENGAR = IMAGEM_GENGAR.getHeight();

  private final ImageView HAUNTER = new ImageView(IMAGEM_HAUNTER), GENGAR = new ImageView(IMAGEM_GENGAR);

  private ArrayList<PathTransition> arrayAnimacoes = new ArrayList<>();
  private Map<String, Pair<Line, Label>> mapaLinhas = new HashMap<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O BackboneController foi carregado corretamente!");
    rede.carregarArquivo(ARQUIVO, this);

    Platform.runLater(() -> {
      desenharRede(ARQUIVO);
      abrirSobre();

      // Itera sobre os roteadores e inicializa as threads
      for (Roteador r : rede.getRoteadores()) {
        r.setDaemon(true);
        r.start();
      } // fim do for

      // Itera sobre os roteador para colocar na choiceBox
      for (Roteador r : rede.getRoteadores()) {
        choiceOrigem.getItems().add(r.getIdRoteador());
        choiceDestino.getItems().add(r.getIdRoteador());
      } // fim do for

      // Muda as imagens do roteador de origem do pacote
      choiceOrigem.getSelectionModel().selectedItemProperty().addListener((observable, valorAntigo, valorNovo) -> {
        paneRoteadores.getChildren().remove(HAUNTER);
        if (valorAntigo != null && valorAntigo <= rede.getRoteadores().size()) {
          Roteador roteadorAntigo = rede.getRoteadores().get(valorAntigo - 1);
          if (roteadorAntigo != null && roteadorAntigo.getImageView() != null) {
            roteadorAntigo.getImageView().setImage(IMAGEM_ROTEADOR);
          } // fim do if
        } // fim do if
        if (valorNovo != null && valorNovo <= rede.getRoteadores().size()) {
          Roteador roteadorNovo = rede.getRoteadores().get(valorNovo - 1);
          if (roteadorNovo != null && roteadorNovo.getImageView() != null) {
            roteadorNovo.getImageView().setImage(IMAGEM_ROTEADOR_ORIGEM);
            double[] coordenada = calcularPosicaoPokemon(valorNovo);
            HAUNTER.setLayoutX(coordenada[0] - LARGURA_HAUNTER / 2);
            HAUNTER.setLayoutY(coordenada[1] - ALTURA_HAUNTER / 2);
            paneRoteadores.getChildren().add(HAUNTER);
          } // fim do if
        } // fim do if
      });

      // Muda as imagens do roteador de destino do pacote
      choiceDestino.getSelectionModel().selectedItemProperty().addListener((observable, valorAntigo, valorNovo) -> {
        if (valorAntigo != null && valorAntigo <= rede.getRoteadores().size()) {
          Roteador roteadorAntigo = rede.getRoteadores().get(valorAntigo - 1);
          if (roteadorAntigo != null && roteadorAntigo.getImageView() != null) {
            roteadorAntigo.getImageView().setImage(IMAGEM_ROTEADOR);
          } // fim do if
        } // fim do if
        if (valorNovo != null && valorNovo <= rede.getRoteadores().size()) {
          Roteador roteadorNovo = rede.getRoteadores().get(valorNovo - 1);
          if (roteadorNovo != null && roteadorNovo.getImageView() != null) {
            roteadorNovo.getImageView().setImage(IMAGEM_ROTEADOR_DESTINO);
          } // fim do if
        } // fim do if
      });

    });
  } // fim do initialize

  /*
   * Metodo: desenharRede
   * Funcao: Faz a disposicao dos roteadores na topologia e os exibem com suas
   * devidas conexoes
   * Parametros: caminho = rota do arquivo backbone.txt
   * Retorno: void
   */
  private void desenharRede(String caminho) {
    centroDaTopologiaX = paneRoteadores.getPrefWidth() / 2;
    centroDaTopologiaY = paneRoteadores.getPrefHeight() / 2;
    quantRoteadores = rede.getRoteadores().size();

    anguloDosRoteadores = 360 / quantRoteadores;

    // itera sobre os roteadores da rede para calcular a coordenada de cada um deles
    // para exibir na tela
    for (Roteador roteador : rede.getRoteadores()) {
      int idRoteador = roteador.getIdRoteador();
      roteador.setCoordenadaXY(calcularPosicaoRoteador(idRoteador));
      exibirRoteador(roteador);
      // itera sobre as arestas de conexao para exibir cada um delas na tela
      for (Aresta conexao : roteador.getConexoes()) {
        exibirConexao(roteador, conexao.getDestino(), conexao.getLatencia());
      } // fim do for
    } // fim do for
  } // fim do metodo desenharRede

  /*
   * Metodo: calcularPosicaoRoteador
   * Funcao: calcula a posicao do roteador
   * Parametros: idRoteador = id dele para saber qual sera sua posicao
   * Retorno: double[] = coordenada (x,y) do roteador
   */
  public double[] calcularPosicaoRoteador(int idRoteador) {
    double angulo = Math.toRadians(idRoteador * anguloDosRoteadores);
    double posX = centroDaTopologiaX + RAIO * Math.cos(angulo);
    double posY = centroDaTopologiaY + RAIO * Math.sin(angulo);
    return new double[] { posX, posY };
  } // fim do metodo calcularPosicaoRoteador

  /*
   * Metodo: calcularPosicaoPokemon
   * Funcao: calcula a posicao do pokemon
   * Parametros: idRoteador = id do roteador referente ao pokemon
   * Retorno: double[] = coordenada (x,y) do pokemon
   */
  public double[] calcularPosicaoPokemon(int idRoteador) {
    double angulo = Math.toRadians(idRoteador * anguloDosRoteadores);
    double posX = centroDaTopologiaX + RAIO_POKEMON * Math.cos(angulo);
    double posY = centroDaTopologiaY + RAIO_POKEMON * Math.sin(angulo);
    return new double[] { posX, posY };
  } // fim do metodo calcularPosicaoPokemon

  /*
   * Metodo: exibirRoteador
   * Funcao: recebe o roteador e o posiciona na tela
   * Parametros: roteador = roteador
   * Retorno: void
   */
  public void exibirRoteador(Roteador roteador) {
    Platform.runLater(() -> {
      ImageView roteadorView = new ImageView(IMAGEM_ROTEADOR);
      roteador.setImageView(roteadorView);
      Label labelIdRoteador = new Label("" + roteador.getIdRoteador());
      labelIdRoteador.setFont(Font.font("system", FontWeight.BOLD, FontPosture.REGULAR, 18));

      double posX = roteador.getCoordenadaXY()[0];
      double posY = roteador.getCoordenadaXY()[1];

      roteadorView.setFitWidth(LARGURA_ROTEADOR);
      roteadorView.setFitHeight(ALTURA_ROTEADOR);
      roteadorView.setLayoutX(posX - LARGURA_ROTEADOR / 2);
      roteadorView.setLayoutY(posY - ALTURA_ROTEADOR / 2);

      labelIdRoteador.setLayoutX(posX + 14);
      labelIdRoteador.setLayoutY(posY - 30);

      // Imagem do roteador
      paneRoteadores.getChildren().add(roteadorView);
      // Id do roteador
      paneRoteadores.getChildren().add(labelIdRoteador);
    });
  } // fim do metodo exibirRoteador

  /*
   * Metodo: exibirConexao
   * Funcao: exibir a conexao entre dois roteadores
   * Parametros: r1 = roteador; r2 = roteador, getLatencia = latencia da conexao
   * Retorno: void
   */
  public void exibirConexao(Roteador r1, Roteador r2, int latencia) {
    Platform.runLater(() -> {

      int idMin = Math.min(r1.getIdRoteador(), r2.getIdRoteador());
      int idMax = Math.max(r1.getIdRoteador(), r2.getIdRoteador());
      String chave = idMin + "-" + idMax;

      if (mapaLinhas.containsKey(chave)) {
        Label label = mapaLinhas.get(chave).getValue();
        label.setText(label.getText() + latencia);
        return;
      }

      double[] posicaoR1 = r1.getCoordenadaXY();
      double[] posicaoR2 = r2.getCoordenadaXY();

      Line conexao = new Line(posicaoR1[0], posicaoR1[1], posicaoR2[0], posicaoR2[1]);
      conexao.setStroke(Color.WHITE);
      paneRoteadores.getChildren().add(conexao);
      conexao.toBack();

      Label latenciaConexao = new Label("" + latencia + ";");
      latenciaConexao.setStyle(
          "-fx-font-weight: bold; -fx-background-color: gray; -fx-text-fill: white; -fx-border-radius: 50%; -fx-background-radius: 50%;");
      latenciaConexao.setPrefSize(40, 20);
      latenciaConexao.setAlignment(Pos.CENTER);
      latenciaConexao.setLayoutX((posicaoR1[0] + posicaoR2[0]) / 2);
      latenciaConexao.setLayoutY((posicaoR1[1] + posicaoR2[1]) / 2);
      paneRoteadores.getChildren().add(latenciaConexao);

      // Quando passar o mouse, a linha fica vermelha avisando o perigo
      conexao.setOnMouseEntered(e -> {
        corAnterior = conexao.getStroke();
        strokeAnterior = conexao.getStrokeWidth();
        conexao.setStroke(Color.RED);
        conexao.setStrokeWidth(3);
        conexao.setCursor(Cursor.HAND);
      });

      // Quando tirar o mouse, volta ao normal
      conexao.setOnMouseExited(e -> {
        conexao.setStroke(corAnterior);
        conexao.setStrokeWidth(strokeAnterior);
        conexao.setCursor(Cursor.DEFAULT);
      });

      latenciaConexao.setCursor(Cursor.HAND);
      // Ao clicar na linha ou no numero da latencia, corta a conexao!
      conexao.setOnMouseClicked(e -> cortarConexao(r1, r2, chave));
      latenciaConexao.setOnMouseClicked(e -> cortarConexao(r1, r2, chave));

      mapaLinhas.put(chave, new Pair<Line, Label>(conexao, latenciaConexao));
    });
  } // fim do metodo exibirConexao

  /*
   * Metodo: cortarConexao
   * Funcao: Permite que o usuario corte uma das conexoes da backbone
   * Parametros: r1 = roteador, r2 = roteador, chaveLinha = conexao
   * Retorno: void
   */
  public void cortarConexao(Roteador r1, Roteador r2, String chaveLinha) {
    System.out
        .println("Cortando conexao entre roteador " + r1.getIdRoteador() + " e roteador " + r2.getIdRoteador() + "!");

    // Interrompe todas as animacoes e limpa as filas de espera
    reiniciarRede();
    for (Roteador r : rede.getRoteadores()) {
      r.getBufferPacotes().clear();
    } // fim do for

    // Mostra o Pop-out de aviso
    Alert alerta = new Alert(Alert.AlertType.WARNING);
    alerta.setTitle("Conexao Rompida!");
    alerta.setHeaderText("Alerta de Falha!");
    alerta.setContentText("O cabo entre o Roteador " + r1.getIdRoteador() + " e o Roteador " + r2.getIdRoteador()
        + " foi rompido!\nTodos os pacotes em transito foram descartados. A rede ira recalcular as rotas agora.");
    alerta.showAndWait();

    // Remove a conexao logicamente da lista de cabos dos roteadores
    r1.getConexoes().removeIf(c -> c.getDestino().getIdRoteador() == r2.getIdRoteador());
    r2.getConexoes().removeIf(c -> c.getDestino().getIdRoteador() == r1.getIdRoteador());

    // Remove a linha e a label visualmente da tela
    Pair<Line, Label> componentes = mapaLinhas.get(chaveLinha);
    if (componentes != null) {
      paneRoteadores.getChildren().removeAll(componentes.getKey(), componentes.getValue());
      mapaLinhas.remove(chaveLinha);
    }

    synchronized (r1.getTabelaRoteamento()) {
      for (Map.Entry<Integer, Aresta> entrada : r1.getTabelaRoteamento().entrySet()) {
        if (entrada.getValue().getDestino().getIdRoteador() == r2.getIdRoteador()) {
          entrada.setValue(new Aresta(r2, 9999)); 
        }
      }
    }
    synchronized (r2.getTabelaRoteamento()) {
      for (Map.Entry<Integer, Aresta> entrada : r2.getTabelaRoteamento().entrySet()) {
        if (entrada.getValue().getDestino().getIdRoteador() == r1.getIdRoteador()) {
          entrada.setValue(new Aresta(r1, 9999));
        }
      }
    }
    r1.enviarVetorParaVizinhos();
    r2.enviarVetorParaVizinhos();

    atualizarTabelasNaTela();
    verificarStatusDaRede();
  } // fim do metodo cortarConexao

  /*
   * Metodo: exibirPacote
   * Funcao: Exibe o pacote e faz a animacao do pacote indo de um roteador a outro
   * Parametros: pacote = pacote sendo enviado; rOrigem = roteador que esta
   * enviando o pacote; rDestino = roteador que esta recebendo o pacote
   * Retorno: void
   */
  public void exibirPacote(Pacote pacote, Roteador rOrigem, Roteador rDestino) {
    Platform.runLater(() -> {

      ImageView imageViewPacote = new ImageView();
      // muda a imagem de acordo com o tipo de pacote enviado
      if (pacote instanceof PacoteEcho) {
        imageViewPacote.setImage(escolherPacoteEcho((PacoteEcho) pacote));
      } else if (pacote instanceof PacoteVetor) {
        imageViewPacote.setImage(IMAGEM_PACOTE_VETOR);
      } else {
        imageViewPacote.setImage(IMAGEM_PACOTE);
      } // fim do if
      imageViewPacote.setFitWidth(30);
      imageViewPacote.setFitHeight(30);
      Path caminho = new Path();

      double[] posicaoR1 = rOrigem.getCoordenadaXY();
      double[] posicaoR2 = rDestino.getCoordenadaXY();

      caminho.getElements().add(new MoveTo(posicaoR1[0], posicaoR1[1]));
      caminho.getElements().add(new LineTo(posicaoR2[0], posicaoR2[1]));

      PathTransition animacao = new PathTransition();
      animacao.setDuration(Duration.seconds(1));
      animacao.setNode(imageViewPacote);
      animacao.setPath(caminho);
      animacao.setCycleCount(1);
      animacao.setAutoReverse(true);
      arrayAnimacoes.add(animacao);

      // bloqueia o botao de enviar quando tiver alguma animacao ocorrendo
      if (buttonIniciar != null)
        buttonIniciar.setDisable(true);

      // Quando a animacao acabar o roteador recebe o pacote e o processa
      animacao.setOnFinished(e -> {
        paneRoteadores.getChildren().remove(imageViewPacote);
        rDestino.receberPacote(pacote);
        arrayAnimacoes.remove(animacao);
        verificarStatusDaRede();
      });

      animacao.play();

      paneRoteadores.getChildren().add(imageViewPacote);
    });
  } // fim do metodo exibirPacote

  /*
   * Metodo: escolherPacoteEcho
   * Funcao: Define a imagem do pacote de echo
   * Parametros: pacote = pacote recebido
   * Retorno: retorna a imagem
   */
  public Image escolherPacoteEcho(PacoteEcho pacote) {
    if (pacote.isReply()) {
      return IMAGEM_PACOTE_ECHO_REPLY;
    } else {
      return IMAGEM_PACOTE_ECHO_REQUEST;
    } // fim do if
  } // fim do metodo escolherPacoteEcho

  /*
   * Metodo: recarregarBackbone
   * Funcao: recarrega o backbone caso tenha uma alteracao no txt
   * Parametros:
   * Retorno: void
   */
  public void recarregarBackbone() {
    System.out.println("--- REINICIANDO O BACKBONE ---");
    reiniciarRede();
    // Itera sobre os roteadores para desliga-los
    for (Roteador r : rede.getRoteadores()) {
      r.desligar();
      r.interrupt();
    } // fim do for

    mapaLinhas.clear();
    rede.carregarArquivo(ARQUIVO, this);

    Platform.runLater(() -> {
      paneRoteadores.getChildren().removeIf(elemento -> !(elemento instanceof VBox));
      choiceOrigem.getItems().clear();
      choiceDestino.getItems().clear();
      desenharRede(ARQUIVO);

      for (Roteador r : rede.getRoteadores()) {
        r.setDaemon(true);
        r.start();
      } // fim do for

      // Itera sobre os roteador para colocar na choiceBox
      for (Roteador r : rede.getRoteadores()) {
        choiceOrigem.getItems().add(r.getIdRoteador());
        choiceDestino.getItems().add(r.getIdRoteador());
      } // fim do for
    });
  } // fim do metodo recarregarBackbone

  /*
   * Metodo: iniciarEnvio
   * Funcao: faz a chamada dos roteadores e comece o encaminhamento do pacote de
   * um roteador a outro
   * Parametros:
   * Retorno: void
   */
  public void iniciarEnvio() {
    if (choiceOrigem.getValue() == null || choiceDestino.getValue() == null)
      return;
    int idOrigem = choiceOrigem.getValue();
    int idDestino = choiceDestino.getValue();
    if (idOrigem == idDestino)
      return;

    // Limpa apenas o visual da entrega anterior na tela (O Pokemon e o Custo)
    paneRoteadores.getChildren().remove(GENGAR);
    Pacote.setCustoTotalDeEnvio(0);
    atualizarCustoTotalDoCaminho(0);

    // Apaga os rastros verdes de qualquer envio anterior
    for (Pair<Line, Label> par : mapaLinhas.values()) {
      par.getKey().setStroke(Color.WHITE);
      par.getKey().setStrokeWidth(1);
    } // fim do for

    desenharMenorCaminho(idOrigem, idDestino);

    // Cria o pacote e entrega diretamente para o roteador de origem!
    Pacote pacoteDados = new Pacote(idOrigem, idDestino);
    Roteador rOrigem = rede.getRoteadores().get(idOrigem - 1);

    System.out.println(">> Iniciando envio de DADOS do R" + idOrigem + " para R" + idDestino);
    rOrigem.enviarPacoteDados(pacoteDados);

    // Trava o botao para o usuario nao floodar a rede com multiplos pacotes
    if (buttonIniciar != null)
      buttonIniciar.setDisable(true);
  } // fim do metodo iniciarEnvio

  /*
   * Metodo: desenharMenorCaminho
   * Funcao: destaca o menor caminho entre roteadores
   * Parametros: idOridem = id do roteador que envia o pacote
   * idDestino = id do roteador final que recebe o pacote
   * Retorno: double = o tempo que a animacao demora
   */
  public void desenharMenorCaminho(int idOrigem, int idDestino) {
    int atual = idOrigem;
    int limiteDeSaltos = rede.getRoteadores().size();
    int saltos = 0;

    // Itera sobre os roteadores ate chegar no roteador de destino
    while (atual != idDestino && saltos < limiteDeSaltos) {
      Roteador roteadorAtual = rede.getRoteadores().get(atual - 1);

      // Pega a rota da copia SEGURA para evitar travamentos de thread
      Aresta rota = roteadorAtual.getCopiaSeguraTabelaRoteamento().get(idDestino);

      // Se a rede ainda nao conhece o caminho, interrompe a pintura
      if (rota == null || rota.getDestino() == null)
        break;

      int proximo = rota.getDestino().getIdRoteador();

      // Identifica o cabo fisico correto na tela
      int idMin = Math.min(atual, proximo);
      int idMax = Math.max(atual, proximo);
      Pair<Line, Label> componentesCabo = mapaLinhas.get(idMin + "-" + idMax);

      // Destaca o cabo IMEDIATAMENTE de verde!
      if (componentesCabo != null) {
        Line linhaCaminho = componentesCabo.getKey();
        linhaCaminho.setStroke(Color.LIMEGREEN); // Pinta de verde neon
        linhaCaminho.setStrokeWidth(3); // Deixa a linha mais grossa
        linhaCaminho.toBack(); // Mantem a linha atras dos icones
      } // fim do if

      // O atual pula para o proximo roteador do caminho
      atual = proximo;
      saltos++;
    } // fim do while
  } // fim do metodo desenharMenorCaminho

  /*
   * Metodo: reiniciarRede
   * Funcao: reinicia as contagens e desliga as animacoes dos pacotes
   * Parametros:
   * Retorno: void
   */
  public void reiniciarRede() {
    Pacote.setCustoTotalDeEnvio(0);
    atualizarCustoTotalDoCaminho(0);

    paneRoteadores.getChildren().remove(HAUNTER);
    paneRoteadores.getChildren().remove(GENGAR);

    // Itera sobre a conexoes para deixar na cor padrao
    for (Pair<Line, Label> par : mapaLinhas.values()) {
      par.getKey().setStroke(Color.WHITE);
      par.getKey().setStrokeWidth(1);
    } // fim do for

    // Itera sobre o array de animacoes para desliga-las
    for (PathTransition animacao : arrayAnimacoes) {
      animacao.stop();
      paneRoteadores.getChildren().remove(animacao.getNode());
    } // fim do for
    arrayAnimacoes.clear();
  } // fim do metodo reiniciarRede

  /*
   * Metodo: atualizarCustoTotalDoCaminho
   * Funcao: exibi na interface o custo do caminho
   * Parametros: contador = quantidade de pacotes gerados
   * Retorno: void
   */
  public void atualizarCustoTotalDoCaminho(int contador) {
    Platform.runLater(() -> labelCustoCaminho.setText(String.valueOf(contador)));
  } // fim do metodo atualizarCustoTotalDoCaminho

  /*
   * Metodo: abrirSobre
   * Funcao: Abre popout falando sobre o algoritmo
   * Parametros:
   * Retorno: void
   */
  public void abrirSobre() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlRotas.TELA_SOBRE));
      Parent root = loader.load();

      Stage popOut = new Stage();

      popOut.initModality(Modality.APPLICATION_MODAL);
      popOut.setResizable(false);

      popOut.setScene(new Scene(root));
      popOut.setTitle("ROTEAMENTO POR VETOR DISTANCIA - SOBRE");
      Image icon = new Image(getClass().getResourceAsStream("../view/img/icon.png"));
      popOut.getIcons().add(icon);
      popOut.show();

    } catch (IOException e) {
      System.out.println("Erro ao abrir a tela de Sobre!");
      e.printStackTrace();
    } // fim do try-catch
  } // fim do metodo abrirSobre

  /*
   * Metodo: exibirGengar
   * Funcao: exibe o Gengar (pokemon) na tela
   * Parametros: idRoteador = roteador destino
   * Retorno: void
   */
  public void exibirGengar(int idRoteador) {
    Platform.runLater(() -> {
      // Caso ja tenha um gengar na tela nao exibe outro
      if (!paneRoteadores.getChildren().contains(GENGAR)) {
        double[] coordenada = calcularPosicaoPokemon(idRoteador);
        GENGAR.setLayoutX(coordenada[0] - LARGURA_GENGAR / 2);
        GENGAR.setLayoutY(coordenada[1] - ALTURA_GENGAR / 2);

        paneRoteadores.getChildren().add(GENGAR);
      } // fim do if
    });
  } // fim do metodo exibirGengar

  /*
   * Metodo: atualizarTabelasNaTela
   * Funcao: Le a tabela de cada roteador e desenha no painel lateral
   * Parametros:
   * Retorno: void
   */
  public void atualizarTabelasNaTela() {
    if (paneTabelasLateral == null)
      return;

    Platform.runLater(() -> {
      vboxTabelas.getChildren().clear(); // Limpa as tabelas antigas

      for (Roteador r : rede.getRoteadores()) {
        VBox cardRoteador = new VBox(5);
        cardRoteador.setStyle("-fx-background-color: #333333; -fx-padding: 10; -fx-background-radius: 8;");

        Label titulo = new Label("Tabela do Roteador " + r.getIdRoteador());
        titulo.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 14px;");
        cardRoteador.getChildren().add(titulo);

        // Cabecalho da Tabela
        Label cabecalho = new Label(String.format("%-7s | %-6s | %-6s", "Destino", "Custo", "Salto"));
        cabecalho.setStyle("-fx-text-fill: white; -fx-font-family: monospace; -fx-font-weight: bold;");
        cardRoteador.getChildren().add(cabecalho);

        // Linhas da Tabela
        for (Map.Entry<Integer, Aresta> linha : r.getCopiaSeguraTabelaRoteamento().entrySet()) {
          int destino = linha.getKey();
          Aresta aresta = linha.getValue();

          String custoStr = (aresta != null) ? String.valueOf(aresta.getLatencia()) : "Inf";
          String saltoStr = (aresta != null && aresta.getDestino() != null)
              ? String.valueOf(aresta.getDestino().getIdRoteador())
              : "-";

          Label labelLinha = new Label(String.format("%-7d | %-6s | %-6s", destino, custoStr, saltoStr));
          labelLinha.setStyle("-fx-text-fill: #bdc3c7; -fx-font-family: monospace;");
          cardRoteador.getChildren().add(labelLinha);
        } // fim do for

        vboxTabelas.getChildren().add(cardRoteador);
      } // fim do for
    });
  } // fim do metodo atualizarTabelasNaTela

  /*
   * Metodo: verificarStatusDaRede
   * Funcao: Checa se ha pacotes voando ou em fila. Se a rede estiver em paz,
   * libera o botao.
   * Parametros:
   * Retorno: void
   */
  public void verificarStatusDaRede() {
    Platform.runLater(() -> {
      boolean ocupado = !arrayAnimacoes.isEmpty();

      // Checa se as filas estao vazias
      for (Roteador r : rede.getRoteadores()) {
        if (!r.getBufferPacotes().isEmpty()) {
          ocupado = true;
          break;
        } // fim do if
      } // fim do for

      // Se tiver pacote voando ou na fila, desativa o botao
      if (buttonIniciar != null)
        buttonIniciar.setDisable(ocupado);
    });
  } // fim do metodo verificarStatusDaRede

}
