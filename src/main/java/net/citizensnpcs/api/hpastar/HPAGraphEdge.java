package net.citizensnpcs.api.hpastar;

/*
 * ChunkSnapshot cs = ((Player)
 * sender).getLocation().getChunk().getChunkSnapshot(); int c = 0; String s =
 * ""; for (int i = 79; i < 79 + 16; i++) { for (int j = 0; j < 16; j++) { for
 * (int k = 0; k < 16; k++) { c++; s += (j + " " + i + " " + k + " " +
 * cs.getBlockType(j, i, k)) + '\n'; } } } File f = new
 * File(CitizensAPI.getDataFolder(), "test.txt"); try { FileWriter wr = new
 * FileWriter(f); wr.write(s); wr.close(); } catch (IOException e) {
 * e.printStackTrace(); }
 *
 * System.out.println(new File(CitizensAPI.getDataFolder(), "test.txt"));
 * System.out.println(c);
 */
public class HPAGraphEdge {
    final HPAGraphNode from;
    final HPAGraphNode to;
    final HPAGraphEdge.EdgeType type;
    final float weight;

    public HPAGraphEdge(HPAGraphNode from, HPAGraphNode to, HPAGraphEdge.EdgeType type, float weight) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.weight = weight;
    }

    public enum EdgeType {
        INTER,
        INTRA;
    }
}