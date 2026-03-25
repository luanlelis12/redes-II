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

        if (partes.length == 3) {
          int idOrigem = Integer.parseInt(partes[0].trim());
          int idDestino = Integer.parseInt(partes[1].trim());
          int peso = Integer.parseInt(partes[2].trim());

          Roteador r1 = roteadores.get(idOrigem-1);
          Roteador r2 = roteadores.get(idDestino-1);

          gerarConexao(r1, r2, peso);

          System.out.println("Conexão criada: " + idOrigem + " <-> " + idDestino + " peso " + peso);
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
  public void gerarConexao(Roteador r1, Roteador r2, int peso) {
    r1.addVizinho(r2, peso);
    r2.addVizinho(r1, peso);
  } // fim do metodo gerarConexao

  public ArrayList<Roteador> getRoteadores() {
    return roteadores;
  }
}
