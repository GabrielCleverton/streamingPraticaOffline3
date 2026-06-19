package structure;

public class TabelaHash {
    private static final double PHI = (Math.sqrt(5.0) - 1.0) / 2.0;
    private int M = 1009;
    private NoHash[] tabela = new NoHash[M];

    public int hash(int id) {
        long k = ((long) id) & 0xFFFFFFFFL;
        double f = (k * PHI) % 1.0;
        return (int) Math.floor(M * f);
    }

    public void inserir(int id, NoLista referencia) {
        int h  = hash(id);
        NoHash no = tabela[h];
        while (no != null) {
            if (no.getId() == id) return;
            no = no.getProximo();
        }
        NoHash novo = new NoHash(id, referencia);
        novo.setProximo(tabela[h]);
        tabela[h] = novo;
    }

    public NoLista buscar(int id, int[] contadorRef) {
        int h = hash(id);
        NoHash prev = null;
        NoHash cur = tabela[h];
        int comp = 0;

        while (cur != null) {
            comp++;
            if (cur.getId() == id) {
                if (prev != null) {
                    prev.setProximo(cur.getProximo());
                    cur.setProximo(tabela[h]);
                    tabela[h] = cur;
                }
                if (contadorRef != null) contadorRef[0] = comp;
                return cur.getReferencia();
            }
            prev = cur;
            cur = cur.getProximo();
        }
        if (contadorRef != null) contadorRef[0] = comp;
        return null;
    }

    public boolean remover(int id) {
        int h = hash(id);
        NoHash prev = null;
        NoHash cur  = tabela[h];
        while (cur != null) {
            if (cur.getId() == id) {
                if (prev == null)
                    tabela[h] = cur.getProximo();
                else
                    prev.setProximo(cur.getProximo());
                return true;
            }
            prev = cur;
            cur  = cur.getProximo();
        }
        return false;
    }
}
