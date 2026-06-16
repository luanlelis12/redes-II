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

import model.Cliente;
import util.processadorTexto;

public class clienteController implements Initializable {

  @FXML
  private Button enviarMensagemButton;
  @FXML
  private Button enviarButton;
  @FXML
  private TextArea caixaDeMensagem;
  @FXML
  private TextField nomeGrupoField;
  @FXML
  private TextArea mensagemField;

  private static Cliente cliente;

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

  } // fim do metodo initialize

  public static void criarCliente(String nome) {
    try {
      cliente = new Cliente(nome, "10.227.119.130");
      cliente.start();
    } catch (Exception e) {
      System.out.println();
    }
  }

  /*
   * Metodo: enviarMensagem
   * Funcao: envia para a classe cliente a mensagem que o usuario quer enviar
   * Parametros:
   * Retorno: void
   */
  public void enviarMensagem() {
    String mensagem = mensagemField.getText();
    mensagemField.clear();

    mensagem = processadorTexto.inserirFlagEscape(mensagem);
    System.out.println("Enviando mensagem: " + mensagem);
    cliente.enviarMensagem("teste", mensagem);
  } // fim do metodo enviarMensagem

  /*
   * Metodo: entrarGrupo
   * Funcao: envia para a classe cliente o grupo que o usuario quer entrar
   * Parametros:
   * Retorno: void
   */
  public void entrarGrupo() {
    String nomeGrupo = nomeGrupoField.getText();
    nomeGrupo = processadorTexto.inserirFlagEscape(nomeGrupo);

    if (nomeGrupo == null || nomeGrupo.trim().isEmpty()) {
      Alert alerta = new Alert(AlertType.WARNING);

      alerta.setTitle("NOME DE GRUPO VAZIO");
      alerta.setHeaderText("Nenhum nome foi inserido");
      alerta.setContentText("Digite o nome do grupo para entrar.");

      alerta.show();
      return;
    } // fim do if

    cliente.entrarGrupo(nomeGrupo);
  } // fim do metodo entrarGrupo

  /*
   * Metodo: fecharAplicacao
   * Funcao: fechar a aplicacao
   * Parametros:
   * Retorno: void
   */
  public void fecharAplicacao() {
    System.out.println("Fechando aplicacao");
    Platform.exit();
    System.exit(0);
  } // fim do metodo fecharAplicacao

  /*
   * Metodo: minimizarTela
   * Funcao: minimizar a tela
   * Parametros: event = evento que ativou o metodo
   * Retorno: void
   */
  public void minimizarTela(ActionEvent event) {
    Stage janela = (Stage) ((Node) event.getSource()).getScene().getWindow();
    janela.setIconified(true);
  } // fim do metodo minimizarTela

}
