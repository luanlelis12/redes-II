/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 
* Nome.............: BackboneController.java
* Funcao...........: 
*************************************************************** */
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
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
  ChoiceBox<String> choiceImplementacao;
  @FXML
  ChoiceBox<Integer> choiceOrigem;
  @FXML
  ChoiceBox<Integer> choiceDestino;
  @FXML
  TextField ttlField;
  @FXML
  Label labelPacoteGerado;
  @FXML
  Label labelPacoteChegado;

  private Backbone rede = new Backbone();
  private int quantRoteadores = 0;
  private int pacotesChegados = 0;
  private double anguloDosRoteadores = 0;
  private int versaoAlgoritmo;

  private double centroDaTopologiaX;
  private double centroDaTopologiaY;

  private final String arquivo = "backbone.txt";
  private final Image imagemRoteador = new Image("file:view/img/roteador.png");
  private final Image imagemPacote = new Image("file:view/img/pacote.png");
  private final double raio = 300;

  private ArrayList<PathTransition> arrayAnimacoes = new ArrayList<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O BackboneController foi carregado corretamente!");
    rede.carregarArquivo(arquivo, this);

    for (Roteador r : rede.getRoteadores()) {
      Thread threadRoteador = new Thread(r);
      threadRoteador.setDaemon(true);
      threadRoteador.start();
    } // fim do for

    Platform.runLater(() -> {
      desenharRede(arquivo);
      // Adiciona as opcoes nas choiceBoxs
      choiceImplementacao.getItems().addAll("Opcao 1", "Opcao 2", "Opcao 3", "Opcao 4");
      choiceImplementacao.setValue("Opcao 1");
      for (Roteador r : rede.getRoteadores()) {
        choiceOrigem.getItems().add(r.getIdRoteador());
        choiceDestino.getItems().add(r.getIdRoteador());
      } // fim do for

      choiceOrigem.setValue(1);
      choiceDestino.setValue(quantRoteadores);

      ttlField.setText("1");
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
      double[] coordenadaRoteador = calcularPosicaoRoteador(idRoteador);
      exibirRoteador(idRoteador, coordenadaRoteador);
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
    double posX = centroDaTopologiaX + raio * Math.cos(angulo);
    double posY = centroDaTopologiaY + raio * Math.sin(angulo);
    return new double[] { posX, posY };
  } // fim do metodo calcularPosicaoRoteador

  /*
   * Metodo: exibirRoteador
   * Funcao: recebe a posicao do roteador e o posiciona na tela
   * Parametros: posX = posicao horizontal; posY = posicao vertical
   * Retorno: void
   */
  public void exibirRoteador(int idRoteador, double[] coordenadaRoteador) {
    ImageView roteadorView = new ImageView(imagemRoteador);
    Label labelIdRoteador = new Label("" + idRoteador);

    double posX = coordenadaRoteador[0];
    double posY = coordenadaRoteador[1];

    roteadorView.setFitWidth(50);
    roteadorView.setFitHeight(50);
    roteadorView.setLayoutX(posX - 25);
    roteadorView.setLayoutY(posY - 25);

    labelIdRoteador.setLayoutX(posX - 25);
    labelIdRoteador.setLayoutY(posY - 25);

    paneRoteadores.getChildren().add(roteadorView);
    paneRoteadores.getChildren().add(labelIdRoteador);
  } // fim do metodo exibirRoteador

  /*
   * Metodo: exibirConexao
   * Funcao: exibir a conexao entre dois roteadores
   * Parametros: r1 = roteador; r2 = roteador, peso = peso da conexao
   * Retorno: void
   */
  public void exibirConexao(Roteador r1, Roteador r2, int peso) {
    int idR1 = r1.getIdRoteador();
    int idR2 = r2.getIdRoteador();

    double[] posicaoR1 = calcularPosicaoRoteador(idR1);
    double[] posicaoR2 = calcularPosicaoRoteador(idR2);

    Line conexao = new Line(posicaoR1[0], posicaoR1[1], posicaoR2[0], posicaoR2[1]);
    paneRoteadores.getChildren().add(conexao);

    Label pesoConexao = new Label("" + peso);
    pesoConexao.setStyle("-fx-font-weight: bold; -fx-background-color: gray");
    pesoConexao.setPrefSize(15, 15);
    pesoConexao.setAlignment(Pos.CENTER);
    pesoConexao.setLayoutX((posicaoR1[0] + posicaoR2[0]) / 2);
    pesoConexao.setLayoutY((posicaoR1[1] + posicaoR2[1]) / 2);
    paneRoteadores.getChildren().add(pesoConexao);

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
      ImageView imageViewPacote = new ImageView(imagemPacote);
      imageViewPacote.setFitWidth(30);
      imageViewPacote.setFitHeight(30);
      Path caminho = new Path();

      double[] posicaoR1 = calcularPosicaoRoteador(rOrigem.getIdRoteador());
      double[] posicaoR2 = calcularPosicaoRoteador(rDestino.getIdRoteador());

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

      paneRoteadores.getChildren().addAll(caminho, imageViewPacote);
    });
  } // fim do metodo exibirPacote

  /*
   * Metodo: recarregarBackbone
   * Funcao: recarrega o backbone caso tenha uma alteracao no txt
   * Parametros:
   * Retorno: void
   */
  public void recarregarBackbone() {
    reiniciarRede();
    rede.carregarArquivo(arquivo, this);

    Platform.runLater(() -> {
      paneRoteadores.getChildren().clear();
      choiceOrigem.getItems().clear();
      choiceDestino.getItems().clear();

      desenharRede(arquivo);

      for (Roteador r : rede.getRoteadores()) {
        choiceOrigem.getItems().add(r.getIdRoteador());
        choiceDestino.getItems().add(r.getIdRoteador());
      } // fim do for

      choiceOrigem.setValue(1);
      choiceDestino.setValue(quantRoteadores);

      ttlField.setText("1");
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

    if (choiceImplementacao.getValue() == null || choiceOrigem.getValue() == null || choiceDestino.getValue() == null) {
      System.out.println("Erro: Selecione todos os campos antes de enviar!");
      return;
    } // fim do if

    // Itera para encerrar as threads de todos os roteadores e limpa os buffers
    for (Roteador roteador : rede.getRoteadores()) {
      roteador.desligar();
      roteador.getBufferPacotes().clear();
    } // fim do for

    try {
      String algoritmo = choiceImplementacao.getValue();
      versaoAlgoritmo = Integer.parseInt(algoritmo.replaceAll("\\D", ""));

      int idOrigem = choiceOrigem.getValue();
      int idDestino = choiceDestino.getValue();
      int ttl = Integer.parseInt(ttlField.getText());

      Pacote primeiroPacote;
      // Caso seja o algoritmo 1 ou 2 e ignorado o ttl
      if (versaoAlgoritmo == 1 | versaoAlgoritmo == 2) {
        primeiroPacote = new Pacote(idOrigem, idDestino);
      } else {
        primeiroPacote = new Pacote(idOrigem, idDestino, ttl);
      } // fim do if

      // itera sobre os roteadores para definir os algoritmos e ligalos
      for (Roteador roteador : rede.getRoteadores()) {
        roteador.ligar();
        roteador.setAlgoritmo(versaoAlgoritmo);
      } // fim do for

      System.out.println(
          "Iniciando Algoritmo " + versaoAlgoritmo + " do roteador " + idOrigem + " para o roteador " + idDestino);

      Roteador rOrigem = rede.getRoteadores().get(idOrigem - 1);
      rOrigem.enviarPacote(primeiroPacote);

    } catch (NumberFormatException e) {
      System.out.println("Erro: O TTL deve ser um numero inteiro!");
    } // fim do try-catch
  } // fim do metodo iniciarEnvio

  /*
   * Metodo: reiniciarRede
   * Funcao: reinicia as contagens e desliga as animacoes dos pacotes
   * Parametros:
   * Retorno: void
   */
  public void reiniciarRede() {
    pacotesChegados = 0;
    atualizarContadorPacotes(0);
    Pacote.setContadorPacotes(0);
    atualizarContadorPacotesChegados();

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
   * Parametros:
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

      Scene cena = new Scene(root);
      popOut.setScene(cena);
      popOut.show();

    } catch (IOException e) {
      System.out.println("Erro ao abrir a tela de Sobre!");
      e.printStackTrace();
    } // fim do try-catch
  } // fim do metodo abrirSobre

}
