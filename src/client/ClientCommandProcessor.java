package client;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import graphics.HalloweenSimulation;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.JSpinner;

import stringProcessors.*;
import stringProcessors.Number;
import util.annotations.ComponentWidth;
import util.annotations.MaxValue;
import util.annotations.MinValue;
import util.annotations.Position;
import util.annotations.PreferredWidgetClass;
import util.annotations.StepValue;
import util.annotations.StructurePattern;
import util.annotations.Visible;
import util.trace.bean.AddedPropertyChangeListener;
import util.trace.bean.NotifiedPropertyChangeEvent;
import util.trace.bean.RemovedPropertyChangeListener;
import util.trace.bean.SetProperty;
import util.trace.trickOrTreat.CommandSubmitted;
import util.trace.trickOrTreat.ConnectedToSimulation;
import veto.PropertyChangeVetoer;

@StructurePattern("Bean Pattern")
public class ClientCommandProcessor implements HalloweenCommandProcessor, Serializable {
    public static final boolean RESET_COMMAND = false;
    transient HalloweenSimulation MyHalloweenSim;
    transient Undoer undoer = new AHistoryUndoer();
    transient int commands;
    transient int placeholderCommands;
    String inputString = "";
    transient IteratorEnabledScanner stringProcessor;
    transient IteratorEnabledScanner placeholderProcessor;
    String errorMessages = "";
    transient TokenInterface commandToken;
    transient TokenInterface placeholderToken;
    transient boolean stillNeedToTestCurrentToken = false;
    int totalWordCount = 0;
    int totalCommandCount = 0;
    String macro1 = "";
    String macroBuildString = "";
    transient TokenInterface macro1Token = new InvalidWord("macro1");
    boolean animationOn = false;
    boolean statsOn = false;
    int numberOfSteps = 20;
    int pauseTime = 100;
    boolean smoothAnimation = false;
    int simWorldSize;
    String SimErrorMessages = "";
    transient Vector<PropertyChangeListener> listeners = new Vector();
    transient Vector<PropertyChangeVetoer> vetoers = new Vector();
    boolean connectedToSimulation = true;
    boolean replayInputString = false;
    PropertyChangeEvent preconditionEvent = new PropertyChangeEvent(this, "this", (Object)null, this);
    PropertyChangeListener[] emptyPropertyChangeListenerArray = new PropertyChangeListener[0];
    boolean replay;

    public ClientCommandProcessor(HalloweenSimulation inputSim) {
        this.init(inputSim);
    }

    public ClientCommandProcessor() {
    }

    void replay() {
        this.replay = true;
        if (this.replayInputString && !this.inputString.equals("")) {
            this.processCommand(this.inputString);
        }

        this.replay = false;
    }

    public void init(HalloweenSimulation inputSim) {
        this.MyHalloweenSim = inputSim;
        this.simWorldSize = this.MyHalloweenSim.getWorldSize();
        this.replay();
        this.MyHalloweenSim.addCommandLineListener(this);
        this.MyHalloweenSim.toggleAnimation(this.animationOn, this.numberOfSteps, this.pauseTime);
    }

    @Position(0)
    @ComponentWidth(900)
    public String getInputString() {
        return this.inputString;
    }

    @Position(1)
    @ComponentWidth(900)
    public String getErrorMessages() {
        return this.errorMessages;
    }

    @Position(2)
    @ComponentWidth(900)
    public String getSimFeedback() {
        return this.MyHalloweenSim != null ? this.MyHalloweenSim.getErrorMessages() : "";
    }

    @Position(3)
    public boolean getAnimationActivated() {
        return this.animationOn;
    }

    public boolean preGetNumberOfSteps() {
        return this.animationOn;
    }

    @Position(4)
    @PreferredWidgetClass(JSpinner.class)
    @MinValue(1L)
    @MaxValue(500L)
    @StepValue(1L)
    public int getNumberOfSteps() {
        assert this.preGetNumberOfSteps();

        return this.numberOfSteps;
    }

