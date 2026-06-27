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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import model.Cliente;
import model.Conversa;
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

  private static final String GRUPO = "grupo";
  private static final String PRIVADO = "priv";

  private static clienteController instancia;

  private static Cliente cliente;

  private static Pair<String, String> conversaSelecionada = null; // <nomeDaConversa,tipoDeConversa>
  private HashMap<Pair<String, String>, Conversa> listaConversas = new HashMap<>();

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

  /*
   * Metodo: criarCliente
   * Funcao: Cria um cliente e verifica se ele foi aprovado criar esse cliente
   * Parametros: nome = nome do usuario, ipServidor = id o servidor
   * Retorno: retorna se foi aprovado a criacao
   */
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
  } // fim do metodo criarCliente

  /*
   * Metodo: enviarMensagem
   * Funcao: envia para a classe cliente a mensagem que o usuario quer enviar
   * Parametros:
   * Retorno: void
   */
  public void enviarMensagem() {

    // Impede do usuario mandar mensagem sem selecionar alguma conversa
    if (conversaSelecionada == null)
      return;

    String mensagem = mensagemField.getText();
    mensagemField.clear();

    // Impede do usuario mandar mensagem vazia
    if (mensagem.equals(""))
      return;

    HBox balaoDeDialogo = criarBalaoDialogo(mensagem, "Você", true);

    listaConversas.get(conversaSelecionada).adicionarMensagem(balaoDeDialogo);
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
   * Funcao: Recebe uma mensagem e armazena na conversa correta
   * Parametros: mensagem = mensagem recebida, nomeConversa = nome do grupo ou
   * usuario destinario, nomeRemetente = usuario que enviou a mensagem,
   * tipoConversa = se foi mandada para um grupo ou no privado
   * Retorno: void
   */
  public static void receberMensagem(String mensagem, String nomeConversa, String nomeRemetente, String tipoConversa) {

    nomeConversa = nomeConversa.trim();
    mensagem = mensagem.trim();
    nomeRemetente = nomeRemetente.trim();

    final String msgFinal = processadorTexto.retirarFlagEscape(mensagem);
    final String nomeFinal = processadorTexto.retirarFlagEscape(nomeRemetente);
    final String nomeConversaFinal;

    if (tipoConversa.equals(GRUPO)) { // verifica se eh uma mensagem de grupo ou priv e define o nome da conversa
      nomeConversaFinal = processadorTexto.retirarFlagEscape(nomeConversa);
    } else {
      nomeConversaFinal = processadorTexto.retirarFlagEscape(nomeRemetente);
    } // fim if-else

    Platform.runLater(() -> {
      HBox balaoDeDialogo = criarBalaoDialogo(msgFinal, nomeFinal, false);

      // Verifica quem enviou a mensagem
      if (nomeFinal.equals("SERVIDOR")) {
        balaoDeDialogo = criarAvisoSistema(msgFinal);
      } else {
        balaoDeDialogo = criarBalaoDialogo(msgFinal, nomeFinal, false);
      }

      Pair<String, String> chaveRecebida = new Pair<>(nomeConversaFinal, tipoConversa);
      Conversa conversa = instancia.listaConversas.get(chaveRecebida);

      if (conversaSelecionada == null || !conversaSelecionada.equals(chaveRecebida)) {

        // Pega o numero atual de notifiacoes
        int contagemAtual = conversa.getNotificacoes();

        // Adiciona +1 nas notificacoes
        conversa.setNotificacoes(contagemAtual+1);;

        System.out
            .println("CLIENTE - " + nomeConversaFinal + " tem " + (contagemAtual + 1) + " novas mensagens.");
      }
      
      if (!instancia.listaConversas.containsKey(chaveRecebida)) {
        instancia.adicionarConversaNaTela(nomeConversaFinal, tipoConversa);
      } // fim if

      conversa.adicionarMensagem(balaoDeDialogo);

      if (conversaSelecionada != null && conversaSelecionada.equals(chaveRecebida)) {
        instancia.conversaVBox.getChildren().add(balaoDeDialogo);
      } // fim if
    });

    System.out
        .println(
            "CLIENTE - exibindo mensagem \"" + msgFinal + "\" de " + nomeFinal + " na conversa " + nomeConversaFinal
                + ".");
  } // fim do metodo receberMensagem

  /*
   * Metodo: criarBalaoDialogo
   * Funcao: Exibir o balao de dialogo com a mensagem
   * Parametros: mensagem = mensagem recebida, nomeRemetente = usuario que enviou
   * a mensagem,
   * enviadaPorMim = se foi mandada por ele mesmo
   * Retorno: void
   */
  public static HBox criarBalaoDialogo(String mensagem, String nomeRemetente, boolean enviadaPorMim) {

    VBox balao = new VBox(5);

    balao.setMinWidth(200);
    balao.setMaxWidth(400);
    balao.setPadding(new Insets(10));

    if (!enviadaPorMim && nomeRemetente != null && !nomeRemetente.isEmpty()) {
      Label labelNome = new Label(nomeRemetente);
      labelNome.setStyle("-fx-font-weight: bold; -fx-text-fill: #0000aa;");
      labelNome.setFont(new Font("System", 13));
      balao.getChildren().add(labelNome);
    }

    Label textoMsg = new Label(mensagem);
    textoMsg.setFont(new Font("System", 14));
    textoMsg.setWrapText(true);

    textoMsg.setMaxWidth(380);

    balao.getChildren().add(textoMsg);

    HBox linha = new HBox(balao);

    if (enviadaPorMim) {
      linha.setPadding(new Insets(10, 25, 10, 15));
    } else {
      linha.setPadding(new Insets(10, 15, 10, 25));
    }

    String estiloRetroBase = "-fx-border-color: #000000; " +
        "-fx-border-width: 2px; " +
        "-fx-effect: dropshadow(three-pass-box, #000000, 0, 0, 4, 4); ";

    if (enviadaPorMim) {
      linha.setAlignment(Pos.CENTER_RIGHT);
      balao.setStyle(estiloRetroBase + "-fx-background-color: #c6e7f8;");
    } else {
      linha.setAlignment(Pos.CENTER_LEFT);
      balao.setStyle(estiloRetroBase + "-fx-background-color: #FFFFFF;");
    }

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

    // Alert para impedir do usuario criar grupo com nome vazio
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
      } // fim do try-catch
      return;
    } // fim do if

    Pair<String, String> grupo = new Pair<>(nomeGrupo, GRUPO);

    // Alert para impedir do usuario criar grupo com nome repetido
    if (listaConversas.containsKey(grupo)) {
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
      } // fim do try-catch
      return;
    } // fim do if

    try {
      cliente.entrarGrupo(nomeGrupoProcessado);
      listaConversas.put(grupo, new Conversa(nomeGrupo, GRUPO));
      adicionarConversaNaTela(nomeGrupo, GRUPO);
    } catch (Exception e) {
      e.printStackTrace();
    } // fim do try-catch
  } // fim do metodo entrarGrupo

  /*
   * Metodo: sairGrupo
   * Funcao: tira o usuario da conversa e tira a conversa da interface
   * Parametros: itemConversa = AnchorPane que contem o botao do grupo
   * Retorno: void
   */
  public void sairGrupo(AnchorPane itemConversa) {
    Label label = (Label) itemConversa.lookup("#nomeConversa");

    if (label != null) {
      String nomeGrupo = label.getText();
      System.out.println("CLIENTE - Saindo do grupo " + nomeGrupo + ".");
      Pair<String,String> grupo = new Pair<String,String>(nomeGrupo, GRUPO);

      listaConversas.remove(grupo);

      if (conversaSelecionada != null) {
        String nomeConversaAberta = processadorTexto.retirarFlagEscape(conversaSelecionada.getKey());

        if (nomeConversaAberta.equals(nomeGrupo)) {
          conversaVBox.getChildren().clear();
          conversaSelecionadaLabel.setText("");
          conversaSelecionada = null;
        }
      }

      String nomeGrupoProcessado = processadorTexto.inserirFlagEscape(nomeGrupo);
      cliente.sairGrupo(nomeGrupoProcessado);
    }

    itemConversa.getChildren().clear();
    vboxGrupos.getChildren().remove(itemConversa);
  } // fim do metodo sairGrupo

  /*
   * Metodo: criarConversa
   * Funcao: cria uma conversa privada com outro usuario
   * Parametros:
   * Retorno: void
   */
  public void criarConversa() {
    String nome = nomeUserField.getText();

    // impede de criar conversa com alguem de nome vazio
    if (nome == null || nome.trim().isEmpty())
      return;

    nomeUserField.clear();
    adicionarConversaNaTela(nome, PRIVADO);
  } // fim do metodo criarConversa

  /*
   * Metodo: criarConversa
   * Funcao: cria uma conversa privada com outro usuario
   * Parametros:
   * Retorno: void
   */
  public void adicionarConversaNaTela(String nomeConversa, String tipoConversa) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/grupoButton.fxml"));
      AnchorPane itemConversa = loader.load();

      ImageView icone = (ImageView) itemConversa.lookup("#iconeConversa");

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

      if (tipoConversa.equals(PRIVADO)) {
        String caminhoImagem = "/view/img/iconPriv.png";

        try {
          Image novaImagem = new Image(getClass().getResourceAsStream(caminhoImagem));
          icone.setImage(novaImagem);
        } catch (Exception e) {
          System.out.println("Aviso: Imagem não encontrada, mantendo a foto padrão.");
        }

        itemConversa.getChildren().remove(sairConversa);
      }

      Pair<String, String> chaveConversa = new Pair<>(nomeConversa, tipoConversa);

      if (!listaConversas.containsKey(chaveConversa)) {
        listaConversas.put(chaveConversa, new Conversa(nomeConversa, tipoConversa));
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
   * Metodo: criarAvisoSistema
   * Funcao: Cria um aviso centralizado com estilo Retro (Arcade/Win95)
   */
  public static HBox criarAvisoSistema(String mensagem) {
    Label textoMsg = new Label(">>> " + mensagem + " <<<");
    textoMsg.setFont(new Font("System", 12));

    String estiloRetroAlerta = "-fx-background-color: #fdf289; " +
        "-fx-text-fill: #000000; " +
        "-fx-font-weight: bold; " +
        "-fx-border-color: #000000; " +
        "-fx-border-width: 2px; " +
        "-fx-padding: 5px 15px; " +
        "-fx-effect: dropshadow(three-pass-box, #000000, 0, 0, 4, 4);";

    textoMsg.setStyle(estiloRetroAlerta);
    textoMsg.setWrapText(true);
    textoMsg.setMaxWidth(380);
    textoMsg.setAlignment(Pos.CENTER);

    HBox linha = new HBox(textoMsg);
    linha.setAlignment(Pos.CENTER);

    linha.setPadding(new Insets(10, 15, 15, 15));

    return linha;
  }

  /*
   * Metodo: abrirConversa
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  public void abrirConversa(String nomeConversa, String tipoConversa) {
    conversaSelecionada = new Pair<>(nomeConversa, tipoConversa);
    conversaSelecionadaLabel.setText(nomeConversa);

    // Zera o contador de notificações desta conversa na memória
    listaConversas.get(conversaSelecionada).setNotificacoes(0);
    System.out.println("Lidas as mensagens de: " + nomeConversa);

    // Usa o Pair inteiro para pescar as mensagens!
    conversaVBox.getChildren().clear();
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


      listaConversas.forEach((chave, valor) -> {
        System.out.println("CLIENTE - Desconectando do grupo: " + valor.getNome());

        String grupoProcessado = processadorTexto.inserirFlagEscape(valor.getNome());

        cliente.sairGrupo(grupoProcessado);
      });
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
