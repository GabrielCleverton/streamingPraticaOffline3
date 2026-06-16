package model;

public class Filme {
    private int id;
    private String nome;
    private String sinopse;
    private int ano;
    private Categoria categoria;

    public Filme(int id, String nome, String sinopse, int ano, Categoria categoria){
        this.id = id;
        this.nome = nome;
        this.sinopse = sinopse;
        this.ano = ano;
        this.categoria = categoria;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String toMensagem() {
        return "FILME:" + id + "|" + nome + "|" + ano + "|" + categoria.name();
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nome + " (" + ano + ") | " + categoria.name();
    }

    public String toStringCompleto() {
        return "ID       : " + id        + "\n" +
                "Nome     : " + nome      + "\n" +
                "Categoria: " + categoria + "\n" +
                "Ano      : " + ano       + "\n" +
                "Sinopse  : " + sinopse;
    }
}
