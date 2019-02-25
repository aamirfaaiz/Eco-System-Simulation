import java.util.List;
import java.util.Iterator;
import java.util.Random;


/**
 * A simple model of an owl.
 * Owls age, move, eat mice or squirrels, and die.
 *
 * @author Aamir Faaiz
 * @version 2019-FEB
 */

public class Owl extends Animal {

    // Characteristics shared by all owls (class variables).

    // The age at which a  can start to breed.
    private static final int BREEDING_AGE = 10;
    // The age to which a  can live.
    private static final int MAX_AGE = 135;
    // The likelihood of a  breeding.
    private static final double BREEDING_PROBABILITY = 0.03;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 6;
    // The food value of a single rabbit. In effect, this is the
    // number of steps a  can go before it has to eat again.
    private static final int MOUSE_FOOD_VALUE = 10;
    // The food value of a single squirrel. In effect, this is the
    // number of steps a  can go before it has to eat again.
    private static final int SQUIRREL_FOOD_VALUE = 4;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();

    // Individual characteristics (instance fields).
    // The owls's age.
    private int age;
    // The owls's food level, which is increased by eating mice/squirrels.
    private int foodLevel;

    /**
     * Create an owl. A wol can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     *
     * @param randomAge If true, the owl will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */


    public Owl(boolean randomAge, Field field, Location location){

        super(field, location);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(MOUSE_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = MOUSE_FOOD_VALUE;
        }

    }

    /**
     *This is what the owl would do during the day, which is essentially nothing
     * @param weatherType The current weather in the simulation
     * @param newOwls A list to return newly born Owls.
     */

    public void act(List<Animal> newOwls, String weatherType)
    {
       /* int randHungerIncrement = rand.nextInt(4);
        foodLevel-=randHungerIncrement;*/

    }

    /**
     * This is what the owl would do during the night cycle
     *
     * Owls will reproduce in the night and hunt for either squirrels or rabbit
     * @param weatherType The current weather in the simulation
     * @param newOwls A list to return newly born Owls.
     */

    public void sleep(List<Animal> newOwls,String weatherType){

        if ((!(weatherType.toLowerCase().equals("rainy"))) || weatherType.toLowerCase().equals("windy")){
                incrementAge();
                incrementHunger();
                if(isAlive()) {
                giveBirth(newOwls);

            // Move towards a source of food if found.
            Location newLocation = findFood();
            if (newLocation == null) {
                // No food found - try to move to a free location.
                newLocation = getField().freeAdjacentLocation(getLocation());
            }
            // See if it was possible to move.
            if (newLocation != null) {
                setLocation(newLocation);
            }
            else {
                // Overcrowding.
                setDead();
            }

                }
    }
            else{

                act(newOwls, weatherType);
            }

        }


    /**
     * Increase the age. This could result in the owl's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    /**
     * Make this owl more hungry. This could result in the owl's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }

    /**
     * Look for rabbits or squirrels adjacent to the current location.
     * Only the first live rabbit/squirrel is eaten.
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
            if(animal instanceof Mouse) {
                Mouse mouse = (Mouse) animal;

                if(mouse.isAlive()) {
                    mouse.setDead();
                    foodLevel = MOUSE_FOOD_VALUE;
                    return where;
                }
            }

            else if(animal instanceof Squirrel){
                Squirrel squirrel = (Squirrel) animal;

                if(squirrel.isAlive()) {
                    squirrel.setDead();
                    foodLevel = SQUIRREL_FOOD_VALUE;
                    return where;
                }

            }
        }
        return null;
    }

    /**
     * Check whether or not this owl is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newOwls A list to return newly born owls.
     */
    private void giveBirth(List<Animal> newOwls)
    {
        // New owls are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Owl young = new Owl(false, field, loc);
            newOwls.add(young);
        }
    }




    /**
     * This method will check adjacent Locations for an owl of opposite gender
     * @return true if the owl in the adjacent location is of opposite gender
     */
    private boolean adjacentGenderCheck() {

        boolean state = false;
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Owl) {
                Owl adjacentOwl = (Owl) animal;

                if(adjacentOwl.isAlive()) {
                    if(adjacentOwl.getGender()!=this.getGender()){
                        state = true;
                        break;
                    }
                }
            }
        }
        return state;
    }


    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }


    /**
     * An Owl can breed if it has reached the breeding age,
     * if its foodLevel is of a certain value and if it's gender is female/F
     */
    private boolean canBreed()
    {
        if(age>= BREEDING_AGE && foodLevel>=3 && getGender()=='F'){
            return true;
        }
        return false;
    }
}









