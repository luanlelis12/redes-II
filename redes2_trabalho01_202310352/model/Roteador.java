package model;

import java.util.ArrayList;

public class Roteador {
  private int idRoteador;
  private ArrayList<Aresta> conexoes = new ArrayList<>();

  public Roteador(int idRoteador) {
    this.idRoteador = idRoteador;
  }
  
  public void addVizinho(Roteador vizinho, int peso) {
    this.conexoes.add(new Aresta(vizinho, peso));
  }

  public int getIdRoteador() {
    return idRoteador;
  }

  public ArrayList<Aresta> getConexoes() {
    return conexoes;
  }
 
}