    public void setNumberOfSteps(int nSteps) {
        this.numberOfSteps = nSteps;
        if (this.animationOn && this.MyHalloweenSim != null) {
            this.MyHalloweenSim.toggleAnimation(this.animationOn, this.numberOfSteps, this.pauseTime);
        }

    }

    public boolean preGetPauseTime() {
        return this.animationOn;
    }

    @Position(5)
    @PreferredWidgetClass(JSpinner.class)
    @MinValue(10L)
    @MaxValue(2000L)
    @StepValue(10L)
    public int getPauseTime() {
        assert this.preGetPauseTime();

        return this.pauseTime;
    }

    public void setPauseTime(int pTime) {
        this.pauseTime = pTime;
        if (this.animationOn && this.MyHalloweenSim != null) {
            this.MyHalloweenSim.toggleAnimation(this.animationOn, this.numberOfSteps, this.pauseTime);
        }

    }

    @Position(6)
    public boolean getStatisticsActivated() {
        return this.statsOn;
    }

    public void setStatisticsActivated(boolean toggle) {
        this.statsOn = toggle;
        this.notifyAllListeners(this.preconditionEvent);
    }

    public boolean preGetStoredMacro() {
        return this.statsOn;
    }

    @Position(7)
    @ComponentWidth(500)
    public String getStoredMacro() {
        assert this.preGetStoredMacro();

        return this.macro1.length() == 0 ? "Macro is empty." : "Macro [" + this.macro1 + "] stored as Token: " + this.macro1Token.getUserInputString();
    }

    public boolean preGetWordCount() {
        return this.statsOn;
    }

    @Position(8)
    public int getWordCount() {
        assert this.preGetWordCount();

        return this.totalWordCount;
    }

    public boolean preGetCommandCount() {
        return this.statsOn;
    }

    @Position(9)
    public int getCommandCount() {
        assert this.preGetCommandCount();

        return this.totalCommandCount;
    }

    public boolean preGetDistanceMoved() {
        return this.statsOn;
    }

    @Position(10)
    public int getDistanceMoved() {
        assert this.preGetDistanceMoved();

        return this.MyHalloweenSim != null ? this.MyHalloweenSim.getDistanceMoved() : 0;
    }

    public boolean preGetHousesAdded() {
        return this.statsOn;
    }

    @Position(11)
    public int getHousesAdded() {
        assert this.preGetHousesAdded();

        return this.MyHalloweenSim != null ? this.MyHalloweenSim.getHousesAdded() : 0;
    }

    public boolean preGetHousesRemoved() {
        return this.statsOn;
    }

    @Position(12)
    public int getHousesRemoved() {
        assert this.preGetHousesRemoved();

        return this.MyHalloweenSim != null ? this.MyHalloweenSim.getHousesRemoved() : 0;
    }

    public boolean preGetCupcakesEaten() {
        return this.statsOn;
    }

    @Position(13)
    public int getCupcakesEaten() {
        assert this.preGetCupcakesEaten();

        return this.MyHalloweenSim != null ? this.MyHalloweenSim.getCupcakesEaten() : 0;
    }

    public boolean preGetHousePreset1() {
        return this.statsOn;
    }

    @Position(14)
    public boolean getHousePreset1() {
        assert this.preGetHousePreset1();

        return this.MyHalloweenSim != null ? this.MyHalloweenSim.getPreset1() : false;
    }

    public boolean preGetHousePreset2() {
        return this.statsOn;
    }

    @Position(15)
    public boolean getHousePreset2() {
        assert this.preGetHousePreset2();

        return this.MyHalloweenSim != null ? this.MyHalloweenSim.getPreset2() : false;
    }

    public boolean preGetHousePreset3() {
        return this.statsOn;
    }

    @Position(16)
    public boolean getHousePreset3() {
        assert this.preGetHousePreset3();

        return this.MyHalloweenSim != null ? this.MyHalloweenSim.getPreset3() : false;
    }

