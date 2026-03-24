package model;

public class Aresta {
  private Roteador destino;
  private int peso;

  public Aresta(Roteador destino, int peso) {
    this.destino = destino;
    this.peso = peso;
  }
}
