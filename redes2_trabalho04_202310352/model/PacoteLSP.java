package model;

import java.util.HashMap;
import java.util.Map;

public class PacoteLSP extends Pacote {
  private int idRoteadorGerador; // O roteador original que mediu e gerou esta fofoca
  private int numeroSequencia;   // Para sabermos se o pacote é novo ou velho
  private int ttl;               // Time to Live (Idade) para morrer se ficar em loop
  private Map<Integer, Integer> enlaces; // Mapa com: ID do Vizinho -> Latência Anunciada

  public PacoteLSP(int idOrigemImediata, int idDestino, int idRoteadorGerador, int numeroSequencia, int ttl, Map<Integer, Integer> enlaces) {
    super(idOrigemImediata, idDestino);
    this.idRoteadorGerador = idRoteadorGerador;
    this.numeroSequencia = numeroSequencia;
    this.ttl = ttl;
    if (enlaces != null) {
      this.enlaces = new HashMap<>(enlaces);
    } else {
      this.enlaces = new HashMap<>();
    }
  }

  public int getIdRoteadorGerador() { return idRoteadorGerador; }
  public int getNumeroSequencia() { return numeroSequencia; }
  public int getTtl() { return ttl; }
  public void setTtl(int ttl) { this.ttl = ttl; }
  public Map<Integer, Integer> getEnlaces() { return enlaces; }
}