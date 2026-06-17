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
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.processadorTexto;

public class menuInicialController implements Initializable {

  @FXML
  TextField nomeTextField;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O Controller foi carregado corretamente!");
  }

  public void criarCliente(ActionEvent event) {
    String nomeCliente = nomeTextField.getText();
    nomeCliente = processadorTexto.inserirFlagEscape(nomeCliente);

    if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/alert.fxml"));
        Parent root = loader.load();

        alertController controladorDoAlerta = loader.getController();

        controladorDoAlerta.setDetalhes("tituloErro", "mensagemErro");

        Stage janelaAlerta = new Stage();
        janelaAlerta.setScene(new Scene(root));
        janelaAlerta.initStyle(StageStyle.UNDECORATED);
        janelaAlerta.initModality(Modality.APPLICATION_MODAL);

        janelaAlerta.show();
      } catch (IOException e) {
        System.out.println("CLIENTE - Erro: Nao foi possivel carregar o alerta!");
      }
      return;
    } // fim do if

    clienteController.criarCliente(nomeCliente);
    System.out.println("CLIENTE - criando usuario " + nomeCliente + ".");

    try {
      Parent novaRaiz = FXMLLoader.load(getClass().getResource("/view/chat.fxml"));
      Scene novaCena = new Scene(novaRaiz);

      Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

      primaryStage.setScene(novaCena);
      primaryStage.show();
    } catch (IOException e) {
      System.out.println("CLIENTE - Erro: Nao foi possivel trocar de tela: ");
    } // fim do try-catch

  } // fim do metodo criarCliente

  public void fecharAplicacao() {
    System.out.println("CLIENTE - Fechando aplicacao.");
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
      System.out.println("CLIENTE - Erro: Nao foi possivel trocar de tela: ");
    } // fim do try-catch

  }

}