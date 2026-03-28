package models;

public class ConexaoRoteadores {
    private Roteador vertice1;
    private Roteador vertice2;
    private int pesoConexao;

    public ConexaoRoteadores(Roteador vertice1, Roteador vertice2, int pesoConexao) {
        this.vertice1 = vertice1;
        this.vertice2 = vertice2;
        this.pesoConexao = pesoConexao;
        System.out.println("Vertice " + vertice1.getIdRoteador() +" está conectado com vertice "+ vertice2.getIdRoteador() + " com peso "+pesoConexao);
    }

    /* GETTERS E SETTERS */
    public Roteador getVertice1() {
        return vertice1;
    }

    public void setVertice1(Roteador vertice1) {
        this.vertice1 = vertice1;
    }

    public Roteador getVertice2() {
        return vertice2;
    }

    public void setVertice2(Roteador vertice2) {
        this.vertice2 = vertice2;
    }

    public int getPesoConexao() {
        return pesoConexao;
    }

    public void setPesoConexao(int pesoConexao) {
        this.pesoConexao = pesoConexao;
    }
    
}
