package it.univr.rubikcube.resolutionstrategies;

import it.univr.rubikcube.model.CubeRotation;
import it.univr.rubikcube.model.Move;
import it.univr.rubikcube.model.RubikCubeCorner;
import it.univr.rubikcube.model.RubikCubeCornerColor;
import it.univr.rubikcube.model.RubikCubeEdgeColor;
import it.univr.rubikcube.model.RubikCubeFaceColor;
import it.univr.rubikcube.model.RubikCubeModel;
import it.univr.rubikcube.model.RubikCubeModel3Edge;
import it.univr.rubikcube.model.RubikCubeSide;
import it.univr.rubikcube.moves.B;
import it.univr.rubikcube.moves.D;
import it.univr.rubikcube.moves.F;
import it.univr.rubikcube.moves.L;
import it.univr.rubikcube.moves.R;
import it.univr.rubikcube.moves.U;
import it.univr.rubikcube.moves.X;
import it.univr.rubikcube.moves.Y;
import it.univr.rubikcube.moves.Z;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Singmaster resolution strategy.
 * @author Alessandro Menti
 * @see http://www.cs.swarthmore.edu/~knerr/helps/rcube.html
 */
public class Singmaster extends ResolutionStrategy {
    /**
     * Creates a new instance of the Kociemba method.
     * @param m Rubik cube model.
     * @throws IllegalArgumentException Thrown if the dimension of the cube is
     * not equal to three, if the cube has not nine faces per color, if there
     * isn't a single facelet per color.
     * @throws NullPointerException Thrown if <tt>m</tt> is <tt>null</tt>.
     */
    public Singmaster(final RubikCubeModel m)
            throws IllegalArgumentException, NullPointerException {
        super(m);
        if (m.getDimension() != 3) {
            throw new IllegalArgumentException("The dimension of the cube must"
                                               + " be equal to three");
        }
        if (!RubikCubeModel.isWithSaneColors(m)) {
            throw new IllegalArgumentException("The cube has not sane colors");
        }
    }
    /**
     * Gets a list of next moves.
     * @return List of next moves.
     * @throws NoSolutionException Thrown in case the algorithm does not find a
     * solution.
     * @throws TimeoutException Thrown in case the algorithm fails to find a
     * solution before the timeout.
     */
    @Override
    public final List<Move> getNextMoves() throws NoSolutionException,
        TimeoutException {
        final List<Move> listMoves = new ArrayList<>();
        Move currentMove;
        // Get a clone of the existing cube model so that we'll be able to
        // perform any required moves without altering the existing one.
        final RubikCubeModel m = new RubikCubeModel(this.getModel());
        // Check if the green cross is solved: get the green central facelet
        // and check the other relevant facelets on the same side.
        RubikCubeSide greenSide = null;
        for (RubikCubeSide s: RubikCubeSide.values()) {
            if (m.getFace(s, 1, 1) == RubikCubeFaceColor.GREEN) {
                greenSide = s;
                break;
            }
        }
        if (greenSide == null) {
            throw new NoSolutionException("Malformed cube, no green side"
                    + " found");
        }
        if (m.getFace(greenSide, 0, 1) != RubikCubeFaceColor.GREEN
                || m.getFace(greenSide, 1, 0) != RubikCubeFaceColor.GREEN
                || m.getFace(greenSide, 1, 1) != RubikCubeFaceColor.GREEN
                || m.getFace(greenSide, 1, 2) != RubikCubeFaceColor.GREEN
                || m.getFace(greenSide, 2, 1) != RubikCubeFaceColor.GREEN) {
            // FIXME più edge!
            // Move the green side up
            switch (greenSide) {
                case BACK:
                    currentMove = new X(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case DOWN:
                    currentMove = new X(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    // fall through
                case FRONT:
                    currentMove = new X(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case LEFT:
                    currentMove = new Z(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case RIGHT:
                    currentMove = new Z(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case UP:
                default:
                    // Nothing to do
                    break;
            }
            // Until the green cross on the top is formed...
            // FIXME edges as well
            while (m.getFace(RubikCubeSide.UP, 0, 1) != RubikCubeFaceColor.GREEN
                   || m.getFace(RubikCubeSide.UP, 1, 0) != RubikCubeFaceColor.GREEN
                   || m.getFace(RubikCubeSide.UP, 1, 1) != RubikCubeFaceColor.GREEN
                   || m.getFace(RubikCubeSide.UP, 1, 2) != RubikCubeFaceColor.GREEN
                   || m.getFace(RubikCubeSide.UP, 2, 1) != RubikCubeFaceColor.GREEN) {
                // ...find a green edge piece that is not already in place...
                RubikCubeEdgeColor c;
                RubikCubeModel3Edge edge = null;
                for (RubikCubeModel3Edge e : RubikCubeModel3Edge.values()) {
                    edge = e;
                    c = m.get3DEdge(e);
                    if (c.getFirstColor() != RubikCubeFaceColor.GREEN
                            && c.getSecondColor() != RubikCubeFaceColor.GREEN) {
                        continue;
                    }
                    if (c.getFirstColor() == RubikCubeFaceColor.GREEN) {
                        if (e == RubikCubeModel3Edge.UB
                                && m.getFace(RubikCubeSide.BACK, 1, 1)
                                == c.getSecondColor()) {
                            continue;
                        }
                        if (e == RubikCubeModel3Edge.UF
                                && m.getFace(RubikCubeSide.FRONT, 1, 1)
                                == c.getSecondColor()) {
                            continue;
                        }
                        if (e == RubikCubeModel3Edge.UL
                                && m.getFace(RubikCubeSide.LEFT, 1, 1)
                                == c.getSecondColor()) {
                            continue;
                        }
                        if (e == RubikCubeModel3Edge.UR
                                && m.getFace(RubikCubeSide.RIGHT, 1, 1)
                                == c.getSecondColor()) {
                            continue;
                        }
                    }
                    break;
                }
                if (edge == null) {
                    // FIXME Just need to perform an equator move to get the edges right
                }
                // ...move it to the down side...
                RubikCubeModel3Edge edgeOnDown;
                RubikCubeSide lateralSide;
                switch (edge) {
                    case UR:
                        currentMove = new R(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new R(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        edgeOnDown = RubikCubeModel3Edge.DR;
                        lateralSide = RubikCubeSide.RIGHT;
                        break;
                    case UL:
                        currentMove = new L(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new L(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        edgeOnDown = RubikCubeModel3Edge.DL;
                        lateralSide = RubikCubeSide.LEFT;
                        break;
                    case UF:
                        currentMove = new F(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new F(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        edgeOnDown = RubikCubeModel3Edge.DF;
                        lateralSide = RubikCubeSide.FRONT;
                        break;
                    case UB:
                        currentMove = new B(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new B(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        edgeOnDown = RubikCubeModel3Edge.DB;
                        lateralSide = RubikCubeSide.BACK;
                        break;
                    case DR:
                        lateralSide = RubikCubeSide.RIGHT;
                        edgeOnDown = edge;
                        break;
                    case DF:
                        lateralSide = RubikCubeSide.FRONT;
                        edgeOnDown = edge;
                        break;
                    case DL:
                        lateralSide = RubikCubeSide.LEFT;
                        edgeOnDown = edge;
                        break;
                    case DB:
                        lateralSide = RubikCubeSide.BACK;
                        edgeOnDown = edge;
                        break;
                    case FR:
                        currentMove = new F(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        edgeOnDown = RubikCubeModel3Edge.DF;
                        lateralSide = RubikCubeSide.FRONT;
                        break;
                    case FL:
                        currentMove = new F(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        edgeOnDown = RubikCubeModel3Edge.DF;
                        lateralSide = RubikCubeSide.FRONT;
                        break;
                    case BL:
                        currentMove = new B(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        edgeOnDown = RubikCubeModel3Edge.DB;
                        lateralSide = RubikCubeSide.BACK;
                        break;
                    case BR:
                        currentMove = new B(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        edgeOnDown = RubikCubeModel3Edge.DB;
                        lateralSide = RubikCubeSide.BACK;
                        break;
                    default:
                        throw new NoSolutionException("Green edge in unforeseen position");
                }
                // ...rotate the down side until the other color on the edge
                // matches the central facelet...
                RubikCubeEdgeColor downEdgeColor = m.get3DEdge(edgeOnDown);
                RubikCubeFaceColor downEdgeOtherColor = downEdgeColor.getFirstColor();
                int numberOfDownRotations = 0;
                if (downEdgeOtherColor == RubikCubeFaceColor.GREEN) {
                    downEdgeOtherColor = downEdgeColor.getSecondColor();
                }
                while (downEdgeOtherColor != m.getFace(lateralSide, 1, 1)) {
                    if (edgeOnDown == RubikCubeModel3Edge.DF) {
                        edgeOnDown = RubikCubeModel3Edge.DR;
                    } else if (edgeOnDown == RubikCubeModel3Edge.DR) {
                        edgeOnDown = RubikCubeModel3Edge.DB;
                    } else if (edgeOnDown == RubikCubeModel3Edge.DB) {
                        edgeOnDown = RubikCubeModel3Edge.DL;
                    } else {
                        edgeOnDown = RubikCubeModel3Edge.DF;
                    }
                    ++numberOfDownRotations;
                    downEdgeColor = m.get3DEdge(edgeOnDown);
                    downEdgeOtherColor = downEdgeColor.getFirstColor();
                    if (downEdgeOtherColor == RubikCubeFaceColor.GREEN) {
                        downEdgeOtherColor = downEdgeColor.getSecondColor();
                    }
                }
                // This optimization avoids using three clockwise rotations
                // when the right center facelet is on the left of the
                // original position of the edge.
                if (numberOfDownRotations == 1) {
                    currentMove = new D(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                } else if (numberOfDownRotations == 2) {
                    currentMove = new D(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new D(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                } else if (numberOfDownRotations == 3) {
                    currentMove = new D(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                }
                // ...and twist the side 180 degrees to get the green edge on
                // the green side: if it's flipped, perform F' U L' U' (or
                // the equivalent move in case we're not working on the front
                // face)...
                if (edgeOnDown == RubikCubeModel3Edge.DF) {
                    currentMove = new F(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new F(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    if (m.getFace(RubikCubeSide.UP, 2, 1) != RubikCubeFaceColor.GREEN) {
                        currentMove = new F(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new U(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new L(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new U(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                    }
                } else if (edgeOnDown == RubikCubeModel3Edge.DR) {
                    currentMove = new R(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new R(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    if (m.getFace(RubikCubeSide.UP, 1, 2) != RubikCubeFaceColor.GREEN) {
                        currentMove = new R(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new U(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new F(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new U(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                    }
                } else if (edgeOnDown == RubikCubeModel3Edge.DB) {
                    currentMove = new B(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new B(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    if (m.getFace(RubikCubeSide.UP, 0, 1) != RubikCubeFaceColor.GREEN) {
                        currentMove = new B(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new U(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new R(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new U(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                    }
                } else {
                    currentMove = new L(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new L(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    if (m.getFace(RubikCubeSide.UP, 1, 0) != RubikCubeFaceColor.GREEN) {
                        currentMove = new L(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new U(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new B(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        currentMove = new U(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                    }
                }
                // ...and then move on to the next edge.
            }
        }
        // Check the first layer green side corners: rotate a clone of the
        // model and check the corners.
        final RubikCubeModel mClone = new RubikCubeModel(m);
        switch (greenSide) {
            case UP:
                // Nothing to do
                break;
            case FRONT:
                mClone.rotateCube(CubeRotation.UPWISE);
                break;
            case LEFT:
                mClone.rotateCube(CubeRotation.CLOCKWISE_FROM_FRONT);
                break;
            case RIGHT:
                mClone.rotateCube(CubeRotation.ANTICLOCKWISE_FROM_FRONT);
                break;
            case DOWN:
                mClone.rotateCube(CubeRotation.UPWISE);
                mClone.rotateCube(CubeRotation.UPWISE);
                break;
            case BACK:
                mClone.rotateCube(CubeRotation.DOWNWISE);
                break;
            default:
                throw new NoSolutionException("Green side on unknown face");
        }
        if (!RubikCubeModel.isCornerInPlace(mClone, RubikCubeCorner.UFL)
                || !RubikCubeModel.isCornerInPlace(mClone, RubikCubeCorner.URF)
                || !RubikCubeModel.isCornerInPlace(mClone, RubikCubeCorner.UBR)
                || !RubikCubeModel.isCornerInPlace(mClone, RubikCubeCorner.ULB)) {
            // Rotate the cube to get the green side up, if necessary.
            switch (greenSide) {
                case UP:
                    // Nothing to do
                    break;
                case FRONT:
                    currentMove = new X(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case LEFT:
                    currentMove = new Z(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case RIGHT:
                    currentMove = new Z(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case DOWN:
                    currentMove = new X(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new X(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case BACK:
                    currentMove = new X(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                default:
                    throw new NoSolutionException("Green side on unknown face");
            }
            // Fix the green side corners:
            do {
                // * find a green corner that is not already in place
                RubikCubeCorner foundGreenCorner = null;
                RubikCubeCornerColor cornerColor;
                for (RubikCubeCorner c : RubikCubeCorner.values()) {
                    cornerColor = m.getCorner(c);
                    if (!RubikCubeModel.isCornerInPlace(m, c)
                            && (cornerColor.getFirstColor() == RubikCubeFaceColor.GREEN
                            || cornerColor.getSecondColor() == RubikCubeFaceColor.GREEN
                            | cornerColor.getThirdColor() == RubikCubeFaceColor.GREEN)) {
                        foundGreenCorner = c;
                        break;
                    }
                }
                if (foundGreenCorner == null) {
                    throw new NoSolutionException("No green corners found");
                }
                // * get it directly under where it should go (match the other two
                //   colors on the corner piece to the other two side colors).
                //   As corners remain in corner positions:
                //   - if it's in the top row but it's wrongly placed, perform
                //     R' D' R (for the UFR case) or a similar move, then the cube
                //     will be moved to the bottom row;
                //   - if it's in the bottom row, just rotate the bottom row (D)
                //     until the central facelets match, then rotate the cube
                //     so that the other colors of the angle are on the front and
                //     right sides.
                // First, move the corner to DFR.
                if (foundGreenCorner.getUpDownSide() == RubikCubeSide.UP) {
                    switch (foundGreenCorner.getLeftSide()) {
                        case FRONT:
                            // Nothing to do
                            break;
                        case RIGHT:
                            currentMove = new Y(m);
                            listMoves.add(currentMove);
                            currentMove.perform();
                            break;
                        case BACK:
                            currentMove = new Y(m);
                            listMoves.add(currentMove);
                            currentMove.perform();
                            currentMove = new Y(m);
                            listMoves.add(currentMove);
                            currentMove.perform();
                            break;
                        case LEFT:
                            currentMove = new Y(m, true);
                            listMoves.add(currentMove);
                            currentMove.perform();
                            break;
                        default:
                            throw new NoSolutionException("Left side on top or bottom");
                    }
                    // FIXME If on left...?
                    currentMove = new R(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new D(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new R(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    foundGreenCorner = RubikCubeCorner.DFR;
                }
                // * Do R' D' R D 1-6 times (keeping the same face forward at
                //   all times) until the facelets are oriented correctly
                switch (foundGreenCorner.getLeftSide()) {
                    case FRONT:
                        for (int i = 0; i < 6; ++i) {
                            currentMove = new R(m, true);
                            currentMove.perform();
                            listMoves.add(currentMove);
                            currentMove = new D(m, true);
                            currentMove.perform();
                            listMoves.add(currentMove);
                            currentMove = new R(m);
                            currentMove.perform();
                            listMoves.add(currentMove);
                            currentMove = new D(m);
                            currentMove.perform();
                            listMoves.add(currentMove);
                            // FIXME facelets OK
                            if (true) {
                                break;
                            }
                        }
                        break;
                    case RIGHT:
                        break;
                    case BACK:
                        break;
                    case LEFT:
                        break;
                }
                // * Repeat the algorithm until all corners are in place
            } while (!RubikCubeModel.isCornerInPlace(mClone, RubikCubeCorner.UFL)
                    || !RubikCubeModel.isCornerInPlace(mClone, RubikCubeCorner.URF)
                    || !RubikCubeModel.isCornerInPlace(mClone, RubikCubeCorner.UBR)
                    || !RubikCubeModel.isCornerInPlace(mClone, RubikCubeCorner.ULB));
        }
        // Check if the second layer is solved
        boolean secondLayerSolved = true;
        switch (greenSide) {
            case UP:
            case DOWN:
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.FRONT, 1, i) != RubikCubeSide.FRONT.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.RIGHT, 1, i) != RubikCubeSide.RIGHT.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.BACK, 1, i) != RubikCubeSide.BACK.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.LEFT, 1, i) != RubikCubeSide.LEFT.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                break;
            case LEFT:
            case RIGHT:
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.FRONT, i, 1) != RubikCubeSide.FRONT.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.DOWN, i, 1) != RubikCubeSide.DOWN.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.BACK, i, 1) != RubikCubeSide.BACK.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.UP, i, 1) != RubikCubeSide.UP.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                break;
            case FRONT:
            case BACK:
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.UP, 1, i) != RubikCubeSide.UP.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.RIGHT, i, 1) != RubikCubeSide.RIGHT.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.DOWN, 1, i) != RubikCubeSide.DOWN.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                for (int i = 0; i < 3; ++i) {
                    if (m.getFace(RubikCubeSide.LEFT, i, 1) != RubikCubeSide.LEFT.getStandardColor()) {
                        secondLayerSolved = false;
                    }
                }
                break;
        }
        if (!secondLayerSolved) {
            // FIXME Change: Move the green side down
            switch (greenSide) {
                case BACK:
                    currentMove = new X(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case UP:
                    currentMove = new X(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    // fall through
                case FRONT:
                    currentMove = new X(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case LEFT:
                    currentMove = new Z(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case RIGHT:
                    currentMove = new Z(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    break;
                case DOWN:
                default:
                    // Nothing to do
                    break;
            }
            // * Find an edge piece without blue in it, if there is one.
            RubikCubeModel3Edge edgeWithoutBlue = null;
            RubikCubeEdgeColor tmp = m.get3DEdge(RubikCubeModel3Edge.UF);
            if (tmp.getFirstColor() != RubikCubeFaceColor.BLUE && tmp.getSecondColor() != RubikCubeFaceColor.BLUE) {
                edgeWithoutBlue = RubikCubeModel3Edge.UF;
            } else {
                tmp = m.get3DEdge(RubikCubeModel3Edge.UR);
                if (tmp.getFirstColor() != RubikCubeFaceColor.BLUE && tmp.getSecondColor() != RubikCubeFaceColor.BLUE) {
                    edgeWithoutBlue = RubikCubeModel3Edge.UR;
                } else {
                    tmp = m.get3DEdge(RubikCubeModel3Edge.UB);
                    if (tmp.getFirstColor() != RubikCubeFaceColor.BLUE && tmp.getSecondColor() != RubikCubeFaceColor.BLUE) {
                        edgeWithoutBlue = RubikCubeModel3Edge.UB;
                    } else {
                        tmp = m.get3DEdge(RubikCubeModel3Edge.UL);
                        if (tmp.getFirstColor() != RubikCubeFaceColor.BLUE && tmp.getSecondColor() != RubikCubeFaceColor.BLUE) {
                            edgeWithoutBlue = RubikCubeModel3Edge.UL;
                        }
                    }
                }
            }
            if (edgeWithoutBlue != null) {
                // twist the top blue side so edge piece matches side color
                final RubikCubeFaceColor edgeColor = m.get3DEdge(edgeWithoutBlue).getSecondColor();
                int numberOfRotations = 0;
                RubikCubeSide edgeSide = edgeWithoutBlue.getLateralSide();
                while (m.getFace(edgeSide, 1, 1) != edgeColor) {
                    ++numberOfRotations;
                    switch (edgeSide) {
                        case LEFT:
                            edgeSide = RubikCubeSide.FRONT;
                            break;
                        case FRONT:
                            edgeSide = RubikCubeSide.RIGHT;
                            break;
                        case RIGHT:
                            edgeSide = RubikCubeSide.BACK;
                            break;
                        case BACK:
                            edgeSide = RubikCubeSide.LEFT;
                            break;
                        default:
                            throw new NoSolutionException("Edge side in an unconsistent state");
                    }
                }
                switch (numberOfRotations) {
                    case 3:
                        currentMove = new U(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        break;
                    case 2:
                        currentMove = new U(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        // fall through
                    case 1:
                        currentMove = new U(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        break;
                    case 0:
                    default:
                        // Nothing to do
                        break;
                }
                // Put that piece front and center (rotate the cube)
                switch (edgeSide) {
                    case LEFT:
                        currentMove = new Y(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        break;
                    case BACK:
                        currentMove = new Y(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        // fall through
                    case RIGHT:
                        currentMove = new Y(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        break;
                    case FRONT:
                    default:
                        // Nothing to do
                        break;
                }
                if (m.get3DEdge(RubikCubeModel3Edge.UF).getFirstColor()
                        == m.getFace(RubikCubeSide.RIGHT, 1, 1)) {
                    // if top matches right side: U R U' R' U' F' U F
                    currentMove = new U(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new R(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new U(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new R(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new U(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new F(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new U(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new F(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                } else {
                    // if top matches left side: U' L' U L U F U' F'
                    currentMove = new U(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new L(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new U(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new L(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new U(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new F(m);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new U(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                    currentMove = new F(m, true);
                    listMoves.add(currentMove);
                    currentMove.perform();
                }
            } else {
                // Find an edge without blue in the middle layer (better if
                // it's not in place)
                RubikCubeModel3Edge edgeInMiddle = null;
                for (RubikCubeModel3Edge e : new RubikCubeModel3Edge[]
                        {RubikCubeModel3Edge.FR, RubikCubeModel3Edge.BR,
                         RubikCubeModel3Edge.BL, RubikCubeModel3Edge.FL, }) {
                    if (m.get3DEdge(e).getFirstColor() == RubikCubeFaceColor.BLUE
                            || m.get3DEdge(e).getSecondColor() == RubikCubeFaceColor.BLUE) {
                        continue;
                    }
                    if (RubikCubeModel.isEdgeInPlace(m, e)) {
                        if (edgeInMiddle == null) {
                            // TODO: is this acceptable (even if the edge is in place)?
                            edgeInMiddle = e;
                        }
                    } else {
                        edgeInMiddle = e;
                    }
                }
                if (edgeInMiddle == null) {
                    throw new NoSolutionException("Must have a suitable edge in middle");
                }
                // ...put it in the FR edge by rotating the cube...
                switch (edgeInMiddle) {
                    case FR:
                        // Nothing to do
                        break;
                    case BL:
                        currentMove = new Y(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        // fall through
                    case BR:
                        currentMove = new Y(m);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        break;
                    case FL:
                        currentMove = new Y(m, true);
                        listMoves.add(currentMove);
                        currentMove.perform();
                        break;
                    default:
                        throw new NoSolutionException("Edge seems not to be in the middle layer");
                }
                // ...then perform U R U' R' U' F' U F
                currentMove = new U(m);
                listMoves.add(currentMove);
                currentMove.perform();
                currentMove = new R(m);
                listMoves.add(currentMove);
                currentMove.perform();
                currentMove = new U(m, true);
                listMoves.add(currentMove);
                currentMove.perform();
                currentMove = new R(m, true);
                listMoves.add(currentMove);
                currentMove.perform();
                currentMove = new U(m, true);
                listMoves.add(currentMove);
                currentMove.perform();
                currentMove = new F(m, true);
                listMoves.add(currentMove);
                currentMove.perform();
                currentMove = new U(m);
                listMoves.add(currentMove);
                currentMove.perform();
                currentMove = new F(m);
                listMoves.add(currentMove);
                currentMove.perform();
            }
            // FIXME Repeat until 2nd layer is done
        }
        // Check if the blue cross is solved
        /*
         * 3. Solve the BLUE CROSS....

 > look at top blue layer for one of these cases:
     no small L: do F R U R' U' F' to get small L
        small L: make it top back left, do F R U R' U' F'

   the "L" you are looking for is made up out of three blue pieces,
   the center and two edge pieces
        you should now have three in a row.
     make the 3-in-a-row go left to right across the top
          and again do F R U R' U' F' to get blue cross
         */
        /*
         *  > try twisting top blue side so two edge pieces line up
   with their correct sides

     if you can't get two lined up do R U R' U R 2U R'

     if you get two lined up, and they are 
     across from each other, make the lined-up edges
     the front and back sides and do R U R' U R 2U R'
     (this should make the two lined-up sides adjacent)

     if the two lined up are adjacent, make
     them the right and back sides and do R U R' U R 2U R'
          should now have blue cross on top and be able to twist
     top to get all edge pieces lined up
         */
        // Check the final four corners on the blue side
        /*
         * 4. solve the final 4 corners!!!

 > check the corners to see if they are in their correct
   locations (even though they may be flipped). You should
   either have 0, 1, or all 4 in their correct spots.
       if 0: do U R U' L' U R' U' L with blue cross on top

       this should get 1 or more corners correct

    if 1: do U R U' L' U R' U' L with blue cross on top and
          correct corner as upper right of front face

       this should get 4 corners correct, but you may need
       to repeat this U R U' L' U R' U' L once

    if 4: go to next step...
     > with 4 corners correct (but flipped), pick a side and make
   it the front, with a corner to flip in the top right
      NOTE: always keep this front side the same during the next
         few steps!!!!

     do   R' D' R D  twice and see if that corner is flipped correctly

     if no, do R' D' R D twice again

   if yes, turn up face 90 degrees and repeat with next corner, always
   keeping the same front face (even though it looks like you're 
   messing things up!)
    NOW TWIST THE TOP AND THE CUBE IS SOLVED!!!
         */
        return listMoves;
    }
    /**
     * Gets the name of this method.
     * @return <tt>Singmaster</tt>
     */
    @Override
    public final String toString() {
        return "Singmaster";
    }
    /**
     * Gets a textual description of this method.
     * @return <tt>Singmaster's method solves the cube layer by layer.</tt>
     */
    @Override
    public final String getDescription() {
        return "Singmaster's method solves the cube layer by layer.";
    }

}
