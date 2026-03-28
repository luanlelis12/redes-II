/* ***************************************************************
* Autor............: Ana Clara Veiga Prates
* Matricula........: 202310370
* Inicio...........: 14/03/2025
* Ultima alteracao.: 25/03/2025
* Nome.............: ConfiguracoesController.java
* Funcao...........: Controla a interface grafica da janela 'Configuracoes'
seta as escolhas do usuario e reinicia a simulacao
*******************************************************************/
package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.ManipuladorArquivo;
import models.Pacote;
import models.Roteador;

public class ConfiguracoesController implements Initializable {
    //elementos do fxml
    @FXML public ChoiceBox<String> escolhaVersaoAlgoritmo = new ChoiceBox<>();
    @FXML public ChoiceBox<String> roteadorRemetente = new ChoiceBox<>();
    @FXML public ChoiceBox<String> roteadorDestino = new ChoiceBox<>();
    @FXML public TextField ttlPacote;
    @FXML public ImageView botaoPLAY;

    // valores inteiros que correspondem as escolhas do user
    private static int versaoAlgoritmo = 0;
    private static int idRemetente = 0;
    private static int idDestino = 0;
    private static int ttl = 0;

    // guardando as ultimas escolhas do user
    private static String ultimaVersaoAlgoritmo;
    private static String ultimoRoteadorRemetente;
    private static String ultimoRoteadorDestino;
    private static String ultimoTTL;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        escolhaVersaoAlgoritmo.getItems().addAll("Versao 1", "Versao 2", "Versao 3", "Versao 4");
        ArrayList<Roteador> listaRoteadores = ManipuladorArquivo.getListaRoteadores();
        for (Roteador r : listaRoteadores) {
            roteadorRemetente.getItems().add("Roteador" + r.getIdRoteador());
            roteadorDestino.getItems().add("Roteador" + r.getIdRoteador());
        }

        // restaurando as ultimas escolhas feitas pelo user
        if (ultimaVersaoAlgoritmo != null) {
            escolhaVersaoAlgoritmo.setValue(ultimaVersaoAlgoritmo);
        }
        if (ultimoRoteadorRemetente != null) {
            roteadorRemetente.setValue(ultimoRoteadorRemetente);
        }
        if (ultimoRoteadorDestino != null) {
            roteadorDestino.setValue(ultimoRoteadorDestino);
        }
        if (ultimoTTL != null) {
            ttlPacote.setText(ultimoTTL);
        }

