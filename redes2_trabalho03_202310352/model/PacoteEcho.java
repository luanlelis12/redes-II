/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 29 04 2026
* Ultima alteracao.: 29 04 2026
* Nome.............: PacoteVetor.java
* Funcao...........: Pacote de ping enviado entre os roteadores
*************************************************************** */
package model;

public class PacoteEcho extends Pacote {
    private boolean isReply; // false = Request, true = Reply
    private int latenciaDaAresta;
    
    public PacoteEcho(int idRoteadorOrigem, int idRoteadorDestino, int latenciaDaAresta) {
        super(idRoteadorOrigem, idRoteadorDestino);
        this.latenciaDaAresta = latenciaDaAresta;
    }

    public boolean isReply() {
        return isReply;
    }

    public void setReply(boolean isReply) {
        this.isReply = isReply;
    }

    public int getLatenciaDaAresta() {
        return latenciaDaAresta;
    }

    public void setLatenciaDaAresta(int latenciaDaAresta) {
        this.latenciaDaAresta = latenciaDaAresta;
    }
    
}
