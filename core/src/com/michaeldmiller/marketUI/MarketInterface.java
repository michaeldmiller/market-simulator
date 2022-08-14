package com.michaeldmiller.marketUI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Select;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.michaeldmiller.economicagents.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.michaeldmiller.economicagents.MarketMain.*;

public class MarketInterface implements Screen {
    final MarketUI marketUI;
    public Stage stage;
    Skin firstSkin;
    Market market;
    ArrayList<MarketInfo> currentMarketProfile;
    HashMap<String, Color> colorLookup;
    Label prices;
    Label moreInfo;
    Label errorLabel;
    TextField goodField;
    TextField consumptionField;
    TextField costField;
    TextField consumptionCostField;
    TextField agentField;
    double scale;
    int frame;
    double secondFraction;
    int numberOfAgents;
    ScrollingGraph priceGraph;
    ScrollingGraph professionGraph;
    ScrollingGraph moneyGraph;
    ScrollingGraph unmetNeedGraph;
    ScrollingGraph marketInventoryGraph;
    ScrollingGraph agentPropertyGraph;
    String agentID;
    Drawable infoIcon;
    Drawable infoIconClicked;
    Table masterTable;
    VerticalGroup marketGoods;
    Label infoLabel;
    HashMap<Integer, ScrollingGraph> graphs;
    HashMap<String, String> graphTypesLookup;
    int numberOfGraphsSelectedByUser;


