/* ***************************************************************
* Autor............: Ana Clara Veiga Prates
* Matricula........: 202310370
* Inicio...........: 14/03/2025
* Ultima alteracao.: 25/03/2025
* Nome.............: BackboneController.java
* Funcao...........: Controla a interface grafica da janela principal, 
eh responsavel por desenhar os roteadores e as conexoes do backbone
*******************************************************************/

package controllers;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.animation.PathTransition;
import javafx.animation.PathTransition.OrientationType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.ConexaoRoteadores;
import models.ManipuladorArquivo;
import models.Roteador;

public class BackboneController implements Initializable{
    @FXML Pane paneBackbone;
    @FXML Label contadorPacotes;
    Stage janelinha = new Stage();
    private String caminhoImagemRoteador = "/views/img/roteador.png"; 
    private String caminhoImagemPacote = "/views/img/pacote.png"; 
    private static ArrayList<PathTransition> animacoesAtivas = new ArrayList<>();
    private static ArrayList<ImageView> roteadoresIcon = new ArrayList<>();
    private static ArrayList<Line> conexoesLinhas = new ArrayList<>();
    private static double centroX, centroY, raio; 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("O Controller foi carregado corretamente!");
        desenharBackbone(ManipuladorArquivo.getListaRoteadores());
    }

    /****************************************************************
    * Metodo: desenharBackbone
    * Funcao:  eh responsavel por gerar o backbone, determina onde a imagem de cada roteador ira ficar 
    (formando uma arrumacao circular). chama os metodos que cria as conexoes e os roteadores
    ****************************************************************/    
    public void desenharBackbone(ArrayList<Roteador> roteadores){
        //determinando os parametros do circulo do backbone 
        centroX = 500;
        centroY = 300;
        raio = 250;
        int totalRoteadores = roteadores.size();
        double anguloEspacamento = 360/totalRoteadores;

        //gerando os roteadores
        for (int i = 0; i < totalRoteadores; i++) {
            Roteador r = roteadores.get(i);
            r.setController(this);
            double angulo = Math.toRadians(i * anguloEspacamento); //converte o angulo de espacamento para radianos
            double x = centroX + raio * Math.cos(angulo); //calcula coordenada x
            double y = centroY + raio * Math.sin(angulo); //calcula coordenada y

            criarRoteadores(i+1, x, y); //cria a imageview com a posicao calculada
        }

        //gerando as linhas das conexoes 
        for (Roteador r: roteadores){
            for(ConexaoRoteadores c: r.getListaConexoes()){
                desenharConexao(c.getVertice1().getIdRoteador()-1, c.getVertice2().getIdRoteador()-1);
            }
        }

        //chamando esse metodo depois para que as imageviews fiquem na frente da conexao
        mostrarRoteadores();
    }

    /****************************************************************
    * Metodo: desenharConexao
    * Funcao:  cria uma linha entre duas imageViews, conectando os roteadores passados como parametro
    ****************************************************************/
    public void desenharConexao(int id1, int id2){
        ImageView r1 = roteadoresIcon.get(id1);
        ImageView r2 = roteadoresIcon.get(id2);

        if (r1 != null && r2 != null) {
            Line conexao = new Line(r1.getX()+35, r1.getY()+35, r2.getX()+35, r2.getY()+35);
            conexao.setStroke(Color.BLACK);
            paneBackbone.getChildren().add(conexao);
            conexoesLinhas.add(conexao);
        }
    }

    /****************************************************************
    * Metodo: criarRoteadores
    * Funcao: cria a imageView do roteador e o posiciona (de acordo com os parametros de posicao passados)
    ****************************************************************/
    public void criarRoteadores(int id, double posX, double posY){
        ImageView roteadorView = new ImageView(new Image(caminhoImagemRoteador));

        roteadorView.setFitWidth(70); 
        roteadorView.setFitHeight(70);
        roteadorView.setX(posX - 35);
        roteadorView.setY(posY - 35);

        roteadoresIcon.add(roteadorView);
    }

    /****************************************************************
    * Metodo: mostrarRoteadores
    * Funcao: seta como visivel as imageView dos roteadores
    ****************************************************************/
    public void mostrarRoteadores(){
        int contadorRoteadores = 1;
        for(ImageView roteadorView:roteadoresIcon){
            paneBackbone.getChildren().add(roteadorView);
            paneBackbone.getChildren().add(new Text(roteadorView.getX()+15, roteadorView.getY()+15, String.valueOf(contadorRoteadores))); 
            contadorRoteadores++;
        }
    }

    /****************************************************************
    * Metodo: desenhaPacote
    * Funcao: eh responsavel por animar a transmissao de um pacote entre dois roteadores na interface grafica
    ****************************************************************/
    public void desenhaPacote(Roteador roteadorEmissor, Roteador roteadorReceptor){
        Platform.runLater(() -> {
            ImageView pacoteView = new ImageView(new Image(caminhoImagemPacote));

            ImageView roteadorEmissorImagem = roteadoresIcon.get(roteadorEmissor.getIdRoteador()-1);
            ImageView roteadorReceptorImagem = roteadoresIcon.get(roteadorReceptor.getIdRoteador()-1);

            double posInicialX = roteadorEmissorImagem.getX();
            double posInicialY = roteadorEmissorImagem.getY();

            //colocando a imagem do pacote sobreposto a imagem do roteador emissor
            pacoteView.setFitWidth(45); 
            pacoteView.setFitHeight(45);
            pacoteView.setX(posInicialX);
            pacoteView.setY(posInicialY);
            paneBackbone.getChildren().add(pacoteView);

            //cria a animacao que vai da imagem de um roteador ao outro
            Line path = procuraConexao(roteadorEmissorImagem.getX()+35, roteadorReceptorImagem.getX()+35);
            PathTransition pathTransition = new PathTransition();
            pathTransition.setDuration(Duration.millis(1200)); // duracao da animacao
            pathTransition.setNode(pacoteView);
            pathTransition.setPath(path);
            pathTransition.setOrientation(OrientationType.ORTHOGONAL_TO_TANGENT);
            pathTransition.setAutoReverse(false);
            animacoesAtivas.add(pathTransition);
            pathTransition.setOnFinished(event -> {
                paneBackbone.getChildren().remove(pacoteView); // remove a img do pacote ao chegar no destino
                roteadorReceptor.finalizaAnimacaoPacote();
                animacoesAtivas.remove(pathTransition);
            });
            pathTransition.play();
        });
    }

    /****************************************************************
    * Metodo: procuraConexao
    * Funcao: 
    ****************************************************************/
    public Line procuraConexao(double posicaoInicialX, double posicaoFinalX){
        for(Line linha: conexoesLinhas){
            if (linha.getStartX() == posicaoInicialX && linha.getEndX()==posicaoFinalX){
                return linha;
            }
        }
        return null;
    }

    /****************************************************************
    * Metodo: sinalizarRoteadores
    * Funcao: responsavel por destacar a imagem dos roteadores remetente e destino da simulacao
    ****************************************************************/
    public static void sinalizarRoteadores(int idRemetente, int idDestino){
        ImageView remetente = roteadoresIcon.get(idRemetente);
        ImageView destino = roteadoresIcon.get(idDestino);
        remetente.setEffect(new DropShadow(20, Color.BLUE)); //sombreado azul -> remetente
        destino.setEffect(new DropShadow(20, Color.RED)); //sombreado vermelho -> destino
    }

    /****************************************************************
    * Metodo: sinalizarChegadaPacote
    * Funcao: eh chamado quando o pacote chega ao seu destino final. Eh responsavel por colocar um efeito 
    na imagem que sinaliza a chegada
    ****************************************************************/
    public static void sinalizarChegadaPacote(int idDestino){
        ImageView destino = roteadoresIcon.get(idDestino);
        destino.setEffect(new DropShadow(20, Color.GREEN)); //coloca um sombreado verde na img do roteador
    }

    /****************************************************************
    * Metodo: configurarSimulacao
    * Funcao: eh chamado apos o usuario clicar no botao 'configuracao'. Eh responsavel por abrir outra stage 
    que tera as choiceBox para o usuario determinar os parametros da simulacao
    ****************************************************************/
    public void configurarSimulacao(MouseEvent event) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/views/configuracoes.fxml"));
        janelinha.setScene(new Scene(root)); 
        janelinha.setResizable(false); 
        janelinha.setOnCloseRequest(eventClose -> {
          eventClose.consume(); // Consome o evento de fechamento, impedindo o fechamento
        });
        janelinha.setTitle("Configurações");
        janelinha.getIcons().add(new Image(getClass().getResourceAsStream("/views/img/roteador.png")));
        janelinha.show();
    }

    /****************************************************************
    * Metodo: abrirHelp
    * Funcao: eh chamado apos o usuario clicar no botao 'help'. Eh responsavel por abrir outra stage 
    que tera as infos sobre o trabalho
    ****************************************************************/
    public void abrirHelp(MouseEvent event) throws Exception{
        Stage abaHelp = new Stage();
        abaHelp.setScene(new Scene(FXMLLoader.load(getClass().getResource("/views/help.fxml")))); 
        abaHelp.setResizable(false); 
        abaHelp.initModality(Modality.APPLICATION_MODAL); // Impede interacao com o Stage principal
        abaHelp.setTitle("Help");
        abaHelp.getIcons().add(new Image(getClass().getResourceAsStream("/views/img/roteador.png")));
        abaHelp.show();
    }

    public void atualizarContadorPacotes(int contador){
        Platform.runLater(() -> contadorPacotes.setText(String.valueOf(contador)));
    }

    /* GETTERS E SETTERS */
    public static ArrayList<PathTransition> getAnimacoesAtivas() {
        return animacoesAtivas;
    }

    public static ArrayList<ImageView> getRoteadoresIcon() {
        return roteadoresIcon;
    }

    public static void setRoteadoresIcon(ArrayList<ImageView> roteadoresIcon) {
        BackboneController.roteadoresIcon = roteadoresIcon;
    }

}