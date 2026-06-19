package structure;

public class ListaEncadeada<T> {

    private static class No<E> {
        E dado;
        No<E> proximo;

        No(E dado) {
            this.dado = dado;
            this.proximo = null;
        }
    }

    private No<T> cabeca;
    private int tamanho;

    public ListaEncadeada() {
        this.cabeca = null;
        this.tamanho = 0;
    }

    public void adicionar(T elemento) {
        No<T> novoNo = new No<>(elemento);
        if (cabeca == null) {
            cabeca = novoNo;
        } else {
            No<T> atual = cabeca;
            while (atual.proximo != null) {
                atual = atual.proximo;
            }
            atual.proximo = novoNo;
        }
        tamanho++;
    }

    public T obter(int indice) {
        if (indice < 0 || indice >= tamanho) {
            return null;
        }
        No<T> atual = cabeca;
        for (int i = 0; i < indice; i++) {
            atual = atual.proximo;
        }
        return atual.dado;
    }

    public int getTamanho() {
        return tamanho;
    }

    public boolean vazia() {
        return tamanho == 0;
    }
}