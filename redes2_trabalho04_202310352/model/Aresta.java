/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 24 03 2026
* Ultima alteracao.: 29 03 2026
* Nome.............: Aresta.java
* Funcao...........: Conexao entre os roteadores
*************************************************************** */
package model;

public class Aresta {
  private Roteador destino;
  private int peso;
  private int latencia;
  private int latenciaAnunciada;

  public Aresta(Roteador destino, int peso) {
    this.destino = destino;
    this.peso = peso;
    this.latencia = 0;
    this.latenciaAnunciada = 0;
  }

  public Roteador getDestino() {
    return destino;
  }

  public int getLatencia() {
    return latencia;
  }

  public void setLatencia(int latencia) {
    this.latencia = latencia;
  }

  public int getPeso() {
    return peso;
  }
  
  public int getLatenciaAnunciada() {
    return latenciaAnunciada;
  }

  public void setLatenciaAnunciada(int latenciaAnunciada) {
    this.latenciaAnunciada = latenciaAnunciada;
  }
  
}