    public boolean preGetHousePreset4() {
        return this.statsOn;
    }

    @Position(17)
    public boolean getHousePreset4() {
        assert this.preGetHousePreset4();

        return this.MyHalloweenSim != null ? this.MyHalloweenSim.getPreset4() : false;
    }

    public boolean preGetWorldSize() {
        return this.statsOn;
    }

    @Position(18)
    public int getWorldSize() {
        return this.simWorldSize;
    }

    @Visible(false)
    public void simulationListener(String simInput) {
        String OldMessage = this.SimErrorMessages;
        this.SimErrorMessages = simInput;
    }

    public void notifyAllListeners(PropertyChangeEvent event) {
        for(int i = 0; i < this.listeners.size(); ++i) {
            ((PropertyChangeListener)this.listeners.get(i)).propertyChange(event);
        }

    }

    private boolean checkWithAllVetoers(PropertyChangeEvent event) {
        boolean retVal = true;

        for(int i = 0; i < this.vetoers.size(); ++i) {
            retVal &= ((PropertyChangeVetoer)this.vetoers.get(i)).allowPropertyChange(event);
            if (!retVal) {
                return false;
            }
        }

        return true;
    }

    public void addPropertyChangeListener(PropertyChangeListener newListener) {
        AddedPropertyChangeListener.newCase(this, newListener);
        if (!this.listeners.contains(newListener)) {
            this.listeners.add(newListener);
        }

    }

    public void removePropertyChangeListener(PropertyChangeListener newListener) {
        RemovedPropertyChangeListener.newCase(this, newListener);
        this.listeners.remove(newListener);
    }

    public void addPropertyChangeVetoer(PropertyChangeVetoer newVetoer) {
        this.vetoers.add(newVetoer);
    }

    public void setAnimationActivated(boolean toggle) {
        boolean oldVal = this.animationOn;
        this.animationOn = toggle;
        if (this.MyHalloweenSim != null) {
            this.MyHalloweenSim.toggleAnimation(this.animationOn, this.numberOfSteps, this.pauseTime);
        }

        PropertyChangeEvent inputEvent = new PropertyChangeEvent(this, "AnimationActivated", oldVal, this.animationOn);
        this.notifyAllListeners(inputEvent);
        this.notifyAllListeners(this.preconditionEvent);
    }

    public void setInputString(String newVal) {
        SetProperty.newCase(this, "InputString", newVal);
        String oldInputString = this.inputString;
        PropertyChangeEvent inputEvent = new PropertyChangeEvent(this, "InputString", oldInputString, newVal);
        if (this.checkWithAllVetoers(inputEvent)) {
            if (this.isConnectedToSimulation()) {
                this.processCommand(newVal);
            }

            NotifiedPropertyChangeEvent.newCase(this, inputEvent, (PropertyChangeListener[])this.listeners.toArray(this.emptyPropertyChangeListenerArray));
            //tell outcoupler to send it out
            this.notifyAllListeners(inputEvent);
        } else {
            System.out.println("Property change vetoed");
        }

    }

