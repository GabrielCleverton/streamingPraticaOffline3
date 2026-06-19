package structure;
import model.Filme;
import java.util.ArrayList;
import java.util.List;

public class ListaDupla {
    private NoDuplo cabeca;  // MRU
    private NoDuplo cauda;   // LRU
    private int tamanho;

    public NoDuplo getCabeca() {
        return cabeca;
    }
    public NoDuplo getCauda(){
        return cauda;
    }
    public int getTamanho(){
        return tamanho;
    }

    public NoDuplo inserirNoInicio(Filme filme) {
        NoDuplo novo = new NoDuplo(filme);
        if (cabeca == null) {
            cabeca = cauda = novo;
        } else {
            novo.setProximo(cabeca);
            cabeca.setAnterior(novo);
            cabeca = novo;
        }
        tamanho++;
        return novo;
    }

    public void moverParaInicio(NoDuplo no) {
        if (no == cabeca)
            return;
        if (no.getAnterior() != null)
            no.getAnterior().setProximo(no.getProximo());
        if (no.getProximo()  != null)
            no.getProximo().setAnterior(no.getAnterior());
        if (no == cauda)
            cauda = no.getAnterior();
        no.setAnterior(null);
        no.setProximo(cabeca);
        if (cabeca != null) cabeca.setAnterior(no);
        cabeca = no;
        if (cauda == null) cauda = no;
    }

    public void removerNo(NoDuplo no) {
        if (no.getAnterior() != null)
            no.getAnterior().setProximo(no.getProximo());
        else
            cabeca = no.getProximo();
        if (no.getProximo()  != null)
            no.getProximo().setAnterior(no.getAnterior());
        else
            cauda  = no.getAnterior();
        tamanho--;
    }

    public List<Filme> listar() {
        List<Filme> lista = new ArrayList<>();
        NoDuplo cur = cabeca;
        while (cur != null) { lista.add(cur.getFilme()); cur = cur.getProximo(); }
        return lista;
    }
}
