/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 03 05 2026
* Nome.............: Principal.java
* Funcao...........: Executar os algoritmos de roteamento por inundacao num rede simulada
*************************************************************** */

import util.FxmlRotas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import controller.BackboneController;
import controller.SobreController;

public class Principal extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {

    // Carrega o arquivo FXML da tela principal
    FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlRotas.TELA_ROTEAMENTO));
    Parent root = loader.load();

    // Configura a cena
    primaryStage.setTitle("ROTEAMENTO POR VETOR DISTANCIA");
    primaryStage.setScene(new Scene(root));
    Image icon = new Image(getClass().getResourceAsStream("view/img/icon.png"));
    primaryStage.getIcons().add(icon);

    primaryStage.setOnCloseRequest(evento -> {
      Platform.exit();
      System.exit(0);
    });

    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }

}
