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
import javafx.scene.shape.Line;
import model.Aresta;
import model.Backbone;
import model.Roteador;

public class BackboneController implements Initializable {
  @FXML
  AnchorPane paneRoteadores;
  @FXML
  AnchorPane paneConfiguracoes;
  private Backbone rede = new Backbone();
  private int quantRoteadores = 0;
  private double anguloDosRoteadores = 0;

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
      for(Aresta conexao : roteador.getConexoes()) {
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
    return new double[]{posX, posY};
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

    Label pesoConexao = new Label(""+peso);
    pesoConexao.setStyle("-fx-font-weight: bold; -fx-background: gray");
    pesoConexao.setLayoutX((posicaoR1[0]+posicaoR2[0])/2);
    pesoConexao.setLayoutY((posicaoR1[1]+posicaoR2[1])/2);
    paneRoteadores.getChildren().add(pesoConexao);
    
  } // fim do metodo exibirConexao

}