    public void processCommand(String newInputString) {
        CommandSubmitted.newCase(this, newInputString);
        this.inputString = newInputString;
        if (this.MyHalloweenSim != null) {
            String oldErrorMessages = this.errorMessages;
            this.errorMessages = "";
            if (this.MyHalloweenSim != null) {
                this.MyHalloweenSim.clearErrorMessages();
            }

            this.stringProcessor = new AnIteratorEnabledScanner(this.inputString);
            this.stillNeedToTestCurrentToken = false;
            this.commands = 0;

            for(int loopCounter = 0; this.stringProcessor.hasNext(); ++loopCounter) {
                if (!this.stillNeedToTestCurrentToken || loopCounter > 1) {
                    this.stillNeedToTestCurrentToken = false;
                    loopCounter = 0;

                    try {
                        this.commandToken = this.stringProcessor.next();
                    } catch (AScannerException scannerE) {
                        this.errorMessages = this.errorMessages + scannerE.getMessage();
                        this.commandToken = scannerE.getThrownToken();
                    }

                    ++this.totalWordCount;
                }

                if (this.commandToken instanceof Move) {
                    try {
                        this.moveAvatar();
                    } catch (AParserException parserE) {
                        this.errorMessages = this.errorMessages + parserE.getMessage();
                    }
                }

                if (this.commandToken instanceof addHouse) {
                    try {
                        this.addHouse();
                    } catch (AParserException parserE) {
                        this.errorMessages = this.errorMessages + parserE.getMessage();
                    } catch (ASemanticException semanticE) {
                        this.errorMessages = this.errorMessages + semanticE.getMessage();
                    }
                }

                if (this.commandToken instanceof removeHouse) {
                    try {
                        this.removeHouse();
                    } catch (ASemanticException semanticE) {
                        this.errorMessages = this.errorMessages + semanticE.getMessage();
                    }
                }

                if (this.commandToken instanceof Give) {
                    try {
                        this.giveCandy();
                    } catch (AParserException parserE) {
                        this.errorMessages = this.errorMessages + parserE.getMessage();
                    } catch (ASemanticException semanticE) {
                        this.errorMessages = this.errorMessages + semanticE.getMessage();
                    }
                }

                if (this.commandToken instanceof Take) {
                    try {
                        this.takeCandy();
                    } catch (AParserException parserE) {
                        this.errorMessages = this.errorMessages + parserE.getMessage();
                    } catch (ASemanticException semanticE) {
                        this.errorMessages = this.errorMessages + semanticE.getMessage();
                    }
                }

                if (this.commandToken instanceof Animate) {
                    this.animateSimulation();
                }

                if (this.commandToken instanceof Undo) {
                    this.undoCommand();
                }

                if (this.commandToken instanceof Redo) {
                    this.redoCommand();
                }

                if (this.commandToken instanceof Preset) {
                    try {
                        this.storeHousePreset();
                    } catch (AParserException parserE) {
                        this.errorMessages = this.errorMessages + parserE.getMessage();
                    }
                }

                if (this.commandToken instanceof Eat) {
                    try {
                        this.eatCommand();
                    } catch (ASemanticException semanticE) {
                        this.errorMessages = this.errorMessages + semanticE.getMessage();
                    }
                }

                if (this.commandToken instanceof Statistics) {
                    this.toggleStatistics();
                }

                if (this.commandToken instanceof Assign) {
                    try {
                        this.assignMacroCommand();
                    } catch (AParserException parserE) {
                        this.errorMessages = this.errorMessages + parserE.getMessage();
                    }
                }

                if (this.commandToken instanceof Word && this.commandToken.getUserInputString().equals(this.macro1Token.getUserInputString())) {
                    this.processMacro();
                    ++this.commands;
                }
            }

            try {
                if (this.commands == 0) {
                    System.out.println(" >>>> No valid commands entered.");
                    throw new AParserException(" >>>> No valid commands entered.");
                }
            } catch (AParserException parserE) {
                this.errorMessages = this.errorMessages + parserE.getMessage();
            }

            this.macroBuildString = "";
            this.totalCommandCount += this.commands;
            PropertyChangeEvent errorEvent = new PropertyChangeEvent(this, "ErrorMessages", oldErrorMessages, this.errorMessages);
            this.notifyAllListeners(errorEvent);
        }
    }

