/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 24 03 2026
* Ultima alteracao.: 03 05 2026
* Nome.............: Pacote.java
* Funcao...........: Pacote enviado entre os roteadores
*************************************************************** */
package model;

public class Pacote {
    private int idRoteadorOrigem;
    private int idRoteadorDestino;

    private static int custoTotalDeEnvio = 0;

    public Pacote(int idRoteadorOrigem, int idRoteadorDestino) {
        this.idRoteadorOrigem = idRoteadorOrigem;
        this.idRoteadorDestino = idRoteadorDestino;
    }

    public int getIdRoteadorDestino() {
        return idRoteadorDestino;
    }

    public void setIdRoteadorDestino(int idRoteadorDestino) {
        this.idRoteadorDestino = idRoteadorDestino;
    }

    public int getIdRoteadorOrigem() {
        return idRoteadorOrigem;
    }

    public void setIdRoteadorOrigem(int idRoteadorOrigem) {
        this.idRoteadorOrigem = idRoteadorOrigem;
    }

    public static int getCustoTotalDeEnvio() {
        return custoTotalDeEnvio;
    }

    public static void setCustoTotalDeEnvio(int custoTotalDeEnvio) {
        Pacote.custoTotalDeEnvio = custoTotalDeEnvio;
    }

}
