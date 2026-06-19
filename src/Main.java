import model.Categoria;
import model.Filme;
import structure.*;
import system.Cliente;
import system.Servidor;

import java.util.Scanner;

public class Main {
    static Servidor      servidor;
    static ListaEncadeada<Cliente> clientes;
    static ListaEncadeada<Filme>   catalogo;
    static Scanner       scanner;
    static ListaEncadeada<ArvoreHuffman.ResultadoCompressao> historicoHuffman = new ListaEncadeada<>();

    static int[] idsCache;
    static int[] idsFora;
    static final int[] IDS_INVALIDOS = {9998, 9999};

    static ListaEncadeada<Integer> compSemHash = new ListaEncadeada<>();
    static ListaEncadeada<Integer> compComHash = new ListaEncadeada<>();

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        inicializar();
        exibirInterfaceCliente();
        executarConsultas();
        gerarRelatorios();
        scanner.close();
    }

    static void inicializar() {
        sep('═', "INICIANDO SISTEMA DE STREAMING");
        servidor = new Servidor();
        catalogo = gerarFilmesReais();

        for (int i = 0; i < catalogo.getTamanho(); i++) {
            servidor.inserirFilme(catalogo.obter(i));
        }
        System.out.println("✔ Servidor: " + catalogo.getTamanho() + " filmes reais carregados no banco.");

        clientes = new ListaEncadeada<>();
        clientes.adicionar(new Cliente("Alice", servidor));
        clientes.adicionar(new Cliente("Bruno", servidor));
        clientes.adicionar(new Cliente("Carla", servidor));

        idsCache = new int[50];
        idsFora  = new int[12];

        for (int i = 0; i < 50; i++) {
            idsCache[i] = catalogo.obter(catalogo.getTamanho() - 1 - i).getId();
        }
        for (int i = 0; i < 12; i++) {
            idsFora[i] = catalogo.obter(i).getId();
        }

        for (int i = 0; i < clientes.getTamanho(); i++) {
            Cliente c = clientes.obter(i);
            for (int j = 0; j < 50; j++) {
                c.preCarregarCache(catalogo.obter(catalogo.getTamanho() - 1 - j));
            }
            capturarDadosCompressao("LOGIN_OK:" + c.getNome());
        }
        System.out.println("✔ Caches locais configurados com 50 filmes iniciais.");
    }

    static void exibirInterfaceCliente() {
        sep('═', "INTERFACE DO CLIENTE INTERATIVA");
        Categoria[] cats = Categoria.values();
        Cliente clienteAtivo = clientes.obter(0);

        boolean continuar = true;
        while (continuar) {
            System.out.println("\nMenu de Navegação (" + clienteAtivo.getNome() + "):");
            for (int i = 0; i < cats.length; i++) {
                System.out.printf("  [%2d] Categoria: %s%n", i + 1, cats[i].name());
            }
            System.out.println("  [ 0] Buscar filme diretamente por NOME");
            System.out.println("  [-1] Sair da interface e rodar bateria de testes automáticos");
            System.out.print("\nEscolha uma opção: ");

            int opcao = lerInt();

            if (opcao == -1) {
                continuar = false;
            } else if (opcao == 0) {
                System.out.print("Digite o nome do filme (Ex: Die Hard, Inception, Shrek...): ");
                String nome = scanner.nextLine().trim();
                Filme f = servidor.buscarPorNome(nome);
                if (f != null) {
                    processarFluxoFilmeInterativo(clienteAtivo, f.getId());
                } else {
                    System.out.println("✘ Filme não localizado na base geral.");
                }
            } else if (opcao >= 1 && opcao <= cats.length) {
                Categoria catSelecionada = cats[opcao - 1];
                ListaLigada lista = servidor.getListaPorCategoria(catSelecionada);
                NoLista cur = lista.getCabeca();

                System.out.println("\n── Filmes Disponíveis em " + catSelecionada.name() + " ──");
                int exibidos = 0;
                while (cur != null && exibidos < 10) {
                    System.out.printf("  • %s (%d)%n", cur.getFilme().getNome(), cur.getFilme().getAno());
                    cur = cur.getProximo();
                    exibidos++;
                }

                System.out.print("\nDigite o NOME do filme que deseja assistir (ou pressione Enter para voltar): ");
                String nomeFilmeEscolhido = scanner.nextLine().trim();
                if (!nomeFilmeEscolhido.isEmpty()) {
                    Filme f = servidor.buscarPorNome(nomeFilmeEscolhido);
                    if (f != null && f.getCategoria() == catSelecionada) {
                        processarFluxoFilmeInterativo(clienteAtivo, f.getId());
                    } else {
                        System.out.println("✘ Título não encontrado ou não pertence a esta categoria.");
                    }
                }
            } else {
                System.out.println("Opção inválida.");
            }
        }
    }

    static void processarFluxoFilmeInterativo(Cliente c, int id) {
        System.out.println();
        imprimirLogHuffman("GET /filme/" + id);
        imprimirLogHuffman("HASH_GET:" + id);

        Cliente.ResultadoConsulta r = c.buscar(id);

        if (r.encontrado()) {
            imprimirLogHuffman("HASH_RESP:" + id + "|" + r.filme.getNome());
            imprimirLogHuffman("SPLAY_INS:" + id + "|" + r.filme.getNome());

            System.out.println("\n✔ Filme encontrado:");
            System.out.println("----------------------------------------");
            System.out.println(r.filme.toStringCompleto());
            System.out.println("----------------------------------------");

            System.out.println("\nRecomendado para você (Baseado no seu perfil de " + r.filme.getCategoria().name() + "):");
            System.out.println("----------------------------------------");

            ListaEncadeada<Filme> recs = c.recomendarFilmes();
            Filme recomendado = null;

            for (int i = 0; i < recs.getTamanho(); i++) {
                Filme f = recs.obter(i);
                if (f.getId() != id) {
                    recomendado = f;
                    break;
                }
            }

            if (recomendado == null) {
                ListaLigada listaCat = servidor.getListaPorCategoria(r.filme.getCategoria());
                NoLista no = listaCat.getCabeca();
                while (no != null) {
                    if (no.getFilme().getId() != id) {
                        recomendado = no.getFilme();
                        break;
                    }
                    no = no.getProximo();
                }
            }

            if (recomendado != null) {
                System.out.println(recomendado.toStringCompleto());
            } else {
                System.out.println("  [Nenhuma outra recomendação disponível nesta categoria ainda.]");
            }
            System.out.println("----------------------------------------");

            imprimirLogHuffman(r.filme.toMensagem());
            System.out.println("\n[Enter] para voltar...");
            scanner.nextLine();
        } else {
            System.out.println("✘ Filme não disponível ou ID inválido.");
        }
    }

    static void imprimirLogHuffman(String msg) {
        ArvoreHuffman h = new ArvoreHuffman(msg);
        ArvoreHuffman.ResultadoCompressao res = h.analisar(msg);
        historicoHuffman.adicionar(res);

        System.out.println("--------- Transmissão Huffman ---------");
        System.out.printf("Original   : %s%n", res.mensagem);
        System.out.printf("Tamanho    : %d bits (%d chars)%n", res.bitsOriginais, res.chars);
        System.out.printf("Comprimido : %d bits%n", res.bitsComprimidos);
        System.out.printf("Compressão : %.2f%% de redução%n", res.taxa);
        System.out.println("----------------------------------------");

        exibirDetalhesHuffman(res);
    }

    static void exibirDetalhesHuffman(ArvoreHuffman.ResultadoCompressao res) {
        System.out.println("\n┌────────────────────────────────────────────────────────┐");
        System.out.println("│          PROPRIEDADES DA ESTRUTURA DE HUFFMAN          │");
        System.out.println("├────────────────────────────────────────────────────────┤");
        System.out.printf("│ • Mensagem Original   : \"%s\"%n", res.mensagem.length() > 30 ? res.mensagem.substring(0, 27) + "..." : res.mensagem);
        System.out.printf("│ • Fila de Prioridades : Min Heap Nativo [Custo O(log N)]│%n");
        System.out.printf("│ • Caracteres Totais   : %-31d│%n", res.chars);
        System.out.printf("│ • Tamanho Original    : %-5d bytes (%-5d bits)       │%n", res.chars, res.bitsOriginais);
        System.out.printf("│ • Tamanho Comprimido  : %-5d bits                       │%n", res.bitsComprimidos);
        System.out.printf("│ • Taxa de Compressão  : %-31.2f%%│%n", res.taxa);
        System.out.printf("│ • Eficiência Líquida  : Redução de %-20.2f%%│%n", res.taxa);
        System.out.println("└────────────────────────────────────────────────────────┘");
    }

    static void executarConsultas() {
        sep('═', "EXECUTANDO BATERIA DE 20 CONSULTAS AUTOMÁTICAS POR CLIENTE");
        for (int i = 0; i < clientes.getTamanho(); i++) {
            Cliente c = clientes.obter(i);
            System.out.println("\n>>>> Rodando Bloco de Testes para: " + c.getNome() + " <<<<");
            int n = 0;

            System.out.println("\n[BLOCO 1] 2 Consultas Inválidas (IDs Inexistentes):");
            servidor.habilitarIndexacao(true);
            for (int id : IDS_INVALIDOS) {
                Cliente.ResultadoConsulta r = c.buscar(id);
                imprimirLinhaTabela(++n, "INVÁLIDA", id, r, servidor.getUltimasComparacoes());
            }

            System.out.println("\n[BLOCO 2] 6 Consultas com Cache HIT (Filmes pré-carregados):");
            for (int j = 0; j < 6; j++) {
                Cliente.ResultadoConsulta r = c.buscar(idsCache[j]);
                imprimirLinhaTabela(++n, "CACHE HIT", idsCache[j], r, 0);
            }

            System.out.println("\n[BLOCO 3] 6 Consultas Sem Indexação (Busca Linear Sequencial no Servidor):");
            servidor.habilitarIndexacao(false);
            for (int j = 0; j < 6; j++) {
                int id = idsFora[j];
                Cliente.ResultadoConsulta r = c.buscar(id);
                compSemHash.adicionar(servidor.getUltimasComparacoes());
                imprimirLinhaTabela(++n, "SEM HASH ", id, r, servidor.getUltimasComparacoes());
            }

            System.out.println("\n[BLOCO 4] 6 Consultas Com Indexação (Uso da Tabela Hash do Servidor):");
            servidor.habilitarIndexacao(true);
            for (int j = 0; j < 6; j++) c.removerDoCache(idsFora[j]);
            for (int j = 0; j < 6; j++) {
                int id = idsFora[j];
                Cliente.ResultadoConsulta r = c.buscar(id);
                compComHash.adicionar(servidor.getUltimasComparacoes());
                imprimirLinhaTabela(++n, "COM HASH ", id, r, servidor.getUltimasComparacoes());
            }
        }
    }

    static void imprimirLinhaTabela(int n, String tipo, int id, Cliente.ResultadoConsulta r, int comps) {
        String titulo = r.encontrado() ? r.filme.getNome() : "[NÃO ENCONTRADO]";
        System.out.printf("  #%02d [%s] ID=%-4d %-30s comparações=%d%n", n, tipo, id, titulo, comps);
    }

    static int lerInt() {
        try { return Integer.parseInt(scanner.nextLine().trim()); }
        catch (NumberFormatException e) { return -99; }
    }

    static void capturarDadosCompressao(String msg) {
        if(msg == null || msg.isEmpty()) return;
        ArvoreHuffman h = new ArvoreHuffman(msg);
        historicoHuffman.adicionar(h.analisar(msg));
    }

    static void gerarRelatorios() {
        relatorioLRU();
        relatorioSplayClientes();
        relatorioSplayServidor();
        relatorioHuffman();
    }

    static void relatorioLRU() {
        sep('═', "ANÁLISE DO CACHE LRU");
        for (int i = 0; i < clientes.getTamanho(); i++) {
            Cliente c = clientes.obter(i);
            System.out.println("\nCliente: " + c.getNome());
            System.out.printf("  Hits: %d | Misses: %d | Taxa: %.1f%%%n", c.getHits(), c.getMisses(), c.getTaxaHit());
            System.out.println("  Últimos 10 gravados no Cache (MRU -> LRU):");
            ListaEncadeada<Filme> rec = c.top10Recentes();
            for (int j = 0; j < rec.getTamanho(); j++) System.out.println("    " + (j+1) + ". " + rec.obter(j));

            if(!c.getRemovidosCache().vazia()) {
                System.out.println("  Elementos removidos por estouro de capacidade (Evicted):");
                for(int j = 0; j < c.getRemovidosCache().getTamanho(); j++) {
                    System.out.println("    x " + c.getRemovidosCache().obter(j).getNome());
                }
            }
        }
    }

    static void relatorioSplayClientes() {
        sep('═', "SPLAY DE PREFERÊNCIAS (CLIENTES)");
        for (int i = 0; i < clientes.getTamanho(); i++) {
            Cliente c = clientes.obter(i);
            System.out.println("\nPerfil: " + c.getNome());
            if (c.getSplayFilmes().vazia()) { System.out.println("  Sem registros de acesso na árvore splay."); continue; }
            System.out.println("  Raiz Atual: " + c.getSplayFilmes().getRaiz().getFilme().getNome());
            System.out.println("  Categoria Mais Consumida: " + c.categoriaFavorita());
        }
    }

    static void relatorioSplayServidor() {
        sep('═', "POPULARIDADE GLOBAL (SERVIDOR)");
        ArvoreSplay splay = servidor.getSplayPopularidade();
        if (splay.vazia()) { System.out.println("Sem acessos globais computados."); return; }
        System.out.println("Filme no Topo da Raiz Global: " + splay.getRaiz().getFilme().getNome());
        System.out.println("\nTop 10 Populares:");
        ListaEncadeada<Filme> top = servidor.top10Populares();
        for(int i=0; i<top.getTamanho(); i++) {
            if(top.obter(i) != null) System.out.println("  " + (i+1) + ". " + top.obter(i).getNome());
        }
    }

    static void relatorioHuffman() {
        sep('═', "RELATÓRIO MÉTRICO DE TRANSMISSÃO (HUFFMAN)");
        long totalO = 0, totalC = 0;
        for (int i = 0; i < historicoHuffman.getTamanho(); i++) {
            ArvoreHuffman.ResultadoCompressao r = historicoHuffman.obter(i);
            totalO += r.bitsOriginais;
            totalC += r.bitsComprimidos;
        }
        double tx = totalO == 0 ? 0 : 100.0 * (1.0 - (double) totalC / totalO);
        System.out.printf("Bits Originais Trafegados : %d bits%n", totalO);
        System.out.printf("Bits Comprimidos Enviados  : %d bits%n", totalC);
        System.out.printf("Taxa Líquida de Economia   : %.2f%%%n", tx);
        sep('═', "FIM DA SIMULAÇÃO — PRÁTICA OFFLINE 3");
    }

    static ListaEncadeada<Filme> gerarFilmesReais() {
        ListaEncadeada<Filme> lista = new ListaEncadeada<>();
        Categoria[] cats = Categoria.values();

        String[][][] producoesReais = {
                // AÇÃO
                {{"Die Hard", "1988", "Off-duty cop thwarts a terrorist takeover of a Los Angeles skyscraper."},
                        {"The Gray Man", "2022", "A globe-trotting CIA operative becomes the target of a ruthless mercenary."},
                        {"Mad Max: Fury Road", "2015", "In a post-apocalyptic wasteland, a woman rebels against a tyrannical ruler."},
                        {"John Wick", "2014", "An ex-hit-man comes out of retirement to track down the gangsters that took everything."},
                        {"Gladiator", "2000", "A former Roman General sets out to exact vengeance against the corrupt emperor."}},
                // COMÉDIA
                {{"Superbad", "2007", "Two co-dependent high school seniors navigate a chaotic night trying to buy alcohol."},
                        {"The Hangover", "2009", "Three buddies wake up from a bachelor party in Las Vegas with no memory."},
                        {"Dumb and Dumber", "1994", "Two well-meaning but incredibly stupid friends travel cross-country."},
                        {"White Chicks", "2004", "Two FBI agents go undercover as high-society blondes to foil a kidnapping plot."},
                        {"The Mask", "1994", "Bank clerk Stanley Ipkiss transforms into a manic superhero when he wears a mask."}},
                // DRAMA
                {{"Forrest Gump", "1994", "The history of the United States unfolds through the perspective of an Alabama man."},
                        {"The Godfather", "1972", "The aging patriarch of an organized crime dynasty transfers control to his reluctant son."},
                        {"The Green Mile", "1999", "Guards at a penitentiary death row affect a psychological bond with an inmate."},
                        {"The Shawshank Redemption", "1994", "Over the course of several years, two convicts form a friendship, seeking solace."},
                        {"Schindler's List", "1993", "In German-occupied Poland during WWII, industrialist Oskar Schindler saves Jewish workers."}},
                // TERROR
                {{"The Conjuring", "2013", "Paranormal investigators work to help a family experiencing dark disturbances."},
                        {"Hereditary", "2018", "A grieving family is haunted by tragic occurrences after their grandmother passes."},
                        {"It", "2017", "A group of bullied kids band together to destroy a shape-shifting monster clown."},
                        {"The Exorcist", "1973", "When a young girl is possessed by an entity, her mother seeks help from two priests."},
                        {"A Nightmare on Elm Street", "1984", "The spirit of a slain child murderer seeks revenge by invading dreams."}},
                // FICÇÃO CIENTÍFICA
                {{"Inception", "2010", "A thief who steals corporate secrets through dream-sharing is given an inverse task."},
                        {"Interstellar", "2014", "A team of explorers travel through a wormhole to ensure humanity's survival."},
                        {"The Matrix", "1999", "A computer hacker learns from mysterious rebels about the true nature of his reality."},
                        {"Blade Runner 2049", "2017", "A new blade runner unearths a long-buried secret that could plunge society into chaos."},
                        {"Avatar", "2009", "A paraplegic Marine on the moon Pandora becomes torn between orders and protecting the world."}},
                // ROMANCE
                {{"Titanic", "1997", "A seventeen-year-old aristocrat falls in love with a kind but poor artist aboard the R.M.S. Titanic."},
                        {"The Notebook", "2004", "An epic love story centered around an older man who reads aloud to a woman."},
                        {"Pride and Prejudice", "2005", "Sparks fly when spirited Elizabeth Bennet meets single, rich, and proud Mr. Darcy."},
                        {"La La Land", "2016", "While navigating their careers in LA, a pianist and an actress fall in love."},
                        {"About Time", "2013", "At the age of 21, Tim discovers he can travel in time and change his own life."}},
                // DOCUMENTÁRIO
                {{"Our Planet", "2019", "Experiencing our planet's natural beauty and examining how climate change impacts creatures."},
                        {"The Last Dance", "2020", "Charting the rise of the 1990s Chicago Bulls, led by Michael Jordan."},
                        {"Free Solo", "2018", "Alex Honnold attempts to conquer the first free solo climb of El Capitan's wall."},
                        {"Social Dilemma", "2020", "This documentary-drama hybrid explores the dangerous human impact of social networking."},
                        {"Formula 1: Drive to Survive", "2019", "Drivers, managers and team owners live life in the fast lane during a cutthroat season."}},
                // ANIMAÇÃO
                {{"Shrek", "2001", "A mean-spirited ogre manages to rescue a beautiful princess for a lord to get his swamp back."},
                        {"The Lion King", "1994", "Lion prince Simba and his father are targeted by his bitter uncle Scar."},
                        {"Toy Story", "1995", "A cowboy doll is profoundly threatened when a new spaceman figure displaces him."},
                        {"Spirited Away", "2001", "A sullen 10-year-old girl wanders into a world ruled by gods, witches, and spirits."},
                        {"Spider-Man: Into the Spider-Verse", "2018", "Teen Miles Morales becomes the Spider-Man of his universe and joins alternatives."}},
                // SUSPENSE
                {{"Shutter Island", "2010", "In 1954, a U.S. Marshal investigates the disappearance of a murderer from an island hospital."},
                        {"The Silence of the Lambs", "1991", "A young F.B.I. cadet must receive help from an incarcerated manipulative cannibal killer."},
                        {"Se7en", "1995", "Two detectives, a rookie and a veteran, hunt a serial killer who uses seven deadly sins."},
                        {"Prisoners", "2013", "When Keller Dover's daughter goes missing, he takes matters into his own hands."},
                        {"Gone Girl", "2014", "With his wife's disappearance the focus of a media circus, a man sees the spotlight turn on him."}},
                // FANTASIA
                {{"The Lord of the Rings: The Fellowship of the Ring", "2001", "A meek Hobbit and companions set out to destroy the One Ring."},
                        {"Harry Potter and the Sorcerer's Stone", "2001", "An orphaned boy enrolls in a school of wizardry, where he learns the truth."},
                        {"The Chronicles of Narnia", "2005", "Four kids travel through a wardrobe to free Narnia with a mystical lion."},
                        {"Pirates of the Caribbean", "2003", "Blacksmith Will Turner teams up with eccentric pirate Captain Jack Sparrow."},
                        {"The Hobbit", "2012", "A reluctant Hobbit sets out to the Lonely Mountain to reclaim a home from a dragon."}}
        };

        int idContador = 1;
        for (int c = 0; c < producoesReais.length; c++) {
            Categoria catAtual = cats[c];
            for (int f = 0; f < producoesReais[c].length; f++) {
                lista.adicionar(new Filme(idContador++, producoesReais[c][f][0], producoesReais[c][f][2], Integer.parseInt(producoesReais[c][f][1]), catAtual));
            }
        }

        int seed = 42;
        while (idContador <= 1000) {
            Categoria catAtual = cats[idContador % cats.length];
            seed = (seed * 1103515245 + 12345) & 0x7fffffff;
            int randSufix = seed % 7;
            String nomeFormatado = "Simulated " + catAtual.name().toLowerCase() + " Production Vol " + idContador + " (V" + randSufix + ")";
            lista.adicionar(new Filme(idContador, nomeFormatado, "Synthetic simulated node verification " + idContador, 1980 + (idContador % 46), catAtual));
            idContador++;
        }

        ListaEncadeada<Filme> listaInvertida = new ListaEncadeada<>();
        for (int i = lista.getTamanho() - 1; i >= 0; i--) {
            listaInvertida.adicionar(lista.obter(i));
        }
        return listaInvertida;
    }

    static void sep(char c, String t) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<65; i++) sb.append(c);
        String linha = sb.toString();
        System.out.println("\n" + linha + "\n  " + t + "\n" + linha);
    }
}