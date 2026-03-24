package model;

import java.util.ArrayList;

import javafx.scene.image.ImageView;

public class Roteador {
  private int idRoteador;
  private ArrayList<Aresta> conexoes;

  public Roteador(int idRoteador) {
    this.idRoteador = idRoteador;
  }

  public int getIdRoteador() {
    return idRoteador;
  }

}
