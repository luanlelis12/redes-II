/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 
* Nome.............: BackboneController.java
* Funcao...........: 
*************************************************************** */
package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O BackboneController foi carregado corretamente!");
    rede.carregarArquivo(arquivo, this);

    for (Roteador r : rede.getRoteadores()) {
      Thread threadRoteador = new Thread(r);
      threadRoteador.setDaemon(true);
      threadRoteador.start();
    }

    Platform.runLater(() -> {
      desenharRede(arquivo);
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
  }

  /*
   * Metodo: desenharRede
   * Funcao:
   * Parametros: caminho = rota do arquivo backbone.txt
   * Retorno: void
   */
  private void desenharRede(String caminho) {
    centroDaTopologiaX = paneRoteadores.getPrefWidth() / 2;
    centroDaTopologiaY = paneRoteadores.getPrefHeight() / 2;
    quantRoteadores = rede.getRoteadores().size();

    anguloDosRoteadores = 360 / quantRoteadores;

    for (Roteador roteador : rede.getRoteadores()) {
      int idRoteador = roteador.getIdRoteador();
      double[] coordenadaRoteador = calcularPosicaoRoteador(idRoteador);
      exibirRoteador(idRoteador, coordenadaRoteador);
      for (Aresta conexao : roteador.getConexoes()) {
        exibirConexao(roteador, conexao.getDestino(), conexao.getPeso());
      }
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

      animacao.setOnFinished(e -> {
        paneRoteadores.getChildren().remove(imageViewPacote);
        rDestino.receberPacote(pacote);
      });

      // 4. Iniciar
      animacao.play();

      paneRoteadores.getChildren().addAll(caminho, imageViewPacote); // Adiciona caminho e o objeto
    });
  } // fim do metodo exibirPacote

  /*
   * Metodo: recarregarBackbone
   * Funcao: recarrega o backbone caso tenha uma alteracao no txt
   * Parametros:
   * Retorno: void
   */
  public void recarregarBackbone() {
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
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  public void iniciarEnvio() {

    if (choiceImplementacao.getValue() == null || choiceOrigem.getValue() == null || choiceDestino.getValue() == null) {
      System.out.println("Erro: Selecione todos os campos antes de enviar!");
      return;
    } // fim do if

    try {
      String algoritmo = choiceImplementacao.getValue();
      versaoAlgoritmo = Integer.parseInt(algoritmo.replaceAll("\\D", ""));

      int idOrigem = choiceOrigem.getValue();
      int idDestino = choiceDestino.getValue();
      int ttl = Integer.parseInt(ttlField.getText());

      Pacote primeiroPacote;
      if (versaoAlgoritmo == 1 | versaoAlgoritmo == 2) {
        primeiroPacote = new Pacote(idOrigem, idDestino);
      } else {
        primeiroPacote = new Pacote(idOrigem, idDestino, ttl);
      }

      for (Roteador roteador : rede.getRoteadores()) {
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

  public void atualizarContadorPacotes(int contador) {
    Platform.runLater(() -> labelPacoteGerado.setText(String.valueOf(contador)));
  }

  public void atualizarContadorPacotesChegados() {
    pacotesChegados++;
    Platform.runLater(() -> labelPacoteChegado.setText(String.valueOf(pacotesChegados)));
  }

}
