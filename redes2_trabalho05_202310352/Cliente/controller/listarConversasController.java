package controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class listarConversasController implements Initializable {

  private clienteController controladorPai;
  private ArrayList<String> conversas = new ArrayList<>();
  private String tipoConversa;

  @FXML
  private VBox vboxConversas;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    for (String conversa : conversas) {
      adicionarConversaNaTela(conversa);
    }
  }

  // Apague o conteúdo do initialize()!

  /*
   * Metodo: carregarDados
   * Funcao: Recebe a lista que veio do servidor e desenha os botões
   */
  public void carregarDados(ArrayList<String> itens, String tipo) {
    this.tipoConversa = tipo;
    this.conversas = itens;

    vboxConversas.getChildren().clear(); // Limpa itens velhos, se houver

    if (conversas.isEmpty()) {
      // Opcional: Mostra um aviso bonitinho se não houver ninguem
      vboxConversas.getChildren().add(new Label("Nenhum item encontrado."));
      return;
    }

    for (String item : conversas) {
      adicionarConversaNaTela(item);
    }
  }

  /*
   * Metodo: adicionarConversaNaTela
   * Funcao:
   * Parametros:
   * Retorno: void
   */
  public void adicionarConversaNaTela(String nomeConversa) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/grupoButton.fxml"));
      AnchorPane itemConversa = loader.load();

      ImageView icone = (ImageView) itemConversa.lookup("#iconeConversa");

      Label labelNome = (Label) itemConversa.lookup("#nomeConversa");
      if (labelNome != null) {
        labelNome.setText(nomeConversa);
      }

      Label notificacaoLabel = (Label) itemConversa.lookup("#notificacaoLabel");
      itemConversa.getChildren().remove(notificacaoLabel);

      Button sairConversa = (Button) itemConversa.lookup(".buttonSair");
      itemConversa.getChildren().remove(sairConversa);

      if (tipoConversa.equals(clienteController.PRIVADO)) {
        String caminhoImagem = "/view/img/iconPriv.png";

        try {
          Image novaImagem = new Image(getClass().getResourceAsStream(caminhoImagem));
          icone.setImage(novaImagem);
        } catch (Exception e) {
          System.out.println("Aviso: Imagem não encontrada, mantendo a foto padrão.");
        }

      }

      if (tipoConversa.equals(clienteController.GRUPO)) {
        itemConversa.setOnMouseClicked(event -> {
          controladorPai.entrarGrupo(nomeConversa);
          fecharTela(null);
        });
      } else if (tipoConversa.equals(clienteController.PRIVADO)) {
        itemConversa.setOnMouseClicked(event -> {
          controladorPai.criarConversaPrivada(nomeConversa);
          fecharTela(null);
        });
      }

      vboxConversas.getChildren().add(itemConversa);
    } catch (Exception e) {
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

  public ArrayList<String> getConversas() {
    return conversas;
  }

  public void setConversas(ArrayList<String> conversas) {
    this.conversas = conversas;
  }

  public void addConversas(String conversa) {
    conversas.add(conversa);
  }

  public String getTipoConversa() {
    return tipoConversa;
  }

  public void setTipoConversa(String tipoConversa) {
    this.tipoConversa = tipoConversa;
  }

}