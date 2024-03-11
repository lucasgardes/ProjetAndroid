package com.example.survivortest;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private GridLayout gridLayout;
    private int columnCount = 5;
    private int rowCount = 6;
    private Grid gameGrid;
    private Tile tile;
    private boolean isActionInProgress = false;
    private boolean inLoop = false;
    private boolean gameOver = false;

    // function call on app launch
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // create the grid container
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameGrid = new Grid(rowCount, columnCount);
        gridLayout = findViewById(R.id.gridLayout);
        // create first tile
        tile = new Tile(2);
        showNextTile(tile);
        initializeGrid();

        Button retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetGrid();
            }
        });
    }

    // function to create the grid
    private void initializeGrid() {
        gridLayout.removeAllViews();
        gridLayout.setColumnCount(columnCount);
        gridLayout.setRowCount(rowCount);

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                LayoutInflater inflater = LayoutInflater.from(this);
                FrameLayout cellView = (FrameLayout) inflater.inflate(R.layout.grid_cell, gridLayout, false);
                final int columnIndex = j;
                // set a onclick listener to know where the user want to play the tile
                cellView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCellClicked(columnIndex);
                    }
                });
                gridLayout.addView(cellView);
            }
        }
    }

    // function called when user click choose a column
    public void onCellClicked(int columnIndex) {
        if (isActionInProgress) {
            return;
        }
        isActionInProgress = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int previousRowIndex = -1;
                final boolean[] shouldBreak = {false};
                // fetch all cell of the selected column
                for (int rowIndex = 0; rowIndex < rowCount + 1 && !shouldBreak[0] && !gameOver; rowIndex++) {
                    final int finalRowIndex = rowIndex;
                    final int finalPreviousRowIndex = previousRowIndex;
                    Cell aboveCell = finalRowIndex > 0 ? gameGrid.getCell(finalPreviousRowIndex, columnIndex) : null;
                    // if last cell of column
                    if (rowIndex == 6) {
                        boolean fusion = true;
                        String checkAdjacentReturn = "true";
                        do {
                            inLoop = true;
                            if (checkAdjacentReturn == "none") {
                                fusion = false;
                            } else {
                                checkAdjacentReturn = checkAdjacentCell(aboveCell);
                            }
                        } while (fusion);
                        inLoop = false;
                    } else {
                        Cell cell = gameGrid.getCell(finalRowIndex, columnIndex);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // if cell of column already has a value
                                if (!cell.isCellEmpty()) {
                                    if (finalRowIndex == 0) {
                                        gameOver();
                                    } else {
                                        boolean fusion = true;
                                        String checkAdjacentReturn = "true";
                                        Cell previousUpdatedCell = aboveCell;
                                        do {
                                            inLoop = true;
                                            if (checkAdjacentReturn == "none") {
                                                fusion = false;
                                            } else if (checkAdjacentReturn == "bellow") {
                                                int rowToCheck = previousUpdatedCell.getX() + 1;
                                                int columnToCheck = previousUpdatedCell.getY();
                                                previousUpdatedCell = rowToCheck < 6 ? gameGrid.getCell(rowToCheck, columnToCheck) : null;
                                                // call function to fusion cell if possible
                                                checkAdjacentReturn = checkAdjacentCell(previousUpdatedCell);
                                            } else {
                                                // call function to fusion cell if possible
                                                checkAdjacentReturn = checkAdjacentCell(previousUpdatedCell);
                                            }
                                        } while (fusion);
                                        inLoop = false;
                                        // update adjacent column after fusion
                                        checkAdjacentColumn(previousUpdatedCell.getY());
                                    }
                                } else {
                                    if (finalPreviousRowIndex >= 0) {
                                        // clear previous cell value
                                        resetCell(aboveCell);
                                    }
                                    // update current cell
                                    updateCell(cell, null, null, null,null, false);
                                }
                            }
                        });

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        previousRowIndex = rowIndex;
                    }
                }
                // generate new tile
                int newTileValue = generateRandomTileValue();
                tile = new Tile(newTileValue);
                showNextTile(tile);
                updateGameScore();
                isActionInProgress = false;
            }
        }).start();
    }
    public int generateRandomTileValue() {
        final int[] TILE_VALUES = {4, 8, 16, 32, 64, 128, 256, 512};
        final Random random = new Random();
        int tileValue = TILE_VALUES[random.nextInt(TILE_VALUES.length)];
        return tileValue;
    }

    public void showNextTile(Tile tile) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView nextTileView = findViewById(R.id.nextTile);
                String text = "Next : " + tile.getValue();
                nextTileView.setText(text);

                nextTileView.setBackgroundColor(tile.getTileColor());
            }
        });
    }
    public String checkAdjacentCell(Cell cell) {
        Cell bellowCell = checkBellowCell(cell);
        Cell leftCell = checkLeftCell(cell);
        Cell rightCell = checkRightCell(cell);

        if (bellowCell != null) {
            boolean bellowFusion = checkCellFusion(cell, bellowCell);
            if (!bellowFusion) {
                bellowCell = null;
            }
        }
        if (leftCell != null) {
            boolean leftFusion = checkCellFusion(cell, leftCell);
            if (!leftFusion) {
                leftCell = null;
            }
        }

        if (rightCell != null) {
            boolean rightFusion = checkCellFusion(cell, rightCell);
            if (!rightFusion) {
                rightCell = null;
            }
        }
        updateCell(cell, bellowCell, leftCell, rightCell, null, false);
        // return which fusion was done
        if (bellowCell == null && leftCell == null && rightCell == null) {
            return "none";
        } else if (bellowCell != null) {
            return "bellow";
        } else {
            return "true";
        }
    }

    public Cell checkBellowCell(Cell cell) {
        int row = cell.getX();
        int column = cell.getY();
        if (row < rowCount - 1) {
            Cell bellowCell = gameGrid.getCell(row + 1, column);
            if(!bellowCell.isCellEmpty()) {
                return bellowCell;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Cell checkLeftCell(Cell cell) {
        int row = cell.getX();
        int column = cell.getY();
        if (column > 0) {
            Cell leftCell = gameGrid.getCell(row, column - 1);
            if(!leftCell.isCellEmpty()) {
                return leftCell;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Cell checkRightCell(Cell cell) {
        int row = cell.getX();
        int column = cell.getY();
        if (column < 4) {
            Cell rightCell = gameGrid.getCell(row, column + 1);
            if(!rightCell.isCellEmpty()) {
                return rightCell;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean checkCellFusion (Cell mainCell, Cell secondCell) {
        if (mainCell.getValue() == null || secondCell.getValue() == null) {
            return false;
        } else {
            return mainCell.getValue().getValue() == secondCell.getValue().getValue();
        }
    }

    public void resetCell(Cell cell) {
        int row = cell.getX();
        int column = cell.getY();
        View cellView = gridLayout.getChildAt(row  * columnCount + column);
        cell.setValue(null);
        cellView.setBackgroundColor(Color.parseColor("#000000"));
        TextView TextView = cellView.findViewById(R.id.cell_text);
        TextView.setText("");
    }

    public void updateCell(Cell cellToUpdate, Cell bellowCell, Cell leftCell, Cell rightCell, Cell aboveCell, boolean self) {
        if (!cellToUpdate.isCellEmpty()) {
            int value = cellToUpdate.getValue().getValue();
            boolean bellowFusion = false;
            if (bellowCell != null) {
                // update value to put in cell
                if (value > 0) {
                    value += value;
                } else {
                    value += bellowCell.getValue().getValue();
                }
                bellowFusion = true;
                resetCell(cellToUpdate);
            }

            if (leftCell != null) {
                // update value to put in cell
                if (value > 0) {
                    value += value;
                } else {
                    value += leftCell.getValue().getValue();
                }
                resetCell(leftCell);
            }

            if (rightCell != null) {
                // update value to put in cell
                if (value > 0) {
                    value += value;
                } else {
                    value += rightCell.getValue().getValue();
                }
                resetCell(rightCell);
            }

            if (aboveCell != null) {
                // update value to put in cell
                if (value > 0) {
                    value += value;
                } else {
                    value += aboveCell.getValue().getValue();
                }
                resetCell(aboveCell);
            }
            // create tile with value to put in cell
            Tile tile = new Tile(value);
            if (bellowFusion) {
                View cellView = gridLayout.getChildAt(bellowCell.getX()  * columnCount + bellowCell.getY());
                bellowCell.setValue(tile);
                cellView.setBackgroundColor(bellowCell.getValue().getTileColor());
                TextView textView = cellView.findViewById(R.id.cell_text);
                textView.setText(String.valueOf(bellowCell.getValue().getValue()));
            } else {
                View cellView = gridLayout.getChildAt(cellToUpdate.getX()  * columnCount + cellToUpdate.getY());
                cellToUpdate.setValue(tile);
                cellView.setBackgroundColor(cellToUpdate.getValue().getTileColor());
                TextView textView = cellView.findViewById(R.id.cell_text);
                textView.setText(String.valueOf(cellToUpdate.getValue().getValue()));
            }
            // if a left/right fusion was done then update their column
            if (leftCell != null) {
                updateColumn(leftCell);
            }
            if (rightCell != null) {
                updateColumn(rightCell);
            }
        } else {
            if (aboveCell != null) {
                // if it's the above cell that is being fused
                int value = 0;
                value += aboveCell.getValue().getValue();
                resetCell(aboveCell);
                Tile tileToUse = new Tile(value);
                cellToUpdate.setValue(tileToUse);
            } else {
                Tile tileToUse = tile;
                cellToUpdate.setValue(tileToUse);
            }
            View cellView = gridLayout.getChildAt(cellToUpdate.getX()  * columnCount + cellToUpdate.getY());
            cellView.setBackgroundColor(cellToUpdate.getValue().getTileColor());
            TextView textView = cellView.findViewById(R.id.cell_text);
            textView.setText(String.valueOf(cellToUpdate.getValue().getValue()));
            if (aboveCell != null && !inLoop) {
                checkAdjacentCell(cellToUpdate);
            }
        }
    }

    public void updateColumn(Cell cell)
    {
        int row = cell.getX();
        int column = cell.getY();
        for (int rowIndex = row - 1; rowIndex >= 0; rowIndex--) {
            Cell aboveCell = gameGrid.getCell(rowIndex, column);
            if (!aboveCell.isCellEmpty()) {
                updateCell(cell, null, null, null, aboveCell, false);
            }
            cell = aboveCell;
        }
    }

    public void checkAdjacentColumn(int column)
    {
        if (column > 0 && column < columnCount) {
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                Cell cell = gameGrid.getCell(rowIndex, column - 1);
                if (!cell.isCellEmpty()) {
                    checkAdjacentCell(cell);
                }
            }
        }
        if (column >= 0 && column + 1 < columnCount) {
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                Cell cell = gameGrid.getCell(rowIndex, column + 1);
                if (!cell.isCellEmpty()) {
                    checkAdjacentCell(cell);
                }
            }
        }
    }

    public void updateGameScore ()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView gameScoreView = findViewById(R.id.gameScore);
                gameScoreView.setVisibility(View.VISIBLE);
                String text = "Score : " + gameGrid.getGridScore();
                gameScoreView.setText(text);
                gameScoreView.setBackgroundColor(Color.parseColor("#edc850"));
            }
        });
    }

    public void gameOver()
    {
        // game is lost
        gameOver = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView gameOverView = findViewById(R.id.gameOver);
                String text = "Perdu ! Score final : " + gameGrid.getGridScore();
                gameOverView.setText(text);
                gameOverView.setBackgroundColor(Color.parseColor("#f65e3b"));
                gameOverView.setVisibility(View.VISIBLE);
                TextView retryButtonView = findViewById(R.id.retryButton);
                retryButtonView.setVisibility(View.VISIBLE);
                retryButtonView.setText("Retry");
                TextView nextTileView = findViewById(R.id.nextTile);
                nextTileView.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void resetGrid()
    {
        gameOver = false;
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                Cell cell = gameGrid.getCell(i, j);
                resetCell(cell);
            }
        }
        tile = new Tile(2);
        showNextTile(tile);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView gameOverView = findViewById(R.id.gameOver);
                TextView retryButtonView = findViewById(R.id.retryButton);
                gameOverView.setText("");
                TextView nextTileView = findViewById(R.id.nextTile);
                nextTileView.setVisibility(View.VISIBLE);
                gameOverView.setVisibility(View.INVISIBLE);
                retryButtonView.setVisibility(View.INVISIBLE);
            }
        });
        updateGameScore();
    }
}