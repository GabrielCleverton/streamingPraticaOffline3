package structure;
import model.Filme;

public class ArvoreSplay {
    private NoSplay raiz;
    private int tamanho;

    public NoSplay getRaiz(){
        return raiz;
    }
    public int getTamanho(){
        return tamanho;
    }
    public boolean vazia(){
        return raiz == null;
    }

    private void rotDir(NoSplay x) {
        NoSplay y = x.getEsquerda();
        x.setEsquerda(y.getDireita());
        if (y.getDireita() != null) y.getDireita().setPai(x);
        y.setPai(x.getPai());
        if (x.getPai() == null)
            raiz = y;
        else if (x == x.getPai().getDireita())
            x.getPai().setDireita(y);
        else
            x.getPai().setEsquerda(y);
        y.setDireita(x);
        x.setPai(y);
    }

    private void rotEsq(NoSplay x) {
        NoSplay y = x.getDireita();
        x.setDireita(y.getEsquerda());
        if (y.getEsquerda() != null)
            y.getEsquerda().setPai(x);
        y.setPai(x.getPai());
        if (x.getPai() == null)
            raiz = y;
        else if (x == x.getPai().getEsquerda())
            x.getPai().setEsquerda(y);
        else
            x.getPai().setDireita(y);
        y.setEsquerda(x);
        x.setPai(y);
    }

    private void splay(NoSplay x) {
        while (x.getPai() != null) {
            NoSplay p = x.getPai();
            NoSplay g = p.getPai();
            if (g == null) {
                if (x == p.getEsquerda()) rotDir(p); else rotEsq(p);
            } else if (x == p.getEsquerda() && p == g.getEsquerda()) {
                rotDir(g); rotDir(p);
            } else if (x == p.getDireita() && p == g.getDireita()) {
                rotEsq(g); rotEsq(p);
            } else if (x == p.getDireita() && p == g.getEsquerda()) {
                rotEsq(p); rotDir(g);
            } else {
                rotDir(p); rotEsq(g);
            }
        }
    }

    private NoSplay buscarNo(int chave) {
        NoSplay cur    = raiz;
        NoSplay ultimo = null;
        while (cur != null) {
            ultimo = cur;
            if (chave < cur.getChave())
                cur = cur.getEsquerda();
            else if (chave > cur.getChave())
                cur = cur.getDireita();
            else
                return cur;
        }
        if (ultimo != null) splay(ultimo);
        return null;
    }

    public void inserirOuAtualizar(int chave, Filme filme) {
        if (raiz == null)
        { raiz = new NoSplay(chave, filme);
            tamanho++;
            return;
        }
        NoSplay no = buscarNo(chave);
        if (no != null) {
            no.setAcessos(no.getAcessos() + 1);
            no.setFilme(filme);
            splay(no);
            return;
        }
        NoSplay novo = new NoSplay(chave, filme);
        inserirNaBST(novo);
        splay(novo);
        tamanho++;
    }

    public void inserirOuAtualizarCategoria(String categoria) {
        int chave = Math.abs(categoria.hashCode());
        if (raiz == null) {
            raiz = new NoSplay(chave, categoria);
            tamanho++;
            return;
        }
        NoSplay no = buscarNo(chave);
        if (no != null) {
            no.setAcessos(no.getAcessos() + 1);
            splay(no);
            return;
        }
        NoSplay novo = new NoSplay(chave, categoria);
        inserirNaBST(novo);
        splay(novo);
        tamanho++;
    }

    private void inserirNaBST(NoSplay novo) {
        NoSplay cur = raiz;
        while (true) {
            if (novo.getChave() < cur.getChave()) {
                if (cur.getEsquerda() == null) {
                    cur.setEsquerda(novo);
                    novo.setPai(cur);
                    break; }
                cur = cur.getEsquerda();
            } else {
                if (cur.getDireita()  == null) {
                    cur.setDireita(novo);
                    novo.setPai(cur);
                    break;
                }
                cur = cur.getDireita();
            }
        }
    }

    public ListaEncadeada<NoSplay> topN(int n) {
        ListaEncadeada<NoSplay> resultado = new ListaEncadeada<>();
        if (raiz == null)
            return resultado;

        NoSplay[] fila = new NoSplay[tamanho + 10];
        int inicio = 0, fim = 0;

        fila[fim++] = raiz;
        while (inicio < fim && resultado.getTamanho() < n) {
            NoSplay no = fila[inicio++];
            resultado.adicionar(no);
            if (no.getEsquerda() != null)
                fila[fim++] = no.getEsquerda();
            if (no.getDireita()  != null)
                fila[fim++] = no.getDireita();
        }
        return resultado;
    }
}
