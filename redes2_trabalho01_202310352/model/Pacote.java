/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 24 03 2026
* Ultima alteracao.: 27 03 2026
* Nome.............: Pacote.java
* Funcao...........: 
*************************************************************** */
package model;

public class Pacote {
    private int idRoteadorOrigemAnterior;
    private int idRoteadorDestino;
    private int ttl;

    private int idRoteadorCriador;
    private int numeroSequencia;

    private static int contadorPacotes = 0;

    public Pacote(int idRoteadorOrigem, int idRoteadorDestino) {
        this.idRoteadorOrigemAnterior = idRoteadorOrigem;
        this.idRoteadorDestino = idRoteadorDestino;
        contadorPacotes++;
    }

    public Pacote(int idRoteadorOrigem, int idRoteadorDestino, int ttl) {
        this.idRoteadorOrigemAnterior = idRoteadorOrigem;
        this.idRoteadorDestino = idRoteadorDestino;
        this.ttl = ttl;
        contadorPacotes++;
    }

    public Pacote(int idRoteadorOrigem, int idRoteadorDestino, int ttl, int idRoteadorCriador, int numeroSequencia) {
        this.idRoteadorOrigemAnterior = idRoteadorOrigem;
        this.idRoteadorDestino = idRoteadorDestino;
        this.ttl = ttl;        
        this.idRoteadorCriador = idRoteadorCriador;
        this.numeroSequencia = numeroSequencia;
        contadorPacotes++;
    }

    public int getIdRoteadorOrigemAnterior() {
        return idRoteadorOrigemAnterior;
    }

    public void setIdRoteadorOrigemAnterior(int idRoteadorOrigem) {
        this.idRoteadorOrigemAnterior = idRoteadorOrigem;
    }

    public int getIdRoteadorDestino() {
        return idRoteadorDestino;
    }

    public void setIdRoteadorDestino(int idRoteadorDestino) {
        this.idRoteadorDestino = idRoteadorDestino;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getIdRoteadorCriador() {
        return idRoteadorCriador;
    }

    public void setIdRoteadorCriador(int idRoteadorCriador) {
        this.idRoteadorCriador = idRoteadorCriador;
    }

    public int getNumeroSequencia() {
        return numeroSequencia;
    }

    public void setNumeroSequencia(int numeroSequencia) {
        this.numeroSequencia = numeroSequencia;
    }
    
    public static int getContadorPacotes() {
        return contadorPacotes;
    }

    public static void setContadorPacotes(int contadorPacotes) {
        Pacote.contadorPacotes = contadorPacotes;
    }
    
}
