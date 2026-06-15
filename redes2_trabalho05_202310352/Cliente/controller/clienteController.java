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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class clienteController implements Initializable {

  @FXML
  Button enviarMensagemButton;
  @FXML
  TextArea caixaDeMensagem;
  @FXML
  TextField nomeGrupoField;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O Controller foi carregado corretamente!");
  }

  public void enviarMensagem() {
  }

  public void criarGrupo() {
    String nomeGrupo = nomeGrupoField.getText();

    // Cliente.criarGrupo();

  }

}
