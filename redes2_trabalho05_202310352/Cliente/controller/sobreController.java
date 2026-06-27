package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class sobreController implements Initializable {

  @FXML
  private Pane barraSuperior;
  
  private double xOffset = 0;
  private double yOffset = 0;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O Controller foi carregado corretamente!");

    if (barraSuperior != null) {
      barraSuperior.setOnMousePressed(event -> {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
      });

      barraSuperior.setOnMouseDragged(event -> {
        Stage janela = (Stage) barraSuperior.getScene().getWindow();

        janela.setX(event.getScreenX() - xOffset);
        janela.setY(event.getScreenY() - yOffset);
      });
    } // fim do if
  }

  /*
   * Metodo: fecharSobre
   * Funcao: Fecha a janela de informacoes do trabalho
   */
  @FXML
  public void fecharSobre(ActionEvent event) {
    Stage janela = (Stage) ((Node) event.getSource()).getScene().getWindow();
    janela.close();
  }

}