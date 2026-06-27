/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 15/06/2026
* Ultima alteracao.: 
* Nome.............: MenuInicial.java
* Funcao...........: 
*******************************************************************/
package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.processadorTexto;

public class menuInicialController implements Initializable {

  @FXML
  TextField nomeTextField;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println("O Controller foi carregado corretamente!");
  }

  /*
   * Metodo: criarCliente
   * Funcao: inicializa o cliente
   * Parametros: event = evento que iniciou o metodo
   * Retorno: void
   */
  public void criarCliente(ActionEvent event) {
    String nomeCliente = nomeTextField.getText();
    nomeCliente = processadorTexto.inserirFlagEscape(nomeCliente);

    if (nomeCliente == null || nomeCliente.trim().isEmpty()) { // verifica se o nome eh vazio
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/alert.fxml"));
        Parent root = loader.load();

        alertController controladorDoAlerta = loader.getController();

        controladorDoAlerta.setDetalhes("tituloErro", "mensagemErro");

        Stage janelaAlerta = new Stage();
        janelaAlerta.setScene(new Scene(root));
        janelaAlerta.initStyle(StageStyle.UNDECORATED);
        janelaAlerta.initModality(Modality.APPLICATION_MODAL);

        janelaAlerta.show();
      } catch (IOException e) {
        System.out.println("CLIENTE - Erro: Nao foi possivel carregar o alerta!");
        e.printStackTrace();
      } // fim do try-catch
      return;
    } // fim do if

    String ipServidor = descobrirServidor();

    if (ipServidor == null) { // se o servidor estiver fora de ar emitir alert
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/alert.fxml"));
        Parent root = loader.load();

        alertController controladorDoAlerta = loader.getController();
        controladorDoAlerta.setDetalhes("ERRO: Servidor fora de ar.", "O Servidor não está online!");

        Stage janelaAlerta = new Stage();
        janelaAlerta.setScene(new Scene(root));
        janelaAlerta.initStyle(StageStyle.UNDECORATED);
        janelaAlerta.initModality(Modality.APPLICATION_MODAL);
        janelaAlerta.show();
      } catch (IOException e) {
        System.out.println("CLIENTE - Erro ao abrir alerta!");
        e.printStackTrace();
      } // fim do try-catch
      return;
    } // fim do if

    // Se achou, tenta conectar enviando o IP que descobriu!
    boolean sucesso = clienteController.criarCliente(nomeCliente, ipServidor);

    if (!sucesso) {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/alert.fxml"));
        Parent root = loader.load();

        alertController controladorDoAlerta = loader.getController();
        controladorDoAlerta.setDetalhes("NOME EM USO", "Este nome ja esta sendo utilizado no servidor.");

        Stage janelaAlerta = new Stage();
        janelaAlerta.setScene(new Scene(root));
        janelaAlerta.initStyle(StageStyle.UNDECORATED);
        janelaAlerta.initModality(Modality.APPLICATION_MODAL);
        janelaAlerta.show();
      } catch (IOException e) {
        System.out.println("CLIENTE - Erro ao abrir alerta!");
        e.printStackTrace();
      } // fim do try-catch
      return;
    } // fim do if

    System.out.println("CLIENTE - criando usuario " + nomeCliente + ".");

    try {
      Parent novaRaiz = FXMLLoader.load(getClass().getResource("/view/chat.fxml"));
      Scene novaCena = new Scene(novaRaiz);

      Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

      primaryStage.setScene(novaCena);
      primaryStage.show();
    } catch (IOException e) {
      System.out.println("CLIENTE - Erro: Nao foi possivel trocar de tela");
      e.printStackTrace();
    } // fim do try-catch

  } // fim do metodo criarCliente

  public void fecharAplicacao() {
    System.out.println("CLIENTE - Fechando aplicacao.");
    Platform.exit();
    System.exit(0);
  } // fim do metodo fecharAplicacao

  public void abrirSobre(ActionEvent event) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("view/menuInicial.fxml"));
      Parent root = loader.load();

      Stage popOut = new Stage();

      popOut.setScene(new Scene(root));

      popOut.show();
    } catch (IOException e) {
      System.out.println("CLIENTE - Erro: Nao foi possivel trocar de tela: ");
      e.printStackTrace();
    } // fim do try-catch

  }

  /*
   * Metodo: descobrirServidor
   * Funcao: Envia um pacote na rede e devolve o IP de quem responder
   * Parametros:
   * Retorno: void
   */
  private String descobrirServidor() {
    System.out.println("CLIENTE - Procurando servidor na rede local...");
    try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {
      socket.setBroadcast(true);
      socket.setSoTimeout(3000);

      byte[] dados = "DISCOVER".getBytes();

      java.net.DatagramPacket pacoteEnvio = new java.net.DatagramPacket(dados, dados.length,
          java.net.InetAddress.getByName("255.255.255.255"), 8080);
      socket.send(pacoteEnvio);

      byte[] bufferResposta = new byte[1024];
      java.net.DatagramPacket pacoteResposta = new java.net.DatagramPacket(bufferResposta, bufferResposta.length);

      socket.receive(pacoteResposta);

      String resposta = new String(pacoteResposta.getData(), 0, pacoteResposta.getLength()).trim();
      if (resposta.equals("DISCOVER_OK")) {
        String ipEncontrado = pacoteResposta.getAddress().getHostAddress();
        System.out.println("CLIENTE - Servidor encontrado no IP: " + ipEncontrado);
        return ipEncontrado;
      }
    } catch (Exception e) {
      System.out.println("CLIENTE - Servidor não encontrado (Timeout).");
    }
    return null;
  }

}