    void moveAvatar() throws AParserException {
        if (this.MyHalloweenSim != null) {
            if (this.stringProcessor.hasNext()) {
                try {
                    this.placeholderToken = this.stringProcessor.next();
                } catch (AScannerException scannerE) {
                    this.errorMessages = this.errorMessages + scannerE.getMessage();
                    this.placeholderToken = scannerE.getThrownToken();
                }

                ++this.totalWordCount;
                if (this.placeholderToken instanceof stringProcessors.Number) {
                    int parameter1 = ((stringProcessors.Number)this.placeholderToken).convertToInt();
                    if (this.stringProcessor.hasNext()) {
                        try {
                            this.placeholderToken = this.stringProcessor.next();
                        } catch (AScannerException scannerE) {
                            this.errorMessages = this.errorMessages + scannerE.getMessage();
                            this.placeholderToken = scannerE.getThrownToken();
                        }

                        ++this.totalWordCount;
                        if (this.placeholderToken instanceof stringProcessors.Number) {
                            int parameter2 = ((stringProcessors.Number)this.placeholderToken).convertToInt();
                            this.undoer.execute(new MoveCommand(this.MyHalloweenSim, parameter1, parameter2));
                            this.macroBuildString = this.macroBuildString + "move " + parameter1 + " " + parameter2 + " ";
                            ++this.commands;
                        } else {
                            this.commandToken = this.placeholderToken;
                            this.stillNeedToTestCurrentToken = true;
                            throw new AParserException("Y is not an integer! Correct Syntax is:  move x y ");
                        }
                    } else {
                        throw new AParserException("Move Command Missing Y Value! Correct Syntax is: move x y ");
                    }
                } else {
                    this.commandToken = this.placeholderToken;
                    this.stillNeedToTestCurrentToken = true;
                    throw new AParserException("X is not an integer! Move Command must be followed by positive/negative integers for x and y.");
                }
            } else {
                throw new AParserException("Move Missing Parameters! Move Command must be followed by positive/negative integers for x and y.");
            }
        }
    }

    void addHouse() throws AParserException, ASemanticException {
        if (!this.MyHalloweenSim.preAddHouse()) {
            throw new ASemanticException("Cannot AddHouse, the neighborhood is full!");
        } else {
            if (this.stringProcessor.hasNext()) {
                try {
                    this.placeholderToken = this.stringProcessor.next();
                } catch (AScannerException scannerE) {
                    this.errorMessages = this.errorMessages + scannerE.getMessage();
                    this.placeholderToken = scannerE.getThrownToken();
                }

                ++this.totalWordCount;
                if (this.placeholderToken instanceof stringProcessors.Number) {
                    int parameter1 = ((stringProcessors.Number)this.placeholderToken).convertToInt();
                    if (parameter1 <= 0 || parameter1 >= 5) {
                        this.undoer.execute(new addHouseCommand(this.MyHalloweenSim));
                        this.macroBuildString = this.macroBuildString + "addhouse ";
                        ++this.commands;
                        throw new AParserException(" Macro Parameter must be between 1 and 4 - Parameter Discarded / Random House Added.");
                    }

                    this.undoer.execute(new addHouseCommand(this.MyHalloweenSim, parameter1));
                    this.macroBuildString = this.macroBuildString + "addhouse " + parameter1 + " ";
                    ++this.commands;
                } else {
                    this.commandToken = this.placeholderToken;
                    this.stillNeedToTestCurrentToken = true;
                    this.undoer.execute(new addHouseCommand(this.MyHalloweenSim));
                    this.macroBuildString = this.macroBuildString + "addhouse ";
                    ++this.commands;
                }
            } else {
                this.undoer.execute(new addHouseCommand(this.MyHalloweenSim));
                this.macroBuildString = this.macroBuildString + "addhouse ";
                ++this.commands;
            }

        }
    }

    void removeHouse() throws ASemanticException {
        if (!this.MyHalloweenSim.preRemoveHouse()) {
            throw new ASemanticException("Cannot RemoveHouse, the neighborhood is empty!");
        } else {
            this.undoer.execute(new removeHouseCommand(this.MyHalloweenSim));
            this.macroBuildString = this.macroBuildString + "removeHouse ";
            ++this.commands;
        }
    }

