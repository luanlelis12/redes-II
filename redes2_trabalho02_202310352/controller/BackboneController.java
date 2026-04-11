/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 29 03 2026
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
import model.Aresta;
import model.Backbone;
import model.Pacote;
import model.Roteador;

public class BackboneController implements Initializable {
  // Elementos do javaFx
  @FXML
  AnchorPane paneRoteadores;
  @FXML
  AnchorPane paneConfiguracoes;
  @FXML
  ChoiceBox<Integer> choiceOrigem;
  @FXML
  ChoiceBox<Integer> choiceDestino;
  @FXML
  Label labelPacoteGerado;
  @FXML
  Label labelPacoteChegado;

  private Backbone rede = new Backbone();
  private int quantRoteadores = 0, pacotesChegados = 0;
  private double anguloDosRoteadores = 0, centroDaTopologiaX, centroDaTopologiaY;

  private final String ARQUIVO = "backbone.txt";

  private final Image IMAGEM_ROTEADOR = new Image("file:view/img/roteador.png"),
      IMAGEM_ROTEADOR_ORIGEM = new Image("file:view/img/roteadorOrigem.png"),
      IMAGEM_ROTEADOR_DESTINO = new Image("file:view/img/roteadorDestino.png"),
      IMAGEM_PACOTE = new Image("file:view/img/pacote.png"),
      IMAGEM_HAUNTER = new Image("file:view/img/haunter.png"),
      IMAGEM_GENGAR = new Image("file:view/img/gengar.png");

  private final double RAIO = 250, RAIO_POKEMON = 180, LARGURA_ROTEADOR = IMAGEM_ROTEADOR.getWidth(),
      ALTURA_ROTEADOR = IMAGEM_ROTEADOR.getHeight(), LARGURA_HAUNTER = IMAGEM_HAUNTER.getWidth(),
      ALTURA_HAUNTER = IMAGEM_HAUNTER.getHeight(),
      LARGURA_GENGAR = IMAGEM_GENGAR.getWidth(), ALTURA_GENGAR = IMAGEM_GENGAR.getHeight();

  private final ImageView HAUNTER = new ImageView(IMAGEM_HAUNTER), GENGAR = new ImageView(IMAGEM_GENGAR);

  private ArrayList<PathTransition> arrayAnimacoes = new ArrayList<>();
  private Map<String, Line> mapaLinhas = new HashMap<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O BackboneController foi carregado corretamente!");
    rede.carregarArquivo(ARQUIVO, this);

    for (Roteador r : rede.getRoteadores()) {
      r.setDaemon(true);
      r.start();
    } // fim do for

