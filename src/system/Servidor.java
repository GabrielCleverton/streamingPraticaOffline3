package system;

import model.Categoria;
import model.Filme;
import structure.*;

public class Servidor {
    private ListaLigada     listaGeral;
    private TabelaHash      hash;
    private ListaLigada[]   listasPorCategoria;
    private ArvoreSplay     splayPopularidade;
    private boolean indexacaoHabilitada = true;

    private String ultimaMensagemCrua = "";
    private int ultimasComparacoes = 0;

    public Servidor() {
        listaGeral          = new ListaLigada();
        hash                = new TabelaHash();
        splayPopularidade   = new ArvoreSplay();
        listasPorCategoria  = new ListaLigada[Categoria.values().length];
        for (int i = 0; i < listasPorCategoria.length; i++)
            listasPorCategoria[i] = new ListaLigada();
    }

    public void inserirFilme(Filme f) {
        NoLista no = listaGeral.inserir(f);
        listasPorCategoria[f.getCategoria().ordinal()].inserir(f);
        hash.inserir(f.getId(), no);
    }

    public String processarRequisicaoComprimida(String msgOriginal) {
        ArvoreHuffman huffmanRede = new ArvoreHuffman(msgOriginal);
        String bitsTransmitidos = huffmanRede.comprimir(msgOriginal);
        String msgDescomprimida = huffmanRede.descomprimir(bitsTransmitidos);

        String respostaCrua = "FILME_NAO_ENCONTRADO";
        int[] contador = {0};

        if (msgDescomprimida.startsWith("GET /filme/")) {
            try {
                int id = Integer.parseInt(msgDescomprimida.replace("GET /filme/", "").trim());
                Filme f = indexacaoHabilitada ? buscarComHash(id, contador) : buscarSemHash(id, contador);

                if (f != null) {
                    respostaCrua = f.toMensagem();
                    splayPopularidade.inserirOuAtualizar(id, f);
                }
            } catch (Exception e) {
                respostaCrua = "ERR:INVALID_ID";
            }
        }

        this.ultimasComparacoes = contador[0];
        this.ultimaMensagemCrua = respostaCrua;

        ArvoreHuffman huffmanResposta = new ArvoreHuffman(respostaCrua);
        return huffmanResposta.comprimir(respostaCrua);
    }

    private Filme buscarComHash(int id, int[] contador) {
        NoLista no = hash.buscar(id, contador);
        return no != null ? no.getFilme() : null;
    }

    private Filme buscarSemHash(int id, int[] contador) {
        return listaGeral.buscaLinear(id, contador);
    }

    public Filme buscarPorNome(String nome){
        return listaGeral.buscarPorNome(nome);
    }
    public Filme buscarFilmePorId(int id){
        return buscarComHash(id, new int[]{0});
    }
    public ListaLigada getListaPorCategoria(Categoria cat){
        return listasPorCategoria[cat.ordinal()];
    }

    public ListaEncadeada<Filme> top10Populares() {
        ListaEncadeada<Filme> lista = new ListaEncadeada<>();
        ListaEncadeada<NoSplay> topSplay = splayPopularidade.topN(10);
        for (int i = 0; i < topSplay.getTamanho(); i++) {
            if (topSplay.obter(i) != null && topSplay.obter(i).getFilme() != null) {
                lista.adicionar(topSplay.obter(i).getFilme());
            }
        }
        return lista;
    }

    public void habilitarIndexacao(boolean b){
        this.indexacaoHabilitada = b;
    }
    public boolean isIndexacaoHabilitada(){
        return indexacaoHabilitada;
    }
    public ArvoreSplay getSplayPopularidade(){
        return splayPopularidade;
    }
    public String getUltimaMensagemCrua(){
        return ultimaMensagemCrua;
    }
    public int getUltimasComparacoes(){
        return ultimasComparacoes;
    }
}
