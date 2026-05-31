/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 29 04 2026
* Ultima alteracao.: 31 05 2026
* Nome.............: PacoteEcho.java
* Funcao...........: Pacote de ping enviado entre os roteadores
*************************************************************** */
package model;

public class PacoteEcho extends Pacote {
  private boolean isReply;
  private int latenciaIda;
  private int latenciaVolta;

  public PacoteEcho(int idRoteadorOrigem, int idRoteadorDestino) {
    super(idRoteadorOrigem, idRoteadorDestino);
  }

  public boolean isReply() {
    return isReply;
  }

  public void setReply(boolean isReply) {
    this.isReply = isReply;
  }

  public int getLatenciaIda() {
    return latenciaIda;
  }

  public void setLatenciaIda(int latenciaIda) {
    this.latenciaIda = latenciaIda;
  }

  public int getLatenciaVolta() {
    return latenciaVolta;
  }

  public void setLatenciaVolta(int latenciaVolta) {
    this.latenciaVolta = latenciaVolta;
  }
}