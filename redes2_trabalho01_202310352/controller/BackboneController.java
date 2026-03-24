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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import model.Aresta;
import model.Backbone;
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

  private Backbone rede = new Backbone();
  private int quantRoteadores = 0;
  private double anguloDosRoteadores = 0;
  private int versaoAlgoritmo;

  private double centroDaTopologiaX;
  private double centroDaTopologiaY;

  private final String arquivo = "backbone.txt";
  private final Image imagemRoteador = new Image("file:view/img/roteador.png");
  private final double raio = 300;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O BackboneController foi carregado corretamente!");
    rede.carregarArquivo(arquivo);

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

  /*
   * Metodo: recarregarBackbone
   * Funcao: recarrega o backbone caso tenha uma alteracao no txt
   * Parametros:
   * Retorno: void
   */
  public void recarregarBackbone() {
    rede.carregarArquivo(arquivo);

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

    String algoritmo = choiceImplementacao.getValue();
    versaoAlgoritmo = Integer.parseInt(algoritmo.replaceAll("\\D", ""));

    System.out.println(versaoAlgoritmo);
  } // fim do metodo iniciarEnvio

}
