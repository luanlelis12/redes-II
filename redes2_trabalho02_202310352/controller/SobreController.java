/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 26 03 2026
* Ultima alteracao.: 11 04 2026
* Nome.............: SobreController.java
* Funcao...........: Controller para gerenciar a tela de sobre o programa
*************************************************************** */
package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class SobreController implements Initializable {
  @FXML
  TextFlow textoSobre;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    textoSobre.setStyle("-fx-font-size: 16px;");
    
    Text Titulo = new Text("Roteamento por menor caminho: ");
    Titulo.setStyle("-fx-font-weight: bold;");
    
    Text Texto = new Text(
        "O roteamento por menor caminho busca a rota mais eficiente (menor distancia, tempo ou custo) entre um ponto de origem e um destino em uma rede. Ele utiliza algoritmos em grafos, sendo o Algoritmo de Dijkstra o mais comum, para minimizar a soma dos custos (pesos) dos arcos entre nos.");

    textoSobre.getChildren().addAll(
        Titulo, Texto
    );
  }

}