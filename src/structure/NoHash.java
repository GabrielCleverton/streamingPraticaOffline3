package structure;

public class NoHash {
    private int id;
    private NoLista referencia;
    private NoHash  proximo;

    public NoHash(int id, NoLista referencia) {
        this.id         = id;
        this.referencia = referencia;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NoLista getReferencia() {
        return referencia;
    }

    public void setReferencia(NoLista referencia) {
        this.referencia = referencia;
    }

    public NoHash getProximo() {
        return proximo;
    }

    public void setProximo(NoHash proximo) {
        this.proximo = proximo;
    }
}
