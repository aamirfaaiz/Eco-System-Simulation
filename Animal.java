import java.util.List;
import java.util.Random;

/**
 * A class representing shared characteristics of animals.
 * 
 * @author David J. Barnes and Michael Kölling
 * @edited by Aamir Faaiz
 *
 * @version 2019-FEB
 */
public abstract class Animal
{
    // Whether the animal is alive or not.
    private boolean alive;
    // The animal's field.
    private Field field;
    // The animal's position in the field.
    private Location location;
    
    private char gender;
    
    /**
     * Create a new animal at location in field.
     * 
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Animal(Field field, Location location)
    {
        alive = true;
        this.gender = setRandGender();
        this.field = field;
        setLocation(location);
    }

    /**
     * This method will assign a random gender to every animal that's created
     * @return a character which represents the gender of the animal created, F = Female, M = Male
     */

    private char setRandGender(){
        Random r = new Random();
        String gender = "MF";
        char randGender = gender.charAt(r.nextInt(gender.length()));
        return  randGender;
    }

    /**
     *
     * @return the gender of the animal
     */

    protected char getGender(){
        return gender;
    }

    /**
     * Make this animal act - that is: make it do
     * whatever it wants/needs to do.
     * @param newAnimals A list to receive newly born animals.
     */
    abstract public void act(List<Animal> newAnimals, String weatherType);


    /**
     * Make this animal sleep
     * @param newAnimals A list to receive newly born animals.
     * @param weatherType The current weather in the simulation
     */
    abstract public void sleep(List<Animal> newAnimals, String weatherType);

    /**
     * Check whether the animal is alive or not.
     * @return true if the animal is still alive.
     */
    protected boolean isAlive()
    {
        return alive;
    }

    /**
     * Indicate that the animal is no longer alive.
     * It is removed from the field.
     */
    protected void setDead()
    {
        alive = false;
        if(location != null) {
            field.clear(location);
            location = null;
            field = null;
        }
    }

    /**
     * Return the animal's location.
     * @return The animal's location.
     */
    protected Location getLocation()
    {
        return location;
    }
    
    /**
     * Place the animal at the new location in the given field.
     * @param newLocation The animal's new location.
     */
    protected void setLocation(Location newLocation)
    {
        if(location != null) {
            field.clear(location);
        }
        location = newLocation;
        field.place(this, newLocation);
    }
    
    /**
     * Return the animal's field.
     * @return The animal's field.
     */
    protected Field getField()
    {

        return field;
    }
}