    Platform.runLater(() -> {

      desenharRede(ARQUIVO);
      // Itera sobre os roteador para colocar na choiceBox
      for (Roteador r : rede.getRoteadores()) {
        choiceOrigem.getItems().add(r.getIdRoteador());
        choiceDestino.getItems().add(r.getIdRoteador());
      } // fim do for

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
        exibirConexao(roteador, conexao.getDestino(), conexao.getPeso());
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
   * Parametros: r1 = roteador; r2 = roteador, peso = peso da conexao
   * Retorno: void
   */
  public void exibirConexao(Roteador r1, Roteador r2, int peso) {
    Platform.runLater(() -> {

      int idMin = Math.min(r1.getIdRoteador(), r2.getIdRoteador());
      int idMax = Math.max(r1.getIdRoteador(), r2.getIdRoteador());
      String chave = idMin + "-" + idMax;

      if (mapaLinhas.containsKey(chave)) {
        return;
      }

      double[] posicaoR1 = r1.getCoordenadaXY();
      double[] posicaoR2 = r2.getCoordenadaXY();

      Line conexao = new Line(posicaoR1[0], posicaoR1[1], posicaoR2[0], posicaoR2[1]);
      conexao.setStroke(Color.WHITE);

      mapaLinhas.put(chave, conexao);

      paneRoteadores.getChildren().add(conexao);
      conexao.toBack();

      Label pesoConexao = new Label("" + peso);
      pesoConexao.setStyle(
          "-fx-font-weight: bold; -fx-background-color: gray; -fx-text-fill: white; -fx-border-radius: 50%; -fx-background-radius: 50%;");
      pesoConexao.setPrefSize(20, 20);
      pesoConexao.setAlignment(Pos.CENTER);
      pesoConexao.setLayoutX((posicaoR1[0] + posicaoR2[0]) / 2);
      pesoConexao.setLayoutY((posicaoR1[1] + posicaoR2[1]) / 2);
      paneRoteadores.getChildren().add(pesoConexao);
    });
  } // fim do metodo exibirConexao

  /*
   * Metodo: exibirPacote
   * Funcao: Exibe o pacote e faz a animacao do pacote indo de um roteador a outro
   * Parametros: pacote = pacote sendo enviado; rOrigem = roteador que esta
   * enviando o pacote; rDestino = roteador que esta recebendo o pacote
   * Retorno: void
   */
  public void exibirPacote(Pacote pacote, Roteador rOrigem, Roteador rDestino) {
    Platform.runLater(() -> {
      ImageView imageViewPacote = new ImageView(IMAGEM_PACOTE);
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

      // Quando a animacao acabar o roteador recebe o pacote e o processa
      animacao.setOnFinished(e -> {
        paneRoteadores.getChildren().remove(imageViewPacote);
        rDestino.receberPacote(pacote);
      });

      animacao.play();

      paneRoteadores.getChildren().add(imageViewPacote);
    });
  } // fim do metodo exibirPacote

  /*
   * Metodo: recarregarBackbone
   * Funcao: recarrega o backbone caso tenha uma alteracao no txt
   * Parametros:
   * Retorno: void
   */
  public void recarregarBackbone() {
    System.out.println("--- REINICIANDO O BACKBONE ---");
    reiniciarRede();

    for (Roteador r : rede.getRoteadores()) {
      r.desligar();
      r.interrupt();
    }

    mapaLinhas.clear();

    rede.carregarArquivo(ARQUIVO, this);

    for (Roteador r : rede.getRoteadores()) {
      r.setDaemon(true);
      r.start();
    } // fim do for

    Platform.runLater(() -> {
      paneRoteadores.getChildren().removeIf(elemento -> !(elemento instanceof VBox));
      choiceOrigem.getItems().clear();
      choiceDestino.getItems().clear();

      desenharRede(ARQUIVO);

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
    reiniciarRede();

    // Caso o roteador de origem ou de destino nao sejam selecionado nao eh iniciado
    // a simulacao
    if (choiceOrigem.getValue() == null || choiceDestino.getValue() == null) {
      System.out.println("Erro: Selecione todos os campos antes de enviar!");
      return;
    } // fim do if

    int idOrigem = choiceOrigem.getValue();
    int idDestino = choiceDestino.getValue();

    // Caso o roteador de origem e destino sejam o mesmo nao eh iniciado a simulacao
    if (idOrigem == idDestino) {
      System.out.println("Erro: Selecione um roteador de destino diferente do roteador de origem!");
      return;
    } // fim do if

    // Itera para encerrar as threads de todos os roteadores e limpa os buffers
    for (Roteador roteador : rede.getRoteadores()) {
      roteador.desligar();
      roteador.getBufferPacotes().clear();
    } // fim do for

    Pacote primeiroPacote = new Pacote(idOrigem, idDestino);

    // itera sobre os roteadores para definir os algoritmos e ligalos
    for (Roteador roteador : rede.getRoteadores()) {
      roteador.calcularDijkstra(rede.getRoteadores());
      roteador.ligar();
    } // fim do for

    System.out.println("Caminho calculado!");
    double esperaTotal = desenharMenorCaminho(idOrigem, idDestino);

    // Cria um cronometro para o pacote esperar
    javafx.animation.PauseTransition pausaDoPacote = new javafx.animation.PauseTransition(
        javafx.util.Duration.seconds(esperaTotal + 0.2));

    pausaDoPacote.setOnFinished(e -> {
      // O pacote so sai da origem quando o cronometro termina
      Roteador rOrigem = rede.getRoteadores().get(idOrigem - 1);
      rOrigem.enviarPacote(primeiroPacote);
    });

    pausaDoPacote.play();
  } // fim do metodo iniciarEnvio

  /*
   * Metodo: desenharMenorCaminho
   * Funcao: destaca o menor caminho entre roteadores
   * Parametros: idOridem = id do roteador que envia o pacote
   * idDestino = id do roteador final que recebe o pacote
   * Retorno: double = o tempo que a animacao demora
   */
  public double desenharMenorCaminho(int idOrigem, int idDestino) {
    int atual = idOrigem;
    double tempoAcumulado = 0.0;
    double tempoPorLinha = 0.5;

    // Itera sobre os roteadores ate chegar no roteador de destino
    while (atual != idDestino) {
      Roteador roteadorAtual = rede.getRoteadores().get(atual - 1);

      // Pega o roteador que recebera o pacote
      Integer proximo = roteadorAtual.getTabelaProximoSalto().get(idDestino);

      if (proximo == null)
        break;

      // Pega a conexao entre os roteadores
      int idMin = Math.min(atual, proximo);
      int idMax = Math.max(atual, proximo);
      Line linhaCaminho = mapaLinhas.get(idMin + "-" + idMax);

      // Destaca a conexao de vermelho e deixa mais grossa
      if (linhaCaminho != null) {
        // Cria um delay para destacar esta linha
        javafx.animation.PauseTransition pausa = new javafx.animation.PauseTransition(
            javafx.util.Duration.seconds(tempoAcumulado));

        pausa.setOnFinished(e -> {
          linhaCaminho.setStroke(Color.LIMEGREEN);
          linhaCaminho.setStrokeWidth(3);
        });

        pausa.play();
        // Adiciona o tempo para a proxima linha acender depois desta
        tempoAcumulado += tempoPorLinha;
      } // fim do if

      // O atual agora passa a ser o proximo
      atual = proximo;
    } // fim do while
    return tempoAcumulado; // Devolve o tempo total que a animacao vai levar
  } // fim do metodo desenharMenorCaminho

  /*
   * Metodo: reiniciarRede
   * Funcao: reinicia as contagens e desliga as animacoes dos pacotes
   * Parametros:
   * Retorno: void
   */
  public void reiniciarRede() {
    pacotesChegados = -1;
    atualizarContadorPacotes(0);
    Pacote.setContadorPacotes(0);
    atualizarContadorPacotesChegados();

    paneRoteadores.getChildren().remove(HAUNTER);
    paneRoteadores.getChildren().remove(GENGAR);

    // Itera sobre a conexoes para deixar na cor padrao
    for (Line linha : mapaLinhas.values()) {
      linha.setStroke(Color.WHITE);
      linha.setStrokeWidth(1);
    } // fim do for

    // Itera sobre o array de animacoes para desliga-las
    for (PathTransition animacao : arrayAnimacoes) {
      animacao.stop();
      paneRoteadores.getChildren().remove(animacao.getNode());
    } // fim do for
    arrayAnimacoes.clear();
  } // fim do metodo reiniciarRede

  /*
   * Metodo: atualizarContadorPacotes
   * Funcao: adiciona mais um na contagem de pacotes gerados
   * Parametros: contador = quantidade de pacotes gerados
   * Retorno: void
   */
  public void atualizarContadorPacotes(int contador) {
    Platform.runLater(() -> labelPacoteGerado.setText(String.valueOf(contador)));
  } // fim do metodo atualizarContadorPacotes

  /*
   * Metodo: atualizarContadorPacotesChegados
   * Funcao: adiciona mais um na contagem de pacotes chegados no roteador destino
   * Parametros:
   * Retorno: void
   */
  public void atualizarContadorPacotesChegados() {
    pacotesChegados++;
    Platform.runLater(() -> labelPacoteChegado.setText(String.valueOf(pacotesChegados)));
  } // fim do metodo atualizarContadorPacotesChegados

  /*
   * Metodo: abrirSobre
   * Funcao: Abre popout falando sobre os 4 algoritmos
   * Parametros:
   * Retorno: void
   */
  public void abrirSobre() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/telaDeSobre.fxml"));
      Parent root = loader.load();

      Stage popOut = new Stage();
      popOut.setTitle("Sobre o Simulador");

      popOut.initModality(Modality.APPLICATION_MODAL);

      popOut.setResizable(false);

      popOut.setScene(new Scene(root));
      popOut.setTitle("ROTEAMENTO POR INUNDACAO - SOBRE");
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

}
