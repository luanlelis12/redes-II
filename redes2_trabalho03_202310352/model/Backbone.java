/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 16 03 2026
* Ultima alteracao.: 24 03 2026
* Nome.............: Backbone.java
* Funcao...........: Fazer a leitura do arquivo txt e criar os roteadores e suas conexoes
*************************************************************** */
package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import controller.BackboneController;

public class Backbone {
  private ArrayList<Roteador> roteadores = new ArrayList<>();

  public Backbone() {
  }

  /*
   * Metodo: carregarArquivo
   * Funcao: recebe a posicao do roteador e o posiciona na tela
   * Parametros: posX = posicao horizontal; posY = posicao vertical
   * Retorno: void
   */
  public void carregarArquivo(String caminho, BackboneController controller) {
    try {
      BufferedReader buffRead = new BufferedReader(new FileReader(caminho));
      String linha = buffRead.readLine();

      int quantidadeRoteadores = 0;

      int i = 0;
      while (linha.charAt(i) != ';') {
        int valor = Character.getNumericValue(linha.charAt(i));
        quantidadeRoteadores = (quantidadeRoteadores * 10) + valor;
        i++;
      } // fim do while

      gerarRoteadores(quantidadeRoteadores, controller);

      System.out.println("Quantidade de roteadores no backbone: " + quantidadeRoteadores);

      while ((linha = buffRead.readLine()) != null) {
        if (linha.trim().isEmpty())
          continue;

        String[] partes = linha.split(";");

        if (partes.length == 4) {
          int idOrigem = Integer.parseInt(partes[0].trim());
          int idDestino = Integer.parseInt(partes[1].trim());
          int latencia01 = Integer.parseInt(partes[2].trim());
          int latencia02 = Integer.parseInt(partes[3].trim());

          Roteador r1 = roteadores.get(idOrigem-1);
          Roteador r2 = roteadores.get(idDestino-1);

          gerarConexao(r1, r2, latencia01, latencia02);

          System.out.println("Conexao criada: " + idOrigem + " -> " + idDestino + " latencia " + latencia01);
          System.out.println("Conexao criada: " + idDestino + " -> " + idOrigem + " latencia " + latencia02);
        } // fim do if
      } // fim do while

      buffRead.close();
    } catch (IOException e) {
      e.printStackTrace();
    } // fim do try-catch
  } // fim do metodo carregarArquivo

  /*
   * Metodo: gerarRoteadores
   * Funcao: cria os roteadores e os adicionam na rede
   * Parametros: quantR = quantidade de roteadores a ser criado
   * Retorno: void
   */
  public void gerarRoteadores(int quantR, BackboneController controller) {
    roteadores.clear();
    for (int j = 1; j <= quantR; j++) {
      roteadores.add(new Roteador(j, controller));
    } // fim do for
  } // fim do metodo carregarArquivo

  /*
   * Metodo: gerarConexao
   * Funcao:
   * Parametros:
   * Retorno:
   */
  public void gerarConexao(Roteador r1, Roteador r2, int latencia01, int latencia02) {
    r1.addVizinho(r2, latencia01);
    r2.addVizinho(r1, latencia02);
  } // fim do metodo gerarConexao

  public ArrayList<Roteador> getRoteadores() {
    return roteadores;
  }
}
