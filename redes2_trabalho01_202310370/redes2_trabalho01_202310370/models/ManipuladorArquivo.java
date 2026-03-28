package models;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ManipuladorArquivo {
	private static ArrayList<Roteador> listaRoteadores;

	public ManipuladorArquivo(){
		listaRoteadores = new ArrayList<>();
	}

	/****************************************************************
    * Metodo: leitor
    * Funcao: eh responsavel por ler o arquivo com as infos do backbone, gerar os roteadores e conexoes
    ****************************************************************/
	public void leitor(String path) throws IOException {
		BufferedReader buffRead = new BufferedReader(new FileReader(path));
		String linha = "";

		// linha 1 = numero de nos da sub-rede
		linha = buffRead.readLine();
		
		int quantidadeRoteadores;
		if(linha.toCharArray().length > 2){ // se a primeira linha tem mais de um digito 
			quantidadeRoteadores = Character.getNumericValue(linha.toCharArray()[1]); //unidade
			quantidadeRoteadores += Character.getNumericValue(linha.toCharArray()[0])*10; //dezena
		} else{
			quantidadeRoteadores = Character.getNumericValue(linha.toCharArray()[0]); //apenas unidade
		}
		
		System.out.println("Teremos um backbone com "+ quantidadeRoteadores + " roteadores.");
		criarRoteadores(quantidadeRoteadores);
		
		// demais linhas = conexoes entre os nos e seus respectivos pesos
		linha = buffRead.readLine();
		while(linha != null){
			//indentificando as informações 
			char[] linhaPorCaracteres = linha.toCharArray();
			int idVertice1 = Character.getNumericValue(linhaPorCaracteres[0]);
			int idVertice2 = Character.getNumericValue(linhaPorCaracteres[2]);
			int pesoConexao = Character.getNumericValue(linhaPorCaracteres[4]);
			
			//adicionando nas listas de ambos roteadores a conexao informada
			listaRoteadores.get(idVertice1-1).adicionaConexao(new ConexaoRoteadores(listaRoteadores.get(idVertice1-1), listaRoteadores.get(idVertice2-1), pesoConexao));
			listaRoteadores.get(idVertice2-1).adicionaConexao(new ConexaoRoteadores(listaRoteadores.get(idVertice2-1), listaRoteadores.get(idVertice1-1), pesoConexao));

			//lendo a proxima linha
			linha = buffRead.readLine();
		}

		buffRead.close();
	}

	/****************************************************************
    * Metodo: criarRoteadores
    * Funcao: cria as threads dos roteadores e da start nelas
    ****************************************************************/
	public static void criarRoteadores(int quantidadeRoteadores){
		listaRoteadores.clear();
		for(int i=1; i<=quantidadeRoteadores; i++){
			listaRoteadores.add(new Roteador(i));
			listaRoteadores.get(i-1).start();
		}
	}
	
	/* GETTER E SETTER  */
	public static ArrayList<Roteador> getListaRoteadores() {
		return listaRoteadores;
	}
	
	public static void setListaRoteadores(ArrayList<Roteador> listaRoteadores) {
		ManipuladorArquivo.listaRoteadores = listaRoteadores;
	}

}