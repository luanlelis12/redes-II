package models;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import controllers.BackboneController;
import controllers.ConfiguracoesController;
import javafx.application.Platform;
import javafx.util.Pair;

public class Roteador extends Thread {
    private static int contadorRoteadores;
    private int idRoteador;
    private ArrayList<ConexaoRoteadores> listaConexoes;
    private BackboneController controller;
    private boolean estaAtivo;

    /*obs: O pair possui o primeiro atributo sendo o KEY e o segundo sendo o VALUE 
           KEY = idRoteador e VALUE = pacote */
    
    //fila de espera de pacotes recebidos e q nao podem ser processados pois estao esperando o fim da animacao
    private Queue<Pair<Integer, Pacote>> filaPacotesEsperandoAnimacao; 
   
    //fila de espera dos pacotes que ja podem ser processados 
    private BlockingQueue<Pair<Integer, Pacote>> filaEsperaPacotes; 

    
    public Roteador(int idRoteador) {
        this.idRoteador = idRoteador;
        listaConexoes = new ArrayList<>();
        filaPacotesEsperandoAnimacao = new LinkedList<>();
        filaEsperaPacotes =  new LinkedBlockingQueue<>();
        this.estaAtivo = true;
        contadorRoteadores++;
        System.out.println("Roteador " + idRoteador + " - criado com sucesso!");
    }

