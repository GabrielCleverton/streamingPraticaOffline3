package system;

import model.Filme;
import structure.*;

public class Cliente {
    private String nome;
    private TabelaHash hashCache;
    private ListaDupla listaLRU;
    private int tamanhoCache = 0;
    private static final int CAPACIDADE_CACHE = 50;
    private ArvoreSplay splayFilmes;
    private ArvoreSplay splayCategorias;
    private ListaEncadeada<Filme> removidos;
    private int hits = 0, misses = 0, evictions = 0;
    private Servidor servidor;

    public Cliente(String nome, Servidor servidor) {
        this.nome = nome;
        this.servidor = servidor;
        this.hashCache = new TabelaHash();
        this.listaLRU  = new ListaDupla();
        this.splayFilmes  = new ArvoreSplay();
        this.splayCategorias = new ArvoreSplay();
        this.removidos  = new ListaEncadeada<Filme>();
    }

    public void preCarregarCache(Filme filme) {
        if (hashCache.buscar(filme.getId(), null) != null)
            return;
        if (tamanhoCache >= CAPACIDADE_CACHE)
            evict();
        listaLRU.inserirNoInicio(filme);
        hashCache.inserir(filme.getId(), new NoLista(filme));
        tamanhoCache++;
    }

    public Cliente.ResultadoConsulta buscar(int id) {
        NoLista noCache = hashCache.buscar(id, null);
        if (noCache != null) {
            hits++;
            Filme f = noCache.getFilme();
            moverParaInicioLRU(f);
            atualizarSplay(f);
            return new ResultadoConsulta(id, f, true, 0, 0, true);
        }

        misses++;

        String requisicaoOriginal = "GET /filme/" + id;
        String bitsResposta = servidor.processarRequisicaoComprimida(requisicaoOriginal);

        ArvoreHuffman huffmanCliente = new ArvoreHuffman(servidor.getUltimaMensagemCrua());
        String respostaDescomprimida = huffmanCliente.descomprimir(bitsResposta);

        Filme filmeRetornado = null;
        if (respostaDescomprimida.startsWith("FILME:")) {
            filmeRetornado = servidor.buscarFilmePorId(id);
            if (filmeRetornado != null) {
                inserirNoCache(filmeRetornado);
                atualizarSplay(filmeRetornado);
            }
        }

        return new ResultadoConsulta(id, filmeRetornado, false, servidor.getUltimasComparacoes(), 0, servidor.isIndexacaoHabilitada());
    }

    private void inserirNoCache(Filme filme) {
        if (hashCache.buscar(filme.getId(), null) != null) {
            moverParaInicioLRU(filme);
            return;
        }
        if (tamanhoCache >= CAPACIDADE_CACHE) evict();
        listaLRU.inserirNoInicio(filme);
        hashCache.inserir(filme.getId(), new NoLista(filme));
        tamanhoCache++;
    }

    private void moverParaInicioLRU(Filme filme) {
        NoDuplo cur = listaLRU.getCabeca();
        while (cur != null) {
            if (cur.getFilme().getId() == filme.getId()) {
                listaLRU.moverParaInicio(cur);
                return;
            }
            cur = cur.getProximo();
        }
    }

    private void evict() {
        NoDuplo cauda = listaLRU.getCauda();
        if (cauda == null) return;
        Filme removido = cauda.getFilme();
        listaLRU.removerNo(cauda);
        hashCache.remover(removido.getId());
        removidos.adicionar(removido);
        tamanhoCache--;
        evictions++;
    }

    public void removerDoCache(int id) {
        NoDuplo cur = listaLRU.getCabeca();
        while (cur != null) {
            if (cur.getFilme().getId() == id) {
                listaLRU.removerNo(cur);
                hashCache.remover(id);
                tamanhoCache--;
                return;
            }
            cur = cur.getProximo();
        }
    }

    private void atualizarSplay(Filme f) {
        splayFilmes.inserirOuAtualizar(f.getId(), f);
        splayCategorias.inserirOuAtualizarCategoria(f.getCategoria().name());
    }

    public ListaEncadeada<Filme> recomendarFilmes() {
        ListaEncadeada<Filme> rec = new ListaEncadeada<Filme>();
        ListaEncadeada<NoSplay> topSplay = splayFilmes.topN(5);

        for (int i = 0; i < topSplay.getTamanho(); i++) {
            NoSplay no = topSplay.obter(i);
            if (no != null && no.getFilme() != null) {
                rec.adicionar(no.getFilme());
            }
        }
        return rec;
    }

    public String categoriaFavorita() {
        ListaEncadeada<NoSplay> topCategorias = splayCategorias.topN(10);
        if (topCategorias == null || topCategorias.vazia()) {
            return "Nenhuma";
        }

        NoSplay favorita = topCategorias.obter(0);
        for (int i = 0; i < topCategorias.getTamanho(); i++) {
            NoSplay no = topCategorias.obter(i);
            if (no != null && no.getAcessos() > favorita.getAcessos()) {
                favorita = no;
            }
        }
        return favorita.getCategoria() + " (Acessos: " + favorita.getAcessos() + ")";
    }

    public ListaEncadeada<Filme> top10Recentes() {
        ListaEncadeada<Filme> recentes = new ListaEncadeada<Filme>();
        NoDuplo atual = listaLRU.getCabeca();
        int contador = 0;

        while (atual != null && contador < 10) {
            recentes.adicionar(atual.getFilme());
            atual = atual.getProximo();
            contador++;
        }
        return recentes;
    }


    public String getNome() { return nome; }
    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    public int getEvictions() { return evictions; }
    public ListaEncadeada<Filme> getRemovidosCache() { return removidos; }
    public ArvoreSplay getSplayFilmes() { return splayFilmes; }

    public double getTaxaHit() {
        int t = hits + misses;

        return t == 0 ? 0.0 : 100.0 * hits / t;
    }

    public static class ResultadoConsulta {
        public final int id;
        public final Filme filme;
        public final boolean cacheHit;
        public final int     comparacoes;
        public final long    tempoNs;
        public final boolean usouHash;

        public ResultadoConsulta(int id, Filme filme, boolean hit, int comp, long ns, boolean usouHash) {
            this.id = id; this.filme = filme;
            this.cacheHit = hit;
            this.comparacoes = comp;
            this.tempoNs = ns;
            this.usouHash = usouHash;
        }
        public boolean encontrado() {
            return filme != null;
        }
    }
}
