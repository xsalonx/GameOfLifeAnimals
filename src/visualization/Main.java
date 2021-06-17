package visualization;

import cosie.*;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.PriorityQueue;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    static public WorldEvolution getWorldEvolution() {
        ReadFromJson readFromJson = null;
        try {
            readFromJson = new ReadFromJson();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Brak pliku");
            System.exit(1);
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Plik jest niepoprawny");
            System.exit(1);
        }

        World world = new World(readFromJson);

        return new WorldEvolution1(world, readFromJson);
    }

    static public WorldEvolution[] getWorldsEvolutions() {
        WorldEvolution[] worldEvolutions = new WorldEvolution[2];
        worldEvolutions[0] = getWorldEvolution();
        worldEvolutions[1] = worldEvolutions[0].copyWithSameWorld();
        return worldEvolutions;
    }

    static public void plotAnim(Canvas map, GraphicsContext gc, WorldEvolution worldEvolution) {
        double deltaX = (map.getWidth() / worldEvolution.getWidth());
        double deltaY = (map.getHeight() / worldEvolution.getHeight());


        gc.setFill(Color.rgb(35, 165, 10));
        gc.fillRect(0, 0, map.getWidth(), map.getHeight());
        gc.setFill(Color.rgb(250, 255, 120));
        for (Location l : worldEvolution.getPlants()) {
            gc.fillRect(deltaX * l.getX(), deltaY * l.getY(), deltaX, deltaY);
        }

        HashMap<Location, PriorityQueue<Animal>> animalsInLocations = worldEvolution.getAnimalsInLocations();
        for (PriorityQueue<Animal> pq : animalsInLocations.values()) {

            try {
                gc.setFill(Color.rgb(255 * pq.peek().getEnergy() / Animal.maxEnergy, 0, 0));
                if (worldEvolution.getObservedAncestor() != null
                        && pq.stream().anyMatch(animal -> animal == worldEvolution.getObservedAncestor().ancestor)) {
                    gc.setFill(Color.rgb(255, 255, 255));
                } else if (pq.stream().anyMatch(animal -> (animal.getAncestorUnderObservation() != null && animal.getAncestorUnderObservation() == worldEvolution.getObservedAncestor()))){
                    gc.setFill(Color.rgb(0, 0, 255 * pq.peek().getEnergy() / Animal.maxEnergy));
                }
                Location l = pq.peek().getLocation();
                gc.fillRect(deltaX * l.getX(), deltaY * l.getY(), deltaX, deltaY);
            } catch (NullPointerException e) {
                System.out.println("sth is wrong");
            }
        }
    }

    static public void anim(WorldEvolution worldEvolutions, Canvas map, GraphicsContext gc, GridPane data, Stage theStage, Scene scene) {

        new AnimationTimer() {
            long simDelay = 0;

            public void handle(long currentNanoTime) {
                if (!worldEvolutions.isStopped() && worldEvolutions.getLeavingAnimalsNumber() > 1) {
                    try {
                        worldEvolutions.letDayGoBy();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
//                    System.out.println(worldEvolution.getDay());
                }


                plotAnim(map, gc, worldEvolutions);

                Text t = (Text) data.getChildren().get(1);
                t.setText("" + worldEvolutions.getDay());

                t = (Text) data.getChildren().get(3);
                t.setText("" + worldEvolutions.getLeavingAnimalsNumber());

                t = (Text) data.getChildren().get(5);
                t.setText("" + worldEvolutions.getPlants().size());

                t = (Text) data.getChildren().get(6);
                t.setText(worldEvolutions.getCurrMostPopularGenome().toString());

                t = (Text) data.getChildren().get(9);
                t.setText("" + worldEvolutions.getAvgHeathLevel());

                t = (Text) data.getChildren().get(11);
                t.setText("" + worldEvolutions.getAvgLifeLength());

                t = (Text) data.getChildren().get(13);
                t.setText("" + worldEvolutions.getAvgChildrenNumberOfLeaving());
                if (worldEvolutions.getObservedAncestor() != null)

// observed
//                t = (Text) data.getChildren().get(15);
//                t.setText("" + worldEvolutions.getObservedAncestor());
                {
                    t = (Text) data.getChildren().get(17);
                    t.setText("" + (worldEvolutions.getDay() - worldEvolutions.getObservedAncestor().observedFor));

                    t = (Text) data.getChildren().get(19);
                    t.setText("" + worldEvolutions.getObservedAncestor().ancestor.numberOfChildren);

                    t = (Text) data.getChildren().get(21);
                    t.setText("" + worldEvolutions.getObservedAncestor().currNumberOfDescendants);

                    t = (Text) data.getChildren().get(23);
                    t.setText("" + worldEvolutions.getObservedAncestor().ancestor.dayOfDeath);
                }

                t = (Text) data.getChildren().get(25);
                t.setText("" + worldEvolutions.getCurrGenomesOccurence().size());

                try {
                    Thread.sleep(simDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    Scene menu;
    Scene doubleVisScene;
    Scene singleVisScene;

    static public GridPane getData() {
        GridPane gridPane = new GridPane();

        gridPane.add(new Text("dzień:  "), 0, 0);
        gridPane.add(new Text("0"), 1, 0);

        gridPane.add(new Text("liczba żywych:  "),0, 1);
        gridPane.add(new Text("0"),1, 1);

        gridPane.add(new Text("liczba roślin:  "),0, 2);
        gridPane.add(new Text("0"),1, 2);

        gridPane.add(new Text("dominujący genotyp:  "),0, 3);
        gridPane.add(new Text("0"),1, 3);

        gridPane.add(new Text("średnie życie żywych:  "),0, 4);
        gridPane.add(new Text("0"),1, 4);

        gridPane.add(new Text("średnia długość życia martwych:  "),0, 5);
        gridPane.add(new Text("0"),1, 5);

        gridPane.add(new Text("średnia ilość dzieci żyjących:  "),0, 6);
        gridPane.add(new Text("0"),1, 6);
// observed
        gridPane.add(new Text("imię obserwowanego:  "),0, 7);
        gridPane.add(new Text("Darek"),1, 7);

        gridPane.add(new Text("obserwowany przez:  "),0, 8);
        gridPane.add(new Text("0"),1, 8);

        gridPane.add(new Text("liczba dzieci:  "),0, 9);
        gridPane.add(new Text("0"),1, 9);

        gridPane.add(new Text("liczba potomków:  "),0, 10);
        gridPane.add(new Text("0"),1, 10);

        gridPane.add(new Text("umarł w dniu:  "),0, 11);
        gridPane.add(new Text("0"),1, 11);

        gridPane.add(new Text("obecna liczba różnych genomów:  "),0, 12);
        gridPane.add(new Text("0"),1, 12);


        return gridPane;
    }

    static public void addToSetObservedAnimalEvent(Canvas map, Stage theStage, WorldEvolution worldEvolution, Scene scene) {
        EventHandler<MouseEvent> eventHandler = e -> {
            double x;
            double y;
            Robot robot = new Robot();
            x = robot.getMouseX() - theStage.getX();
            y = robot.getMouseY() - theStage.getY();
            if (map.getTranslateX() < x && x < map.getTranslateX() + map.getWidth() &&
                    map.getTranslateY() < y && y < map.getTranslateY() + map.getHeight()
                    && robot.getPixelColor(robot.getMousePosition()).getRed() > 0
                    && robot.getPixelColor(robot.getMousePosition()).getBlue() == 0
                    && robot.getPixelColor(robot.getMousePosition()).getGreen() == 0
                    && worldEvolution.isStopped()
                    && worldEvolution.getObservedAncestor() == null
                    && theStage.getScene() == scene) {
                int locX = (int) (x * worldEvolution.getWidth() / map.getWidth());
                int locy = (int) (y * worldEvolution.getHeight() / map.getHeight());
                Location finLocation;
                Location location = new Location(locX, locy);
                System.out.println(location);
                if (worldEvolution.getAnimalsInLocations().containsKey(location)) {
                    System.out.println("Yes");
                } else if (worldEvolution.getAnimalsInLocations().containsKey(new Location(locX-1, locy))) {
                    System.out.println("yes1");
                } else if (worldEvolution.getAnimalsInLocations().containsKey(new Location(locX, locy + 1))) {
                    System.out.println("yes1");
                } else if (worldEvolution.getAnimalsInLocations().containsKey(new Location(locX + 1, locy))) {
                    System.out.println("yes1");
                } else if (worldEvolution.getAnimalsInLocations().containsKey(new Location(locX, locy - 1))) {
                    System.out.println("yes1");
                }
            }
        };
        map.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);

    }

    static Scene doubleVisSceneMange(Stage theStage, WorldEvolution[] worldEvolutions, Scene menu, Canvas[] map, GraphicsContext[] gc, GridPane[] data) {
        Group root = new Group();
        Canvas map1 = new Canvas(300, 300);
        Canvas map2 = new Canvas(300, 300);
        map1.widthProperty().bind(theStage.widthProperty().multiply(0.4));
        map1.heightProperty().bind(theStage.heightProperty().multiply(0.4));
        map2.widthProperty().bind(theStage.widthProperty().multiply(0.4));
        map2.heightProperty().bind(theStage.heightProperty().multiply(0.4));
        map1.translateXProperty().bind(theStage.widthProperty().multiply(0.03));
        map2.translateXProperty().bind(map1.widthProperty().add(theStage.widthProperty().multiply(0.14)));
        map2.translateYProperty().bind(map1.translateYProperty());
        root.getChildren().addAll(map1, map2);

        map[0] = map1;
        map[1] = map2;
        gc[0] = map1.getGraphicsContext2D();
        gc[1] = map2.getGraphicsContext2D();

        Label label2= new Label("Visualization");
        Button goToMenu= new Button("Go to menu");
        goToMenu.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            theStage.setScene(menu);
            worldEvolutions[0].stop();
            worldEvolutions[1].stop();
        });

        Button runFirst = new Button("run first");
        runFirst.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[0].run();
        });
        Button runSecond = new Button("run second");
        runSecond.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[1].run();
        });
        Button runBoth = new Button("run both");
        runBoth.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[1].run();
            worldEvolutions[0].run();
        });

        Button stopFirst = new Button("stop first");
        stopFirst.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[0].stop();
        });
        Button stopSecond = new Button("stop second");
        stopSecond.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[1].stop();
        });
        Button stopBoth = new Button("stop both");
        stopBoth.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[0].stop();
            worldEvolutions[1].stop();
        });

        Button resetFirst = new Button("reset first");
        resetFirst.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> worldEvolutions[0].restart());
        Button resetSecond = new Button("reset second");
        resetSecond.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> worldEvolutions[1].restart());
        Button resetBoth = new Button("reset both");
        resetBoth.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[0].restart();
            worldEvolutions[0].stop();
            worldEvolutions[1].copyPlantsAndAnimals(worldEvolutions[0]);
            worldEvolutions[1].stop();
        });

        Button firstStartObservation = new Button("first start observation");
        firstStartObservation.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> worldEvolutions[0].startObservation());

        Button firstStopObservation = new Button("first stop observation");
        firstStopObservation.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> worldEvolutions[0].stopObserving());

        Button secondStartObservation = new Button("second start observation");
        secondStartObservation.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> worldEvolutions[1].startObservation());

        Button secondStopObservation = new Button("second stop observation");
        secondStopObservation.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> worldEvolutions[1].stopObserving());

        Button bothStartObservation = new Button("both start observation");
        bothStartObservation.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[1].startObservation();
            worldEvolutions[0].startObservation();
        });

        Button bothStopObservation = new Button("both stop observation");
        bothStopObservation.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[1].stopObserving();
            worldEvolutions[0].stopObserving();

        });

        VBox vBox1= new VBox(0);
        VBox vBox2= new VBox(0);
        VBox vBox3= new VBox(0);
        VBox vBox4= new VBox(0);

        vBox1.getChildren().addAll(label2, goToMenu);
        vBox2.getChildren().addAll(runBoth, stopBoth, resetBoth, bothStartObservation, bothStopObservation);
        vBox3.getChildren().addAll(runFirst, stopFirst, resetFirst, firstStartObservation, firstStopObservation);
        vBox4.getChildren().addAll(runSecond, stopSecond, resetSecond, secondStartObservation, secondStopObservation);

        GridPane gridPaneButtons = new GridPane();
        gridPaneButtons.add(vBox1, 0, 0);
        gridPaneButtons.add(vBox2, 1, 0);
        gridPaneButtons.add(vBox3, 2, 0);
        gridPaneButtons.add(vBox4, 3, 0);

        map1.translateYProperty().bind(gridPaneButtons.heightProperty().add(5));


        data[0] = getData();


        int YTrans = 130;
        data[0].translateYProperty().bind(map1.heightProperty().add(YTrans + map1.getTranslateY()));
        data[0].translateXProperty().bind(map1.translateXProperty());

        data[1] = getData();


        data[1].translateYProperty().bind(map1.heightProperty().add(YTrans + map1.getTranslateY()));
        data[1].translateXProperty().bind(map2.translateXProperty());


        Group root2 = new Group();
        root2.getChildren().addAll(gridPaneButtons, data[0], data[1], root);

        Scene doubleVisScene = new Scene(root2,650,600);
        doubleVisScene.setFill(Color.BEIGE);

        addToSetObservedAnimalEvent(map1, theStage, worldEvolutions[0], doubleVisScene);
        addToSetObservedAnimalEvent(map2, theStage, worldEvolutions[1], doubleVisScene);

        return doubleVisScene;
    }

    static Scene singleVisSceneMange(Stage theStage, WorldEvolution[] worldEvolutions, Scene menu, Canvas[] mapW, GraphicsContext[] gc, GridPane[] data) {
        Group root = new Group();
        Canvas map = new Canvas(300, 300);
        map.widthProperty().bind(theStage.widthProperty().multiply(0.6));
        map.heightProperty().bind(theStage.heightProperty().multiply(0.6));

        map.translateXProperty().bind(theStage.widthProperty().multiply(0.03));

        root.getChildren().add(map);

        mapW[0] = map;
        gc[0] = map.getGraphicsContext2D();

        Label label2= new Label("Visualization");
        Button goToMenu= new Button("Go to menu");
        goToMenu.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            theStage.setScene(menu);
            worldEvolutions[0].stop();
        });

        Button run = new Button("run");
        run.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[0].run();
        });

        Button stop = new Button("stop ");
        stop.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            worldEvolutions[0].stop();
        });

        Button reset = new Button("reset");
        reset.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> worldEvolutions[0].restart());

        Button startObservation = new Button("start observation");
        startObservation.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> worldEvolutions[0].startObservation());

        Button stopObservation = new Button("stop observation");
        stopObservation.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> worldEvolutions[0].startObservation());

        VBox vBox1= new VBox(0);
        VBox vBox2= new VBox(0);


        vBox1.getChildren().addAll(label2, goToMenu);
        vBox2.getChildren().addAll(run, stop, reset, startObservation, stopObservation);


        GridPane gridPaneButtons = new GridPane();
        gridPaneButtons.add(vBox1, 0, 0);
        gridPaneButtons.add(vBox2, 1, 0);


        map.translateYProperty().bind(gridPaneButtons.heightProperty().add(50));



        data[0] = getData();

        data[0].translateXProperty().bind(map.widthProperty().add(30));
        data[0].translateYProperty().bind(map.translateYProperty().add(50));

        Group root2 = new Group();
        root2.getChildren().addAll(gridPaneButtons, data[0], root);

        Scene singleVisScene = new Scene(root2,650,600);
        singleVisScene.setFill(Color.BEIGE);

        addToSetObservedAnimalEvent(map, theStage, worldEvolutions[0], singleVisScene);

        return singleVisScene;
    }


    @Override
    public void start(Stage theStage) throws Exception{
        WorldEvolution[] doubleWorldEvolutions = getWorldsEvolutions();
        WorldEvolution[] singleWorldEvolution = {getWorldEvolution()};


        Canvas[] doubleMap = new Canvas[2];
        GraphicsContext[] doubleGC = new GraphicsContext[2];

        Canvas[] singleMap = new Canvas[1];
        GraphicsContext[] singleGC = new GraphicsContext[1];

        GridPane[] doubleData = new GridPane[2];
        GridPane[] singleData = new GridPane[1];

//menu
        Label label1= new Label("Menu");
        Button doubleMapVisualizationButton= new Button("Go to double visualization");
        doubleMapVisualizationButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> theStage.setScene(doubleVisScene));
        Button singleMapVisualizationButton = new Button("Go to single visualization");
        singleMapVisualizationButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> theStage.setScene(singleVisScene));
        VBox layout1 = new VBox(0);
        layout1.getChildren().addAll(label1, doubleMapVisualizationButton, singleMapVisualizationButton);
        menu = new Scene(layout1, 300, 250);

        doubleVisScene = doubleVisSceneMange(theStage, doubleWorldEvolutions, menu, doubleMap, doubleGC, doubleData);
        singleVisScene = singleVisSceneMange(theStage, singleWorldEvolution, menu, singleMap, singleGC, singleData);


        theStage.setScene(menu);



        anim(doubleWorldEvolutions[0], doubleMap[0], doubleGC[0], doubleData[0], theStage, doubleVisScene);
        anim(doubleWorldEvolutions[1], doubleMap[1], doubleGC[1], doubleData[1], theStage, doubleVisScene);
        anim(singleWorldEvolution[0], singleMap[0], singleGC[0], singleData[0], theStage, singleVisScene);


        theStage.show();
    }



}
