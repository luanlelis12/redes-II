/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 12/06/2026
* Ultima alteracao.: 
* Nome.............: clienteController.java
* Funcao...........: 
*******************************************************************/
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
  @FXML
  private Label conversaSelecionadaLabel;
  @FXML
  private VBox vboxGrupos;
  @FXML
  private static VBox conversaVBox;

  private static Cliente cliente;
  private static String grupoSelecionado = null;
  private ArrayList<String> grupos = new ArrayList<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O Controller foi carregado corretamente!");

    mensagemField.setOnKeyPressed((KeyEvent event) -> {
      // Verifica se a tecla apertada foi o ENTER
      if (event.getCode() == KeyCode.ENTER) {
        // Verifica se o usuario NAO esta segurando o Shift
        event.consume();
        if (!event.isShiftDown()) {
          enviarMensagem();
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
      cliente = new Cliente(nome, "10.227.119.229");
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

    if (grupoSelecionado == null)
      return;

    String mensagem = mensagemField.getText();
    mensagemField.clear();

    HBox balaoDeDialogo = criarBalaoDialogo(mensagem, "", true);
    conversaVBox.getChildren().add(balaoDeDialogo);

    mensagem = processadorTexto.inserirFlagEscape(mensagem);
    System.out.println("Cliente: enviando mensagem \"" + mensagem + "\"");
    cliente.enviarMensagem(grupoSelecionado, mensagem);
  } // fim do metodo enviarMensagem

  public static void receberMensagem(String mensagem, String nomeRemetente, String grupo) {

    HBox balaoDeDialogo = criarBalaoDialogo(mensagem, nomeRemetente, false);
    conversaVBox.getChildren().add(balaoDeDialogo);

    mensagem = processadorTexto.inserirFlagEscape(mensagem);
    System.out
        .println("Cliente: recebendo mensagem \"" + mensagem + "\" de " + nomeRemetente + " no grupo " + grupo + ".");
    cliente.enviarMensagem(grupoSelecionado, mensagem);

  }

  public static HBox criarBalaoDialogo(String mensagem, String nomeRemetente, boolean enviadaPorMim) {

    VBox balao = new VBox(5);

    balao.setMinWidth(200);
    balao.setMinHeight(50);
    balao.setMaxWidth(450);

    if (!enviadaPorMim && nomeRemetente != null && !nomeRemetente.isEmpty()) {
      Label labelNome = new Label(nomeRemetente);
      labelNome.setStyle("-fx-font-weight: bold;");
      labelNome.setFont(new Font("Arial", 12));
      balao.getChildren().add(labelNome);
    } // fim do if

    Label textoMsg = new Label(mensagem);
    textoMsg.setFont(new Font("Arial", 14));
    textoMsg.setWrapText(true);

    balao.getChildren().add(textoMsg);

    HBox linha = new HBox(balao);
    linha.setPadding(new Insets(5, 15, 5, 15));

    if (enviadaPorMim) {
      linha.setAlignment(Pos.CENTER_RIGHT);
      balao.setStyle(
          "-fx-background-color: #c6e7f8;");
    } else {
      linha.setAlignment(Pos.CENTER_LEFT);
      balao.setStyle(
          "-fx-background-color: #FFFFFF;");
    } // fim do if

    return linha;
  } // fim do metodo criarBalaoDialogo

  /*
   * Metodo: entrarGrupo
   * Funcao: envia para a classe cliente o grupo que o usuario quer entrar
   * Parametros:
   * Retorno: void
   */
  public void entrarGrupo() {
    String nomeGrupo = nomeGrupoField.getText();
    String nomeGrupoProcessado = processadorTexto.inserirFlagEscape(nomeGrupo);

    if (nomeGrupoProcessado == null || nomeGrupoProcessado.trim().isEmpty()) {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/alert.fxml"));
        Parent root = loader.load();

        alertController controladorDoAlerta = loader.getController();

        controladorDoAlerta.setDetalhes("Nenhum nome foi inserido", "Digite o nome do grupo para entrar.");

        Stage janelaAlerta = new Stage();
        janelaAlerta.setScene(new Scene(root));
        janelaAlerta.initStyle(StageStyle.UNDECORATED);
        janelaAlerta.initModality(Modality.APPLICATION_MODAL);

        janelaAlerta.show();
      } catch (IOException e) {
        System.out.println("Erro ao carregar o alerta!");
      }
      return;
    } // fim do if

    grupos.add(nomeGrupo);
    adicionarGrupoNaTela(nomeGrupo);
    cliente.entrarGrupo(nomeGrupoProcessado);
  } // fim do metodo entrarGrupo

  public void adicionarGrupoNaTela(String nomeDoGrupo) {
    try {
      AnchorPane itemConversa = new AnchorPane();

      // Image icon = new Image("");
      ImageView iconConversa = new ImageView();

      iconConversa.setLayoutX(10);
      iconConversa.setLayoutY(10);
      iconConversa.setFitHeight(50);
      iconConversa.setFitWidth(50);

      Label nomeConversa = new Label(nomeDoGrupo);

      nomeConversa.setLayoutX(70);
      nomeConversa.setLayoutY(20);

      itemConversa.getChildren().addAll(iconConversa, nomeConversa);

      itemConversa.setOnMouseClicked(null);
      itemConversa.getStyleClass().add("itemConversa");

      itemConversa.setPrefSize(280, 70);
      itemConversa.setMinSize(280, 70);

      itemConversa.setOnMouseClicked(event -> {
        grupoSelecionado = nomeDoGrupo;
        conversaSelecionadaLabel.setText(grupoSelecionado);
      });

      vboxGrupos.getChildren().add(itemConversa);

    } catch (Exception e) {
      System.out.println("Erro ao carregar o visual do grupo!");
    }
  } // fim do metodo adicionarGrupoNaTela

  /*
   * Metodo: selecionarGrupo
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  public void selecionarGrupo(ActionEvent event) {
    String grupo = ((Node) event.getSource()).toString();
    System.out.println(grupo);
  } // fim do metodo selecionarGrupo

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
