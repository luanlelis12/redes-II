package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class alertController implements Initializable {

  @FXML
  private Label tituloLabel;
  
  @FXML
  private Label mensagemLabel;
  
  @Override
  public void initialize(URL location, ResourceBundle resources) {}

  /*
   * Metodo: setDetalhes
   * Funcao: Recebe os textos personalizados e injeta na tela ANTES de ela abrir
   */
  public void setDetalhes(String titulo, String mensagem) {
    tituloLabel.setText(titulo);
    mensagemLabel.setText(mensagem);
  } // fim do metodo setDetalhes

  /*
   * Metodo: fecharAlerta
   * Funcao: Fecha a janela atual (O pop-up do alerta)
   */
  @FXML
  public void fecharAlerta(ActionEvent event) {
    // Descobre qual e a janela (Stage) que contem o botao clicado
    Stage janelaAlerta = (Stage) ((Node) event.getSource()).getScene().getWindow();
    
    // Fecha a janela
    janelaAlerta.close();
  } // fim do metodo fecharAlerta

}