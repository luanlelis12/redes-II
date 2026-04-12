/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 24 03 2026
* Ultima alteracao.: 12 04 2026
* Nome.............: Pacote.java
* Funcao...........: Pacote enviado entre os roteadores
*************************************************************** */
package model;

public class Pacote {
    private int idRoteadorOrigemAnterior;
    private int idRoteadorDestino;

    private int idRoteadorCriador;
    private int numeroSequencia;

    private static int custoTotalDeEnvio = 0;

    public Pacote(int idRoteadorOrigem, int idRoteadorDestino) {
        this.idRoteadorOrigemAnterior = idRoteadorOrigem;
        this.idRoteadorDestino = idRoteadorDestino;
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

    public static int getCustoTotalDeEnvio() {
        return custoTotalDeEnvio;
    }

    public static void setCustoTotalDeEnvio(int custoTotalDeEnvio) {
        Pacote.custoTotalDeEnvio = custoTotalDeEnvio;
    }
    
}
