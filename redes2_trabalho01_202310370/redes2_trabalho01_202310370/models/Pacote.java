package models;

public class Pacote {
    private int idRemetente;
    private int idDestino;
    private int ttl;
    private static int quantidadePacotes;
    
    public Pacote(int idRemetente, int idDestino, int ttl) {
        this.idRemetente = idRemetente;
        this.idDestino = idDestino;
        this.ttl = ttl;
        quantidadePacotes++;
        System.out.println("Pacote foi criado com sucesso!");
        System.out.println("Contador de pacotes criados = "+ Pacote.getQuantidadePacotes());
    }

    /* GETTERS E SETTERS */
    public int getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(int idRemetente) {
        this.idRemetente = idRemetente;
    }

    public int getIdDestino() {
        return idDestino;
    }

    public void setIdDestino(int idDestino) {
        this.idDestino = idDestino;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public static int getQuantidadePacotes() {
        return quantidadePacotes;
    }

    public static void setQuantidadePacotes(int quantidadePacotes) {
        Pacote.quantidadePacotes = quantidadePacotes;
    }

    
}
