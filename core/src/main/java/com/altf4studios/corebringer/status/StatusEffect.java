package com.altf4studios.corebringer.status;

public abstract class StatusEffect {
    protected String name;
    protected int power;
    protected int duration;

    public StatusEffect(String name, int power, int duration){
        this.name = name;
        this.power = power;
        this.duration = duration;
    }

    public abstract void onApply();

}

