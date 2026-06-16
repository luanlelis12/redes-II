/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 12/06/2026
* Ultima alteracao.: 
* Nome.............: clienteController.java
* Funcao...........: 
*******************************************************************/
package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import util.processadorTexto;

public class clienteController implements Initializable {

  @FXML
  Button enviarMensagemButton;
  @FXML
  Button enviarButton;
  @FXML
  TextArea caixaDeMensagem;
  @FXML
  TextField nomeGrupoField;
  @FXML
  TextArea mensagemField;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O Controller foi carregado corretamente!");

    mensagemField.setOnKeyPressed((KeyEvent event) -> {
      // Verifica se a tecla apertada foi o ENTER
      if (event.getCode() == KeyCode.ENTER) {
        // Verifica se o usuario NAO esta segurando o Shift
        event.consume();
        if (!event.isShiftDown()) {
          System.out.println(mensagemField.getText());
          mensagemField.clear();
        } // fim do if
      } // fim do if
    });

    mensagemField.textProperty().addListener((observable, valorAntigo, valorNovo) -> {
      if (enviarButton != null) {
        enviarButton.getStyleClass().remove("buttonEnviar");
        enviarButton.getStyleClass().remove("buttonEnviarDes");

        if (valorNovo.trim().isEmpty()) {
          enviarButton.getStyleClass().add("buttonEnviarDes");
        } else {
          enviarButton.getStyleClass().add("buttonEnviar");
        } // fim do if
      } // fim do if
    });

  }

  public void enviarMensagem() {

  } // fim do metodo enviarMensagem

  public void entrarGrupo() {
    String nomeGrupo = nomeGrupoField.getText();

    if (nomeGrupo == null || nomeGrupo.trim().isEmpty()) {
      Alert alerta = new Alert(AlertType.WARNING);

      alerta.setTitle("NOME DE GRUPO VAZIO");
      alerta.setHeaderText("Nenhum nome foi inserido");
      alerta.setContentText("Digite o nome do grupo para entrar.");

      alerta.show();
      return;
    } // fim do if
  } // fim do metodo entrarGrupo

  public void fecharAplicacao() {
    System.out.println("Fechando aplicacao");
    Platform.exit();
    System.exit(0);
  } // fim do metodo fecharAplicacao

  public void minimizarTela(ActionEvent event) {
    Stage janela = (Stage) ((Node) event.getSource()).getScene().getWindow();
    janela.setIconified(true);
  } // fim do metodo minimizarTela

}
