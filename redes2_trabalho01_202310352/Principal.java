/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.:   
* Nome.............: Principal.java
* Funcao...........: 
*************************************************************** */

import util.FxmlRotas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import controller.BackboneController;
public class Principal extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {

    // Carrega o arquivo FXML da tela principal
    FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlRotas.TELA_ROTEAMENTO));
    Parent root = loader.load();

    // Configura a cena
    primaryStage.setTitle("");
    primaryStage.setScene(new Scene(root));
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
  
}
