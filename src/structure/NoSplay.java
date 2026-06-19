package structure;
import model.Filme;

public class NoSplay {
    private int chave;
    private Filme filme;
    private String categoria;
    private int acessos;
    private NoSplay esquerda;
    private NoSplay direita;
    private NoSplay pai;

    public NoSplay(int chave, Filme filme) {
        this.chave   = chave;
        this.filme   = filme;
        this.acessos = 1;
    }

    public NoSplay(int chave, String categoria) {
        this.chave     = chave;
        this.categoria = categoria;
        this.acessos   = 1;
    }

    public int getChave(){
        return chave;
    }
    public Filme getFilme(){
        return filme;
    }
    public void setFilme(Filme f){
        this.filme = f;
    }
    public String getCategoria(){
        return categoria;
    }
    public int getAcessos(){
        return acessos;
    }
    public void setAcessos(int a){
        this.acessos = a;
    }
    public NoSplay getEsquerda(){
        return esquerda;
    }
    public void setEsquerda(NoSplay e){
        this.esquerda = e;
    }
    public NoSplay getDireita(){
        return direita;
    }
    public void setDireita(NoSplay d){
        this.direita = d;
    }
    public NoSplay getPai(){
        return pai;
    }
    public void setPai(NoSplay p){
        this.pai = p;
    }
}
