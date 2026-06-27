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
import java.util.HashMap;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
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
  private VBox conversaVBox;
  @FXML
  private Pane barraSuperior;
  @FXML
  private ScrollPane conversaScrollPane;
  @FXML
  private TextField nomeUserField;

  private double xOffset = 0;
  private double yOffset = 0;

  private final String GRUPO = "grupo";
  private final String PRIVADO = "priv";

  private static clienteController instancia;

  private static Cliente cliente;
  private static Pair<String, String> conversaSelecionada = null; // <nomeDaConversa,tipoDeConversa>
  private ArrayList<String> grupos = new ArrayList<>();
  private HashMap<Pair<String, String>, ArrayList<HBox>> historicoMensagens = new HashMap<>();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    instancia = this;
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

    conversaVBox.heightProperty().addListener((observable, oldValue, newValue) -> {
      conversaScrollPane.layout();
      conversaScrollPane.setVvalue(1.0d);
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
    }

  } // fim do metodo initialize

  public static boolean criarCliente(String nome, String ipServidor) {
    try {
      cliente = new Cliente(nome, ipServidor);
      boolean aprovado = cliente.fazerLogin();

      if (aprovado) {
        cliente.start(); // Só liga a Thread de escuta se o nome for aceite!
        return true;
      } else {
        cliente.desligarCliente();
        System.out.println("CLIENTE - Login desaprovado desligando conexao.");
        return false;
      }

    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /*
   * Metodo: enviarMensagem
   * Funcao: envia para a classe cliente a mensagem que o usuario quer enviar
   * Parametros:
   * Retorno: void
   */
  public void enviarMensagem() {

    if (conversaSelecionada == null)
      return;

    String mensagem = mensagemField.getText();
    mensagemField.clear();

    HBox balaoDeDialogo = criarBalaoDialogo(mensagem, "Você", true);

    historicoMensagens.get(conversaSelecionada).add(balaoDeDialogo);
    conversaVBox.getChildren().add(balaoDeDialogo);

    mensagem = processadorTexto.inserirFlagEscape(mensagem);
    System.out.println("CLIENTE - enviando mensagem \"" + mensagem + "\"");
    if (conversaSelecionada.getValue().equals(GRUPO)) {
      cliente.enviarMensagem(conversaSelecionada.getKey(), mensagem);
    } else if (conversaSelecionada.getValue().equals(PRIVADO)) {
      cliente.enviarMensagemPrivado(conversaSelecionada.getKey(), mensagem);
    } // fim do if
  } // fim do metodo enviarMensagem

  /*
   * Metodo: receberMensagem
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  public static void receberMensagem(String mensagem, String nomeRemetente, String nomeConversa, String tipoConversa) {

    nomeConversa = nomeConversa.trim();
    mensagem = mensagem.trim();
    nomeRemetente = nomeRemetente.trim();

    final String msgFinal = processadorTexto.retirarFlagEscape(mensagem);
    final String nomeFinal = processadorTexto.retirarFlagEscape(nomeRemetente);
    final String nomeConversaFinal = processadorTexto.retirarFlagEscape(nomeConversa);

    Platform.runLater(() -> {
      HBox balaoDeDialogo = criarBalaoDialogo(msgFinal, nomeFinal, false);

      Pair<String, String> chaveRecebida = new Pair<>(nomeConversaFinal, tipoConversa);

      if (!instancia.historicoMensagens.containsKey(chaveRecebida)) {
        instancia.adicionarConversaNaTela(nomeConversaFinal, tipoConversa);
      }

      instancia.historicoMensagens.get(chaveRecebida).add(balaoDeDialogo);

      if (conversaSelecionada != null && conversaSelecionada.equals(chaveRecebida)) {
        instancia.conversaVBox.getChildren().add(balaoDeDialogo);
      }
    });

    System.out
        .println(
            "CLIENTE - exibindo mensagem \"" + msgFinal + "\" de " + nomeFinal + " na conversa " + nomeConversaFinal + ".");
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
    nomeGrupoField.clear();
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
        e.printStackTrace();
      }
      return;
    } // fim do if

    if (grupos.contains(nomeGrupo)) {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/alert.fxml"));
        Parent root = loader.load();

        alertController controladorDoAlerta = loader.getController();

        controladorDoAlerta.setDetalhes("Voce ja esta nesse grupo.", "Voce ja esta nesse grupo.");

        Stage janelaAlerta = new Stage();
        janelaAlerta.setScene(new Scene(root));
        janelaAlerta.initStyle(StageStyle.UNDECORATED);
        janelaAlerta.initModality(Modality.APPLICATION_MODAL);

        janelaAlerta.show();
      } catch (IOException e) {
        System.out.println("Erro ao carregar o alerta!");
        e.printStackTrace();
      }
      return;
    }

    try {
      cliente.entrarGrupo(nomeGrupoProcessado);
      grupos.add(nomeGrupo);
      adicionarConversaNaTela(nomeGrupo, GRUPO);
    } catch (Exception e) {
      e.printStackTrace();
    }
  } // fim do metodo entrarGrupo

  public void sairGrupo(AnchorPane itemConversa) {
    Label label = (Label) itemConversa.lookup("#nomeConversa");

    if (label != null) {
      String nomeGrupo = label.getText();
      System.out.println("CLIENTE - Saindo do grupo " + nomeGrupo + ".");

      nomeGrupo = processadorTexto.inserirFlagEscape(nomeGrupo);
      cliente.sairGrupo(nomeGrupo);
    }

    itemConversa.getChildren().clear();
    vboxGrupos.getChildren().remove(itemConversa);
  }

  public void criarConversa() {
    String nome = nomeUserField.getText();

    if (nome == null || nome.trim().isEmpty())
      return;

    nomeUserField.clear();
    adicionarConversaNaTela(nome, PRIVADO);
  }

  public void adicionarConversaNaTela(String nomeConversa, String tipoConversa) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/grupoButton.fxml"));
      AnchorPane itemConversa = loader.load();

      ImageView icone = (ImageView) itemConversa.lookup("#iconeConversa");

      if (tipoConversa.equals(PRIVADO)) {
        String caminhoImagem = "/view/img/iconPriv.png";

        try {
          Image novaImagem = new Image(getClass().getResourceAsStream(caminhoImagem));
          icone.setImage(novaImagem);
        } catch (Exception e) {
          System.out.println("Aviso: Imagem não encontrada, mantendo a foto padrão.");
        }
      }

      Label labelNome = (Label) itemConversa.lookup("#nomeConversa");
      if (labelNome != null) {
        labelNome.setText(nomeConversa);
      }

      Button sairConversa = (Button) itemConversa.lookup(".buttonSair");
      if (sairConversa != null) {
        sairConversa.setOnAction(event -> {
          sairGrupo(itemConversa);
        });
      }

      Pair<String, String> chaveConversa = new Pair<>(nomeConversa, tipoConversa);

      if (!historicoMensagens.containsKey(chaveConversa)) {
        historicoMensagens.put(chaveConversa, new ArrayList<HBox>());
      }

      itemConversa.setOnMouseClicked(event -> {
        abrirConversa(nomeConversa, tipoConversa);
      });

      vboxGrupos.getChildren().add(itemConversa);

      abrirConversa(nomeConversa, tipoConversa);

    } catch (

    Exception e) {
      System.out.println("CLIENTE - Erro: Nao foi possivel carregar o visual do grupo!");
      e.printStackTrace();
    }
  } // fim do metodo adicionarConversaNaTela

  /*
   * Metodo: abrirConversa
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  public void abrirConversa(String nomeConversa, String tipoConversa) {
    conversaSelecionada = new Pair<>(processadorTexto.inserirFlagEscape(nomeConversa), tipoConversa);
    conversaSelecionadaLabel.setText(nomeConversa);

    // Usa o Pair inteiro para pescar as mensagens!
    conversaVBox.getChildren().clear();
    conversaVBox.getChildren().addAll(historicoMensagens.get(conversaSelecionada));
  } // fim do metodo abrirConversa

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
    System.out.println("CLIENTE - Iniciando encerramento da aplicacao...");

    if (cliente != null) {

      for (String nomeGrupo : grupos) {
        System.out.println("CLIENTE - Desconectando do grupo: " + nomeGrupo);

        String grupoProcessado = processadorTexto.inserirFlagEscape(nomeGrupo);

        cliente.sairGrupo(grupoProcessado);
      }
      cliente.fazerLogout();

      Platform.exit();
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    System.out.println("CLIENTE - Aplicacao encerrada com sucesso.");
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
