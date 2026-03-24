package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import model.Backbone;
import model.Roteador;

public class BackboneController implements Initializable {
  @FXML
  AnchorPane paneRoteadores;
  @FXML
  AnchorPane paneConfiguracoes;
  private Backbone rede = new Backbone();

  private final String arquivo = "backbone.txt";
  private final Image imagemRoteador = new Image("file:view/img/roteador.png");

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O BackboneController foi carregado corretamente!");
    rede.carregarArquivo(arquivo);

    Platform.runLater(() -> {
      desenharRede(arquivo);
    });
  }

  /*
   * Metodo: desenharRede
   * Funcao:
   * Parametros: caminho = rota do arquivo backbone.txt
   * Retorno: void
   */
  private void desenharRede(String caminho) {
    double centroDaTopologiaX = paneRoteadores.getPrefWidth() / 2;
    double centroDaTopologiaY = paneRoteadores.getPrefHeight() / 2;
    int quantRoteadores = rede.getRoteadores().size();
    final double raio = 300;

    double anguloDosRoteadores = 360 / quantRoteadores;

    for (Roteador roteador : rede.getRoteadores()) {
      int idRoteador = roteador.getIdRoteador();
      double angulo = Math.toRadians(idRoteador * anguloDosRoteadores);
      double posX = centroDaTopologiaX + raio * Math.cos(angulo);
      double posY = centroDaTopologiaY + raio * Math.sin(angulo);

      exibirRoteador(posX, posY, idRoteador);
    } // fim do for
  } // fim do metodo desenharRede

  /*
   * Metodo: exibirRoteador
   * Funcao: recebe a posicao do roteador e o posiciona na tela
   * Parametros: posX = posicao horizontal; posY = posicao vertical
   * Retorno: void
   */
  public void exibirRoteador(double posX, double posY, int idRoteador) {
    ImageView roteadorView = new ImageView(imagemRoteador);
    Label labelIdRoteador = new Label("" + idRoteador);

    roteadorView.setFitWidth(50);
    roteadorView.setFitHeight(50);
    roteadorView.setLayoutX(posX - 25);
    roteadorView.setLayoutY(posY - 25);

    labelIdRoteador.setLayoutX(posX-25);
    labelIdRoteador.setLayoutY(posY-25);

    paneRoteadores.getChildren().add(roteadorView);
    paneRoteadores.getChildren().add(labelIdRoteador);
  } // fim do metodo exibirRoteador

}
