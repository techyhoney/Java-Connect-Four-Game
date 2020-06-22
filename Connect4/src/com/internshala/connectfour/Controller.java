package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int DIAMETER = 80;
    private static final String discColor1 = "24303E";
    private static final String discColor2  = "4CAA88";
    private Disc[][] insertedDiscArray = new Disc[ROWS][COLUMNS];
    private static String PLAYER_ONE = "Player One";
    private static String PLAYER_TWO = "Player Two";
    private static boolean isPlayerOneTurn = true;
    private boolean isAllowedToInsert =true; // flag for double click bug

    @FXML
    public GridPane rootGridPane;
    @FXML
    public  Pane insertedDiscsPane;
    @FXML
    public VBox vBox;
    @FXML
    public TextField playerOneField;
    @FXML
    public TextField playerTwoField;
    @FXML
    public Button setNamesButton;
    @FXML
    public Label playerNameLabel;
    public void createPlayground()
    {

        Shape rectangleWithHoles = gameStructureGrid();
        rootGridPane.add(rectangleWithHoles,0,1);

        List<Rectangle> rectangleList  = createClickableColumn();
        for(Rectangle rectangle : rectangleList)
            rootGridPane.add(rectangle,0,1);

        setNamesButton.setOnMouseClicked(event -> setNames());

    }



    private Shape gameStructureGrid()
    {
        Shape rectangleWithHoles = new Rectangle((COLUMNS+1)*DIAMETER,(ROWS+1)*DIAMETER);
        for (int rows = 0; rows <ROWS; rows++) {
            for (int coloumns = 0; coloumns < COLUMNS; coloumns++) {
                Circle circle =new Circle();
                circle.setRadius(DIAMETER/2);
                circle.setCenterX(DIAMETER/2);
                circle.setCenterY(DIAMETER/2);
                circle.setSmooth(true);
                circle.setTranslateX(coloumns*(DIAMETER+5)+DIAMETER/4);
                circle.setTranslateY(rows*(DIAMETER+5)+DIAMETER/4);
                rectangleWithHoles= Shape.subtract(rectangleWithHoles,circle);
            }
        }
        rectangleWithHoles.setFill(Color.WHITE);
        return rectangleWithHoles;
    }

    private List<Rectangle> createClickableColumn() {
        List<Rectangle> rectangleList = new ArrayList<>();
        for (int col = 0; col < COLUMNS; col++) {
            Rectangle rectangle = new Rectangle(DIAMETER,(ROWS+1)*DIAMETER);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(col*(DIAMETER+5)+DIAMETER/4);

            rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
            rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

            final int column = col;
            rectangle.setOnMouseClicked(event ->{
                if(isAllowedToInsert)
                {  isAllowedToInsert=false;
                    insertDisc(new Disc(isPlayerOneTurn),column);

                }

            });
            rectangleList.add(rectangle);
        }
        return rectangleList;
    }
    private void insertDisc(Disc disc,int column)
    {
        int row = ROWS -1;
        while(row >=0)
        {
            if(getDiscIfPresent(row,column)==null)
                break;
            row--;
        }
        if(row<0)
            return;;
        insertedDiscArray[row][column] = disc;
        insertedDiscsPane.getChildren().add(disc);
        disc.setTranslateX(column*(DIAMETER+5)+DIAMETER/4);
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);
        translateTransition.setToY(row*(DIAMETER+5)+DIAMETER/4);

        int finalRow = row;
        translateTransition.setOnFinished(event -> {
            isAllowedToInsert=true;
            if(gameEnded(finalRow,column))
            {
                gameOver();
            }
            isPlayerOneTurn = !isPlayerOneTurn;
            playerNameLabel.setText(isPlayerOneTurn?PLAYER_ONE:PLAYER_TWO);

        });
        translateTransition.play();
    }

    private void gameOver() {
        String winner = isPlayerOneTurn?PLAYER_ONE:PLAYER_TWO;
        System.out.println("Winner is "  +  winner);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("The Winner is " + winner);
        alert.setTitle("Results");
        alert.setContentText("Wanna Play Again ?");
        ButtonType yesBtn = new ButtonType("Yes");
        ButtonType noBtn = new ButtonType("No");
        alert.getButtonTypes().setAll(yesBtn,noBtn);
        Platform.runLater(() ->{
            Optional<ButtonType> clickedBtn = alert.showAndWait();
            if( clickedBtn.isPresent() && (clickedBtn.get() == yesBtn))
            {
                resetGame();
            }
            else{
                Platform.exit();
                System.exit(0);
            }
        });

    }

    public void resetGame() {
        insertedDiscsPane.getChildren().clear();
        for (int row = 0; row < insertedDiscArray.length ; row++) {
            for (int col = 0; col < insertedDiscArray[row].length; col++) {
                insertedDiscArray[row][col] = null;
            }
            isPlayerOneTurn=true; // let player one start the game
            playerNameLabel.setText(PLAYER_ONE);
            playerOneField.clear();
            playerTwoField.clear();
            PLAYER_ONE ="Player One";
            PLAYER_TWO = "Player Two";
            createPlayground();
        }
    }

    private boolean gameEnded(int row,int column)
    {
        List<Point2D> verticalPoints = IntStream.rangeClosed(row-3,row+3)
                .mapToObj(r -> new Point2D(r,column))
                .collect(Collectors.toList());

        List<Point2D> horizontalPoints = IntStream.rangeClosed(column-3,column+3)
                .mapToObj(col -> new Point2D(row,col))
                .collect(Collectors.toList());

        Point2D startPoint1 = new Point2D(row-3,column+3);
        List<Point2D> diagonalPoints1 = IntStream.rangeClosed(0,6)
                .mapToObj(i->startPoint1.add(i,-i))
                .collect(Collectors.toList());

        Point2D startPoint2 = new Point2D(row-3,column-3);
        List<Point2D> diagonalPoints2 = IntStream.rangeClosed(0,6)
                .mapToObj(i->startPoint2.add(i,i))
                .collect(Collectors.toList());


        boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints)
                || checkCombinations(diagonalPoints1) || checkCombinations(diagonalPoints2);
        return isEnded;
    }

    private boolean checkCombinations(List<Point2D> points) {
        int chain=0;
        for (Point2D point: points) {
            int rowIndexForArray = (int) point.getX();
            int columnIndexForArray = (int) point.getY();
            Disc disc = getDiscIfPresent(rowIndexForArray, columnIndexForArray);
            if (disc != null && disc.isPlayerOne == isPlayerOneTurn) {
                chain++;
                if (chain == 4)
                    return true;

            }else
                chain = 0;
        }

        return false;
    }

    private Disc getDiscIfPresent(int row,int column)
    {
        if(row >= ROWS || row <0 || column >=COLUMNS || column <0)
            return null;
        return insertedDiscArray[row][column];

    }


    private static class Disc extends Circle{
        private final boolean isPlayerOne;

        public Disc(boolean isPlayerOne)
        {
            this.isPlayerOne=isPlayerOne;
            setRadius(DIAMETER/2);
            setFill(isPlayerOne?Color.valueOf(discColor1):Color.valueOf(discColor2));
            setCenterX(DIAMETER/2);
            setCenterY(DIAMETER/2);
        }
    }

    public void setNames()
    {
        PLAYER_ONE = playerOneField.getText();
        PLAYER_TWO = playerTwoField.getText();
        playerNameLabel.setText(isPlayerOneTurn?PLAYER_ONE:PLAYER_TWO);

    }



    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
