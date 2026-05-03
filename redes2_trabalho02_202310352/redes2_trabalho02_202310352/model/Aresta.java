/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 24 03 2026
* Ultima alteracao.: 24 03 2026
* Nome.............: Aresta.java
* Funcao...........: Conexao entre os roteadores
*************************************************************** */
package model;

public class Aresta {
  private Roteador destino;
  private int peso;

  public Aresta(Roteador destino, int peso) {
    this.destino = destino;
    this.peso = peso;
  }

  public Roteador getDestino() {
    return destino;
  }

  public int getPeso() {
    return peso;
  }
  
}