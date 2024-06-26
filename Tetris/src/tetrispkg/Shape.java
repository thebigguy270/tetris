package tetrispkg;
import java.util.Random;

public class Shape {

    private Tetrominoes pieceShape;
    private int[][] coords;
    private int[][][] coordsTable;

    public Shape() {
        coords = new int[4][2];
        setShape(Tetrominoes.NoShape);
    }

    public void setShape(Tetrominoes shape) {
        coordsTable = new int[][][] {
            { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } },
            { { 0, -1 }, { 0, 0 }, { -1, 0 }, { -1, 1 } },
            { { 0, -1 }, { 0, 0 }, { 1, 0 }, { 1, 1 } },
            { { 0, -1 }, { 0, 0 }, { 0, 1 }, { 0, 2 } },
            { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 0, 1 } },
            { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } },
            { { -1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } },
            { { 1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } }
        };

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; ++j) {
                coords[i][j] = coordsTable[shape.ordinal()][i][j];
            }
        }
        pieceShape = shape;
    }

    private void setX(int index, int x) {
        coords[index][0] = x;
    }

    private void setY(int index, int y) {
        coords[index][1] = y;
    }

    public int x(int index) {
        return coords[index][0];
    }

    public int y(int index) {
        return coords[index][1];
    }

    public Tetrominoes getShape() {
        return pieceShape;
    }

    public void setRandomShape() {
        var r = new Random();
        int x = Math.abs(r.nextInt()) % 7 + 1;
        Tetrominoes[] values = Tetrominoes.values();
        setShape(values[x]);
    }

    public int minX() {
        int m = coords[0][0];
        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][0]);
        }
        return m;
    }

    public int minY() {
        int m = coords[0][1];
        for (int i = 0; i < 4; i++) {
            m = Math.min(m, coords[i][1]);
        }
        return m;
    }

    public Shape rotateLeft() {
        if (pieceShape == Tetrominoes.SquareShape) {
            return this;
        }
        var result = new Shape();
        result.pieceShape = pieceShape;
        for (int i = 0; i < 4; i++) {
            result.setX(i, y(i));
            result.setY(i, -x(i));
        }
        return result;
    }

    public Shape rotateRight() {
        if (pieceShape == Tetrominoes.SquareShape) {
            return this;
        }
        var result = new Shape();
        result.pieceShape = pieceShape;
        for (int i = 0; i < 4; i++) {
            result.setX(i, -y(i));
            result.setY(i, x(i));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Shape: ").append(pieceShape).append("\n");
        sb.append("Coordinates:\n");
        for (int i = 0; i < 4; i++) {
            sb.append("(").append(coords[i][0]).append(", ").append(coords[i][1]).append(")\n");
        }
        return sb.toString();
    }
}
