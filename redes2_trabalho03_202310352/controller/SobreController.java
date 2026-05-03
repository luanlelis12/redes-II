/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 26 03 2026
* Ultima alteracao.: 11 04 2026
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
    Text titulo = new Text("Trabalho Pratico #03: Roteamento por Vetor de Distancia\n\n");
    titulo.setFont(Font.font("System", FontWeight.BOLD, 18));
    titulo.setFill(Color.WHITE);

    Text texto = new Text(
        "Disciplina: Redes de Computadores II | Prof. Marlos Marques\n" +
            "Discente: Luan Alves Lelis Costa\n\n" +
            "O simulador implementa o algoritmo de Vetor de Distancia. Os roteadores mantem tabelas " +
            "com a linha de saida e a metrica (retardo de tempo) para cada destino. O retardo eh lido do " +
            "arquivo backbone.txt e descoberto via pacotes PING (Echo Request/Reply).\n\n" +
            "As tabelas sao atualizadas periodicamente com os vizinhos a cada 10s. O programa permite " +
            "escolher uma Origem e Destino graficamente, desabilitar links clicando sobre eles para forcar " +
            "recalculos.");
    texto.setFont(Font.font("System", 14));
    texto.setFill(Color.WHITE);

    textoSobre.getChildren().addAll(titulo, texto);

    Text tituloLegenda = new Text("Legenda de Pacotes da Rede");
    tituloLegenda.setFont(Font.font("System", FontWeight.BOLD, 16));
    tituloLegenda.setFill(Color.WHITE);
    vboxLegenda.getChildren().add(tituloLegenda);

    adicionarItemLegenda("pacoteEchoRequest.png", "Echo Request (PING: Descobrindo vizinho)");
    adicionarItemLegenda("pacoteEchoReply.png", "Echo Reply (PING: Confirmacao de latencia)");
    adicionarItemLegenda("pacoteVetor.png", "Pacote Vetor (Atualizacao periodica da tabela)");
    adicionarItemLegenda("pacote.png", "Pacote de Dados (Mensagem do Usuario)");
  }

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
    }
  }
}