        System.out.println("O Controller foi carregado corretamente!");
    }

    /****************************************************************
    * Metodo: setEscolhas
    * Funcao: eh chamado apos o usuario clicar no botao 'play'. Eh responsavel por salvar 
    os valores escolhidos pelo usuario, resetar a simulacao (caso necessario) e iniciar uma nova simulacao
    ****************************************************************/
    public void setEscolhas(MouseEvent event) throws Exception {
        //guardando os valores escolhidos pelo usuario
        ultimaVersaoAlgoritmo = escolhaVersaoAlgoritmo.getValue();
        ultimoRoteadorRemetente = roteadorRemetente.getValue();
        ultimoRoteadorDestino = roteadorDestino.getValue();
        ultimoTTL = ttlPacote.getText();

        //analisa se algum dos valores ficou nulo -> mostra alerta
        if (ultimaVersaoAlgoritmo == null || ultimoRoteadorRemetente == null || ultimoRoteadorDestino == null || ultimoTTL.equals("")) {
            alertaValorNulo();
            return;
        }

        //analisa se os roteadores destino e remetente sao iguais -> mostra alerta
        if (ultimoRoteadorDestino.equals(ultimoRoteadorRemetente)) {
            alertaRoteadorInvalido();
            return;
        }

        // extrai apenas os numeros das strings e os converte para inteiros
        // -- replaceAll("\\D", "") -- remove qualquer caractere que não seja um dígito (0-9)
        versaoAlgoritmo = Integer.parseInt(ultimaVersaoAlgoritmo.replaceAll("\\D", "")) - 1;
        idRemetente = Integer.parseInt(ultimoRoteadorRemetente.replaceAll("\\D", ""));
        idDestino = Integer.parseInt(ultimoRoteadorDestino.replaceAll("\\D", ""));
        ttl = Integer.parseInt(ultimoTTL.replaceAll("\\D", "")); // acrescenta mais um porque no primeiro roteador ja desconta

        /*criei uma thread aqui para o monitor do fim das animacoes (no metodo 'reiniciar') 
        evitando que a execucao fique bloqueada.
        Sem essa thread, a interface poderia travar, impedindo que as animações terminassem corretamente.  
        Todos os comandos que estao dentro da thread precisavam ser executados em sequencia. */
        Thread a = new Thread(){
            public void run(){
                try {
                    reiniciar();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //uma pausa antes de comecar a nova simulacao
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //comeca a simulacao
                System.out.println("\n\nCOMECANDO SIMULACAO!!! \n\n");
                BackboneController.sinalizarRoteadores(idRemetente - 1, idDestino - 1);
                ManipuladorArquivo.getListaRoteadores().get(idRemetente - 1).recebePacote(0, new Pacote(idRemetente, idDestino, ttl));
                ManipuladorArquivo.getListaRoteadores().get(idRemetente - 1).finalizaAnimacaoPacote();
        
            }
        };
        a.start();
    
        Stage stage = (Stage) botaoPLAY.getScene().getWindow(); // Obtendo a janela atual
        stage.close();
    }


    /****************************************************************
    * Metodo: reiniciar
    * Funcao: responsavel por reiniciar as threads dos roteadores e resetar alguns elementos da GUI
    ****************************************************************/
    public void reiniciar() throws IOException {
        System.out.println("Reiniciando a simulacao");
        
        // desativa as threads dos roteadores
        for (Roteador r : ManipuladorArquivo.getListaRoteadores()) {
            r.desativa();
        }

        //espera todas as animacoes acabarem antes de seguir para o resto do processo de reset
        while (!BackboneController.getAnimacoesAtivas().isEmpty()) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // reinicia as threads dos roteadores
        for (Roteador r : ManipuladorArquivo.getListaRoteadores()) {
            r.reiniciar(); // Reseta filas de espera do roteador
            Thread t = new Thread(r);
            t.start();
        }

        // reinicia a contagem de pacotes criados (em sequencia o label do contador tbm ira reiniciar)
        // inicia em -1 porque o primeiro pacote criado nao conta
        Pacote.setQuantidadePacotes(-1);

        // resetando os roteadores sinalizados
        for (ImageView r : BackboneController.getRoteadoresIcon()) {
            r.setEffect(null);
        }

    }
    

    /****************************************************************
    * Metodo: alertaRoteadorInvalido
    * Funcao: cria um alerta para avisar que os roteadores remetente e destino sao iguais 
    ****************************************************************/
    public void alertaRoteadorInvalido() throws Exception {
        Stage janelaAlerta = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/views/alertaRoteadorInvalido.fxml"));
        janelaAlerta.setScene(new Scene(root));
        janelaAlerta.setResizable(false);
        janelaAlerta.initModality(Modality.APPLICATION_MODAL); // Impede interacao com o Stage principal
        janelaAlerta.setTitle("Alerta");
        janelaAlerta.getIcons().add(new Image(getClass().getResourceAsStream("/views/img/roteador.png")));
        janelaAlerta.show();
    }

    /****************************************************************
    * Metodo: alertaValorNulo
    * Funcao: cria um alerta para avisar que ha algum valor nulo
    ****************************************************************/
    public void alertaValorNulo() throws Exception {
        Stage janelaAlerta = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/views/alertaValorNulo.fxml"));
        janelaAlerta.setScene(new Scene(root));
        janelaAlerta.setResizable(false);
        janelaAlerta.initModality(Modality.APPLICATION_MODAL); // Impede interacao com o Stage principal
        janelaAlerta.setTitle("Alerta");
        janelaAlerta.getIcons().add(new Image(getClass().getResourceAsStream("/views/img/roteador.png")));
        janelaAlerta.show();
    }

    /* GETTERS E SETTERS  */
    public static int getVersaoAlgoritmo() {
        return versaoAlgoritmo;
    }
}
