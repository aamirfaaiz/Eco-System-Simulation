import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;

/**
 * A simple predator-prey simulator, based on a rectangular field
 * containing Mouses and foxes.
 * 
 * @author Aamir Faaiz
 * @version 2019-FEB
 */
public class Simulator
{
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 120;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 80;
    // The probability that a cat will be created in any given grid position.
    private static final double CAT_CREATION_PROBABILITY = 0.05;
    // The probability that a Mouse will be created in any given grid position.
    private static final double MOUSE_CREATION_PROBABILITY = 0.20;
    // The probability that a owl will be created in any given grid position.
    private static final double OWL_CREATION_PROBABILITY= 0.02;
    // The probability that a squirrel will be created in any given grid position.
    private static final double SQUIRREL_CREATION_PROBABILITY= 0.18;
    // The probability that a squirrel will be created in any given grid position.
    private static final double PLANT_CREATION_PROBABILITY= 0.5;

    // The probability that a snake will be created in any given grid position.
    private static final double SNAKE_CREATION_PROBABILITY= 0.09;


    
    // List of animals in the field.
    private List<Animal> animals;
    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    // A graphical view of the simulation.
    private List<SimulatorView> views;

    //The current time status ; can be either day/night
    private String timeStatus;

    private int counter;

    //The weather types in this simulation
    private static final String[] weatherType = {"Sunny","Rainy","Windy"};

    //The current weather in the simulation
    private  String weather;


    
    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }

        animals = new ArrayList<>();
        field = new Field(depth, width);

        views = new ArrayList<>();
        SimulatorView view = new GridView(depth, width);
        view.setColor(Mouse.class, Color.ORANGE);
        view.setColor(Cat.class, Color.BLUE);
        view.setColor(Owl.class, Color.RED);
        view.setColor(Plant.class, Color.GREEN);
        view.setColor(Squirrel.class, Color.GRAY);
        view.setColor(Snake.class, Color.YELLOW);
        views.add(view);

        view = new GraphView(500,100,500);
        view.setColor(Mouse.class, Color.ORANGE);
        view.setColor(Cat.class, Color.BLUE);
        view.setColor(Owl.class, Color.RED);
        view.setColor(Plant.class, Color.GREEN);
        view.setColor(Squirrel.class, Color.GRAY);
        view.setColor(Snake.class, Color.YELLOW);
        views.add(view);
        // Setup a valid starting point.
        reset();
    }

    public static void main(String[] args){
        Simulator simulator =  new Simulator();
        simulator.runLongSimulation();
    }
    
    /**
     * Run the simulation from its current state for a reasonably long period,
     * (4000 steps).
     */
    public void runLongSimulation()
    {
        simulate(4000);
    }
    
    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps)
    {
        for(int step = 1; step <= numSteps && views.get(0).isViable(field); step++) {
            simulateOneStep();
            delay(250);//uncomment to make the simulation run faster!
        }
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each
     * fox and Mouse.
     */
    private void simulateOneStep()
    {
        step++;
        counter++;
        setWeather(counter);
        // Provide space for newborn animals.
        List<Animal> newAnimals = new ArrayList<>();
        // Let all Mouses act.

        if(counter<=10){
            timeStatus = "Day"; //setting time status to day
            for(Iterator<Animal> it = animals.iterator(); it.hasNext(); ) {
                Animal animal = it.next();
                animal.act(newAnimals,weather);
                if(! animal.isAlive()) {
                    it.remove();
                }
            }
        }

         else if(counter>10 && counter<=20){

             timeStatus = "Night";
            //animal undergo sleep method
            for(Iterator<Animal> it = animals.iterator(); it.hasNext(); ) {
                Animal animal = it.next();
                animal.sleep(newAnimals,weather);
                if(! animal.isAlive()) {
                    it.remove();
                }
            }
         }
         else{
            //new day
            //therefore counter gets sets to 0
            counter = 0;
        }
        // Add the newly born foxes and Mouses to the main lists.
        animals.addAll(newAnimals);
        

        updateViews();

    }

    /**
     * Reset the simulation to a starting position.
     */
    private void reset()
    {
        step = 0;
        animals.clear();
        populate();
        
        // Show the starting state in the view.
        updateViews();
    }
    
    /**
     * Randomly populate the field with cats,mice,owls,squirrels and plants
     */
    private void populate() {
        Random rand = Randomizer.getRandom();
        field.clear();
        for (int row = 0; row < field.getDepth(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {

                if (rand.nextDouble() <= MOUSE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Mouse mouse = new Mouse(true, field, location);
                    animals.add(mouse);

                } else if (rand.nextDouble() <= CAT_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Cat cat = new Cat(true, field, location);
                    animals.add(cat);
                } else if (rand.nextDouble() <= OWL_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Owl owl = new Owl(true, field, location);
                    animals.add(owl);

                } else if (rand.nextDouble() <= SQUIRREL_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Squirrel squirrel = new Squirrel(true, field, location);
                    animals.add(squirrel);

                } else if (rand.nextDouble() <= PLANT_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Plant plant = new Plant(true, field, location);

                    animals.add(plant);

                }
                else if (rand.nextDouble() <= SNAKE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Snake snake = new Snake(true, field, location);
                    animals.add(snake);

                }
                // else leave the location empty.
            }
        }
    }

    
    /**
     * Pause for a given time.
     * @param millisec  The time to pause for, in milliseconds
     */
    private void delay(int millisec)
    {
        try {
            Thread.sleep(millisec);
        }
        catch (InterruptedException ie) {
            // wake up
        }
        
    }

    /**
     * This method will assign a random weather to weather variable in this class using the weatherType array
     * @param counter
     */
    private void setWeather(int counter){
            Random rand = new Random();
            String randWeather = weatherType[rand.nextInt(weatherType.length)]; //change bound to a value of one to make the weather only sunny

            if(counter<=1){
                weather = randWeather;
            }

            //the weather will randomly change when the counter is equal to 5
            else if(counter == 10){
                weather = randWeather;
            }
            else{
                //do nothing
            }
    }

    /**
     * Update all existing views.
     */
    private void updateViews()
    {
        for (SimulatorView view : views) {
            view.showStatus(step, field);
        }
    }

}
