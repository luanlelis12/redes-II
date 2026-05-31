/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 26 03 2026
* Ultima alteracao.: 31 05 2026
* Nome.............: SobreController.java
* Funcao...........: Controller para gerenciar a tela de sobre o programa
*************************************************************** */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class SobreController implements Initializable {

  @FXML
  private TextFlow textoSobre;

  @FXML
  private VBox vboxLegenda;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Text titulo = new Text("Trabalho Pratico #04: Roteamento por Estado de Enlace\n\n");
    titulo.setFont(Font.font("System", FontWeight.BOLD, 18));
    titulo.setFill(Color.WHITE);

    Text texto = new Text(
        "Disciplina: Redes de Computadores II | Prof. Marlos Marques\n" +
            "Discente: Luan Alves Lelis Costa\n\n" +
            "O simulador implementa o algoritmo de Estado de Enlace. Os roteadores primeiro se\n" +
            "apresentam usando pacotes HELLO. Depois, monitoram ativamente a latencia de seus\n" +
            "links locais usando pacotes ECHO (Ping) para calcular o RTT (Round Trip Time).\n\n" +
            "Sempre que ha uma mudanca drastica na latencia, um Link State Packet (LSP) e gerado e\n" +
            "inundado pela rede. Cada roteador monta o seu proprio mapa global da rede e executa o\n" +
            "Algoritmo de Dijkstra para calcular os caminhos mais curtos e atualizar as rotas.");
    texto.setFont(Font.font("System", 14));
    texto.setFill(Color.WHITE);

    textoSobre.getChildren().addAll(titulo, texto);

    Text tituloLegenda = new Text("Legenda de Pacotes da Rede");
    tituloLegenda.setFont(Font.font("System", FontWeight.BOLD, 16));
    tituloLegenda.setFill(Color.WHITE);
    vboxLegenda.getChildren().add(tituloLegenda);

    adicionarItemLegenda("pacoteEchoRequest.png", "Hello / Echo Request (Apresentacao e Ida do Ping)");
    adicionarItemLegenda("pacoteEchoReply.png", "Hello / Echo Reply (Confirmacao e Volta do Ping)");
    adicionarItemLegenda("pacoteVetor.png", "Pacote LSP (Inundacao do estado do link)");
    adicionarItemLegenda("pacote.png", "Pacote de Dados (Mensagem do Usuario)");
  }

  /*
   * Metodo: adicionarItemLegenda
   * Funcao: adiciona imagens na legenda da pagina sobre
   * Parametros: nomeImagem = nome do arquivo de imagem, descricao = explica o que eh a imagem
   * Retorno: void
   */
  private void adicionarItemLegenda(String nomeImagem, String descricao) {
    try {
      ImageView img = new ImageView(new Image("file:view/img/" + nomeImagem));
      img.setFitWidth(30);
      img.setFitHeight(30);

      Text texto = new Text(" - " + descricao);
      texto.setFill(Color.WHITE);
      texto.setFont(Font.font("System", 14));

      HBox linha = new HBox(10, img, texto);
      linha.setAlignment(Pos.CENTER_LEFT);

      vboxLegenda.getChildren().add(linha);
    } catch (Exception e) {
      System.out.println("Erro ao carregar imagem da legenda: " + nomeImagem);
    } // fim do try-catch
  } // fim do metodo adicionarItemLegenda
}