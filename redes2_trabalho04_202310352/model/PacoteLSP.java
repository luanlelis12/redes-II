/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 29 04 2026
* Ultima alteracao.: 31 05 2026
* Nome.............: PacoteLSP.java
* Funcao...........: Pacote com a tabela de de LSP
*************************************************************** */
package model;

import java.util.HashMap;
import java.util.Map;

public class PacoteLSP extends Pacote {
  private int idRoteadorGerador; // O roteador original do pacote
  private int numeroSequencia;
  private int ttl;
  private Map<Integer, Integer> enlaces; // Map com (ID do Vizinho,Latencia)

  public PacoteLSP(int idOrigemImediata, int idDestino, int idRoteadorGerador, int numeroSequencia, int ttl,
      Map<Integer, Integer> enlaces) {
    super(idOrigemImediata, idDestino);
    this.idRoteadorGerador = idRoteadorGerador;
    this.numeroSequencia = numeroSequencia;
    this.ttl = ttl;
    if (enlaces != null) {
      this.enlaces = new HashMap<>(enlaces);
    } else {
      this.enlaces = new HashMap<>();
    } // fim do if
  }

  public int getIdRoteadorGerador() {
    return idRoteadorGerador;
  }

  public int getNumeroSequencia() {
    return numeroSequencia;
  }

  public int getTtl() {
    return ttl;
  }

  public void setTtl(int ttl) {
    this.ttl = ttl;
  }

  public Map<Integer, Integer> getEnlaces() {
    return enlaces;
  }
}