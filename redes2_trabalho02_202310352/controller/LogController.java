/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 12 04 2026
* Ultima alteracao.: 12 04 2026
* Nome.............: LogController.java
* Funcao...........: Controller para gerenciar a tela de log do algoritmo de roteamento de menor caminho
*************************************************************** */
package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class LogController {

  @FXML
  private TextFlow areaDeLog;

  /*
   * Metodo: adicionarLog
   * Funcao: Adiciona o texto de log a tela de log
   * Parametros: mensagem = log do algoritmo / tipo = se eh um titulo, destaque ou texto normal
   * Retorno: void
   */
  public void adicionarLog(String mensagem, String tipo) {
    Platform.runLater(() -> {
      Text novaLinha = new Text(mensagem + "\n");

      if (tipo.equals("DESTAQUE")) {
        novaLinha.setStyle("-fx-fill: #cc2e2e; -fx-font-weight: bold;");
      } else if (tipo.equals("TITULO")) {
        novaLinha.setStyle("-fx-fill: #3498db; -fx-font-weight: bold; -fx-font-size: 14px;");
      } else {
        novaLinha.setStyle("-fx-fill: #ffffff;");
      } // fim do if

      if (areaDeLog != null) {
        areaDeLog.getChildren().add(novaLinha);
      } // fim do if
    });
  } // fim do metodo adicionarLog

  /*
   * Metodo: limparLog
   * Funcao: Limpa a tela de log
   * Parametros:
   * Retorno: void
   */
  public void limparLog() {
    Platform.runLater(() -> {
      if (areaDeLog != null) {
        areaDeLog.getChildren().clear();
      } // fim do if
    });
  } // fim do metodo limparLog
}