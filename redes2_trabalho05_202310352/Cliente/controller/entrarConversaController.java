package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class entrarConversaController {

  @FXML
  private TextField nomeConversaField;
  @FXML
  private ToggleGroup tipoDeConversa;

  private clienteController controladorPai;

  /*
   * Metodo: criarConversa
   * Funcao: Fecha a tela
   */
  @FXML
  public void confirmarEntrada(ActionEvent event) {
    String nomeDigitado = nomeConversaField.getText();

    if (nomeDigitado != null && !nomeDigitado.trim().isEmpty()) {

      RadioButton selecionado = (RadioButton) tipoDeConversa.getSelectedToggle();

      if (selecionado != null && selecionado.getText().equalsIgnoreCase("Grupo")) {
        controladorPai.entrarGrupo(nomeDigitado);
      } else {
        controladorPai.criarConversaPrivada(nomeDigitado);
      }
    }

    fecharTela(event);
  }

  /*
   * Metodo: fecharTela
   * Funcao: Fecha a tela
   */
  public void fecharTela(ActionEvent event) {
    Stage janela = (Stage) ((Node) event.getSource()).getScene().getWindow();
    janela.close();
  }

  public void setControladorPai(clienteController controladorPai) {
    this.controladorPai = controladorPai;
  }

}