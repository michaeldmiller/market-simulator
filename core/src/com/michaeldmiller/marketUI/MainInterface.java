package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.michaeldmiller.economicagents.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.michaeldmiller.economicagents.MarketMain.*;

public class MainInterface implements Screen {
    final MarketUI marketUI;
    Stage stage;
    Skin firstSkin;
    Market market;
    HashMap<String, Color> colorLookup;
    Label prices;
    Label errorLabel;
    TextField goodField;
    TextField costField;
    double scale;
    int frame;
    double secondFraction;
    int numberOfAgents;
    ScrollingGraph priceGraph;
    ScrollingGraph professionGraph;

    public MainInterface (final MarketUI marketUI) {
        this.marketUI = marketUI;
        firstSkin = new Skin(Gdx.files.internal("skin/clean-crispy-ui.json"));
        frame = 0;
        secondFraction = 0.0167;
        scale = 1.75;
        // set number of agents
        numberOfAgents = 2000;

        // setup color lookup table
        colorLookup = new HashMap<String, Color>();
        colorLookup.put("Fish", new Color(0, 0, 0.7f, 1));
        colorLookup.put("Lumber", new Color(0, 0.7f, 0, 1));
        colorLookup.put("Grain", new Color(0.7f, 0.7f, 0, 1));
        colorLookup.put("Metal", new Color(0.7f, 0.7f, 0.7f, 1));
        // MarketProperty is a reserved good name, used for graphing data which corresponds to the market, not a good
        colorLookup.put("MarketProperty", new Color(0.2f, 0.2f, 0.2f, 1));

        stage = new Stage(new FitViewport(marketUI.worldWidth, marketUI.worldHeight));

        // add buttons
        addButtons();

        // instantiate market
        instantiateMarket();

        // make adjustment fields
        makeAdjustmentFields();

        // add price graph
        priceGraph = new ScrollingGraph((int) (0.025 * marketUI.worldWidth), (int) (0.55 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, scale, "Prices", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);
        professionGraph = new ScrollingGraph((int) (0.025 * marketUI.worldWidth), (int) (0.15 * marketUI.worldHeight),
                (int) (0.35 * marketUI.worldWidth), (int) (0.35 * marketUI.worldHeight), marketUI.worldWidth,
                marketUI.worldHeight, 500.0 / numberOfAgents, "Professions", new HashMap<String, Integer>(),
                colorLookup, firstSkin, frame, stage);

        priceGraph.makeGraph();
        professionGraph.makeGraph();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.9f, 0.9f, 0.9f, 1);
        Gdx.input.setInputProcessor(stage);
        frame += 1;
        // use second fraction to determine how often to call run market
        if (frame % ((int) (secondFraction * 60)) == 0) {
            try {
                runMarket(market, frame);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            updatePriceGraph();
            updateProfessionGraph();
            prices.setText(market.getPrices().toString());

        }
        priceGraph.graphLabels();
        professionGraph.graphLabels();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    // UI Instantiation
    public void addButtons(){
        Button menuButton = new TextButton("Menu", firstSkin);
        menuButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - marketUI.standardButtonHeight);
        menuButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        menuButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                marketUI.setScreen(marketUI.mainMenu);
                dispose();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(menuButton);

        Button printButton = new TextButton("Print", firstSkin);
        printButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 2* marketUI.standardButtonHeight);
        printButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        printButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                System.out.println(priceGraph.getDots().size());
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(printButton);

    }

