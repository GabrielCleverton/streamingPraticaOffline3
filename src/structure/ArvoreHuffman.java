package structure;

public class ArvoreHuffman {

    public static class No implements Comparable<No> {
        public char caractere;
        public int frequencia;
        public No esquerda;
        public No direita;
        public boolean folha;

        public No(char c, int freq) {
            this.caractere  = c;
            this.frequencia = freq;
            this.folha      = true;
        }

        public No(No esq, No dir) {
            this.frequencia = esq.frequencia + dir.frequencia;
            this.esquerda   = esq;
            this.direita    = dir;
            this.folha      = false;
        }

        @Override
        public int compareTo(No outro) {
            return Integer.compare(this.frequencia, outro.frequencia);
        }
    }

    private static class MinHeap {
        private No[] dados;
        private int tamanho;

        public MinHeap(int capacidade) {
            this.dados = new No[capacidade];
            this.tamanho = 0;
        }

        public int getTamanho() {
            return tamanho;
        }

        public void inserir(No no) {
            dados[tamanho] = no;
            subir(tamanho);
            tamanho++;
        }

        public No extrairMinimo() {
            if (tamanho == 0) return null;
            No minimo = dados[0];
            dados[0] = dados[tamanho - 1];
            tamanho--;
            descer(0);
            return minimo;
        }

        private void subir(int i) {
            int pai = (i - 1) / 2;
            while (i > 0 && dados[i].frequencia < dados[pai].frequencia) {
                No temp = dados[i];
                dados[i] = dados[pai];
                dados[pai] = temp;
                i = pai;
                pai = (i - 1) / 2;
            }
        }

        private void descer(int i) {
            int esq = 2 * i + 1;
            int dir = 2 * i + 2;
            int menor = i;

            if (esq < tamanho && dados[esq].frequencia < dados[menor].frequencia) {
                menor = esq;
            }
            if (dir < tamanho && dados[dir].frequencia < dados[menor].frequencia) {
                menor = dir;
            }

            if (menor != i) {
                No temp = dados[i];
                dados[i] = dados[menor];
                dados[menor] = temp;
                descer(menor);
            }
        }
    }


    private No raiz;
    private char[] tabelaChaves;
    private String[] tabelaCodigos;
    private int tamanhoTabela;

    public ArvoreHuffman(String mensagem) {
        if (mensagem == null || mensagem.isEmpty()) {
            return;
        }

        char[] caracteresUnicos = new char[mensagem.length()];
        int[] frequencias = new int[mensagem.length()];
        int totalUnicos = 0;

        for (int i = 0; i < mensagem.length(); i++) {
            char c = mensagem.charAt(i);
            int idx = -1;
            for (int j = 0; j < totalUnicos; j++) {
                if (caracteresUnicos[j] == c) {
                    idx = j;
                    break;
                }
            }
            if (idx != -1) {
                frequencias[idx]++;
            } else {
                caracteresUnicos[totalUnicos] = c;
                frequencias[totalUnicos] = 1;
                totalUnicos++;
            }
        }

        if (totalUnicos == 1) {
            char c = caracteresUnicos[0];
            this.raiz = new No(c, frequencias[0]);
            this.tabelaChaves = new char[]{c};
            this.tabelaCodigos = new String[]{"0"};
            this.tamanhoTabela = 1;
            return;
        }

        MinHeap heap = new MinHeap(totalUnicos);
        for (int i = 0; i < totalUnicos; i++) {
            heap.inserir(new No(caracteresUnicos[i], frequencias[i]));
        }

        while (heap.getTamanho() > 1) {
            No esq = heap.extrairMinimo();
            No dir = heap.extrairMinimo();
            No pai = new No(esq, dir);
            heap.inserir(pai);
        }
        this.raiz = heap.extrairMinimo();

        this.tabelaChaves = new char[totalUnicos];
        this.tabelaCodigos = new String[totalUnicos];
        this.tamanhoTabela = 0;
        gerarCodigos(this.raiz, "");
    }

    private void gerarCodigos(No no, String prefixo) {
        if (no == null) return;
        if (no.folha) {
            tabelaChaves[tamanhoTabela] = no.caractere;
            tabelaCodigos[tamanhoTabela] = prefixo.isEmpty() ? "0" : prefixo;
            tamanhoTabela++;
            return;
        }
        gerarCodigos(no.esquerda, prefixo + "0");
        gerarCodigos(no.direita, prefixo + "1");
    }

    public String comprimir(String mensagem) {
        if (mensagem == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mensagem.length(); i++) {
            char c = mensagem.charAt(i);
            for (int j = 0; j < tamanhoTabela; j++) {
                if (tabelaChaves[j] == c) {
                    sb.append(tabelaCodigos[j]);
                    break;
                }
            }
        }
        return sb.toString();
    }

    public String descomprimir(String bits) {
        if (raiz == null || bits == null || bits.isEmpty()) return "";
        StringBuilder res = new StringBuilder();
        if (raiz.folha) {
            for (int i = 0; i < bits.length(); i++) res.append(raiz.caractere);
            return res.toString();
        }
        No cur = raiz;
        for (int i = 0; i < bits.length(); i++) {
            char bit = bits.charAt(i);
            cur = (bit == '0') ? cur.esquerda : cur.direita;
            if (cur.folha) {
                res.append(cur.caractere);
                cur = raiz;
            }
        }
        return res.toString();
    }

    public ResultadoCompressao analisar(String original) {
        String bits = comprimir(original);
        int bitOr  = original.length() * 8;
        int bitCom = bits.length();
        double taxa = bitOr == 0 ? 0 : 100.0 * (1.0 - (double) bitCom / bitOr);
        return new ResultadoCompressao(original, original.length(), bitOr, bits, bitCom, taxa);
    }

    public static class ResultadoCompressao {
        public final String mensagem;
        public final int chars;
        public final int bitsOriginais;
        public final String bits;
        public final int bitsComprimidos;
        public final double taxa;

        public ResultadoCompressao(String mensagem, int chars, int bitsOr, String bits, int bitsCom, double taxa) {
            this.mensagem = mensagem;
            this.chars = chars;
            this.bitsOriginais = bitsOr;
            this.bits = bits;
            this.bitsComprimidos = bitsCom;
            this.taxa = taxa;
        }
    }
}
