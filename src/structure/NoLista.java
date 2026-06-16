package structure;
import model.Filme;

public class NoLista {
    private Filme filme;
    private NoLista proximo;

    public NoLista(Filme filme) {
        this.filme   = filme;
        this.proximo = null;
    }

    public Filme getFilme(){
        return filme;
    }
    public void setFilme(Filme filme){
        this.filme = filme;
    }
    public NoLista getProximo(){
        return proximo;
    }
    public void setProximo(NoLista p){
        this.proximo = p;
    }
}