    void giveCandy() throws AParserException, ASemanticException {
        if (!this.MyHalloweenSim.preGiveCandy()) {
            throw new ASemanticException("Cannot Give Cupcakes, Your feet are not on the path of a house!");
        } else if (this.stringProcessor.hasNext()) {
            try {
                this.placeholderToken = this.stringProcessor.next();
            } catch (AScannerException scannerE) {
                this.errorMessages = this.errorMessages + scannerE.getMessage();
                this.placeholderToken = scannerE.getThrownToken();
            }

            ++this.totalWordCount;
            if (this.placeholderToken instanceof stringProcessors.Number) {
                int parameter1 = ((stringProcessors.Number)this.placeholderToken).convertToInt();
                this.undoer.execute(new GiveCommand(this.MyHalloweenSim, parameter1));
                this.macroBuildString = this.macroBuildString + "give " + parameter1 + " ";
                ++this.commands;
            } else if (this.placeholderToken instanceof Minus) {
                throw new AParserException("If you want to give a negative amount, just use the Take Command!");
            } else {
                this.commandToken = this.placeholderToken;
                this.stillNeedToTestCurrentToken = true;
                throw new AParserException("Give Command must be followed by positive integer.");
            }
        } else {
            throw new AParserException(this.errorMessages = this.errorMessages + "Missing Parameter! Give Command must be followed by positive integer.");
        }
    }

    void takeCandy() throws AParserException, ASemanticException {
        if (!this.MyHalloweenSim.preTakeCandy()) {
            throw new ASemanticException("Cannot Take Cupcakes, Your feet are not on the path of a house!");
        } else if (this.stringProcessor.hasNext()) {
            try {
                this.placeholderToken = this.stringProcessor.next();
            } catch (AScannerException scannerE) {
                this.errorMessages = this.errorMessages + scannerE.getMessage();
                this.placeholderToken = scannerE.getThrownToken();
            }

            ++this.totalWordCount;
            if (this.placeholderToken instanceof stringProcessors.Number) {
                int parameter1 = ((stringProcessors.Number)this.placeholderToken).convertToInt();
                this.undoer.execute(new TakeCommand(this.MyHalloweenSim, parameter1));
                this.macroBuildString = this.macroBuildString + "take " + parameter1 + " ";
                ++this.commands;
            } else if (this.placeholderToken instanceof Minus) {
                throw new AParserException("If you want to take a negative amount, just use the Give Command!");
            } else {
                this.commandToken = this.placeholderToken;
                this.stillNeedToTestCurrentToken = true;
                throw new AParserException("Take Command must be followed by positive integer.");
            }
        } else {
            throw new AParserException("Missing Parameter! Take Command must be followed by positive integer.");
        }
    }

    void storeHousePreset() throws AParserException {
        if (this.stringProcessor.hasNext()) {
            try {
                this.placeholderToken = this.stringProcessor.next();
            } catch (AScannerException scannerE) {
                this.errorMessages = this.errorMessages + scannerE.getMessage();
                this.placeholderToken = scannerE.getThrownToken();
            }

            ++this.totalWordCount;
            if (this.placeholderToken instanceof stringProcessors.Number) {
                int parameter1 = ((stringProcessors.Number)this.placeholderToken).convertToInt();
                if (parameter1 >= 1 && parameter1 <= this.simWorldSize) {
                    if (this.stringProcessor.hasNext()) {
                        try {
                            this.placeholderToken = this.stringProcessor.next();
                        } catch (AScannerException scannerE) {
                            this.errorMessages = this.errorMessages + scannerE.getMessage();
                            this.placeholderToken = scannerE.getThrownToken();
                        }

                        ++this.totalWordCount;
                        if (this.placeholderToken instanceof stringProcessors.Number) {
                            int parameter2 = ((Number)this.placeholderToken).convertToInt();
                            if (parameter2 >= 1 && parameter2 <= 4) {
                                this.undoer.execute(new PresetCommand(this.MyHalloweenSim, parameter1, parameter2));
                                ++this.commands;
                            } else {
                                throw new AParserException("Preset Not Stored! 2nd Argument must be between 1-4");
                            }
                        } else {
                            this.commandToken = this.placeholderToken;
                            this.stillNeedToTestCurrentToken = true;
                            throw new AParserException("Preset Not Stored! Correct Syntax is: preset (1-" + this.simWorldSize + ") (1-4)");
                        }
                    } else {
                        throw new AParserException("Preset Not Stored! Correct Syntax is: preset house# preset#");
                    }
                } else {
                    throw new AParserException("Preset Not Stored! 1st Argument must be between 1-" + this.simWorldSize);
                }
            } else {
                this.commandToken = this.placeholderToken;
                this.stillNeedToTestCurrentToken = true;
                throw new AParserException("Preset 1st & 2nd Parameters must be integers. # of house to copy (1-" + this.simWorldSize + "), # of preset to save (1-4).");
            }
        } else {
            throw new AParserException("Preset Command Missing Parameters! Preset must be followed by house # to copy, and Preset # to save into.");
        }
    }