    public void makeAdjustmentFields(){
        goodField = new TextField("Good", firstSkin);
        goodField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (2.5 * marketUI.standardButtonHeight));
        stage.addActor(goodField);
        costField = new TextField("New Cost", firstSkin);
        costField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 3 * marketUI.standardButtonHeight);
        stage.addActor(costField);

        Button changeCostButton = new TextButton("Update Cost", firstSkin);
        changeCostButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 4* marketUI.standardButtonHeight);
        changeCostButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        changeCostButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                changePrice();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(changeCostButton);

        errorLabel = new Label ("Errors Here", firstSkin);
        errorLabel.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (4.5 * marketUI.standardButtonHeight));
        stage.addActor(errorLabel);

    }
    // Market instantiation
    public void instantiateMarket(){
        // set up market profile
        MarketInfo fish = new MarketInfo("Fish", 0.35, -0.5, 0.7,
                9, 1, "Fisherman", 0.4);
        MarketInfo lumber = new MarketInfo("Lumber", 0.2, -0.5, 0.8,
                15, 1, "Lumberjack", 0.2);
        MarketInfo grain = new MarketInfo("Grain", 0.45, -0.5, 0.4,
                7, 1, "Farmer", 0.4);
        MarketInfo metal = new MarketInfo("Metal", 0.10, -1.2, 1.5,
                50, 1, "Blacksmith", 0.05);
        ArrayList<MarketInfo> currentMarketProfile = new ArrayList<MarketInfo>();
        currentMarketProfile.add(fish);
        currentMarketProfile.add(lumber);
        currentMarketProfile.add(grain);
        currentMarketProfile.add(metal);


        // create agents
        ArrayList<Agent> marketAgents = makeAgents(currentMarketProfile, numberOfAgents);
        // create market
        market = makeMarket(currentMarketProfile, marketAgents);

        prices = new Label ("Prices", firstSkin);
        prices.setPosition(100, marketUI.worldHeight - 50);
        stage.addActor(prices);

    }

    // modifier function
    public void changePrice(){
        // given information in good and cost text fields, attempt to change the corresponding cost in the market
        boolean costOK = false;
        int costValue = 0;
        String good = goodField.getText();
        String cost = costField.getText();

        // make sure the user entered value is an integer
        try{
            System.out.println(cost);
            costValue = Integer.parseInt(cost);
            costOK = true;
        } catch (NumberFormatException e){
            errorLabel.setText("Not a valid cost!");
        }
        // if value is ok, check goods for match and assign cost
        if (costOK){
            for (Price p : market.getPrices()){
                if (p.getGood().equals(good)){
                    p.setOriginalCost(costValue);
                }
            }
        }

    }
    // graph update functions
    public void updatePriceGraph(){
        // update function for the price graph, gets price data from market and then turns it into coordinates
        priceGraph.setFrame(frame);
        HashMap<String, Integer> priceCoordinates = new HashMap<String, Integer>();
        for (Price p : market.getPrices()){
            priceCoordinates.put(p.getGood(), (int) (p.getCost() * (priceGraph.getScale())));
        }
        priceGraph.setDataCoordinates(priceCoordinates);
        priceGraph.graphData();
        priceGraph.removeGraphDots(priceGraph.getX(), priceGraph.getDots());
        priceGraph.removeGraphLabels(priceGraph.getX(), priceGraph.getLabels());
    }

    public void updateProfessionGraph(){
        // update function for the profession graph, gets job data from market and then turns it into coordinates
        professionGraph.setFrame(frame);

        HashMap<String, Integer> jobsTotal = new HashMap<String, Integer>();
        // get total amount in each job by looping through all agents
        for (Agent a : market.getAgents()){
            if (!jobsTotal.containsKey(a.getProfession().getJob())){
                jobsTotal.put(a.getProfession().getJob(), 1);
            }
            else {
                String key = a.getProfession().getJob();
                jobsTotal.put(key, jobsTotal.get(key) + 1);
            }
        }
        // convert profession names to good names to match with color lookup
        HashMap<String, Integer> professionCoordinates = new HashMap<String, Integer>();
        for (Map.Entry<String, Integer> professionTotal : jobsTotal.entrySet()){
            for (JobOutput j : market.getJobOutputs()){
                if (j.getJob().equals(professionTotal.getKey())){
                    professionCoordinates.put(j.getGood(), (int) (professionTotal.getValue() *
                            (professionGraph.getScale())));
                }
            }
        }
        professionGraph.setDataCoordinates(professionCoordinates);
        professionGraph.graphData();
        professionGraph.removeGraphDots(professionGraph.getX(), professionGraph.getDots());
        professionGraph.removeGraphLabels(professionGraph.getX(), professionGraph.getLabels());
    }
}