    public MarketInterface (final MarketUI marketUI, int specifiedNumberOfAgents,
                          ArrayList<MarketInfo> specifiedMarketProfile) {
        this.marketUI = marketUI;
        this.graphs = new HashMap<>();
        numberOfGraphsSelectedByUser = 0;
        firstSkin = new Skin(Gdx.files.internal("skin/cloud-form/cloud-form-ui.json"));
        frame = 0;
        secondFraction = 0.0167;
        // secondFraction = 1;
        scale = 1.75;
        // set number of agents
        numberOfAgents = specifiedNumberOfAgents;
        currentMarketProfile = specifiedMarketProfile;
        // set initial agent
        agentID = "1";

        // info button texture and drawable
        Texture infoIconTexture = new Texture(Gdx.files.internal("info-button-icon-26.png"));
        infoIcon = new TextureRegionDrawable(new TextureRegion(infoIconTexture));
        Texture infoIconClickedTexture = new Texture(Gdx.files.internal("info-button-icon-clicked-26.png"));
        infoIconClicked = new TextureRegionDrawable(new TextureRegion(infoIconClickedTexture));

        // TODO: Add dynamic color lookup
        // setup color lookup table
        colorLookup = new HashMap<String, Color>();
        colorLookup.put("Fish", new Color(0, 0, 0.7f, 1));
        colorLookup.put("Lumber", new Color(0, 0.7f, 0, 1));
        colorLookup.put("Grain", new Color(0.7f, 0.7f, 0, 1));
        colorLookup.put("Metal", new Color(0.7f, 0.7f, 0.7f, 1));
        colorLookup.put("Brick", new Color(0.7f, 0, 0, 1));
        // MarketProperty is a reserved good name, used for graphing data which corresponds to the market, not a good
        colorLookup.put("MarketProperty", new Color(0.2f, 0.2f, 0.2f, 1));

        stage = new Stage(new FitViewport(marketUI.worldWidth, marketUI.worldHeight));

        // create the main UI table
        masterTable = new Table();
        masterTable.setFillParent(true);
        stage.addActor(masterTable);
        // set alignment
        masterTable.top().left();
        masterTable.padTop(10);
        // enable debugging for design purposes
        masterTable.setDebug(true);

        // create labels
        Label title = new Label("Market Creator", firstSkin);

        // information label, wrap is true for multiple information lines
        infoLabel = new Label("------------Information------------", firstSkin);
        infoLabel.setWrap(true);

        // create menu button
        Button menuButton = new TextButton("Menu", firstSkin);
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

        // add the title and button to the first row
        Table titleInstructions = new Table();
        titleInstructions.add(title).padLeft(50);
        titleInstructions.add().expand();
        titleInstructions.add(createInstructions());
        masterTable.add(titleInstructions).align(Align.left).fill();


        masterTable.add(menuButton).top().right().width(100);
        masterTable.row();

        // create vertical group, with one or two rows, each containing a horizontal group, for the graphs
        marketGoods = new VerticalGroup();
        marketGoods.align(Align.left);

        HorizontalGroup graphRow1 = new HorizontalGroup();



        // Texture testTexture = new Texture(Gdx.files.internal("test.png"));
        // Drawable testDraw = new TextureRegionDrawable(new TextureRegion(testTexture));

        // Image x = new Image(testDraw);
        // x.setHeight(4000);
        // x.setSize(1000, 400);
        // graphRow1.addActor(x);

        marketGoods.setSize(2000, 400);

        marketGoods.addActor(graphRow1);
        marketGoods.expand();

        // create labels
        // marketGoods.addActor(createCategoryLabels());

        // create scroll pane for goods
        // set to fill whole rest of the screen (might be changed later), align to the top
        // masterTable.add(marketGoods).expandY().top();

        // create invisible table to provide size suggestions to the graphs, which are positioned outside
        // the master table directly on the stage.

        Table graphDisplayTable = new Table();
        masterTable.add(graphDisplayTable).expand();
        // the code is unnecessary, as the graphs are placed using absolute coordinates, but the graphs are placed
        // within a virtual 5x5 table, in (2, 2), (4, 2), (2, 4), and (4, 4). Row and Column 1, 3, and 5, explicitly
        // control the left, center, and right (top, middle, and bottom) padding of the graph cells.

        // using tables is inefficient for the task, as there does not seem to be an easy way to access the
        // absolute x and y coordinates, relative to the screen, of any given cell.
        // Proposed solution: through trial and error, with the aid of the table measurements gathered from earlier,
        // determine the absolute coordinates of the four graph locations for the screen size. This will mean that
        // this program is fixed to a display size of 1600 x 900, although converting them back into world size ratios,
        // as done previously, may be possible.

        // information panel: graph selectors and information label
        Table informationPanel = new Table();

        Table numberOfGraphsSelectionTable = new Table();
        // create options
        Label numberOfGraphsPrompt = new Label("Number of Graphs:", firstSkin);
        numberOfGraphsSelectionTable.add(numberOfGraphsPrompt).padRight(10);

        final SelectBox<Integer> numberOfGraphsSelector = new SelectBox<>(firstSkin);
        Array<Integer> graphChoices = new Array<>();
        graphChoices.add(0);
        graphChoices.add(1);
        graphChoices.add(2);
        graphChoices.add(3);
        graphChoices.add(4);
        numberOfGraphsSelector.setItems(graphChoices);
        numberOfGraphsSelectionTable.add(numberOfGraphsSelector);
        informationPanel.add(numberOfGraphsSelectionTable);
        informationPanel.row();

        // type label
        Label typePromptLabel = new Label("Graph Type", firstSkin);
        informationPanel.add(typePromptLabel);
        informationPanel.row();

        // add group for type selectors
        final VerticalGroup typeSelectors = new VerticalGroup();
        informationPanel.add(typeSelectors);
        informationPanel.row();

        numberOfGraphsSelector.addListener(new ChangeListener(){
            @Override
            public void changed(ChangeEvent changeEvent, Actor actor) {
                // change selected number of graphs
                numberOfGraphsSelectedByUser = numberOfGraphsSelector.getSelected();
                // get current amount of graphs
                int currentNumberOfSelectors = typeSelectors.getChildren().size;
                // determine if difference is positive or negative
                int numberOfChange = numberOfGraphsSelector.getSelected() - currentNumberOfSelectors;
                // if negative, remove from the end the requested number of times
                if (numberOfChange < 0){
                    for (int i = 0; i < (numberOfChange * -1); i++){
                        // remove actor at the end of the list (have to adjust from size to index)
                        Actor removed = typeSelectors.removeActorAt(currentNumberOfSelectors - 1, false);
                        // clear the removed actor
                        removed.clear();
                        currentNumberOfSelectors--;
                    }
                } else if(numberOfChange > 0){
                    // if positive, add the requested number to the type selector
                    for (int i = 0; i < numberOfChange; i++){
                        // create select box
                        final SelectBox<String> graphTypeSelector = new SelectBox<>(firstSkin);
                        Array<String> graphTypes = new Array<>();
                        graphTypes.add("");
                        graphTypes.add("Price");
                        graphTypes.add("Producers");
                        graphTypes.add("Agent");
                        graphTypes.add("Unmet Needs");
                        graphTypeSelector.setItems(graphTypes);

                        // create table
                        Table graphTypeSelectorTable = new Table();
                        // create label for graph number
                        int graphNumber = currentNumberOfSelectors + i + 1;

                        // create initial graph (if not using starting blank choice)
                        // createGraph(graphNumber, graphTypes.get(0));

                        Label graphNumberLabel = new Label("Graph #" + graphNumber + ":", firstSkin);
                        graphTypeSelectorTable.add(graphNumberLabel).padRight(10);


                        graphTypeSelector.addListener(new ChangeListener(){
                            @Override
                            public void changed(ChangeEvent changeEvent, Actor actor) {
                                // get current graph number
                                int index = 0;
                                int location = 0;
                                // since each of these buttons is nested in a table, and we want to see the button's
                                // index in the vertical group, need to get the button's grandparent and check
                                // it against the grandchildren
                                for (Actor tableActor : actor.getParent().getParent().getChildren()){
                                    // find the actor from the list of actors in its parent
                                    // loop through each child
                                    Table table = (Table) tableActor;
                                    for (Actor labelOrBox : table.getChildren()){
                                        if (labelOrBox.equals(actor)){
                                            location = index;
                                        }
                                    }
                                    // if this box was not found yet, increment index
                                    index++;
                                }

                                // with the location in hand, run the graph creation function
                                // createGraph(index,
                                createGraph(location, graphTypeSelector.getSelected());

                            }
                        });
                        graphTypeSelectorTable.add(graphTypeSelector);

                        typeSelectors.addActor(graphTypeSelectorTable);
                    }

                }
                // if the number has been changed, redraw all the graphs
                redrawGraphs();
            }
        });

        // info box, add information label
        // create scroll pane
        infoLabel = new Label("------------Information------------", firstSkin);
        infoLabel.setWrap(true);
        final ScrollPane informationScrollPane = new ScrollPane(infoLabel);
        informationPanel.add(informationScrollPane).width(250);
        masterTable.add(informationPanel).width(250).center().top();

        // add bottom row
        masterTable.row();
        // left align
        // masterTable.add(createBottomRow()).align(Align.left).top().padLeft(10);
        // center align
        masterTable.add(createBottomRow()).top();
        masterTable.add();

        instantiateMarket();

    }

