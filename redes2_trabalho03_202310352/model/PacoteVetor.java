/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 29 04 2026
* Ultima alteracao.: 03 05 2026
* Nome.............: PacoteVetor.java
* Funcao...........: Pacote com a tabela de roteamento enviado entre os roteadores
*************************************************************** */
package model;

import java.util.HashMap;
import java.util.Map;

public class PacoteVetor extends Pacote {
  private Map<Integer, Integer> vetorDistancias;

  public PacoteVetor(int idOrigem, int idDestino, Map<Integer, Integer> vetorDistancias) {
    super(idOrigem, idDestino);
    if (vetorDistancias != null) {
      this.vetorDistancias = new HashMap<>(vetorDistancias);
    } else {
      this.vetorDistancias = new HashMap<>();
    } // fim do if
  }

  public Map<Integer, Integer> getVetorDistancias() {
    return vetorDistancias;
  }

  public void setVetorDistancias(Map<Integer, Integer> vetorDistancias) {
    this.vetorDistancias = vetorDistancias;
  }

}