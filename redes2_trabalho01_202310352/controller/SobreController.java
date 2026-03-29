/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 26 03 2026
<<<<<<< HEAD
* Ultima alteracao.: 28 03 2026
=======
* Ultima alteracao.: 26 03 2026
>>>>>>> 7c2e7ccf2c136a385af1ecfeb612926858fe5297
* Nome.............: SobreController.java
* Funcao...........: 
*************************************************************** */
package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class SobreController implements Initializable {
  @FXML
  TextFlow textoSobre;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    textoSobre.setStyle("-fx-font-size: 16px;");
    
<<<<<<< HEAD
    Text op1Titulo = new Text("Opcao 1: ");
    op1Titulo.setStyle("-fx-font-weight: bold;");

    Text op1Texto1 = new Text("Cada pacote que chega em um roteador e enviado para ");
=======
    Text op1Titulo = new Text("Opção 1: ");
    op1Titulo.setStyle("-fx-font-weight: bold;");

    Text op1Texto1 = new Text("Cada pacote que chega em um roteador é enviado para ");
>>>>>>> 7c2e7ccf2c136a385af1ecfeb612926858fe5297

    Text op1Destaque = new Text("TODAS");
    op1Destaque.setStyle("-fx-font-weight: bold;");

    Text op1Texto2 = new Text(" as interfaces de rede deste roteador.\n\n");

<<<<<<< HEAD
    Text op2Titulo = new Text("Opcao 2: ");
    op2Titulo.setStyle("-fx-font-weight: bold;");

    Text op2Texto1 = new Text(
        "Cada pacote que chega em um roteador e enviado para todas as interfaces de rede deste roteador, ");
=======
    Text op2Titulo = new Text("Opção 2: ");
    op2Titulo.setStyle("-fx-font-weight: bold;");

    Text op2Texto1 = new Text(
        "Cada pacote que chega em um roteador é enviado para todas as interfaces de rede deste roteador, ");
>>>>>>> 7c2e7ccf2c136a385af1ecfeb612926858fe5297

    Text op2Destaque = new Text("EXCETO");
    op2Destaque.setStyle("-fx-font-weight: bold;");

    Text op2Texto2 = new Text(" por aquela pela qual ele chegou.\n\n");

<<<<<<< HEAD
    Text op3Titulo = new Text("Opcao 3: ");
    op3Titulo.setStyle("-fx-font-weight: bold;");

    Text op3Texto1 = new Text(
        "Cada pacote que chega em um roteador e enviado para todas as interfaces de rede deste roteador, ");
=======
    Text op3Titulo = new Text("Opção 3: ");
    op3Titulo.setStyle("-fx-font-weight: bold;");

    Text op3Texto1 = new Text(
        "Cada pacote que chega em um roteador é enviado para todas as interfaces de rede deste roteador, ");
>>>>>>> 7c2e7ccf2c136a385af1ecfeb612926858fe5297

    Text op3Destaque1 = new Text("EXCETO");
    op3Destaque1.setStyle("-fx-font-weight: bold;");

    Text op3Texto2 = new Text(" por aquela pela qual ele chegou. E cada roteador ");

    Text op3Destaque2 = new Text("VERIFICA");
    op3Destaque2.setStyle("-fx-font-weight: bold;");

<<<<<<< HEAD
    Text op3Texto3 = new Text(" a informacao de ");
=======
    Text op3Texto3 = new Text(" a informação de ");
>>>>>>> 7c2e7ccf2c136a385af1ecfeb612926858fe5297

    Text op3Destaque3 = new Text("TTL");
    op3Destaque3.setStyle("-fx-font-weight: bold;");

    Text op3Texto4 = new Text(" para decidir se o pacote continua a circular na rede.\n\n");

<<<<<<< HEAD
    Text op4Titulo = new Text("Opcao 4: ");
    op4Titulo.setStyle("-fx-font-weight: bold;");

    Text op4Texto1 = new Text(
        "implementara o algoritmo 3 mais uma otimizacao PROPOSTA POR VOCÊ. A logica da sua ideia devera estar documentada nos comentarios dentro do codigo E na opcao about/help/sobre do seu menu principal.");
=======
    Text op4Titulo = new Text("Opção 4: ");
    op4Titulo.setStyle("-fx-font-weight: bold;");

    Text op4Texto1 = new Text(
        "implementará o algoritmo 3 mais uma otimização PROPOSTA POR VOCÊ. A lógica da sua ideia deverá estar documentada nos comentários dentro do código E na opção about/help/sobre do seu menu principal.");
>>>>>>> 7c2e7ccf2c136a385af1ecfeb612926858fe5297

    textoSobre.getChildren().addAll(
        op1Titulo, op1Texto1, op1Destaque, op1Texto2,
        op2Titulo, op2Texto1, op2Destaque, op2Texto2,
        op3Titulo, op3Texto1, op3Destaque1, op3Texto2, op3Destaque2, op3Texto3, op3Destaque3, op3Texto4,
        op4Titulo, op4Texto1);
  }

}