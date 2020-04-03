package minesweeper;
import javafx.util.Duration;
import sun.audio.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Minesweepers extends Application {

    private static final int TileCount = 40;
    private static final int W = 800;
    private static final int H = 600;

    private static final int horTiles = W / TileCount;
    private static final int verTiles = (H) / TileCount;

    private Tile[][] grid = new Tile[horTiles][verTiles];
    private Scene scene;

    public long mines;
    int nmines;

    public static Timer timer;
    public int secondsPassed=0;

    MediaPlayer mediaPlayer;

    private Parent createContent() throws IOException {

        VBox rootNode = new VBox();
        Pane content = new Pane();
        content.setPrefSize(W, H);

        MenuBar menuBar=createMenu();

        FXMLLoader loader = new
                FXMLLoader(getClass().getResource("mine.fxml"));
        rootNode= loader.load();

        //Pane menuPane=(Pane) root.getChildren().get(0);
       //  menuPane.getChildren().add(menuBar);


       content.setPrefSize(W, H);

        for (int y = 0; y < verTiles; y++) {
            for (int x = 0; x < horTiles; x++) {
                Tile tile = new Tile(x, y, Math.random() < 0.2);

                grid[x][y] = tile;
                content.getChildren().add(tile);
            }
        }

        for (int y = 0; y < verTiles; y++) {
            for (int x = 0; x < horTiles; x++) {
                Tile tile = grid[x][y];

                if (tile.ismine){

                    continue;
                }


                 mines = getNeighbors(tile).stream().filter(t -> t.ismine).count();

                if (mines > 0)
                    tile.text.setText(String.valueOf(mines));
            }
        }

        rootNode.getChildren().addAll(menuBar, content);


        return rootNode;
    }

    private List<Tile> getNeighbors(Tile tile) {



        List<Tile> neighbors = new ArrayList<>();


        int[] points = new int[] {
                -1, -1, -1, 0, -1, 1, 0, -1, 0, 1, 1, -1, 1, 0, 1, 1};

        for (int i = 0; i < points.length; i++) {
            int dx = points[i];
            int dy = points[++i];

            int nX = tile.x + dx;
            int nY = tile.y + dy;

            if (nX >= 0 && nX < horTiles
                    && nY >= 0 && nY < verTiles) {
                neighbors.add(grid[nX][nY]);

                if (grid[nX][nY].ismine)
                nmines++;
            }


        }


        return neighbors;
    }

    private class Tile extends StackPane {
        private int x, y;
        private boolean ismine;
        private boolean isOpen = false;

        private Rectangle border = new Rectangle(TileCount - 2, TileCount- 2);
        private Text text = new Text();

        public Tile(int x, int y, boolean ismine) {
            this.x = x;
            this.y = y;
            this.ismine = ismine;

            border.setStroke(Color.LIGHTGRAY);

            text.setFont(Font.font(18));
            text.setText(ismine ? "X" : "");
            text.setVisible(false);

            getChildren().addAll(border, text);

            setTranslateX(x * TileCount);
            setTranslateY(y * TileCount);



            setOnMouseClicked(e -> {
                try {
                    String path =new File("src/media/button.mp3").getAbsolutePath();

                    Media mediafile= new Media(new File(path).toURI().toString());
                    mediaPlayer=new MediaPlayer(mediafile);

                    mediaPlayer.play();

                    mediaPlayer.setVolume(0.1);



                    check();


                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }

        public void check() throws IOException {
            if (isOpen) {

                return;
            }

            if (ismine) {

                String path =new File("src/media/soundg.wav").getAbsolutePath();

                Media mediafile= new Media(new File(path).toURI().toString());
                mediaPlayer=new MediaPlayer(mediafile);

                mediaPlayer.play();

                mediaPlayer.setVolume(0.1);







                for (int y = 0; y < verTiles; y++) {
                    for (int x = 0; x < horTiles; x++) {
                        if (grid[x][y].ismine) {

                            Tile tileg=grid[x][y];
                            System.out.println(x+" "+y);
                            tileg.text.setFill(Color.RED);
                            tileg.text.setFont(Font.font(18));
                            tileg.text.setText("X");
                            tileg.text.setVisible(true);


                        }
                    }
                }





                System.out.println("Game Over");
                Alert alertovver=new Alert(Alert.AlertType.WARNING);

                alertovver.setTitle("Game Over");
                alertovver.setHeaderText("MineStuck!");
                alertovver.setContentText("Ah Oh, Game over...Try Again$$");

                ButtonType yesButton=new ButtonType("Again?");
                ButtonType noButton=new ButtonType("Exit?");
                alertovver.getButtonTypes().setAll(yesButton,noButton);
                
                Optional<ButtonType> clickedButton=alertovver.showAndWait();
                if(clickedButton.isPresent() && clickedButton.get()==noButton)
                {
                    Platform.exit();
                }

                scene.setRoot(createContent());
                return;
            }

            isOpen = true;
            text.setVisible(true);
            border.setFill(null);

            if (text.getText().isEmpty()) {
                for (Tile tile : getNeighbors(this)) {
                    tile.check();
                }
            }
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        scene = new Scene(createContent());


        timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                secondsPassed++;
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 1000);



        stage.setTitle("MineSweeper$$");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    private MenuBar createMenu(){
        Menu fileMenu=new Menu("File"
        );
        MenuItem newMenuItem=new MenuItem("New Game");
        newMenuItem.setOnAction(event -> {
            try {
                 scene.setRoot(createContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        SeparatorMenuItem separatorMenuItem=new SeparatorMenuItem();
        MenuItem resetMenuItem=new MenuItem("Reset Game");
        resetMenuItem.setOnAction(event -> {
            try {
                 scene.setRoot(createContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        MenuItem quitMenuItem=new MenuItem("Quit");
        quitMenuItem.setOnAction(event -> Platform.exit());

        fileMenu.getItems().addAll(newMenuItem,separatorMenuItem,quitMenuItem);

        Menu helpMenu=new Menu("Help");

        MenuItem aboutgmMenuItem=new MenuItem("About Game");
        aboutgmMenuItem.setOnAction(event -> aboutGame());

        helpMenu.getItems().add(aboutgmMenuItem);
        MenuBar menuBar=new MenuBar();
        menuBar.getMenus().addAll(fileMenu,helpMenu);
        return menuBar;
    }
    private void aboutGame() {
        Alert alertgm=new Alert(Alert.AlertType.INFORMATION);
        alertgm.setTitle("Game Rules");
        alertgm.setHeaderText("How to Play");
        alertgm.setContentText("Play till you discover all flags. If met with a mine then its all over");
        alertgm.show();
        Button yButton=new Button();
        yButton.setOnAction(event -> Platform.exit());
    }

}
