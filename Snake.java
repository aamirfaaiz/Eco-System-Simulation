import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a snake.
 * Snakes age, move, eat squirrels and die.
 *
 * @author Aamir Faaiz
 * @version 2018 Feb
 */

public class Snake extends Animal {
    // Characteristics shared by all snakes (class variables).

    // The age at which snake can start to breed.
    private static final int BREEDING_AGE = 6;
    // The age to which a Snake can live.
    private static final int MAX_AGE = 70;

    // The likelihood of a Snake breeding during the day.
    private static final double DAY_BREEDING_PROBABILITY = 0.01;

    // The likelihood of a Snake breeding during the night.
    private static final double NIGHT_BREEDING_PROBABILITY = 0.07;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 40;
    // The food value of a single squirrel. In effect, this is the
    // number of steps a Snake can go before it has to eat again.
    private static final int SQUIRREL_FOOD_VALUE = 8;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();

    // Individual characteristics (instance fields).
    // The Snake's age.
    private int age;
    // The Snake's food level, which is increased by eating squirrels.
    private int foodLevel;



    public Snake(boolean randomAge, Field field, Location location) {
        super(field, location);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(SQUIRREL_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = SQUIRREL_FOOD_VALUE;
        }
    }

    /**
     * This is what the Snake does most of the time: it hunts for
     * squirrels. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param newSnakes A list to return newly born Snakes.
     * @param weatherType The current state of weather in the simulation.
     */
    public void act(List<Animal> newSnakes, String weatherType)
    {
            if (weatherType.toLowerCase().equals("sunny") || weatherType.toLowerCase().equals("windy")) {
                incrementHunger();
                incrementAge();
                if (isAlive()) {
                    giveBirth(newSnakes, DAY_BREEDING_PROBABILITY);

                    // Move towards a source of food if found.
                    Location newLocation = findFood();
                    if (newLocation == null) {
                        // No food found - try to move to a free location.
                        newLocation = getField().freeAdjacentLocation(getLocation());
                    }
                    // See if it was possible to move.
                    if (newLocation != null) {
                        setLocation(newLocation);
                    } else {
                        // Overcrowding.
                        setDead();
                    }
                }
            }

            else if(weatherType.toLowerCase().equals("rainy")){
                sleep(newSnakes,weatherType);
            }

    }

    /**
     * This is what the snake will not move/sleep in the night and breed
     * @param weatherType The current weather in the simulation
     * @param newSnakes A list to return newly born Snakes.
     */

    public void sleep(List<Animal> newSnakes,String weatherType){
        if(!(weatherType.toLowerCase().equals("rainy"))){
            incrementAge();
            int randHungerIncrement = rand.nextInt();
            foodLevel-=randHungerIncrement;
            if(isAlive()){
                giveBirth(newSnakes,NIGHT_BREEDING_PROBABILITY);
            }
        }
    }

    /**
     * Increase the age. This could result in the Snake's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    /**
     * Make this Snake more hungry. This could result in the Snake's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }

    /**
     * Check whether or not this Snake is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newSnakes A list to return newly born Snake.
     * @param breedingProbability The breeding probability of the snake
     */
    private void giveBirth(List<Animal> newSnakes,double breedingProbability)
    {
        // New Cats are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed(breedingProbability);
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Snake young = new Snake(false, field, loc);
            newSnakes.add(young);
        }
    }

    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     * @param breedingProbability The breeding probability of the snake
     */
    private int breed(double breedingProbability)
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <=  breedingProbability) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A Snake can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        if(age>=BREEDING_AGE && foodLevel>=SQUIRREL_FOOD_VALUE/4 && getGender()=='F'){
            return true;
        }
        return false;

    }

    /**
     * Look for squirrels adjacent to the current location.
     * Only the first live squirrel is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Squirrel) {
                Squirrel squirrel = (Squirrel) animal;

                if(squirrel.isAlive()) {//

                    squirrel.setDead();
                    foodLevel = SQUIRREL_FOOD_VALUE;
                    return where;}

            }
        }
        return null;
    }

    /**
     *
     * @return true if the gender of the squirrel in a adjacent field is of opposite gender
     */

    private boolean adjacentGenderCheck() {

        boolean state = false;
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Snake) {
                Snake adjacentSnake = (Snake) animal;

                if(adjacentSnake.isAlive()) {
                    if(adjacentSnake.getGender()!=this.getGender()){
                        state = true;
                        break;
                    }
                }
            }
        }
        return state;
    }












}