    void animateSimulation() {
        if (!this.animationOn) {
            this.setAnimationActivated(true);
            this.errorMessages = this.errorMessages + " Animation set to ON.";
        } else {
            this.setAnimationActivated(false);
            this.errorMessages = this.errorMessages + " Animation set to OFF.";
        }

        ++this.commands;
    }

    void toggleStatistics() {
        if (!this.statsOn) {
            this.setStatisticsActivated(true);
            this.errorMessages = this.errorMessages + " Statistics set to DISPLAYED.";
        } else {
            this.setStatisticsActivated(false);
            this.errorMessages = this.errorMessages + " Statistics set to HIDDEN.";
        }

        ++this.commands;
    }

    public boolean preUndoCommand() {
        return this.undoer.getCanUndo();
    }

    public void undoCommand() {
        this.undoer.undo();
        this.macroBuildString = this.macroBuildString + "undo ";
        ++this.commands;
    }

    public boolean preRedoCommand() {
        return this.undoer.getCanRedo();
    }

    public void redoCommand() {
        this.undoer.redo();
        this.macroBuildString = this.macroBuildString + "redo ";
        ++this.commands;
    }

    void eatCommand() throws ASemanticException {
        if (!this.MyHalloweenSim.preEatCandy()) {
            throw new ASemanticException("You have no cupcakes to eat!  Better go do some trick-or-treating...");
        } else {
            this.undoer.execute(new EatCommand(this.MyHalloweenSim));
            this.macroBuildString = this.macroBuildString + "eat ";
            ++this.commands;
        }
    }

    void assignMacroCommand() throws AParserException {
        if (this.macroBuildString.length() > 0) {
            if (this.stringProcessor.hasNext()) {
                try {
                    this.placeholderToken = this.stringProcessor.next();
                } catch (AScannerException scannerE) {
                    this.errorMessages = this.errorMessages + scannerE.getMessage();
                    this.placeholderToken = scannerE.getThrownToken();
                }

                ++this.totalWordCount;
                if (this.placeholderToken instanceof Word) {
                    this.macro1Token = this.placeholderToken;
                    this.macro1 = this.macroBuildString;
                    ++this.commands;
                    throw new AParserException(" Assigned macro called [" + this.macro1Token + "] that contains [" + this.macro1 + "]");
                } else {
                    this.commandToken = this.placeholderToken;
                    this.stillNeedToTestCurrentToken = true;
                    throw new AParserException(" Assign Command must be followed by a valid word.");
                }
            } else {
                throw new AParserException(" No Parameter! Assign Command must be followed by a valid word.");
            }
        } else {
            throw new AParserException(" Cannot assign 0 commands to a macro!");
        }
    }

