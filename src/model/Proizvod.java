package model;



public class Proizvod {

   private int id;
   private double price;
   private String name;
   private String image;
   private String screenSize;
   private String cpu;
   private String screenResolution;
   private String numberOfCores;
   private String memory;
   private String ram;
   private Boolean visible;

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Proizvod(int id, double price, String name, String image, String screenSize, String cpu, String screenResolution, String numberOfCores, String memory, String ram) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.image = image;
        this.screenSize = screenSize;
        this.cpu = cpu;
        this.screenResolution = screenResolution;
        this.numberOfCores = numberOfCores;
        this.memory = memory;
        this.ram = ram;
        this.visible = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(String screenSize) {
        this.screenSize = screenSize;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getScreenResolution() {
        return screenResolution;
    }

    public void setScreenResolution(String screenResolution) {
        this.screenResolution = screenResolution;
    }

    public String getNumberOfCores() {
        return numberOfCores;
    }

    public void setNumberOfCores(String numberOfCores) {
        this.numberOfCores = numberOfCores;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }
}
