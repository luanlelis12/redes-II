/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 
* Nome.............: Roteador.java
* Funcao...........: 
*************************************************************** */
package model;

import java.util.ArrayList;

public class Roteador {
  private int idRoteador;
  private ArrayList<Aresta> conexoes = new ArrayList<>();

  public Roteador(int idRoteador) {
    this.idRoteador = idRoteador;
    System.out.println("Roteador " + idRoteador + " - criado com sucesso!");
  }

  /*
   * Metodo: addVizinho
   * Funcao: Adicionar roteador vizinho e o peso da aresta
   * Parametros: Vizinho = roteador que esta interligado; peso = valor numero para
   * medir a conexao
   * Retorno: void
   */
  public void addVizinho(Roteador vizinho, int peso) {
    this.conexoes.add(new Aresta(vizinho, peso));
  } // fim do addVizinho

  public int getIdRoteador() {
    return idRoteador;
  }

  public ArrayList<Aresta> getConexoes() {
    return conexoes;
  }

}