    void processMacro() {
        try {
            throw new AParserException("Processing Macro [" + this.macro1Token + "] which is: " + this.macro1);
        } catch (AParserException parserE) {
            this.errorMessages = this.errorMessages + parserE.getMessage();
            this.placeholderProcessor = this.stringProcessor;
            this.stringProcessor = new AnIteratorEnabledScanner(this.macro1);
            this.placeholderCommands = this.commands;
            this.commands = 0;

            for(int loopCounter = 0; this.stringProcessor.hasNext(); ++loopCounter) {
                if (!this.stillNeedToTestCurrentToken || loopCounter > 1) {
                    this.stillNeedToTestCurrentToken = false;
                    loopCounter = 0;

                    try {
                        this.commandToken = this.stringProcessor.next();
                    } catch (AScannerException scannerE) {
                        this.errorMessages = this.errorMessages + scannerE.getMessage();
                        this.commandToken = scannerE.getThrownToken();
                    }

                    ++this.totalWordCount;
                }

                if (this.commandToken instanceof Move) {
                    try {
                        this.moveAvatar();
                    } catch (AParserException parser2) {
                        this.errorMessages = this.errorMessages + parser2.getMessage();
                    }
                }

                if (this.commandToken instanceof addHouse) {
                    try {
                        this.addHouse();
                    } catch (AParserException parser3) {
                        this.errorMessages = this.errorMessages + parser3.getMessage();
                    } catch (ASemanticException semanticE) {
                        this.errorMessages = this.errorMessages + semanticE.getMessage();
                    }
                }

                if (this.commandToken instanceof removeHouse) {
                    try {
                        this.removeHouse();
                    } catch (ASemanticException semanticE) {
                        this.errorMessages = this.errorMessages + semanticE.getMessage();
                    }
                }

                if (this.commandToken instanceof Give) {
                    try {
                        this.giveCandy();
                    } catch (AParserException parser4) {
                        this.errorMessages = this.errorMessages + parser4.getMessage();
                    } catch (ASemanticException semanticE) {
                        this.errorMessages = this.errorMessages + semanticE.getMessage();
                    }
                }

                if (this.commandToken instanceof Take) {
                    try {
                        this.takeCandy();
                    } catch (AParserException parser5) {
                        this.errorMessages = this.errorMessages + parser5.getMessage();
                    } catch (ASemanticException semanticE) {
                        this.errorMessages = this.errorMessages + semanticE.getMessage();
                    }
                }

                if (this.commandToken instanceof Animate) {
                    this.animateSimulation();
                }

                if (this.commandToken instanceof Undo) {
                    this.undoCommand();
                }

                if (this.commandToken instanceof Redo) {
                    this.redoCommand();
                }

                if (this.commandToken instanceof Eat) {
                    try {
                        this.eatCommand();
                    } catch (ASemanticException semanticE) {
                        this.errorMessages = this.errorMessages + semanticE.getMessage();
                    }
                }
            }

            try {
                if (this.commands == 0) {
                    throw new AParserException(" >>>> No valid commands entered.");
                }
            } catch (AParserException parser6) {
                this.errorMessages = this.errorMessages + parser6.getMessage();
            }

            this.stringProcessor = this.placeholderProcessor;
            this.commands += this.placeholderCommands;
        }
    }

    public void setAnimateCars(boolean newVal) {
        if (this.MyHalloweenSim != null) {
            this.MyHalloweenSim.setAnimateCars(newVal);
        } else {
            System.out.println("AHalloweenCommandProcessor: Halloweeen Simulation Not Initialized - Ignoring setAnimateCars call");
        }

    }

    @Visible(false)
    public boolean getAnimateCars() {
        return this.MyHalloweenSim != null ? this.MyHalloweenSim.getAnimateCars() : false;
    }

    public void initSerializedObject() {
        this.listeners = new Vector();
        this.vetoers = new Vector();
        this.undoer = new AHistoryUndoer();
        this.macro1Token = new InvalidWord("macro1");
    }

    public boolean isConnectedToSimulation() {
        return this.connectedToSimulation;
    }

    public void setConnectedToSimulation(boolean newVal) {
        if (newVal != this.connectedToSimulation) {
            ConnectedToSimulation.newCase(this, newVal);
            this.connectedToSimulation = newVal;
            this.notifyAllListeners(new PropertyChangeEvent(this, "ConnectedToSimulation", (Object)null, newVal));
        }
    }
}

