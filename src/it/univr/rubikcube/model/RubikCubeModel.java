package it.univr.rubikcube.model;

import java.util.Observable;

/**
 * Rubik cube data model.
 * @author Alessandro Menti
 */
public class RubikCubeModel extends Observable {
    /**
     * Cube dimension.
     */
    private int dimension;
    /**
     * Array storing the cube configuration.
     * 
     * The array is structured as follows.
     * <ul>
     * <li>The first index is the side index (use
     * <tt>RubikCubeSide.ordinal()</tt> to get the array index corresponding
     * to a side).</li>
     * <li>The second index is the row index (<tt>0</tt> to <tt>getDimension()
     * - 1</tt>).</li>
     * <li>The third index is the column index (<tt>0</tt> to
     * <tt>getDimension() - 1</tt>).</li>
     * </ul>
     * 
     * Rows are numbered progressively from top to bottom and columns are
     * numbered from the left to the right.
     */
    private RubikCubeFaceColor[][][] configuration;
    /**
     * Creates a new instance of a Rubik cube model.
     * @param dim The cube dimension.
     * @throws IllegalArgumentException Thrown if the cube has an unacceptable
     * dimension (less than two).
     */
    public RubikCubeModel(final int dim) throws IllegalArgumentException {
        // Force the data structures to be initialized
        this.dimension = 0;
        this.setDimension(dim);
    }
    /**
     * Gets the dimension of this cube.
     * @return Dimension of the cube.
     */
    public final int getDimension() {
        return this.dimension;
    }
    /**
     * Sets the dimension of this cube. If the value changes, the cube is
     * reinitialized in the standard configuration.
     * @param dim Dimension of the cube.
     * @throws IllegalArgumentException Thrown if the cube has an unacceptable
     * dimension (less than two).
     */
    public final void setDimension(final int dim)
            throws IllegalArgumentException {
        if (dim < 2) {
            throw new IllegalArgumentException("The dimension must be two or"
                                               + " greater.");
        }
        if (dim != this.dimension) {
            // The value has changed, reinitialize the data structures.
            this.dimension = dim;
            notifyObservers(new RubikCubeModelDimensionChanged(dim));
            this.configuration = new RubikCubeFaceColor[RubikCubeSide.values()
                                                        .length][dim][dim];
            this.resetToStandardConfiguration();
        }
    }
    /**
     * Gets the color of a face.
     * @param s Side of the cube.
     * @param x X coordinate of the face.
     * @param y Y coordinate of the face.
     * @return Color of the specified face.
     */
    public final RubikCubeFaceColor getFace(final RubikCubeSide s, final int x,
                                            final int y) {
        return this.configuration[s.ordinal()][x][y];
    }
    /**
     * <p>Resets the cube to the <em>standard configuration</em>:</p>
     * <ul>
     * <li>all faces on each side have the same color;</li>
     * <li>the top face is white;</li>
     * <li>the front face is red;</li>
     * <li>the left face is green;</li>
     * <li>the right face is blue;</li>
     * <li>the bottom face is yellow;</li>
     * <li>the back face is orange.</li>
     * </ul>
     */
    public final void resetToStandardConfiguration() {
        for (RubikCubeSide s : RubikCubeSide.values()) {
            for (int i = 0; i < this.dimension; ++i) {
                for (int j = 0; j < this.dimension; ++j) {
                    this.configuration[s.ordinal()][i][j] = s
                            .getStandardColor();
                    notifyObservers(new RubikCubeModelFaceChanged(i, j, s));
                }
            }
        }
    }
    /**
     * Rotates a row of the Rubik cube.
     * @param index Row index.
     * @param rotation Direction of the rotation.
     * @throws IndexOutOfBoundsException Thrown if <tt>index</tt> is greater or
     * equal to the dimension of the cube or is less than zero.
     * @throws IllegalArgumentException Thrown if <tt>rotation</tt> has an
     * invalid value.
     */
    public final void rotateRow(final int index, final RowRotation rotation)
            throws IndexOutOfBoundsException, IllegalArgumentException {
        RubikCubeFaceColor[] tmpRow;
        if (index >= this.dimension || index < 0) {
            throw new IndexOutOfBoundsException("The row index must be between"
                    + " 0 and dimension - 1.");
        }
        tmpRow = new RubikCubeFaceColor[this.dimension];
        // Backup the front row and rotate the row in the specified direction
        tmpRow = this.configuration[RubikCubeSide.FRONT.ordinal()][index];
        if (rotation == RowRotation.ANTICLOCKWISE) {
            this.configuration[RubikCubeSide.FRONT.ordinal()][index] =
                    this.configuration[RubikCubeSide.LEFT.ordinal()][index];
            this.configuration[RubikCubeSide.LEFT.ordinal()][index] =
                    this.configuration[RubikCubeSide.BACK.ordinal()][index];
            this.configuration[RubikCubeSide.BACK.ordinal()][index] =
                    this.configuration[RubikCubeSide.RIGHT.ordinal()][index];
            this.configuration[RubikCubeSide.RIGHT.ordinal()][index] = tmpRow;
        } else if (rotation == RowRotation.CLOCKWISE) {
            this.configuration[RubikCubeSide.FRONT.ordinal()][index] =
                    this.configuration[RubikCubeSide.RIGHT.ordinal()][index];
            this.configuration[RubikCubeSide.RIGHT.ordinal()][index] =
                    this.configuration[RubikCubeSide.BACK.ordinal()][index];
            this.configuration[RubikCubeSide.BACK.ordinal()][index] =
                    this.configuration[RubikCubeSide.LEFT.ordinal()][index];
            this.configuration[RubikCubeSide.LEFT.ordinal()][index] = tmpRow;
        } else {
            throw new IllegalArgumentException();
        }
        // Rotate the lateral face if needed.
        if (index == 0) {
            final RubikCubeFaceColor[][] tmp = new RubikCubeFaceColor
                    [this.dimension][this.dimension];
            this.rotateFace(this.configuration[RubikCubeSide.UP.ordinal()],
                    tmp, rotation == RowRotation.CLOCKWISE);
            this.copyArray(tmp, this.configuration[RubikCubeSide.UP.ordinal()]);
        } else if (index == this.dimension - 1) {
            final RubikCubeFaceColor[][] tmp = new RubikCubeFaceColor
                    [this.dimension][this.dimension];
            this.rotateFace(this.configuration[RubikCubeSide.DOWN.ordinal()],
                    tmp, rotation == RowRotation.ANTICLOCKWISE);
            this.copyArray(tmp, this.configuration[RubikCubeSide.DOWN.ordinal()]);
        }
        // Notify the listeners that the row has changed
        notifyObservers(new RubikCubeModelRowRotated(index, rotation));
    }
    /**
     * Rotates a column of the Rubik cube.
     * @param index Column index.
     * @param rotation Direction of the rotation.
     * @throws IndexOutOfBoundsException Thrown if <tt>index</tt> is greater or
     * equal to the dimension of the cube or is less than zero.
     * @throws IllegalArgumentException Thrown if <tt>rotation</tt> has an
     * invalid value.
     */
    public final void rotateColumn(final int index,
                                   final ColumnRotation rotation)
            throws IndexOutOfBoundsException, IllegalArgumentException {
        RubikCubeFaceColor[] tmpCol;
        if (index >= this.dimension || index < 0) {
            throw new IndexOutOfBoundsException("The column index must be"
                    + " between 0 and dimension - 1.");
        }
        tmpCol = new RubikCubeFaceColor[this.dimension];
        // Backup the front column and rotate the column in the specified
        // direction
        for (int i = 0; i < this.dimension; ++i) {
            tmpCol[i] = this.configuration[RubikCubeSide.FRONT.ordinal()][i]
                    [index];
        }
        if (rotation == ColumnRotation.BOTTOM) {
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.FRONT.ordinal()][i][index]
                        = this.configuration[RubikCubeSide.UP.ordinal()][i][index];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.UP.ordinal()][i][index]
                        = this.configuration[RubikCubeSide.BACK.ordinal()][this.dimension - 1 - i][this.dimension - 1 - index];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.BACK.ordinal()][this.dimension - 1 - i][this.dimension - 1 - index]
                        = this.configuration[RubikCubeSide.DOWN.ordinal()][i][index];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.DOWN.ordinal()][i][index]
                        = tmpCol[i];
            }
        } else if (rotation == ColumnRotation.TOP) {
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.FRONT.ordinal()][i][index]
                        = this.configuration[RubikCubeSide.DOWN.ordinal()][i][index];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.DOWN.ordinal()][i][index]
                        = this.configuration[RubikCubeSide.BACK.ordinal()][this.dimension - 1 - i][this.dimension - 1 - index];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.BACK.ordinal()][this.dimension - 1 - i][this.dimension - 1 - index]
                        = this.configuration[RubikCubeSide.UP.ordinal()][i][index];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.UP.ordinal()][i][index]
                        = tmpCol[i];
            }
        } else {
            throw new IllegalArgumentException();
        }
        // Rotate the lateral faces if needed.
        if (index == 0) {
            final RubikCubeFaceColor[][] tmp = new RubikCubeFaceColor
                    [this.dimension][this.dimension];
            this.rotateFace(this.configuration[RubikCubeSide.LEFT.ordinal()],
                    tmp, rotation == ColumnRotation.TOP);
            this.copyArray(tmp, this.configuration[RubikCubeSide.LEFT.ordinal()]);
        } else if (index == this.dimension - 1) {
            final RubikCubeFaceColor[][] tmp = new RubikCubeFaceColor
                    [this.dimension][this.dimension];
            this.rotateFace(this.configuration[RubikCubeSide.RIGHT.ordinal()],
                    tmp, rotation == ColumnRotation.BOTTOM);
            this.copyArray(tmp, this.configuration[RubikCubeSide.RIGHT.ordinal()]);
        }
        // Notify the listeners that the column has changed.
        notifyObservers(new RubikCubeModelColumnRotated(index, rotation));
    }
    /**
     * Rotates a lateral column of the Rubik cube.
     * @param index Column index (counted from front to back on the right side).
     * @param rotation Direction of the rotation.
     * @throws IndexOutOfBoundsException Thrown if <tt>index</tt> is greater or
     * equal to the dimension of the cube or is less than zero.
     * @throws IllegalArgumentException Thrown if <tt>rotation</tt> has an
     * invalid value.
     */
    public final void rotateLateralColumn(final int index,
                                          final LateralColumnRotation rotation)
            throws IndexOutOfBoundsException, IllegalArgumentException {
        this.rotateLateralColumnInternal(index, rotation, true);
    }
    /**
     * Rotates a lateral column of the Rubik cube.
     * @param index Column index (counted from front to back on the right side).
     * @param rotation Direction of the rotation.
     * @param fireEvents Whether the <tt>RubikCubeModelLateralColumnRotated</tt>
     * event should be fired or not.
     * @throws IndexOutOfBoundsException Thrown if <tt>index</tt> is greater or
     * equal to the dimension of the cube or is less than zero.
     * @throws IllegalArgumentException Thrown if <tt>rotation</tt> has an
     * invalid value.
     */
    private void rotateLateralColumnInternal(final int index,
                                             final LateralColumnRotation rotation,
                                             final boolean fireEvents)
            throws IndexOutOfBoundsException, IllegalArgumentException {
        RubikCubeFaceColor[] tmpLatCol;
        if (index >= this.dimension || index < 0) {
            throw new IndexOutOfBoundsException("The lateral column index must"
                    + " be between 0 and dimension - 1.");
        }
        tmpLatCol = new RubikCubeFaceColor[this.dimension];
        // Backup the row on the upper face and rotate the lateral column in
        // the specified direction
        for (int i = 0; i < this.dimension; ++i) {
            tmpLatCol[i] = this.configuration[RubikCubeSide.UP.ordinal()]
                    [this.dimension - 1 - index][i];
        }
        if (rotation == LateralColumnRotation.LEFT) {
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.UP.ordinal()][this.dimension - 1 - index][i] =
                        this.configuration[RubikCubeSide.RIGHT.ordinal()][i][index];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.RIGHT.ordinal()][this.dimension - 1 - i][index] =
                        this.configuration[RubikCubeSide.DOWN.ordinal()][index][i];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.DOWN.ordinal()][index][i] =
                        this.configuration[RubikCubeSide.LEFT.ordinal()][i][this.dimension - 1 - index];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.LEFT.ordinal()][this.dimension - 1 - i][this.dimension - 1 - index] =
                        tmpLatCol[i];
            }
        } else if (rotation == LateralColumnRotation.RIGHT) {
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.UP.ordinal()][this.dimension - 1 - index][i] =
                        this.configuration[RubikCubeSide.LEFT.ordinal()][this.dimension - 1 - i][this.dimension - 1 - index];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.LEFT.ordinal()][i][this.dimension - 1 - index] =
                        this.configuration[RubikCubeSide.DOWN.ordinal()][index][i];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.DOWN.ordinal()][index][this.dimension - 1 - i] =
                        this.configuration[RubikCubeSide.RIGHT.ordinal()][i][index];
            }
            for (int i = 0; i < this.dimension; ++i) {
                this.configuration[RubikCubeSide.RIGHT.ordinal()][i][index] =
                        tmpLatCol[i];
            }
        } else {
            throw new IllegalArgumentException();
        }
        // Rotate the lateral faces if needed.
        if (index == 0) {
            final RubikCubeFaceColor[][] tmp = new RubikCubeFaceColor
                    [this.dimension][this.dimension];
            this.rotateFace(this.configuration[RubikCubeSide.FRONT.ordinal()],
                    tmp, rotation == LateralColumnRotation.LEFT);
            this.copyArray(tmp, this.configuration[RubikCubeSide.FRONT.ordinal()]);
        } else if (index == this.dimension - 1) {
            final RubikCubeFaceColor[][] tmp = new RubikCubeFaceColor
                    [this.dimension][this.dimension];
            this.rotateFace(this.configuration[RubikCubeSide.BACK.ordinal()],
                    tmp, rotation == LateralColumnRotation.RIGHT);
            this.copyArray(tmp, this.configuration[RubikCubeSide.BACK.ordinal()]);
        }
        // Notify the listeners that the lateral column has changed, if needed.
        if (fireEvents) {
            notifyObservers(new RubikCubeModelLateralColumnRotated(index,
                                                                   rotation));
        }
    }
    /**
     * Rotates the entire cube in the specified direction.
     * @param rotation Direction of the rotation.
     * @throws IllegalArgumentException Thrown if <tt>rotation</tt> is an
     * invalid direction.
     */
    public final void rotateCube(final CubeRotation rotation)
            throws IllegalArgumentException {
        // Save the front side in a temporary variable since this move is the
        // same for every rotation
        final RubikCubeFaceColor[][] tmp =
                new RubikCubeFaceColor[this.dimension][this.dimension];
        this.copyArray(this.configuration[RubikCubeSide.FRONT.ordinal()], tmp);
        switch (rotation) {
            case UPWISE:
                this.copyArray(this.configuration[RubikCubeSide.DOWN.ordinal()],
                               this.configuration[RubikCubeSide.FRONT.ordinal()]);
                this.copyArray(this.configuration[RubikCubeSide.BACK.ordinal()],
                               this.configuration[RubikCubeSide.DOWN.ordinal()],
                               true);
                this.copyArray(this.configuration[RubikCubeSide.UP.ordinal()],
                               this.configuration[RubikCubeSide.BACK.ordinal()],
                               true);
                this.copyArray(tmp,
                               this.configuration[RubikCubeSide.UP.ordinal()]);
                this.rotateFace(this.configuration[RubikCubeSide.LEFT.ordinal()],
                                tmp, true);
                this.copyArray(tmp, this.configuration[RubikCubeSide.LEFT.ordinal()]);
                this.rotateFace(this.configuration[RubikCubeSide.RIGHT.ordinal()],
                        tmp, false);
                this.copyArray(tmp, this.configuration[RubikCubeSide.RIGHT.ordinal()]);
                break;
            case DOWNWISE:
                this.copyArray(this.configuration[RubikCubeSide.UP.ordinal()],
                               this.configuration[RubikCubeSide.FRONT.ordinal()]);
                this.copyArray(this.configuration[RubikCubeSide.BACK.ordinal()],
                               this.configuration[RubikCubeSide.UP.ordinal()],
                               true);
                this.copyArray(this.configuration[RubikCubeSide.DOWN.ordinal()],
                               this.configuration[RubikCubeSide.BACK.ordinal()],
                               true);
                this.copyArray(tmp,
                               this.configuration[RubikCubeSide.DOWN.ordinal()]);
                this.rotateFace(this.configuration[RubikCubeSide.LEFT.ordinal()],
                        tmp, false);
                this.copyArray(tmp, this.configuration[RubikCubeSide.LEFT.ordinal()]);
                this.rotateFace(this.configuration[RubikCubeSide.RIGHT.ordinal()],
                        tmp, true);
                this.copyArray(tmp, this.configuration[RubikCubeSide.RIGHT.ordinal()]);
                break;
            case CLOCKWISE:
                this.copyArray(this.configuration[RubikCubeSide.RIGHT.ordinal()],
                               this.configuration[RubikCubeSide.FRONT.ordinal()]);
                this.copyArray(this.configuration[RubikCubeSide.BACK.ordinal()],
                               this.configuration[RubikCubeSide.RIGHT.ordinal()]);
                this.copyArray(this.configuration[RubikCubeSide.LEFT.ordinal()],
                               this.configuration[RubikCubeSide.BACK.ordinal()]);
                this.copyArray(tmp,
                               this.configuration[RubikCubeSide.LEFT.ordinal()]);
                this.rotateFace(this.configuration[RubikCubeSide.UP.ordinal()],
                        tmp, true);
                this.copyArray(tmp, this.configuration[RubikCubeSide.UP.ordinal()]);
                this.rotateFace(this.configuration[RubikCubeSide.DOWN.ordinal()],
                        tmp, false);
                this.copyArray(tmp, this.configuration[RubikCubeSide.DOWN.ordinal()]);
                break;
            case ANTICLOCKWISE:
                this.copyArray(this.configuration[RubikCubeSide.LEFT.ordinal()],
                               this.configuration[RubikCubeSide.FRONT.ordinal()]);
                this.copyArray(this.configuration[RubikCubeSide.BACK.ordinal()],
                               this.configuration[RubikCubeSide.LEFT.ordinal()]);
                this.copyArray(this.configuration[RubikCubeSide.RIGHT.ordinal()],
                               this.configuration[RubikCubeSide.BACK.ordinal()]);
                this.copyArray(tmp,
                               this.configuration[RubikCubeSide.RIGHT.ordinal()]);
                this.rotateFace(this.configuration[RubikCubeSide.UP.ordinal()],
                        tmp, false);
                this.copyArray(tmp, this.configuration[RubikCubeSide.UP.ordinal()]);
                this.rotateFace(this.configuration[RubikCubeSide.DOWN.ordinal()],
                        tmp, true);
                this.copyArray(tmp, this.configuration[RubikCubeSide.DOWN.ordinal()]);
                break;
            case CLOCKWISE_FROM_FRONT:
                for (int i = 0; i < this.dimension; ++i) {
                    this.rotateLateralColumnInternal(i,
                                                     LateralColumnRotation.RIGHT,
                                                     false);
                }
                this.rotateFace(this.configuration[RubikCubeSide.FRONT.ordinal()],
                        tmp, false);
                this.copyArray(tmp, this.configuration[RubikCubeSide.FRONT.ordinal()]);
                this.rotateFace(this.configuration[RubikCubeSide.BACK.ordinal()],
                        tmp, true);
                this.copyArray(tmp, this.configuration[RubikCubeSide.BACK.ordinal()]);
                break;
            case ANTICLOCKWISE_FROM_FRONT:
                for (int i = 0; i < this.dimension; ++i) {
                    this.rotateLateralColumnInternal(i,
                                                     LateralColumnRotation.LEFT,
                                                     false);
                }
                this.rotateFace(this.configuration[RubikCubeSide.FRONT.ordinal()],
                        tmp, true);
                this.copyArray(tmp, this.configuration[RubikCubeSide.FRONT.ordinal()]);
                this.rotateFace(this.configuration[RubikCubeSide.BACK.ordinal()],
                        tmp, false);
                this.copyArray(tmp, this.configuration[RubikCubeSide.BACK.ordinal()]);
                break;
            default:
                throw new IllegalArgumentException();
        }
        // Notify the listeners that the cube was rotated.
        notifyObservers(new RubikCubeModelCubeRotated(rotation));
    }
    /**
     * Gets a textual representation of the cube, suitable for printing.
     * @return Textual representation of the cube.
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder("");
        for (int i = this.dimension; i >= 0; --i) {
            for (int j = 0; j < this.dimension; ++j) {
                sb.append(" ");
            }
            sb.append(" | ");
            for (int j = this.dimension; j >= 0; --j) {
                sb.append(this.configuration[RubikCubeSide.BACK.ordinal()][i][j]);
            }
            sb.append(" |\n");
        }
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < this.dimension; ++k) {
                sb.append("-");
            }
            sb.append("---");
        }
        for (int j = 0; j < this.dimension; ++j) {
            sb.append("-");
        }
        sb.append("\n");
        for (int i = 0; i <= this.dimension; ++i) {
            for (int j = this.dimension; j >= 0; --j) {
                sb.append(this.configuration[RubikCubeSide.LEFT.ordinal()][j][i]);
            }
            sb.append(" | ");
            for (int j = 0; j <= this.dimension; ++j) {
                System.out.print(this.configuration[RubikCubeSide.UP.ordinal()][i][j]);
            }
            sb.append(" | ");
            for (int j = 0; j <= this.dimension; ++j) {
                sb.append(this.configuration[RubikCubeSide.RIGHT.ordinal()][j][this.dimension - i]);
            }
            sb.append(" | ");
            for (int j = this.dimension; j >= 0; --j) {
                sb.append(this.configuration[RubikCubeSide.DOWN.ordinal()][this.dimension - i][j]);
            }
            sb.append("\n");
        }
        for (int j = 0; j < 3; ++j) {
            for (int k = 0; k < this.dimension; ++k) {
                sb.append("-");
            }
            sb.append("---");
        }
        for (int j = 0; j < this.dimension; ++j) {
            sb.append("-");
        }
        sb.append("\n");
        for (int i = 0; i <= this.dimension; ++i) {
            for (int j = 0; j < this.dimension; ++j) {
                sb.append(" ");
            }
            sb.append(" | ");
            for (int j = 0; j <= this.dimension; ++j) {
                sb.append(this.configuration[RubikCubeSide.FRONT.ordinal()][i][j]);
            }
            sb.append(" |\n");
        }
        return sb.toString();
    }
    /**
     * Checks whether a cube is in the standard configuration (each face has
     * all subfaces of the same, standard face color).
     * @param m Model to be checked.
     * @return <tt>true</tt> if and only if the cube is in the standard
     * configuration.
     * @throws NullPointerException Thrown if <tt>m</tt> is <tt>null</tt>.
     */
    public static boolean isInStandardConfiguration(final RubikCubeModel m)
        throws NullPointerException {
        if (m == null) {
            throw new NullPointerException("m must not be null");
        }
        for (RubikCubeSide s: RubikCubeSide.values()) {
            for (int i = 0; i < m.getDimension(); ++i) {
                for (int j = 0; j < m.getDimension(); ++j) {
                    if (s.getStandardColor() != m.getFace(s, i, j)) {
                        return false;
                    }
                }
            }
        }
        return true; 
    }
    /**
     * Checks whether a cube has sane colors (nine faces per color and one
     * facelet &ndash; central face &ndash; for each color).
     * @param m Model to be checked.
     * @return <tt>true</tt> if and only if the cube has sane colors.
     * @throws NullPointerException Thrown if <tt>m</tt> is <tt>null</tt>.
     */
    public static boolean isWithSaneColors(final RubikCubeModel m)
        throws NullPointerException {
        if (m == null) {
            throw new NullPointerException("m must not be null");
        }
        final int[][] colorAndFaceletCount = new int[6][2];
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 2; ++j) {
                colorAndFaceletCount[i][j] = 0;
            }
        }
        for (RubikCubeSide s : RubikCubeSide.values()) {
            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    ++colorAndFaceletCount[m.getFace(s, i, j).ordinal()][0];
                    if (i == 1 && j == 1) {
                        ++colorAndFaceletCount[m.getFace(s, i, j).ordinal()][1];
                    }
                }
            }
        }
        for (int i = 0; i < 6; ++i) {
            if (colorAndFaceletCount[i][0] != 9) {
                return false;
            }
            if (colorAndFaceletCount[i][1] != 1) {
                return false;
            }
        }
        return true;
    }
    /**
     * Deep copies a face of the Rubik cube to another face.
     * @param src Source face.
     * @param dst Destination face.
     */
    private void copyArray(final RubikCubeFaceColor[][] src,
                           final RubikCubeFaceColor[][] dst) {
        this.copyArray(src, dst, false);
    }
    /**
     * Deep copies a face of the Rubik cube to another face.
     * @param src Source face.
     * @param dst Destination face.
     * @param sw Whether to switch the face (perform the back rotation).
     */
    private void copyArray(final RubikCubeFaceColor[][] src,
                           final RubikCubeFaceColor[][] dst,
                           final boolean sw) {
        
        for (int i = 0; i < this.dimension; ++i) {
            for (int j = 0; j < this.dimension; ++j) {
                if (sw) {
                    dst[this.dimension - 1 - i][this.dimension - 1 - j]
                            = src[i][j];
                } else {
                    dst[i][j] = src[i][j];
                }
            }
        }
    }
    /**
     * Rotates a face of the Rubik cube. <strong><tt>src</tt> and <tt>dst</tt>
     * must not be the same array</strong> as the routine is not designed for
     * a copy in place!
     * @param src Source face.
     * @param dst Destination face.
     * @param anticlockwise Specifies if the rotation should be anticlockwise
     * (it will be clockwise otherwise).
     * @throws IllegalArgumentException Thrown if <tt>src</tt> and <tt>dst</tt>
     * are the same array.
     */
    private void rotateFace(final RubikCubeFaceColor[][] src,
                            final RubikCubeFaceColor[][] dst,
                            final boolean anticlockwise)
        throws IllegalArgumentException {
        // The following == is intended as we're comparing references.
        if (src == dst) {
            throw new IllegalArgumentException("src and dst must be different"
                                               + " references");
        }
        for (int i = 0; i < this.dimension; ++i) {
            for (int j = 0; j < this.dimension; ++j) {
                if (anticlockwise) {
                    dst[this.dimension - 1 - j][i] = src[i][j];
                } else {
                    dst[j][this.dimension - 1 - i] = src[i][j];
                }
            }
        }
    }
}
