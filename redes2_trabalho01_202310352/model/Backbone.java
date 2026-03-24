package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Backbone {
  private ArrayList<Roteador> roteadores = new ArrayList<>();

  public Backbone() {
  }

  public void carregarArquivo(String caminho) {
    try {
      BufferedReader buffRead = new BufferedReader(new FileReader(caminho));
      String linha = buffRead.readLine();

      int quantidadeRoteadores = 0;

      int i = 0;
      while (linha.charAt(i) != ';') {
        int valor = Character.getNumericValue(linha.charAt(i));
        quantidadeRoteadores = (quantidadeRoteadores * 10) + valor;
        i++;
      }

      roteadores.clear();
      for (int j = 0; j < quantidadeRoteadores; j++) {
        roteadores.add(new Roteador(j));
      }
      
      System.out.println("Quantidade de roteadores no backbone: " + quantidadeRoteadores);
      buffRead.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void gerarRoteadores() {
  }

  public ArrayList<Roteador> getRoteadores() {
    return roteadores;
  }
}
