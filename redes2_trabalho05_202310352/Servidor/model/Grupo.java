/* ***************************************************************
* Autor............: Luan Alves Lelis Costa
* Matricula........: 202310352
* Inicio...........: 12/06/2026
* Ultima alteracao.: 
* Nome.............: Grupo.java
* Funcao...........: 
*******************************************************************/

package model;

import java.util.ArrayList;

public class Grupo {
  private String nomeGrupo;
  private ArrayList<Usuario> usuarios;
  
  public Grupo(String nomeGrupo, ArrayList<Usuario> usuarios) {
    this.nomeGrupo = nomeGrupo;
    this.usuarios = usuarios;
  }

  public String getNomeGrupo() {
    return nomeGrupo;
  }

  public void setNomeGrupo(String nomeGrupo) {
    this.nomeGrupo = nomeGrupo;
  }

  public void addUsuarios(Usuario usuario) {
    usuarios.add(usuario);
  }

  public ArrayList<Usuario> getUsuarios() {
    return usuarios;
  }

  public void setUsuarios(ArrayList<Usuario> usuarios) {
    this.usuarios = usuarios;
  }

  public boolean containsUser(String nome) {
    for (Usuario usuario : usuarios) {
      if (usuario.getNome() == nome)
        return true;
    }
    return false;
  }

}
