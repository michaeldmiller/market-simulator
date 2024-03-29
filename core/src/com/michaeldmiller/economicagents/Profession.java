package com.michaeldmiller.economicagents;

public class Profession {
    private String job;
    private double skillLevel;
    private double baseProduction;
    private double shortRunProduction;
    private double priceElasticityOfSupply;

    // if deficiency in short run production vs market quantity, permit switch
    // problem: market quantity higher than it is possible for any combination of agents to produce
    // solution: derive production and demand curves from consumption and production /capacity/ of agents

    public Profession(String job, double skillLevel, double baseProduction, double shortRunProduction,
                      double priceElasticityOfSupply) {
        this.job = job;
        this.skillLevel = skillLevel;
        this.baseProduction = baseProduction;
        this.shortRunProduction = shortRunProduction;
        this.priceElasticityOfSupply = priceElasticityOfSupply;
    }

    public String getJob() {
        return job;
    }

    public double getSkillLevel() {
        return skillLevel;
    }
    public double getBaseProduction(){
        return baseProduction;
    }

    public double getShortRunProduction() {
        return shortRunProduction;
    }

    public double getPriceElasticityOfSupply() {
        return priceElasticityOfSupply;
    }

    public void setJob(String newJob) {
        job = newJob;
    }

    public void setSkillLevel(double newSkillLevel) {
        skillLevel = newSkillLevel;
    }
    public void setBaseProduction(double newBaseProduction){
        baseProduction= newBaseProduction;
    }

    public void setPriceElasticityOfSupply(double newPriceElasticity) {
        priceElasticityOfSupply = newPriceElasticity;
    }

    public void setShortRunProduction(double newProduction) {
        shortRunProduction = newProduction;
    }

    public String toString() {
        return (this.getJob() + ", skill level: " +
                this.getSkillLevel() + ", " +
                String.format("base production %.2f", this.getBaseProduction()) + ", " +
                String.format("price elasticity of supply %.2f", this.getPriceElasticityOfSupply()));
    }
}
