/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 31 05 2026
* Ultima alteracao.: 31 05 2026
* Nome.............: PacoteHello.java
* Funcao...........: Pacote enviado para conhecer os vizinhos do roteador
*************************************************************** */

package model;

public class PacoteHello extends Pacote {
  private boolean isReply;

  public PacoteHello(int idRoteadorOrigem, int idRoteadorDestino, boolean isReply) {
    super(idRoteadorOrigem, idRoteadorDestino);
    this.isReply = isReply;
  }

  public boolean isReply() {
    return isReply;
  }

  public void setReply(boolean isReply) {
    this.isReply = isReply;
  }
}