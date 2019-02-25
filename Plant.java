import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a plant.
 * plants age, make seeds and die.
 *
 * @author Aamir Faaiz
 * @version 2019-FEB
 */


public class Plant extends Animal{

    private static int age;

    private static final int maxAge = 500;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //A plant can start making seeds after 2 steps
    private static final int BREEDING_AGE = 2;
    // A plant can start growing soon after it's born
    private static final int GROWING_AGE = 0 ;// A plant can start growing soon after it's born
    //Maximum number of seeds a plant can produce

    //This is the number of squares a plant can occupy when growing
    private static final int maxSizeIncrease= 5;

    private static final int maxSeeds = 10;
    // The likelihood of a Plant Growing during the day.
    private static final double Day_Growing_PROBABILITY = 0.05;
    // The likelihood of a Plant Growing during the night.
    private static final double NIGHT_Growing_PROBABILITY = 0.12;
    // The likelihood of a Plant dispersing a seed when it rains.
    private static final double RAIN_DISPERSAL_PROBABILITY = 0.09;
    // The likelihood of a Plant dispersing a seed when it's windy.
    private static final double WIND_DISPERSAL_PROBABILITY = 0.11;
    // The likelihood of a seed growing.
    private static final double SEED_Growing_PROBABILITY = 0.09;

    //Plants have a water level which is incremented when it rains
    private int waterLevel;


    //Constructor
    public Plant(boolean randomAge,Field field, Location location){

        super(field,location);
        age=0;

        waterLevel = rand.nextInt(20)+1;
        if(randomAge) {
            age = rand.nextInt(50);

        }
    }

    /*
     *This constructor will be used to initialise new Plant objects when the plant is growing
     */
    public Plant(Field field, Location location,boolean useCurrentAge){
        super(field,location);
        age=0;
        //Plants will have an initial water level of 10 i.e could survive 10 steps without water
        waterLevel = getWaterLevel() ;
        if(useCurrentAge){
            age = getAge();
        }
    }

    /**
     * This method will increase the size of the plant in the simulation
     * It creates new plant objects of the same age as the current plant and adjacent to the current plant
     * @param newPlants List to which the new plants are added to
     * @param growingProbability the probability value of a plant growing
     */

    private void grow(List<Animal> newPlants,double growingProbability)
    {
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int sizeIncrement = sizeExpansion(growingProbability);
        for(int s = 0; s < sizeIncrement && free.size() > 0; s++) {
            Location loc = free.remove(0);
            Plant young = new Plant(field,loc,true);
            newPlants.add(young);
        }
    }

    /**
     * Generate a number representing the number of squares a plant can occupy when it's growing,
     *
     * @return The number of squares
     * @param growingProbability The Growing Probability of the plant
     */
    private int sizeExpansion(double growingProbability)
    {
        int size = 0;
        if(rand.nextDouble() <= growingProbability) {
            size = rand.nextInt(maxSizeIncrease) + 1;
        }
        return size;
    }

    /**
     * This is what the Plant does during the day: it grows
     * ,produces seeds and absorbs water when it rains
     *  In the process,die of low water levels or of old age
     *
     * @param weatherType The current weather in the simulation
     * @param newPlants A list to return newly born Plants.
     */
    public void act(List<Animal> newPlants,String weatherType)
    {
        if(weatherType.toLowerCase().equals("sunny")){
            incrementAge();
            incrementThirst();
            if(isAlive()){
                grow(newPlants,Day_Growing_PROBABILITY);
            }
        }
        else if(weatherType.toLowerCase().equals("windy")){
            incrementAge();
            if(isAlive()){
                grow(newPlants,Day_Growing_PROBABILITY);
                seedDispersal(newPlants,WIND_DISPERSAL_PROBABILITY);
            }
        }
        else if(weatherType.toLowerCase().equals("rainy")){
            incrementAge();
            waterLevel++;
            if(isAlive()){
                seedDispersal(newPlants,RAIN_DISPERSAL_PROBABILITY);
            }
        }

    }

    /**
     * This is what the plant does during the night cycle
     *
     * @param newPlants A list to return newly born Plants.
     * @param weatherType The current weather in the simulation
     */


    public void sleep(List<Animal> newPlants,String weatherType){
        incrementAge();
        if(isAlive()) {
            if ((weatherType.toLowerCase().equals("rainy"))) {
                waterLevel++;
                seedDispersal(newPlants,RAIN_DISPERSAL_PROBABILITY);
            }
            else if ((weatherType.toLowerCase().equals("windy"))) {
                grow(newPlants,NIGHT_Growing_PROBABILITY);
                seedDispersal(newPlants,WIND_DISPERSAL_PROBABILITY);

            }
            else{
                grow(newPlants,NIGHT_Growing_PROBABILITY);
            }
        }
    }

    private void incrementAge(){

        age++;
        if(age > maxAge) {
            setDead();
        }
    }

    /**
     * Make this Plant thirsty. This could result in the Plant's death.
     * The plant water level will decrement randomly
     */
    private void incrementThirst()
    {
        waterLevel-=rand.nextInt(); //will be decremented by either 0 or 1
        if(waterLevel < 0) {
            setDead();
        }
    }


    /**
     * A Plant can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
    
    public int getAge(){
        return age;
        
    }

    /**
     *
     * @return water level of the plant
     */
    public int getWaterLevel(){
        return waterLevel;
    }

    /**
     * This is how the plant will disperse its seed
     * It'll only disperse when the weather is either windy/rainy
     *
     * @param newPlants A list to return newly born Plants
     * @param seedDispersalProbability This is the probability that the seeds will disperse
     */

    private void seedDispersal(List<Animal> newPlants, double seedDispersalProbability){
        Field field = getField();
        int seeds = 0;
        if(canBreed() && rand.nextDouble() <=seedDispersalProbability) {
            seeds = rand.nextInt(maxSeeds) + 1;
        }
        for(int seed=0;seed<seeds;seed++) {
            if (rand.nextDouble() >= SEED_Growing_PROBABILITY) {
                int randDepth = rand.nextInt(field.getDepth())+1;
                int randWidth = rand.nextInt(field.getWidth())+1;

                //Random location in the simulation field
                Location location = new Location(randWidth,randDepth);

                try {
                    if (field.getObjectAt(location) == null) {
                        Plant plant = new Plant(false, field, location);
                        newPlants.add(plant);

                    }
                    else{
                        try{
                            Plant plant = new Plant(false, field, field.freeAdjacentLocation(location));
                            newPlants.add(plant);
                        }
                        catch (NullPointerException ex){
                            //Handle Exception when there are no freeAdjacentLocations
                        }

                    }
                }
                catch (ArrayIndexOutOfBoundsException ex){
                    //Handle exception
                }
            }
        }
    }



}
