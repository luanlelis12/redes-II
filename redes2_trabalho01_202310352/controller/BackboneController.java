package controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import model.Roteador;

public class BackboneController implements Initializable {
  @FXML
  AnchorPane paneRoteadores;
  @FXML
  AnchorPane paneConfiguracoes;
  private static ArrayList<Roteador> conjuntoRoteadores = new ArrayList<>();

  private final String arquivo = "backbone.txt";

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O Controller foi carregado corretamente!");
    Platform.runLater(() -> {
      gerarRoteadores(arquivo);
      posicionarRoteadores();
    });
  }

  public void gerarRoteadores(String caminho) {
    try {
      BufferedReader buffRead = new BufferedReader(new FileReader(caminho));
      String linha = buffRead.readLine();

      int quantidadeRoteadores = 0;

      int i = 0;
      while (linha.charAt(i) != ';') {
        int valor = Character.getNumericValue(linha.charAt(i));
        quantidadeRoteadores = (quantidadeRoteadores * 10) + valor;
        i++;
      }

      conjuntoRoteadores.clear();
      for (int j = 0; j < quantidadeRoteadores; j++)
        conjuntoRoteadores.add(new Roteador(j));

      System.out.println("Quantidade de roteadores no backbone: " + quantidadeRoteadores);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void gerarConexoes(String caminho) throws IOException {

  }

  public void posicionarRoteadores() {
    double centroDaTopologiaX = paneRoteadores.getPrefWidth() / 2;
    double centroDaTopologiaY = paneRoteadores.getPrefHeight() / 2;
    int quantTotalRoteadores = conjuntoRoteadores.size();
    final double raio = 300;

    double anguloDosRoteadores = 360 / quantTotalRoteadores;

    for (int i = 0; i < quantTotalRoteadores; i++) {
      Roteador r = conjuntoRoteadores.get(i);
      double angulo = Math.toRadians(i * anguloDosRoteadores);
      double posX = centroDaTopologiaX + raio * Math.cos(angulo);
      double posY = centroDaTopologiaY + raio * Math.sin(angulo);

      criarImagemRoteador(posX, posY);
      // conjuntoRoteadores.get(i).setPosicao(paneRoteadores, posX, posY);
    }

    // Image image = new Image("file:img/roteador.png");
    // ImageView imageView = new ImageView(image);

    // imageView.setFitWidth(50);
    // imageView.setFitHeight(50);
    // imageView.setPreserveRatio(true);
    // imageView.setSmooth(true);
    // imageView.setX(centroDaTopologiaX-imageView.getFitWidth()/2);
    // imageView.setY(centroDaTopologiaY-imageView.getFitHeight()/2);

    // paneRoteadores.getChildren().add(imageView);
  }

  public void criarImagemRoteador(double posX, double posY) {
    Image imagemRoteador = new Image("file:img/roteador.png");
    ImageView roteadorView = new ImageView(imagemRoteador);

    roteadorView.setFitWidth(50);
    roteadorView.setFitHeight(50);
    roteadorView.setX(posX - 35);
    roteadorView.setY(posY - 35);

    paneRoteadores.getChildren().add(roteadorView);
  }

}
