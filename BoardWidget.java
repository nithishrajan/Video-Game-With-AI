/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

import java.awt.event.MouseEvent;

import java.util.concurrent.ArrayBlockingQueue;

import static ataxx.PieceColor.*;
import static ataxx.Utils.*;

/** Widget for displaying an Ataxx board.
 *  @author Nithish Rajan
 */
class BoardWidget extends Pad  {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Number of squares on a side. */
    static final int SIDE = Board.SIDE;
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 15;
    /** Dimension of a block. */
    static final int BLOCK_WIDTH = 40;

    /** Color of red pieces. */
    private static final Color RED_COLOR = Color.RED;
    /** Color of blue pieces. */
    private static final Color BLUE_COLOR = Color.BLUE;
    /** Color of painted lines. */
    private static final Color LINE_COLOR = Color.BLACK;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = Color.WHITE;
    /** Color of selected squared. */
    private static final Color SELECTED_COLOR = new Color(150, 150, 150);
    /** Color of blocks. */
    private static final Color BLOCK_COLOR = Color.BLACK;

    /** Stroke for lines. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.0f);
    /** Stroke for blocks. */
    private static final BasicStroke BLOCK_STROKE = new BasicStroke(5.0f);

    /** A new widget sending commands resulting from mouse clicks
     *  to COMMANDQUEUE. */
    BoardWidget(ArrayBlockingQueue<String> commandQueue) {
        _commandQueue = commandQueue;
        setMouseHandler("click", this::handleClick);
        _dim = SQDIM * SIDE;
        _blockMode = false;
        setPreferredSize(_dim, _dim);
        setMinimumSize(_dim, _dim);
    }

    /** Indicate that SQ (of the form CR) is selected, or that none is
     *  selected if SQ is null. */
    void selectSquare(String sq) {
        if (sq == null) {
            _selectedCol = _selectedRow = 0;
        } else {
            _selectedCol = sq.charAt(0);
            _selectedRow = sq.charAt(1);
        }
        repaint();
    }
    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BLANK_COLOR);
        g.fillRect(0, 0, _dim, _dim);
        drawLines(g);
        int m = (SQDIM / 2) - PIECE_RADIUS;
        int d = PIECE_RADIUS * 2;
        int realX = 0;
        int realY = 3 * 100;
        int lol = 0;
        for (int x = 8 * 3; x <= 12 * 8; x++) {
            if (x % 11 == 2 && x != 12 * 2) {
                realY -= SQDIM;
                realX = 0;
                lol = 0;
            }
            if (_model.get(x) == BLOCKED && lol < 7) {
                drawBlock(g, realX, realY);
                realX += SQDIM;
            } else if (_model.get(x) != BLOCKED) {
                if (_model.get(x) == RED) {
                    g.setColor(RED_COLOR);
                    g.fillOval(realX + m, realY + m, d, d);
                }
                if (_model.get(x) == BLUE) {
                    g.setColor(BLUE_COLOR);
                    g.fillOval(realX + m, realY + m, d, d);
                }
                if (_selectedCol != 0) {
                    PieceColor s = _model.get(_selectedCol, _selectedRow);
                    if (s == RED || s == BLUE) {
                        int ix = _model.convert(_selectedCol, _selectedRow);
                        if (x == ix) {
                            g.setColor(SELECTED_COLOR);
                            g.fillRect(realX, realY, SQDIM, SQDIM);
                            if (s == RED) {
                                g.setColor(RED_COLOR);
                                g.fillOval(realX + m, realY + m, d, d);
                            }
                            if (s == BLUE) {
                                g.setColor(RED_COLOR);
                                g.fillOval(realX + m, realY + m, d, d);
                            }
                        }
                    }
                }
                realX += SQDIM;
            }
            lol++;
        }
    }

    /** Draws Lines for G. */
    void drawLines(Graphics2D g) {
        g.setColor(LINE_COLOR);
        for (int x = SQDIM; x < SIDE * SQDIM; x += SQDIM) {
            g.drawLine(x, 0, x, SIDE * SQDIM);
        }
        for (int y = SQDIM; y < SIDE * SQDIM; y += SQDIM) {
            g.drawLine(0, y, SIDE * SQDIM, y);
        }
    }
    /** Draw a block centered at CX amd CY on G. */
    void drawBlock(Graphics2D g, int cx, int cy) {
        g.setColor(BLOCK_COLOR);
        g.fillRect(cx, cy, SQDIM, SQDIM);
    }


    /** Clear selected block, if any, and turn off block mode. */
    void reset() {
        _selectedRow = _selectedCol = 0;
        setBlockMode(false);
    }

    /** Set block mode on iff ON. */
    void setBlockMode(boolean on) {
        _blockMode = on;
    }

    /** Issue move command indicated by mouse-click event WHERE. */
    private void handleClick(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        char mouseCol, mouseRow;
        if (where.getButton() == MouseEvent.BUTTON1) {
            mouseCol = (char) (x / SQDIM + 'a');
            mouseRow = (char) ((SQDIM * SIDE - y) / SQDIM + '1');
            if (mouseCol >= 'a' && mouseCol <= 'g'
                && mouseRow >= '1' && mouseRow <= '7') {
                if (_blockMode) {
                    _commandQueue.offer("block" + " " + mouseCol + mouseRow);
                } else {
                    if (_selectedCol != 0) {
                        String dash = "-";
                        String fR = String.valueOf(_selectedRow);
                        String fC = String.valueOf(_selectedCol);
                        String sR = String.valueOf(mouseRow);
                        String sC = String.valueOf(mouseCol);
                        _commandQueue.offer(fC + fR + dash + sC + sR);
                        _selectedCol = _selectedRow = 0;
                    } else {
                        _selectedCol = mouseCol;
                        _selectedRow = mouseRow;
                    }
                }
            }
        }
        repaint();
    }

    public synchronized void update(Board board) {
        _model = new Board(board);
        repaint();
    }

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** Model being displayed. */
    private static Board _model;

    /** Coordinates of currently selected square, or '\0' if no selection. */
    private char _selectedCol, _selectedRow;

    /** X and Y coordinates of the selected Square. */
    private int _selectedX, _selectedY;

    /** True iff in block mode. */
    private boolean _blockMode;

    /** Destination for commands derived from mouse clicks. */
    private ArrayBlockingQueue<String> _commandQueue;
}
