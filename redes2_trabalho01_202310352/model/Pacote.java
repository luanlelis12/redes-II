/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 24 03 2026
* Ultima alteracao.: 
* Nome.............: Pacote.java
* Funcao...........: 
*************************************************************** */
package model;

public class Pacote {
    private Roteador roteadorOrigem;
    private Roteador roteadorDestino;
    private int ttl;
    private static int contadorPacotes=0;

    public Pacote(Roteador roteadorOrigem, Roteador roteadorDestino, int ttl) {
        this.roteadorOrigem = roteadorOrigem;
        this.roteadorDestino = roteadorDestino;
        this.ttl = ttl;
        contadorPacotes++;
    }
  
}