    private void createGraph(int index, String graphType){
        // determine if graph already exists with that index

        if (graphs.containsKey(index)){
            // if it does, get it and delete it.
            ScrollingGraph oldGraph = graphs.get(index);
            // delete the graph components, and then remove the graph from the graphs list
            oldGraph.deleteAll(oldGraph.getDots(), oldGraph.getLabels(), oldGraph.getOtherComponents());
            graphs.remove(index);
        }

        // Left, Center, and Right X padding of 70, Left, Center, and Right Padding of 45, noting the 55 height
        // of the lower bar. Given a target width of 1350, confined by 250 wide info box, these are the resulting
        // coordinates of each graph slot:
        // Slot 1: 70, 480;
        // Slot 2: 70, 100;
        // Slot 3: 710, 480;
        // Slot 4: 710, 100;

        // inferred dimensions of each graph given the number of graphs:
        // three or four: (570, 335)
        // two: (1210, 335)
        // one: (1210, 715)

        // determine x and y coordinates
        int xCoordinate = 0;
        if (index == 0 || index == 1){
            xCoordinate = 70;
        } else{
            xCoordinate = 710;
        }
        int yCoordinate = 0;
        if(index == 0 || index == 2){
            yCoordinate = 480;
        } else{
            yCoordinate = 100;
        }
        // determine width and height
        int width = 0;
        int height = 0;
        // use number of possible graphs selected, not the current graph number
        if (numberOfGraphsSelectedByUser == 1){
            width = 1210;
            height = 715;
            yCoordinate = 100;
        } else if (numberOfGraphsSelectedByUser == 2){
            width = 1210;
            height = 335;
        } else{
            width = 570;
            height = 335;
        }


        // create the appropriate graph
        // list of graph types
        if (graphType.equals("Price")){
            PriceGraph priceGraph = new PriceGraph(xCoordinate, yCoordinate, width, height,
                    marketUI.worldWidth, marketUI.worldHeight, scale, "Prices",
                    new HashMap<String, Integer>(), colorLookup, firstSkin, frame, stage, true);
            priceGraph.makeGraph();
            graphs.put(index, priceGraph);

        } else if (graphType.equals("Producers")){
            ProfessionGraph professionGraph = new ProfessionGraph(xCoordinate, yCoordinate, width, height,
            marketUI.worldWidth, marketUI.worldHeight, 500.0 / numberOfAgents, "# of Producers",
                    new HashMap<String, Integer>(), colorLookup, firstSkin, frame, stage, true);
            professionGraph.makeGraph();
            graphs.put(index, professionGraph);

        } else if (graphType.equals("Agent")){
            AgentPropertyGraph agentGraph = new AgentPropertyGraph(xCoordinate, yCoordinate, width, height,
                    marketUI.worldWidth, marketUI.worldHeight, 0.005, "Agent: " + agentID,
                    new HashMap<String, Integer>(), colorLookup, firstSkin, frame, stage, true);
            agentGraph.makeGraph();
            graphs.put(index, agentGraph);

        } else if (graphType.equals("Unmet Needs")){
            TotalUnmetNeedsGraph totalUnmetNeedsGraph = new TotalUnmetNeedsGraph(xCoordinate, yCoordinate, width, height,
                    marketUI.worldWidth, marketUI.worldHeight, 0.01, "Unmet Needs",
                    new HashMap<String, Integer>(), colorLookup, firstSkin, frame, stage, true);
            totalUnmetNeedsGraph.makeGraph();
            graphs.put(index, totalUnmetNeedsGraph);

        }

    }
    private void redrawGraphs(){
        // delete all the graphs
        for (Iterator<Map.Entry<Integer, ScrollingGraph>> iterator = graphs.entrySet().iterator(); iterator.hasNext();){
            Map.Entry<Integer, ScrollingGraph> graphForDeletion = iterator.next();
            ScrollingGraph oldGraph = graphForDeletion.getValue();
            // delete the graph components, and then remove the graph from the graphs list
            oldGraph.deleteAll(oldGraph.getDots(), oldGraph.getLabels(), oldGraph.getOtherComponents());
            iterator.remove();
        }

        // redraw all graphs using the graph creation logic
        // go to table containing the graph selectors
        // located at the fourth cell of the master table, third child of the infobox. Each table contained within
        // has a label and a select box. Run create graph on the index and the value of the select box
        Table InfoPanelCell = (Table) masterTable.getChild(3);
        VerticalGroup graphTypeSelectors = (VerticalGroup) InfoPanelCell.getChild(2);
        int index = 0;
        for (Actor tableActor : graphTypeSelectors.getChildren()){
            // cast to table
            Table table = (Table) tableActor;
            // get select box at index 1. Despite complaints, this is a select box of strings.
            SelectBox<String> selectBox = (SelectBox<String>) table.getChild(1);
            // create graph and increment index
            createGraph(index, selectBox.getSelected());
            index++;
        }

    }


