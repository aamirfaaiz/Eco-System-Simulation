import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * A simple model of a squirrel.
 * squirrels age, move, breed, and die.
 *
 * @author Aamir Faaiz
 * @version 2019-FEB
 */

public class Squirrel extends Animal {

    // Characteristics shared by all squirrel (class variables).

    // The age at which a  squirrel can start to breed.
    private static final int BREEDING_AGE = 4;
    // The age to which a squirrel can live.
    private static final int MAX_AGE = 150;
    // The likelihood of a squirrel breeding during the day.
    private static final double DAY_BREEDING_PROBABILITY = 0.02;
    // The likelihood of a squirrel breeding in the night.
    private static final double NIGHT_BREEDING_PROBABILITY = 0.06;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 5;

    // The food value of a single plant. In effect, this is the
    // number of steps a Squirrel can go before it has to eat again.
    private static int PLANT_FOOD_VALUE = 12;

    

    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();

    // Individual characteristics (instance fields).

    // The squirrel's age.
    private int age;

    //The squirrel's food level
    private int foodLevel;

    public Squirrel(boolean randomAge, Field field, Location location){

        super(field, location);

        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(PLANT_FOOD_VALUE);
        }
        else{
            age = 0;
            foodLevel = PLANT_FOOD_VALUE;
        }
    }

    /**
     * This is what the Squirrel does during the day - it runs
     * around. Sometimes it will breed or die of old age.
     * @param newSquirrels A list to return newly born Squirrels.
     * @param weatherType The current weather of the simulation
     */
    public void act(List<Animal> newSquirrels,String weatherType)
    {
            if(weatherType.toLowerCase().equals("sunny") || weatherType.toLowerCase().equals("windy")) {
                incrementAge();
                incrementHunger();
                if(isAlive()){
                   if(adjacentGenderCheck()) {
                       giveBirth(newSquirrels,DAY_BREEDING_PROBABILITY);
                   }

                    // Move towards a source of food if found.
                    Location newLocation =findFood();

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
            else if(weatherType.toLowerCase().equals("rainy")){
                incrementAge();
                int randHungerIncrement = rand.nextInt();
                foodLevel-=randHungerIncrement;
                //do nothing
            }
        }

    /**
     * This is what the squirrel would do during the night cycle
     * It's food level may decrement be either 0 or 1 , this is random
     * Squirrel's will reproduce in the night with a higher probability than during the day
     * @param weatherType The current weather in the simulation
     * @param newSquirrels A list to return newly born Squirrels.
     */

    public void sleep(List<Animal> newSquirrels,String weatherType){

        if((!(weatherType.toLowerCase().equals("rainy"))) || weatherType.toLowerCase().equals("windy")){
            int randHungerIncrement = rand.nextInt();

            //decrementing the food level
            foodLevel-=randHungerIncrement;

            //checking if the squirrel is alive after decrementing its food level
            if(isAlive()){
                //checking if the squirrel in the adjacent location is compatible for reproduction
                if(adjacentGenderCheck()) {
                    giveBirth(newSquirrels,NIGHT_BREEDING_PROBABILITY);
                }
            }
        }
    }


    /**
     * Look for plants adjacent to the current location.
     * Only the first live plant is eaten.
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
            if(animal instanceof Plant) {
                Plant plant = (Plant) animal;

                if(plant.isAlive()) {

                    plant.setDead();
                    foodLevel = PLANT_FOOD_VALUE;
                    return where;}

            }
        }
        return null;
    }

    /**
     * Increase the age.
     * This could result in the squirrel's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }

    /**
     * Check whether or not this squirrel is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newSquirrels A list to return newly born squirrels.
     * @param breedingProbability this is the breeding probability of the squirrel.
     */
    private void giveBirth(List<Animal> newSquirrels,double breedingProbability)
    {
        // New squirrels are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed(breedingProbability);
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Squirrel young = new Squirrel(false, field, loc);
            newSquirrels.add(young);
        }
    }

    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @param breedingProbability This is the breeding probability of the squirrel
     * @return The number of births (may be zero).
     */
    private int breed(double breedingProbability)
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= breedingProbability) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A squirrel can breed if it has reached a particular age, if its food value is of a certain value and
     * if its gender is female/F
     * @return true if the squirrel can breed, false otherwise.
     */
    private boolean canBreed()
    {
        //checking if the squirrel in the field can give birth
        if(age>=BREEDING_AGE && foodLevel>=PLANT_FOOD_VALUE/4 && getGender()=='F'){
            return true;
        }
        return false;
    }

    /**
     * This method will increment the squirrels hunger. This can result in the death of the squirrel.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }

    /**
     * This method will check adjacent Locations for a squirrel of opposite gender
     * @return true if the squirrel in the adjacent location is of opposite gender
     */

    private boolean adjacentGenderCheck() {

        boolean state = false;
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Squirrel) {
                Squirrel adjacentSquirrel = (Squirrel) animal;

                if(adjacentSquirrel.isAlive()) {
                    if(adjacentSquirrel.getGender()!=this.getGender()){
                        state = true;
                        break;
                    }
                }
            }
        }
        return state;
    }

}



