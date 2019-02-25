import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a cat.
 * Cats age, move, eat mice, and die.
 * 
 * @author Aamir Faaiz
 * @version 2019-FEB
 */
public class Cat extends Animal
{
    // Characteristics shared by all cats (class variables).
    
    // The age at which a cats can start to breed.
    private static final int BREEDING_AGE = 6;
    // The age to which a Cat can live.
    private static final int MAX_AGE = 140;
    // The likelihood of a Cat breeding in the day.
    private static final double DAY_BREEDING_PROBABILITY = 0.08;//Probability of breeding is lower during the day
    // The likelihood of a Cat breeding in the night.
    private static final double NIGHT_BREEDING_PROBABILITY = 0.24;//Probability of breeding is higher during night
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 5;
    // The food value of a single mouse. In effect, this is the
    // number of steps a Cat can go before it has to eat again.
    private static final int MOUSE_FOOD_VALUE = 8;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    // Individual characteristics (instance fields).
    // The Cat's age.
    private int age;
    // The Cat's food level, which is increased by eating mice.
    private int foodLevel;


    /**
     * Create a Cat. A Cat can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the Cat will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Cat(boolean randomAge, Field field, Location location)
    {
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
     * This is what the Cat does during the day: it hunts for
     * mice. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param weatherType The current weather in the simulation
     * @param newCats A list to return newly born Cats.
     */
    public void act(List<Animal> newCats , String weatherType)
    {
            if (weatherType.toLowerCase().equals("sunny") || weatherType.toLowerCase().equals("windy")) {
                incrementHunger();
                incrementAge();
                //checking if the cat is alive after incrementing its hunger and age
                if (isAlive()) {
                    //checking if the cat's are compatible for reproduction
                    if (adjacentGenderCheck()) {
                        giveBirth(newCats, DAY_BREEDING_PROBABILITY);
                    }

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

                sleep(newCats,weatherType);
            }


    }

    /**
     * This is what the cat would do during the night cycle
     * It's food level may decrement be either 0 or 1 , this is random
     * Cats will reproduce in the night with a higher probability than during the day
     * @param weatherType The current weather in the simulation
     * @param newCats A list to return newly born Cats.
     */

    public void sleep(List<Animal> newCats,String weatherType){

        if((!(weatherType.toLowerCase().equals("rainy"))) || weatherType.toLowerCase().equals("windy")){
            int randHungerIncrement = rand.nextInt(1);
            foodLevel-=randHungerIncrement;//decrementing foodLevel of cat
            //checking if the cat is alive after incrementing its hunger
            if(isAlive()){
                //checking if the cat's are compatible for reproduction
                if(adjacentGenderCheck()){
                    giveBirth(newCats,NIGHT_BREEDING_PROBABILITY);
                }
            }
        }
    }

    /**
     * Increase the age. This could result in the Cat's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this Cat more hungry. This could result in the Cat's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for mice adjacent to the current location.
     * Only the first live mouse is eaten.
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
                
                if(mouse.isAlive()) {//

                    mouse.setDead();
                    foodLevel = MOUSE_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }


    
    /**
     * Check whether or not this Cat is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newCats A list to return newly born Cats.
     * @param breedingProbability breeding probability of the cat
     */
    private void giveBirth(List<Animal> newCats,double breedingProbability)
    {
        // New Cats are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed(breedingProbability);
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Cat young = new Cat(false, field, loc);
            newCats.add(young);
        }
    }

    /**
     * This method will check adjacent Locations for a cat of opposite gender
     * @return true if the cat in the adjacent location is of opposite gender
     */

    private boolean adjacentGenderCheck() {

        boolean state = false;
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Cat) {
                Cat adjacentCat = (Cat) animal;

                if(adjacentCat.isAlive()) {
                    if(adjacentCat.getGender()!=this.getGender()){
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
     * @param breedingProbability the breeding probability of the cat
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
     * A Cat can breed if it has reached the breeding age,
     * if its foodLevel is of a certain value and if it's gender is female/F
     */
    private boolean canBreed()
    {
        if(age>=BREEDING_AGE && foodLevel>=MOUSE_FOOD_VALUE/4 && getGender()=='F'){
            return true;
        }
        return false;
    }
}