    private void deleteGraph(int index) {
        // pure deletion function for a graph, deletes the graph at the specified index, if it exists.
        if (graphs.containsKey(index)) {
            ScrollingGraph oldGraph = graphs.get(index);
            // delete the graph components, and then remove the graph from the graphs list
            oldGraph.deleteAll(oldGraph.getDots(), oldGraph.getLabels(), oldGraph.getOtherComponents());
            graphs.remove(index);
        }
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

            // update the graphs, if any exist
            for (ScrollingGraph graph : graphs.values()){
                graph.update(this);
                graph.graphLabels();
            }

        }

        stage.act(delta);
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

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
                // System.out.println(priceGraph.getDots().size());
                // for (Agent a : market.getAgents()){
                //     System.out.println(a.getConsumption());
                // }
                System.out.println(market.getMarketProfile());
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

        // change agent (requires agent property graph)
        agentField = new TextField("New Agent ID:", firstSkin);
        agentField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (5 * marketUI.standardButtonHeight));
        stage.addActor(agentField);

        Button changeAgentButton = new TextButton("Update Agent", firstSkin);
        changeAgentButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (6 * marketUI.standardButtonHeight));
        changeAgentButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        changeAgentButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                changeAgent();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(changeAgentButton);

        consumptionField = new TextField("Good", firstSkin);
        consumptionField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (7.5 * marketUI.standardButtonHeight));
        stage.addActor(consumptionField);
        consumptionCostField = new TextField("New Consumption", firstSkin);
        consumptionCostField.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 8 * marketUI.standardButtonHeight);
        stage.addActor(consumptionCostField);

        Button changeConsumptionButton = new TextButton("Update Consumption", firstSkin);
        changeConsumptionButton.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - 9* marketUI.standardButtonHeight);
        changeConsumptionButton.setSize(marketUI.standardButtonWidth, marketUI.standardButtonHeight);
        changeConsumptionButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                changeConsumption();
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        stage.addActor(changeConsumptionButton);

        errorLabel = new Label ("Errors Here", firstSkin);
        errorLabel.setPosition(marketUI.worldWidth - marketUI.standardButtonWidth,
                marketUI.worldHeight - (int) (10 * marketUI.standardButtonHeight));
        stage.addActor(errorLabel);

    }
    // Market instantiation
    public void instantiateMarket(){
        // create agents
        ArrayList<Agent> marketAgents = makeAgents(currentMarketProfile, numberOfAgents);
        // create market
        market = makeMarket(currentMarketProfile, marketAgents);

        moreInfo = new Label ("", firstSkin);
        moreInfo.setPosition((int) (0.025 * marketUI.worldWidth), marketUI.worldHeight - 100);
        stage.addActor(moreInfo);

    }

    // modifier functions
    public void changePrice(){
        // given information in good and cost text fields, attempt to change the corresponding cost in the market
        boolean costOK = false;
        int costValue = 0;
        String good = goodField.getText();
        String cost = costField.getText();

        // make sure the user entered value is an integer
        try{
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

    public void changeConsumption(){
        // given information in good and cost text fields, attempt to change the corresponding cost in the market
        boolean costOK = false;
        double consumptionValue = 0;
        String good = consumptionField.getText();
        String cost = consumptionCostField.getText();

        // make sure the user entered value is an integer
        try{
            consumptionValue = Double.parseDouble(cost);
            costOK = true;
        } catch (NumberFormatException e){
            errorLabel.setText("Not a valid consumption!");
        }
        // if value is ok, set the matching consumption of each agent to the new consumption value
        if (costOK){
            for (Agent a : market.getAgents()){
                a.getConsumption().get(good).setTickConsumption(consumptionValue);
            }
        }
    }

    public void changeAgent(){
        // given information in good and cost text fields, attempt to change the corresponding cost in the market
        String proposedAgentID = agentField.getText();

        // if value is ok, check goods for match and assign cost
        for (Agent a : market.getAgents()){
            if (a.getId().equals(proposedAgentID)){
                agentID = proposedAgentID;
                agentPropertyGraph.setTitle(proposedAgentID);
                agentPropertyGraph.updateGraphTitle();
            }
        }
    }


    // graph update functions
    public void updateMoneyGraph(){
        // update function for the money graph, gets agent money data from market and then turns it into coordinates
        moneyGraph.setFrame(frame);

        int moneyTotal = 0;
        // get total funds of all agents
        for (Agent a : market.getAgents()) {
            moneyTotal += a.getMoney();
        }

        // convert profession names to good names to match with color lookup
        HashMap<String, Integer> professionCoordinates = new HashMap<String, Integer>();

        // generate non-zero coordinate:
        int moneyCoordinate = 0;
        if (((int) (moneyTotal * (moneyGraph.getScale()))) == 0){
            moneyCoordinate = 1;
        }
        else {
            moneyCoordinate = (int) (moneyTotal * (moneyGraph.getScale()));
        }
        // System.out.println(moneyCoordinate);
        professionCoordinates.put("MarketProperty", moneyCoordinate);

        moneyGraph.setDataCoordinates(professionCoordinates);
        moneyGraph.graphData();
        moneyGraph.removeGraphDots(moneyGraph.getX(), moneyGraph.getDots());
        moneyGraph.removeGraphLabels(moneyGraph.getX(), moneyGraph.getLabels());
    }

    public void updateMarketInventoryGraph(){
        // update function for the price graph, gets price data from market and then turns it into coordinates
        marketInventoryGraph.setFrame(frame);
        HashMap<String, Integer> priceCoordinates = new HashMap<String, Integer>();
        for (Map.Entry<String, Double> good : market.getInventory().entrySet()){
            priceCoordinates.put(good.getKey(), (int) (good.getValue() * (marketInventoryGraph.getScale())));
        }
        marketInventoryGraph.setDataCoordinates(priceCoordinates);
        marketInventoryGraph.graphData();
        marketInventoryGraph.removeGraphDots(marketInventoryGraph.getX(), marketInventoryGraph.getDots());
        marketInventoryGraph.removeGraphLabels(marketInventoryGraph.getX(), marketInventoryGraph.getLabels());
    }


    public Table createInstructions(){
        // create output table
        Table instructionsTable = new Table();

        // create instruction button
        Button instructionsButton = new TextButton(" Instructions ", firstSkin);
        instructionsButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Instructions:\n" + "Welcome to the MarketUI! MarketUI dynamically simulates " +
                        "a virtual marketplace with autonomous agents, which produce and consume goods and then buy " +
                        "and sell them to one another according to basic real world motivations of needs and a " +
                        "desire for profit. Governed by the real laws of microeconomics through supply and demand " +
                        "forces, MarketUI demonstrates the power of simple rules to create emergent behaviors akin " +
                        "to those found in the real world.\n" +
                        "This is the creation screen for the virtual market to be simulated. A market consists of a " +
                        "number of agents (button on the bottom of your screen) working with a number of goods. " +
                        "Click on the add good button, towards the middle of your screen, to add at least one good. " +
                        "When you do this, several boxes will appear with space to provide specific information " +
                        "about that good. Click on the information boxes associated with each to learn more about "+
                        "them. When you are done, click the create button in the bottom right to create the market " +
                        "and begin simulating it. Alternatively, click on a preset to use a prebuilt market and begin " +
                        "simulating right away. Thank you for using MarketUI!");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        instructionsTable.add(instructionsButton);

        return instructionsTable;
    }

    public VerticalGroup createBottomRow(){
        // create output group
        VerticalGroup bottomRow = new VerticalGroup();
        // create the two tables, one for the labels and info buttons, one for the actual
        Table bottomRowLabels = new Table();
        Table bottomRowInteractive = new Table();
        bottomRow.addActor(bottomRowLabels);
        bottomRow.addActor(bottomRowInteractive);

        // create field for number of Agents, prefilled to 2000
        Label numberOfAgents = new Label ("Number of Agents", firstSkin);
        bottomRowLabels.add(numberOfAgents).padLeft(35).padRight(5);
        // info button
        ImageButton numberOfAgentsButton = new ImageButton(infoIcon, infoIconClicked);
        numberOfAgentsButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Number of Agents:\n" + "This value is the number of agents present in the " +
                        "market simulation. A larger number of agents increases the detail of the simulation, at " +
                        "the cost of performance. Only a hundred or so are necessary for a fairly detailed " +
                        "simulation, though at least a thousand is better. At the moment, no multithreading " +
                        "is implemented, on an i7-8700k utilizing a single core, 2000 agents nears the upper " +
                        "performance limit.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        bottomRowLabels.add(numberOfAgentsButton).padRight(35);

        TextField numberOfAgentsField = new TextField("2000", firstSkin);
        bottomRowInteractive.add(numberOfAgentsField).padRight(50);

        // create first preset button
        Label preset1Label = new Label("Preset 1", firstSkin);
        bottomRowLabels.add(preset1Label).padLeft(20).padRight(5);
        // info button
        ImageButton preset1InfoButton = new ImageButton(infoIcon, infoIconClicked);
        preset1InfoButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // change information box text to description
                infoLabel.setText("Preset 1:\n" + "The first preset. Using a preset will start the simulation using " +
                        "one of a few predefined markets. Preset 1 is a balanced market of five goods: Fish, " +
                        "Lumber, Grain, Metal, and Brick, each of which is functionally identical with one another; " +
                        "i.e. they are all produced and consumed at the same rate and prioritized equally. There is a " +
                        "small surplus in the market, and production is distributed proportionally on initialization " +
                        "so that the market starts in a stable equilibrium. It is a great entry point, to get used to " +
                        "how the simulation works and get used to the different display and control options that " +
                        "are available. It is also an excellent platform to demonstrate the effects of modifying " +
                        "various market attributes with the modification tools in the main interface. Note: " +
                        "to respond better to differing performance requirements, presets still use a " +
                        "custom number of agents, as specified in the box to the left of the presets.");
            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        bottomRowLabels.add(preset1InfoButton).padRight(30);

        Button preset1CreateButton = new TextButton("Use Preset", firstSkin);
        preset1CreateButton.addListener(new InputListener(){
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                // preset values
                int numberOfGoods = 6;
                double surplusValue = 0.01;
                MarketInfo fish = new MarketInfo("Fish", (1.0 / numberOfGoods) - (surplusValue / numberOfGoods),
                        1, -1, 0,10, 1,
                        "Fisherman", 1.0 / numberOfGoods);
                MarketInfo lumber = new MarketInfo("Lumber", (1.0 / numberOfGoods) - (surplusValue / numberOfGoods),
                        1,-1, 0, 10, 1,
                        "Lumberjack", 1.0 / numberOfGoods);
                MarketInfo grain = new MarketInfo("Grain", (1.0 / numberOfGoods) - (surplusValue / numberOfGoods),
                        1, -1, 0, 10, 1,
                        "Farmer", 1.0 / numberOfGoods);
                MarketInfo metal = new MarketInfo("Metal", (1.0 / numberOfGoods) - (surplusValue / numberOfGoods),
                        1, -1, 0, 10, 1,
                        "Blacksmith", 1.0 / numberOfGoods);
                MarketInfo brick = new MarketInfo("Brick", (1.0 / numberOfGoods) - (surplusValue / numberOfGoods),
                        1, -1, 0, 10, 1,
                        "Mason", 1.0 / numberOfGoods);
                ArrayList<MarketInfo> preset1MarketProfile = new ArrayList<MarketInfo>();

                preset1MarketProfile.add(fish);
                preset1MarketProfile.add(lumber);
                preset1MarketProfile.add(grain);
                preset1MarketProfile.add(metal);
                preset1MarketProfile.add(brick);

                // overwrite current market profile
                currentMarketProfile = preset1MarketProfile;

                StringBuilder errorText = new StringBuilder();
                errorText.append("Error(s):\n");
                // get the number of agents
                VerticalGroup bottomRow = (VerticalGroup) masterTable.getChild(4);
                Table interactiveBottomRow = (Table) bottomRow.getChild(1);
                TextField numberOfAgentsField = (TextField) interactiveBottomRow.getChild(0);
                int numberOfAgents = 0;
                try{
                    numberOfAgents = Integer.parseInt(numberOfAgentsField.getText());
                    if (numberOfAgents < 0){
                        throw new IllegalArgumentException();
                    }
                } catch(NumberFormatException e){
                    errorText.append("The number of agents must be a number.\n");
                } catch(IllegalArgumentException e){
                    errorText.append("The number of agents must be positive.\n");
                }

                // if there were no errors (i.e. the error text is still its initial value), create the market,
                // otherwise, set the information box to show the encountered error
                if(errorText.toString().equals("Error(s):\n")){
                    // create a new main interface screen using the new market profile
                    marketUI.marketInterface = new MarketInterface(marketUI, numberOfAgents, currentMarketProfile);
                    // if a market did not exist before, refresh the main menu to include an enabled resume button
                    if (!marketUI.marketExists){
                        marketUI.marketExists = true;
                        marketUI.mainMenu = new MainMenu(marketUI);
                    }
                    // set the current screen to the main interface
                    marketUI.setScreen(marketUI.marketInterface);
                } else{
                    // otherwise, display the error text in the info box
                    infoLabel.setText(errorText.toString());
                }

            }
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button){
                return true;
            }
        });
        bottomRowInteractive.add(preset1CreateButton);



        return bottomRow;
    }

}
