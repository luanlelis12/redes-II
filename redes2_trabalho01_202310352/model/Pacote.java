/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 24 03 2026
* Ultima alteracao.: 24 03 2026
* Nome.............: Pacote.java
* Funcao...........: 
*************************************************************** */
package model;

public class Pacote {
    private int idRoteadorOrigem;
    private int idRoteadorDestino;
    private int ttl;

    private static int contadorPacotes = 0;

    public Pacote(int idRoteadorOrigem, int idRoteadorDestino) {
        this.idRoteadorOrigem = idRoteadorOrigem;
        this.idRoteadorDestino = idRoteadorDestino;
        contadorPacotes++;
    }

    public Pacote(int idRoteadorOrigem, int idRoteadorDestino, int ttl) {
        this.idRoteadorOrigem = idRoteadorOrigem;
        this.idRoteadorDestino = idRoteadorDestino;
        this.ttl = ttl;
        contadorPacotes++;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }
    
    public static int getContadorPacotes() {
        return contadorPacotes;
    }

    public static void setContadorPacotes(int contadorPacotes) {
        Pacote.contadorPacotes = contadorPacotes;
    }

    public int getIdRoteadorOrigem() {
        return idRoteadorOrigem;
    }

    public void setIdRoteadorOrigem(int idRoteadorOrigem) {
        this.idRoteadorOrigem = idRoteadorOrigem;
    }

    public int getIdRoteadorDestino() {
        return idRoteadorDestino;
    }

    public void setIdRoteadorDestino(int idRoteadorDestino) {
        this.idRoteadorDestino = idRoteadorDestino;
    }

}
