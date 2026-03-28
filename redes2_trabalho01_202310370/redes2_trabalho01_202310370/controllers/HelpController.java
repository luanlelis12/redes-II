/* ***************************************************************
* Autor............: Ana Clara Veiga Prates
* Matricula........: 202310370
* Inicio...........: 14/03/2025
* Ultima alteracao.: 25/03/2025
* Nome.............: HelpController.java
* Funcao...........: Controla a interface grafica da janela 'Help'
*******************************************************************/
package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class HelpController implements Initializable {
    //elementos do fxml
    @FXML ImageView backgroundView;
    @FXML ImageView proximoView;
    @FXML ImageView anteriorView;

    //variavel para controlar em que scene esta a stage de 'help'
    int contadorPagina = 0;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contadorPagina = 0;
    }

    /****************************************************************
    * Metodo: setProximo
    * Funcao: controla as mudancas de scene do botao da direita
    ****************************************************************/
    public void setProximo(MouseEvent event) throws Exception{
        contadorPagina++;
        switch(contadorPagina){
            case 1:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help1.png")));
                anteriorView.setVisible(true);
                break;
            case 2:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help2.png")));
                break;
            case 3:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help3.png")));
                break;
            case 4:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help4.png")));
                break;
            case 5:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help5.png")));
                proximoView.setImage(new Image(getClass().getResourceAsStream("/views/img/botaoFinalizado.png")));
                break;
            case 6:
                Stage stage = (Stage) proximoView.getScene().getWindow();
                stage.close();
                break;
            default:
                System.out.println("Algo deu errado nos botoes de passar pagina do help");
        }
    }

    /****************************************************************
    * Metodo: setAnterior
    * Funcao: controla as mudancas de scene do botao da esquerda
    ****************************************************************/
    public void setAnterior(MouseEvent event) throws Exception{
        contadorPagina--;
        switch(contadorPagina){
            case 0:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help0.png")));
                anteriorView.setVisible(false);
                break;
            case 1:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help1.png")));
                break;
            case 2:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help2.png")));
                break;
            case 3:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help3.png")));
                break;
            case 4:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help4.png")));
                proximoView.setImage(new Image(getClass().getResourceAsStream("/views/img/botaoProximo.png")));
                break;
            case 5:
                backgroundView.setImage(new Image(getClass().getResourceAsStream("/views/img/help5.png")));
                proximoView.setImage(new Image(getClass().getResourceAsStream("/views/img/botaoFinalizado.png")));
                break;
            default:
                System.out.println("Algo deu errado nos botoes de passar pagina do help");
        }
        
    }

    
}
