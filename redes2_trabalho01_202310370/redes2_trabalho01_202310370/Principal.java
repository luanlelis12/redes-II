/* ***************************************************************
* Autor............: Ana Clara Veiga Prates
* Matricula........: 202310370
* Inicio...........: 14/03/2025
* Ultima alteracao.: 25/03/2025
* Nome.............: Principal.java
* Funcao...........: Responsavel por iniciar o programa
*******************************************************************/

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import models.ManipuladorArquivo;


public class Principal extends Application{

    @Override
	public void start(Stage primaryStage) throws Exception {
        ManipuladorArquivo manipuladorArquivo = new ManipuladorArquivo();
        String path = "backbone.txt"; 
	    manipuladorArquivo.leitor(path); //le o arquivo do backbone e gera os roteadores e conexoes

        //mostrando a tela com o backbone 
        Parent root = FXMLLoader.load(getClass().getResource("/views/backbone.fxml")); //carrega o arquivo.fxml do inicio
        primaryStage.setScene(new Scene(root)); //determina a cena que vai ser usada pelo PrimaryStage
        primaryStage.setResizable(false); //impede que o usuario mude o tamanho da janela 
		primaryStage.setTitle("Simulação de Backbone");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/views/img/roteador.png")));
		primaryStage.show(); //mostra a tela
        primaryStage.setOnCloseRequest(Event -> {
        System.exit(0);
        });

        //mostrando a tela de apresentacao
        Stage janelaApresentacao = new Stage();
        janelaApresentacao.setScene(new Scene(FXMLLoader.load(getClass().getResource("/views/apresentacao.fxml"))));
        janelaApresentacao.setResizable(false); 
		janelaApresentacao.setTitle("Apresentacao");
        janelaApresentacao.getIcons().add(new Image(getClass().getResourceAsStream("/views/img/roteador.png")));
        janelaApresentacao.show(); 

  }

	public static void main(String[] args) {
        launch(args);
    }

}
