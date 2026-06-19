package structure;
import model.Filme;

public class ListaLigada {
    private NoLista cabeca;

    public NoLista getCabeca(){ return cabeca; }

    public NoLista inserir(Filme filme) {
        NoLista novo = new NoLista(filme);
        novo.setProximo(cabeca);
        cabeca = novo;
        return novo;
    }

    public Filme buscaLinear(int id, int[] contadorRef) {
        NoLista temp = cabeca;
        int comp = 0;
        while (temp != null) {
            comp++;
            if (temp.getFilme().getId() == id) {
                if (contadorRef != null) contadorRef[0] = comp;
                return temp.getFilme();
            }
            temp = temp.getProximo();
        }
        if (contadorRef != null) contadorRef[0] = comp;
        return null;
    }

    public Filme buscarPorNome(String nome) {
        NoLista temp = cabeca;
        while (temp != null) {
            if (temp.getFilme().getNome().equalsIgnoreCase(nome.trim()))
                return temp.getFilme();
            temp = temp.getProximo();
        }
        return null;
    }
}
