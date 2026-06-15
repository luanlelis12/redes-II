/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 15/06/2026
* Ultima alteracao.: 
* Nome.............: MenuInicial.java
* Funcao...........: 
*******************************************************************/
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class menuInicialController implements Initializable {

  @FXML
  TextField nomeTextField;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O Controller foi carregado corretamente!");
  }

  public void criarCliente(ActionEvent event) {
    String nomeCliente = nomeTextField.getText();

    if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
      Alert alerta = new Alert(AlertType.WARNING);

      alerta.setTitle("NOME VAZIO");
      alerta.setHeaderText("Nenhum nome foi inserido");
      alerta.setContentText("Digite o seu nome para entrar na aplicacao.");

      alerta.show();
      return;
    } // fim do if

    try {
      Parent novaRaiz = FXMLLoader.load(getClass().getResource("/view/chat.fxml"));
      Scene novaCena = new Scene(novaRaiz);

      Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

      primaryStage.setScene(novaCena);
      primaryStage.show();
    } catch (IOException e) {
      System.out.println("Erro ao tentar trocar de tela: " + e.getMessage());
      e.printStackTrace();
    } // fim do try-catch

  } // fim do metodo criarCliente

  public void fecharAplicacao() {
    System.out.println("Fechando aplicacao");
    Platform.exit();
    System.exit(0);
  } // fim do metodo fecharAplicacao

  public void abrirSobre(ActionEvent event) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("view/menuInicial.fxml"));
      Parent root = loader.load();

      Stage popOut = new Stage();
      
      popOut.setScene(new Scene(root));

      popOut.show();
    } catch (IOException e) {
      System.out.println("Erro ao tentar trocar de tela: " + e.getMessage());
      e.printStackTrace();
    } // fim do try-catch

  }

}