    public void run() {
        System.out.println("Roteador "+ idRoteador+ " - iniciado.");
        while (estaAtivo) {
            try {
                Pair<Integer, Pacote> pacoteRecebidoPair = filaEsperaPacotes.take(); // Aguarda mensagem
                checaEstaAtivo();
                processaPacote(pacoteRecebidoPair);
                checaEstaAtivo();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /****************************************************************
    * Metodo: recebePacote
    * Funcao: eh chamado por outro roteador (simbolizando envio do pacote)
    ao receber, adiciona o pacote na lista de pacotes com animacao ainda ativa
    ****************************************************************/
    public void recebePacote(Integer idRoteadorEmissor, Pacote pacote){
        checaEstaAtivo();
        filaPacotesEsperandoAnimacao.add(new Pair<Integer, Pacote>(idRoteadorEmissor, pacote));
    }

    /****************************************************************
    * Metodo: finalizaAnimacaoPacote
    * Funcao: eh chamado quando o pathTransition do pacote termina, tira o pacote da fila de espera de animacao 
    e passa para a fila de espera dos pacotes (pacote ja pode ser processado)
    ****************************************************************/
    public void finalizaAnimacaoPacote(){
        checaEstaAtivo();    
        if(!filaPacotesEsperandoAnimacao.isEmpty()){
            filaEsperaPacotes.add(filaPacotesEsperandoAnimacao.poll());
        }
    }

    /****************************************************************
    * Metodo: processaPacote
    * Funcao: eh responsavel por analisar se este roteador eh o destino final do pacote, 
    senao chama a funcao de enviar o pacote 
    ****************************************************************/
    public void processaPacote(Pair<Integer, Pacote> pacoteRecebidoPair){
        System.out.println("Roteador "+idRoteador+ " - Esta processando um pacote. Fila = "+filaEsperaPacotes.size());
        checaEstaAtivo();
        if(pacoteRecebidoPair.getValue().getIdDestino() == idRoteador){
            System.out.println("Roteador "+ idRoteador+" - Pacote chegou ao destino!!");
            BackboneController.sinalizarChegadaPacote(idRoteador-1);
        } else{
            enviaPacote(pacoteRecebidoPair);
        }
    }

    /****************************************************************
    * Metodo: enviaPacote
    * Funcao: eh responsavel por checar a escolha de algoritmo escolhido pelo usuario
    e chama a versao do roteamento correspondente
    ****************************************************************/
    public void enviaPacote(Pair<Integer, Pacote> pacoteRecebidoPair){
        System.out.println("Roteador"+idRoteador+ " - versao algoritmo = "+ConfiguracoesController.getVersaoAlgoritmo());
        checaEstaAtivo();
        switch(ConfiguracoesController.getVersaoAlgoritmo()){
            case 0:
                roteamentoVersao1(pacoteRecebidoPair);
                break;
            case 1:
                roteamentoVersao2(pacoteRecebidoPair);
                break;
            case 2:
                roteamentoVersao3(pacoteRecebidoPair);
                break;
            case 3:
                roteamentoVersao4(pacoteRecebidoPair);
                break;
            default:
                System.out.println("Roteador "+idRoteador+" - Algo deu errado na hora de escolher a versao do algoritmo");
            
        }
    }

    /****************************************************************
    * Metodo: roteamentoVersao1
    * Funcao: pega a lista de todas as conexoes do roteador e 
    envia o pacote para todos os roteadores vizinhos
    ****************************************************************/
    public void roteamentoVersao1(Pair<Integer, Pacote> pacoteRecebidoPair){
        System.out.println("Roteador "+idRoteador+" - Executando versao 1 do roteamento por inundacao. (Envia pra todos os vizinhos)");
        Pacote pacote = pacoteRecebidoPair.getValue();
        for(ConexaoRoteadores conexao : listaConexoes){
            conexao.getVertice2().recebePacote(idRoteador, new Pacote(pacote.getIdRemetente(), pacote.getIdDestino(), pacote.getTtl()-1));
            Platform.runLater(() -> controller.desenhaPacote(this, conexao.getVertice2()));
            controller.atualizarContadorPacotes(Pacote.getQuantidadePacotes());
            System.out.println("Roteador "+idRoteador+" - enviou pacote para roteador "+conexao.getVertice2().getIdRoteador());
        }
    }

    
    /****************************************************************
    * Metodo: roteamentoVersao2
    * Funcao: pega a lista de todas as conexoes do roteador e 
    envia o pacote para todos os roteadores vizinhos. Caso a conexao seja por onde o pacote chegou
    o roteador nao envia de volta
    ****************************************************************/
    private void roteamentoVersao2(Pair<Integer, Pacote> pacoteRecebidoPair) {
        System.out.println("Roteador "+idRoteador+" - Executando versao 2 do roteamento por inundacao. (Nao envia de volta)");
        Pacote pacote = pacoteRecebidoPair.getValue();
        int idRoteadorEmissor = pacoteRecebidoPair.getKey();
        for(ConexaoRoteadores conexao : listaConexoes){
            if(conexao.getVertice2().getIdRoteador() != idRoteadorEmissor){
                conexao.getVertice2().recebePacote(idRoteador, new Pacote(pacote.getIdRemetente(), pacote.getIdDestino(), pacote.getTtl()-1));
                Platform.runLater(() -> controller.desenhaPacote(this, conexao.getVertice2()));
                controller.atualizarContadorPacotes(Pacote.getQuantidadePacotes());
                System.out.println("Roteador "+idRoteador+" - enviou pacote para roteador "+conexao.getVertice2().getIdRoteador());
            } else{
                System.out.println("Roteador "+idRoteador+" - NAO enviou pacote para roteador "+conexao.getVertice2().getIdRoteador()+" pq foi a interface de rede pela qual o pacote chegou");
            }

        }
    }

    /****************************************************************
    * Metodo: roteamentoVersao3
    * Funcao: 1- analisa se o ttl do pacote eh maior que zero antes de enviar
              2- envia para os vizinhos, menos para a conexao por onde o pacote chegou
    ****************************************************************/
    private void roteamentoVersao3(Pair<Integer, Pacote> pacoteRecebidoPair) {
        System.out.println("Roteador "+idRoteador+" - Executando versao 3 do roteamento por inundacao. (Analisa o TTL)");
        Pacote pacote = pacoteRecebidoPair.getValue();
        int idRoteadorEmissor = pacoteRecebidoPair.getKey();
        if(pacote.getTtl() > 0){
            for(ConexaoRoteadores conexao : listaConexoes){
                if(conexao.getVertice2().getIdRoteador() != idRoteadorEmissor){
                    conexao.getVertice2().recebePacote(idRoteador, new Pacote(pacote.getIdRemetente(), pacote.getIdDestino(), pacote.getTtl()-1));
                    Platform.runLater(() -> controller.desenhaPacote(this, conexao.getVertice2()));
                    controller.atualizarContadorPacotes(Pacote.getQuantidadePacotes());
                    System.out.println("Roteador "+idRoteador+" - enviou pacote para roteador "+conexao.getVertice2().getIdRoteador());
                } else{
                    System.out.println("Roteador "+idRoteador+" - NAO enviou pacote para roteador "+conexao.getVertice2().getIdRoteador()+" pq foi a interface de rede pela qual o pacote chegou");
                }
            }
        } else{
            System.out.println("Roteador "+ idRoteador+" - Pacote nao enviado pois chegou ao limite do TTL. ");
        }
    }

    /****************************************************************
    * Metodo: roteamentoVersao4
    * Funcao: 1- analisa se o ttl do pacote eh maior que zero antes de enviar
              2- analisa se o roteador destino eh vizinho do atual, se sim envia so para o roteador destino
              3- envia para os vizinhos (EXCETO para a conexao pela qual o pacote chegou E para o roteador remetente)
    ****************************************************************/
    private void roteamentoVersao4(Pair<Integer, Pacote> pacoteRecebidoPair) {
        System.out.println("Roteador "+idRoteador+" - Executando versao 4 do roteamento por inundacao. (Minha versão)");
        Pacote pacote = pacoteRecebidoPair.getValue();
        int idRoteadorEmissor = pacoteRecebidoPair.getKey();
        ConexaoRoteadores destino = ehVizinho(pacote.getIdDestino());
        if(pacote.getTtl() > 0){
            if(destino != null){
                destino.getVertice2().recebePacote(idRoteador, new Pacote(pacote.getIdRemetente(), pacote.getIdDestino(), pacote.getTtl()-1));
                Platform.runLater(() -> controller.desenhaPacote(this, destino.getVertice2()));
                controller.atualizarContadorPacotes(Pacote.getQuantidadePacotes());
                System.out.println("Roteador "+idRoteador+" - Enviou pacote direto para o destino: roteador "+destino.getVertice2().getIdRoteador());
            } else{
                for(ConexaoRoteadores conexao : listaConexoes){
                    if(conexao.getVertice2().getIdRoteador() != idRoteadorEmissor){
                        if(pacote.getIdRemetente() != conexao.getVertice2().getIdRoteador()){
                            conexao.getVertice2().recebePacote(idRoteador, new Pacote(pacote.getIdRemetente(), pacote.getIdDestino(), pacote.getTtl()-1));
                            Platform.runLater(() -> controller.desenhaPacote(this, conexao.getVertice2()));
                            controller.atualizarContadorPacotes(Pacote.getQuantidadePacotes());
                            System.out.println("Roteador "+idRoteador+" - Enviou pacote para roteador "+conexao.getVertice2().getIdRoteador());
                        } else{
                            System.out.println("Roteador "+idRoteador+" - NAO enviou pacote para roteador "+conexao.getVertice2().getIdRoteador() + " pq eh o remetente");
                        }
                    } else{
                        System.out.println("Roteador "+idRoteador+" - NAO enviou pacote para roteador "+conexao.getVertice2().getIdRoteador()+" pq foi a interface de rede pela qual o pacote chegou");
                    }
                }
            }
        }
    }   

    /****************************************************************
    * Metodo: ehVizinho
    * Funcao: recebe o id de um roteador e checa se ele eh vizinho do roteador que chamou o metodo
    ****************************************************************/
    public ConexaoRoteadores ehVizinho (int idRoteador){
        System.out.println("Checando se roteador "+idRoteador+ " eh vizinho de roteador "+ this.idRoteador);
        for(ConexaoRoteadores conexao : listaConexoes){
            if(conexao.getVertice2().getIdRoteador() == idRoteador){
                System.out.println("O roteador eh vizinho");
                return conexao;
            }
        }
        return null;
    }

    /****************************************************************
    * Metodo: desativa
    * Funcao: interrompe a thread atual
    ****************************************************************/
    public void desativa(){
        this.estaAtivo = false;
        this.interrupt();
    }

    /****************************************************************
    * Metodo: reiniciar
    * Funcao: prepara para a thread ser reiniciada, limpa as filas de espera e a variavel 'estaAtivo'
    ****************************************************************/
    public void reiniciar() {
        this.estaAtivo = true;
        this.filaPacotesEsperandoAnimacao.clear();
        this.filaEsperaPacotes.clear();
    }
    
    /****************************************************************
    * Metodo: checaEstaAtivo
    * Funcao: metodo checa se a thread foi desativada (analiando o valor de 'estaAtivo'),
     caso positivo interrompe a thread atual
    ****************************************************************/
    public void checaEstaAtivo(){
        if(!estaAtivo){
            Thread.currentThread().interrupt();
        }
    }

    /* GETTERS E SETTERS */
    public ArrayList<ConexaoRoteadores> getListaConexoes() {
        return listaConexoes;
    }

    public BackboneController getController() {
        return controller;
    }

    public void setController(BackboneController controller) {
        this.controller = controller;
    }

    public static int getContadorRoteadores() {
        return contadorRoteadores;
    }
    public static void setContadorRoteadores(int contadorRoteadores) {
        Roteador.contadorRoteadores = contadorRoteadores;
    }
    public int getIdRoteador() {
        return idRoteador;
    }
    public void setIdRoteador(int idRoteador) {
        this.idRoteador = idRoteador;
    }
    public void adicionaConexao(ConexaoRoteadores novaConexao){
        listaConexoes.add(novaConexao);
    }

}
