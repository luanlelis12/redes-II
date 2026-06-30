package controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class listarConversasController implements Initializable {

  @FXML
  private VBox vboxConversas;
  @FXML
  private Pane barraSuperior;
  @FXML
  private Label tituloLabel;

  private clienteController controladorPai;
  private ArrayList<String> conversas = new ArrayList<>();
  private String tipoConversa;

  private double xOffset = 0;
  private double yOffset = 0;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

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

    if (tipo.equals(clienteController.GRUPO)) {
      tituloLabel.setText("Grupos");
    } else if (tipo.equals(clienteController.PRIVADO)) {
      tituloLabel.setText("Membros");
    }

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
          System.out.println("Aviso: Imagem nao encontrada, mantendo a foto padrao.");
        }

      }

      if (tipoConversa.equals(clienteController.GRUPO)) {
        itemConversa.setOnMouseClicked(event -> {
          controladorPai.entrarGrupo(nomeConversa);
          fecharTela();
        });
      } else if (tipoConversa.equals(clienteController.PRIVADO)) {
        itemConversa.setOnMouseClicked(event -> {
          controladorPai.criarConversaPrivada(nomeConversa);
          fecharTela();
        });
      }

      vboxConversas.getChildren().add(itemConversa);
    } catch (Exception e) {
      System.out.println("CLIENTE - Erro: Nao foi possivel carregar o visual do grupo!");
      e.printStackTrace();
    }
  } // fim do metodo adicionarConversaNaTela

  /*
   * Metodo: fecharTela
   * Funcao: Fecha a tela
   */
  public void fecharTela() {
    Stage janela = (Stage) vboxConversas.getScene().getWindow();
    janela.close();
  }

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