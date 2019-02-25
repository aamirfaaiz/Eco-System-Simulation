import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * A simple model of a Mouse.
 * Mice age, move, breed, and die.
 * 
 * @author Aamir Faaiz
 * @version 2019-FEB
 */
public class Mouse extends Animal
{
    // Characteristics shared by all Mice (class variables).

    // The age at which a Mouse can start to breed.
    private static final int BREEDING_AGE = 4;
    // The age to which a Mouse can live.
    private static final int MAX_AGE = 100;
    // The likelihood of a Mouse breeding during the day.
    private static final double DAY_BREEDING_PROBABILITY = 0.02;
    // The likelihood of a Mouse breeding during the night.
    private static final double NIGHT_BREEDING_PROBABILITY = 0.06;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 6;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();

    // Individual characteristics (instance fields).

    // The food value of a single plant. In effect, this is the
    // number of steps a Mouse can go before it has to eat again.
    private static final int PLANT_FOOD_VALUE = 10;
    
    // The Mouse's age.
    private int age;

    //The food level of the mouse
    private int foodLevel;

    /**
     * Create a new Mouse. A Mouse may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the Mouse will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Mouse(boolean randomAge, Field field, Location location)
    {
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
     * This is what the Mouse does most of the time - it runs 
     * around. Sometimes it will breed or die of old age.
     * @param newMice A list to return newly born Mice.
     * @param weatherType The current weather of the simulation
     */
    public void act(List<Animal> newMice,String weatherType)
    {
        if (weatherType.toLowerCase().equals("sunny") || weatherType.toLowerCase().equals("windy")){
                incrementAge();
                incrementHunger();
                if(isAlive()) {
                    if(adjacentGenderCheck()){ giveBirth(newMice, DAY_BREEDING_PROBABILITY); }
                    // Try to move into a free location.
                    Location newLocation = findFood();


                    if (newLocation == null) {
                        // No food found - try to move to a free location.
                        newLocation = getField().freeAdjacentLocation(getLocation());
                    }

                    if (newLocation != null) {
                        setLocation(newLocation);
                    } else {
                        // Overcrowding.
                        setDead();
                    }
                }
            }
            else if(weatherType.toLowerCase().equals("rainy")){
                incrementAge();
                int randHungerIncrement = rand.nextInt();
                foodLevel-=randHungerIncrement;
                if(isAlive()){
                    if(adjacentGenderCheck()){ giveBirth(newMice,NIGHT_BREEDING_PROBABILITY); }
                }

            }

    }

    /**
     * This is what the cat would do during the night
     * It will not move, it's hunger gets randomly decremented every time this method gets invoked
     * The mouse will reproduce with a breeding probability
     * @param newMice newMice A list to return newly born Mice.
     * @param weatherType The current weather in the simulation
     */


    public void sleep(List<Animal> newMice,String weatherType){
        if((!(weatherType.toLowerCase().equals("rainy"))) || weatherType.toLowerCase().equals("windy")){
            int randHungerIncrement = rand.nextInt();
            foodLevel-=randHungerIncrement;

            //checking if the mouse is alive after decrementing the hunger
            if(isAlive()){
                //
               if(adjacentGenderCheck()){
                   giveBirth(newMice,NIGHT_BREEDING_PROBABILITY);
               }
            }
        }
    }



    /**
     *
     * @return true if the gender of the mouse in a adjacent field is of opposite gender
     */

    private boolean adjacentGenderCheck() {

        boolean state = false;
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Mouse) {
                Mouse adjacentMouse = (Mouse) animal;

                if(adjacentMouse.isAlive()) {
                    if(adjacentMouse.getGender()!=this.getGender()){
                        state = true;
                        break;
                    }
                }
            }
        }
        return state;
    }
    /**
     * Increase the age.
     * This could result in the Mouse's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this Mouse is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newMice A list to return newly born Mice.
     */
    private void giveBirth(List<Animal> newMice, double breedingProbability)
    {
        // New Mice are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed(breedingProbability);
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Mouse young = new Mouse(false, field, loc);
            newMice.add(young);
        }
    }
        
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @param breedingProbability The breeding probability of the Mouse
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
     * A Mouse can breed if it has reached a certain age
     * and if it's food level is of a certain value and if its gender is female
     * @return true if the Mouse can breed, false otherwise.
     */
    private boolean canBreed()
    {

        if(age>=BREEDING_AGE  && foodLevel>=PLANT_FOOD_VALUE/4 && getGender()=='F'){
            return true;
        }

        return false;
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
     * Make this Mouse more hungry. This could result in the death of the mouse
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
}
