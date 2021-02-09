package ranges;

import library.*;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class MDFCalculator {

    HashMap<Pocket, Double> equityMap = new HashMap<>();

    EquityCalcObserver obs = new EquityCalcObserver() {
        @Override
        public void updateEquity(float[] percentage, int progress) {

        }

        @Override
        public boolean checkStop() {
            return false;
        }
    };
    EquityCalc equityCalc = new EquityCalc(obs);
    Set<Card> dead = new HashSet();

    //Tells you which hands you (can) fold to a bet based on your range equity to not be exploitable

    public void raiseRatio() {
        //never raise PSB/overbet
        //raise halfpot bet with freq 12%
        //"raise" check (no bet) with freq 40%?

    }

    double equityOfValueHands = 0.0;

    private Set<Pocket> allProfitableCombinations(Set<Pocket> utgCurrent, Set<Pocket> callerWillContinue, Set<Card> board) {
        Set<Pocket> barrels = new HashSet<>();
        Pocket[] caller = callerWillContinue.toArray(new Pocket[0]);
        equityOfValueHands = 0.0;
        for (Pocket p : utgCurrent) {
            Pocket[] fire = {p};
            Pocket[][] eqInput = new Pocket[][]{fire, caller};
            double[] res = equityCalc.eqPercents(eqInput, board.toArray(new Card[0]), new Card[0]);
            if (res[0] > 50.0) {
                //System.out.println("Equity for " + p.toString() + " " + res[0]);
                barrels.add(p);
                equityOfValueHands += res[0];
            }
        }
        equityOfValueHands /= barrels.size();
        return barrels;
    }

    public void estimateBarrelLine(String[] runout, String pfa, String pfc, double flopBet, double turnBet, double riverBet) {
        Card[] flop = {new Card(runout[0]), new Card(runout[1]), new Card(runout[2])};
        Pocket[] utg = HoldemStrings.pocketsToArray(pfa, flop);
        Pocket[] bbflat = HoldemStrings.pocketsToArray(pfc, flop);
        Set<Pocket> aggCurrent = new HashSet<>(Arrays.asList(utg));
        Set<Pocket> passCurrent = new HashSet<>(Arrays.asList(bbflat));
        Set<Card> currentBoard = new HashSet<>(Arrays.asList(flop));

        System.out.println("FIRING FLOP BET");
        double[] btvRatios = this.bluffRatioByStreet(flopBet, turnBet, riverBet, 1.0);
        passCurrent = this.foldMdf(passCurrent, aggCurrent, currentBoard, flopBet);
        Set<Pocket> valueFlopCombos = this.allProfitableCombinations(aggCurrent, passCurrent, currentBoard);
        Set<Pocket> bluffFlopCombos = Range.generateBluffs(aggCurrent, valueFlopCombos, passCurrent, currentBoard.toArray(new Card[0]), btvRatios[0], this.equityOfValueHands);
        Set<Pocket> checks = new HashSet<>(aggCurrent);
        checks.removeAll(valueFlopCombos); checks.removeAll(bluffFlopCombos);
        System.out.println(Range.beautify(valueFlopCombos, currentBoard));
        System.out.println(Range.beautify(bluffFlopCombos, currentBoard));
        System.out.println("x " + Range.beautify(checks, currentBoard));
        System.out.println("into defending range");
        System.out.println(Range.beautify(passCurrent, currentBoard));
        valueFlopCombos.addAll(bluffFlopCombos);
        aggCurrent = valueFlopCombos;

        currentBoard.add(new Card(runout[3]));
        aggCurrent = Range.removeBoard(aggCurrent, currentBoard);
        passCurrent = Range.removeBoard(passCurrent, currentBoard);
        System.out.println("FIRING TURN BET");
        passCurrent = this.foldMdf(passCurrent, aggCurrent, currentBoard, turnBet);
        Set<Pocket> valueTurnCombos = this.allProfitableCombinations(aggCurrent, passCurrent, currentBoard);
        Set<Pocket> bluffTurnCombos = Range.generateBluffs(aggCurrent, valueTurnCombos, passCurrent, currentBoard.toArray(new Card[0]), btvRatios[1], this.equityOfValueHands);
        checks = new HashSet<>(aggCurrent);
        checks.removeAll(valueTurnCombos); checks.removeAll(bluffTurnCombos);
        System.out.println(Range.beautify(valueTurnCombos, currentBoard));
        System.out.println(Range.beautify(bluffTurnCombos, currentBoard));
        System.out.println("x " + Range.beautify(checks, currentBoard));
        System.out.println("into defending range");
        System.out.println(Range.beautify(passCurrent, currentBoard));
        valueTurnCombos.addAll(bluffTurnCombos);
        aggCurrent = valueTurnCombos;

        currentBoard.add(new Card(runout[4]));
        aggCurrent = Range.removeBoard(aggCurrent, currentBoard);
        passCurrent = Range.removeBoard(passCurrent, currentBoard);
        System.out.println("FIRING RIVER BET");
        passCurrent = this.foldMdf(passCurrent, aggCurrent, currentBoard, riverBet);
        Set<Pocket> valueRiverCombos = this.allProfitableCombinations(aggCurrent, passCurrent, currentBoard);
        Set<Pocket> bluffRiverCombos = Range.generateBluffs(aggCurrent, valueRiverCombos, passCurrent, currentBoard.toArray(new Card[0]), btvRatios[2], this.equityOfValueHands);
        checks = new HashSet<>(aggCurrent);
        checks.removeAll(valueRiverCombos); checks.removeAll(bluffRiverCombos);
        System.out.println(Range.beautify(valueRiverCombos, currentBoard));
        System.out.println(Range.beautify(bluffRiverCombos, currentBoard));
        System.out.println("x " + Range.beautify(checks, currentBoard));
        System.out.println("into defending range");
        System.out.println(Range.beautify(passCurrent, currentBoard));
        //TODO we still need to account for the equity of our bluffs when they are called
    }

    public void mdfCalldown(String[] runout, String pfa, String pfc, double flopBet, double turnBet, double riverBet) {
        Card[] flop = {new Card(runout[0]), new Card(runout[1]), new Card(runout[2])};
        Pocket[] utg = HoldemStrings.pocketsToArray(pfa, flop);
        Pocket[] bbflat = HoldemStrings.pocketsToArray(pfc, flop);
        Set<Pocket> utgCurrent = new HashSet<>(Arrays.asList(utg));
        Set<Pocket> bbCurrent = new HashSet<>(Arrays.asList(bbflat));
        HashSet<Card> currentBoard = new HashSet<>(Arrays.asList(flop));

        System.out.println("FACING FLOP BET");

        bbCurrent = this.foldMdf(bbCurrent, utgCurrent, currentBoard, flopBet);

        currentBoard.add(new Card(runout[3]));
        utgCurrent = Range.removeBoard(utgCurrent, currentBoard);
        bbCurrent = Range.removeBoard(bbCurrent, currentBoard);
        System.out.println("FACING TURN BET");
        bbCurrent = this.foldMdf(bbCurrent, utgCurrent, currentBoard, turnBet);

        currentBoard.add(new Card(runout[4]));
        utgCurrent = Range.removeBoard(utgCurrent, currentBoard);
        bbCurrent = Range.removeBoard(bbCurrent, currentBoard);
        System.out.println("FACING RIVER BET");
        this.foldMdf(bbCurrent, utgCurrent, currentBoard, riverBet);
    }

    public void valueOnlyBarrelLine(String[] runout, String pfa, String pfc, double flopBet, double turnBet, double riverBet) {
        Card[] flop = {new Card(runout[0]), new Card(runout[1]), new Card(runout[2])};
        Pocket[] utg = HoldemStrings.pocketsToArray(pfa, flop);
        Pocket[] bbflat = HoldemStrings.pocketsToArray(pfc, flop);
        Set<Pocket> aggCurrent = new HashSet<>(Arrays.asList(utg));
        Set<Pocket> passCurrent = new HashSet<>(Arrays.asList(bbflat));
        HashSet<Card> currentBoard = new HashSet<>(Arrays.asList(flop));

        System.out.println("FIRING FLOP BET");
        passCurrent = this.foldMdf(passCurrent, aggCurrent, currentBoard, flopBet);
        Set<Pocket> valueFlopCombos = this.allProfitableCombinations(aggCurrent, passCurrent, currentBoard);
        System.out.println(Range.beautify(valueFlopCombos, currentBoard));
        System.out.println("into defending range");
        System.out.println(Range.beautify(passCurrent, currentBoard));
        //TODO must merge with bluff range before dealing turn
        aggCurrent = valueFlopCombos;
        //Set<Pocket> bluffFlopCombos = this

        currentBoard.add(new Card(runout[3]));
        aggCurrent = Range.removeBoard(aggCurrent, currentBoard);
        passCurrent = Range.removeBoard(passCurrent, currentBoard);
        System.out.println("FIRING TURN BET");
        passCurrent = this.foldMdf(passCurrent, aggCurrent, currentBoard, turnBet);
        Set<Pocket> valueTurnCombos = this.allProfitableCombinations(aggCurrent, passCurrent, currentBoard);
        System.out.println(Range.beautify(valueTurnCombos, currentBoard));
        System.out.println("into defending range");
        System.out.println(Range.beautify(passCurrent, currentBoard));
        //TODO must merge with bluff range before dealing riv
        //Set<Pocket> bluffTurnCombos = this
        aggCurrent = valueTurnCombos;

        currentBoard.add(new Card(runout[4]));
        aggCurrent = Range.removeBoard(aggCurrent, currentBoard);
        passCurrent = Range.removeBoard(passCurrent, currentBoard);
        System.out.println("FIRING RIVER BET");
        passCurrent = this.foldMdf(passCurrent, aggCurrent, currentBoard, riverBet);
        Set<Pocket> valueRiverCombos = this.allProfitableCombinations(aggCurrent, passCurrent, currentBoard);
        System.out.println(Range.beautify(valueRiverCombos, currentBoard));
        System.out.println("into defending range");
        System.out.println(Range.beautify(passCurrent, currentBoard));
        //Set<Pocket> bluffRiverCombos = this
        //TODO we still need to account for the equity of our bluffs when they are called
    }


    public double minFlopBet = 0.2;
    public double maxFlopBet = 0.8;
    public double minTurnBet = 0.5;
    public double maxTurnBet = 1.2;
    public double minRiverBet = 0.66;
    public double maxRiverBet = 1.2; //
    public double maxShove = 2;

    public double sprGetsIn(double flop, double turn, double river) {
        double fp = 1 + (flop * 2);
        double tp = fp + (fp * turn * 2);
        double rp = tp + (tp * river * 2);
        return (rp - 1.0) / 2.0;
    }

    //The point of this method is to figure out the best way to have part of your range all-in with a big polarizing bet on the river.
    public double[] allInDownbetSizing(double effectiveStack, double pot, double preferredFlopSize) {
        double spr = (effectiveStack) / pot; //Target multiple
        if (spr < 3.88) {
            return new double[]{spr, 0.0, 0.0};
        }
        if (preferredFlopSize < 0.42) { //.25
            if (spr >= 3.88 && spr < 4.16)
                return new double[]{.25, 2.5, 0.0};
            if (spr >= 4.16 && spr < 4.5)
                return new double[]{.25, 2.7, 0.0};
            if (spr >= 4.5 && spr < 4.88)
                return new double[]{.25, 2.9, 0.0};
            if (spr >= 4.88 && spr < 5.16)
                return new double[]{.25, 0.66, 1.1};
            if (spr >= 5.16 && spr < 5.5)
                return new double[]{.25, 0.66, 1.2};
            if (spr >= 5.55 && spr < 5.88)
                return new double[]{.25, 0.75, 1.15};
            if (spr >= 5.88 && spr < 6.16)
                return new double[]{.25, 0.75, 1.25};
            if (spr >= 6.33 && spr < 6.65)
                return new double[]{.25, 0.75, 1.35};
            if (spr >= 6.65 && spr < 6.8)
                return new double[]{.25, 1.0, 1.1};
            if (spr >= 6.8 && spr < 7.33)
                return new double[]{.25, 1.0, 1.18};
            if (spr >= 7.33 && spr < 7.66)
                return new double[]{.25, 1.0, 1.28};
            if (spr >= 7.66 && spr < 8.33)
                return new double[]{.25, 1.0, 1.4};
            if (spr >= 8.33 && spr < 8.66)
                return new double[]{.25, 1.0, 1.5};
            if (spr >= 8.66 && spr < 9.3)
                return new double[]{.25, 1.0, 1.6};
            return new double[]{.25, 0.66, 1.0};


        }
        if (preferredFlopSize < 0.59) { //.59
            if (spr >= 3.88 && spr < 4.16)
                return new double[]{.5, 1.8, 0.0};
            if (spr >= 4.16 && spr < 4.5)
                return new double[]{.5, 1.9, 0.0};
            if (spr >= 4.5 && spr < 4.88)
                return new double[]{.5, 2.1, 0.0};
            if (spr >= 4.88 && spr < 5.16)
                return new double[]{.5, 0.66, 0.70};
            if (spr >= 5.16 && spr < 5.5)
                return new double[]{.5, 0.66, 0.75};
            if (spr >= 5.5 && spr < 5.88)
                return new double[]{.5, 0.66, 0.82};
            if (spr >= 5.88 && spr < 6.16)
                return new double[]{.5, 0.66, 0.90};
            if (spr >= 6.16 && spr < 6.5)
                return new double[]{.5, 0.66, 0.97};
            if (spr >= 6.5 && spr < 6.88)
                return new double[]{.5, 0.66, 1.05};
            if (spr >= 6.88 && spr < 7.16)
                return new double[]{.5, 0.66, 1.1};
            if (spr >= 7.16 && spr < 7.5)
                return new double[]{.5, 0.66, 1.2};
            if (spr >= 7.5 && spr < 7.88)
                return new double[]{.5, 0.66, 1.25};
            if (spr >= 7.88 && spr < 8.33)
                return new double[]{.5, 0.75, 1.2};
            if (spr >= 8.33 && spr < 8.66)
                return new double[]{.5, 0.75, 1.3};
            if (spr >= 8.66 && spr < 9.33)
                return new double[]{.5, 0.75, 1.4};
            if (spr >= 9.33 && spr < 9.66)
                return new double[]{.5, 1.0, 1.17};
            if (spr >= 9.66 && spr < 10.33)
                return new double[]{.5, 1.0, 1.25};
            if (spr >= 10.33 && spr < 10.66)
                return new double[]{.5, 1.0, 1.32};
            if (spr >= 10.66 && spr < 11.33)
                return new double[]{.5, 1.0, 1.4};
            return new double[]{.5, 0.66, 1.0};
        } else {
            if (spr >= 3.88 && spr < 4.16)
                return new double[]{.66, 1.45, 0.0};
            if (spr >= 4.16 && spr < 4.5)
                return new double[]{.66, 1.6, 0.0};
            if (spr >= 4.5 && spr < 4.88)
                return new double[]{.66, 1.75, 0.0};
            if (spr >= 4.88 && spr < 5.16)
                return new double[]{.66, 1.9, 0.0};
            if (spr >= 5.16 && spr < 5.5)
                return new double[]{.66, 2.0, 0.0};
            if (spr >= 5.5 && spr < 5.88)
                return new double[]{.66, 2.15, 0.0};
            if (spr >= 5.88 && spr < 6.16)
                return new double[]{.66, 2.3, 0.0};
            if (spr >= 6.16 && spr < 6.5)
                return new double[]{.66, 0.6, 0.85};
            if (spr >= 6.5 && spr < 6.88)
                return new double[]{.66, 0.66, 0.85};
            if (spr >= 6.88 && spr < 7.16)
                return new double[]{.66, 0.66, 0.90};
            if (spr >= 7.16 && spr < 7.5)
                return new double[]{.66, 0.66, 0.95};
            if (spr >= 7.5 && spr < 7.88)
                return new double[]{.66, 0.66, 1.0};
            if (spr >= 7.88 && spr < 8.16)
                return new double[]{.66, 0.66, 1.08};
            if (spr >= 8.16 && spr < 8.5)
                return new double[]{.66, 0.66, 1.15};
            if (spr >= 8.5 && spr < 8.88)
                return new double[]{.66, 0.66, 1.2};
            if (spr >= 8.88 && spr < 9.16)
                return new double[]{.66, 0.66, 1.26};
            if (spr >= 9.16 && spr < 9.5)
                return new double[]{.66, 0.75, 1.2};
            if (spr >= 9.5 && spr < 9.88)
                return new double[]{.66, 0.75, 1.25};
            if (spr >= 9.88 && spr < 10.16)
                return new double[]{.66, 0.75, 1.3};
            if (spr >= 10.16 && spr < 10.5)
                return new double[]{.66, 0.75, 1.36};
            if (spr >= 10.5 && spr < 10.88)
                return new double[]{.66, 0.75, 1.42};
            if (spr >= 10.88 && spr < 11.16)
                return new double[]{.66, 0.75, 1.48};
            if (spr >= 11.16 && spr < 11.5)
                return new double[]{.66, 1.0, 1.2};
            if (spr >= 11.5 && spr < 11.88)
                return new double[]{.66, 1.0, 1.25};
            if (spr >= 11.88 && spr < 12.16)
                return new double[]{.66, 1.0, 1.3};
            if (spr >= 12.16 && spr < 12.5)
                return new double[]{.66, 1.0, 1.35};
            if (spr >= 12.5 && spr < 12.88)
                return new double[]{.66, 1.0, 1.4};
            return new double[]{.66, 0.75, 1.0};
        }

    }

    //The benefit of this method is that the opponent's range is much less important.
    //You simply defend the top part of your range on every street to not be exploited by an aggressive strategy
    //Therefore equity is only important for sorting your hands, not making breakeven call decisions.
    public Set<Pocket> foldMdf(Set<Pocket> yourRange, Set<Pocket> opponentStartingRange, Set<Card> board, double potRatio) {
        double mdfRatio = 1 / (1 + potRatio);
        List<Pocket> sortedRange = sortByEquity(yourRange, opponentStartingRange, board);
        boolean haveEnoughCalls = false;
        int mdfCombos = (int) Math.round(mdfRatio * sortedRange.size());
        Set<Pocket> continuance = new HashSet<>();
        int i = 0;
        for (Pocket yourHand : sortedRange) {
            double eq = equityMap.get(yourHand);
            if (!haveEnoughCalls && i > mdfCombos) {
                haveEnoughCalls = true;
                //System.out.println("The above combinations satisfy MDF");
            }
            if (!haveEnoughCalls) {
                continuance.add(yourHand);
            }
            //System.out.println(yourHand.toString() + " " + eq);
            i++;
        }
        return continuance;
    }

    public Set<Pocket> foldLowestEquity(Set<Pocket> yourRange, Set<Pocket> opponentRange, Set<Card> board, double potRatio) {
        double mdfRatio = 1 / (1 + potRatio);
        double equityToCall = potRatio / (1 + potRatio + potRatio) * 100;
        List<Pocket> sortedRange = sortByEquity(yourRange, opponentRange, board);
        boolean lackEquity = false, haveEnoughCalls = false;
        int mdfCombos = (int) Math.round(mdfRatio * sortedRange.size());
        Set<Pocket> continuance = new HashSet<>();
        int i = 0;
        for (Pocket yourHand : sortedRange) {
            double eq = equityMap.get(yourHand);
            if (!lackEquity && eq < equityToCall) {
                lackEquity = true;
                //System.out.println("The above combinations satisfy equity");
            }
            if (!haveEnoughCalls && i > mdfCombos) {
                haveEnoughCalls = true;
                //System.out.println("The above combinations satisfy MDF");
            }
            if (!lackEquity) { //TODO consider using mdf instead
                continuance.add(yourHand);
            }
            System.out.println(yourHand.toString() + " " + eq);
            i++;
        }
        return continuance;
    }

    public List<Pocket> sortByEquity(Set<Pocket> yourRange, Set<Pocket> opponentRange, Set<Card> board) {
        Pocket[] rangeArr = yourRange.toArray(new Pocket[0]);
        buildEquityMap(rangeArr, opponentRange.toArray(new Pocket[0]), board.toArray(new Card[0]));
        Comparator<Pocket> comparator = (o2, o1) -> (int) (1000 * (equityMap.get(o1) - equityMap.get(o2)));
        List<Pocket> ret = Arrays.asList(rangeArr);
        Collections.sort(ret, comparator);
        return ret;
    }

    public void buildEquityMap(Pocket[] yourRange, Pocket[] opponentRange, Card[] board) {
        this.equityMap = new HashMap<>();
        for (Pocket p : yourRange) {
            Pocket[] yourhand = {p};
            Pocket[][] allhands = {yourhand, opponentRange};
            double equity = equityCalc.eqPercents(allhands, board, dead.toArray(new Card[0]))[0];
            equityMap.put(p, equity);
        }
        /*/
        /This is what I WOULD use, if it were any faster...
        List<Pocket> arr = Arrays.asList(yourRange);
            arr.parallelStream().forEach((p ) -> {
                Pocket[] yourhand = {p};
                Pocket[][] allhands = {yourhand, opponentRange};
                EquityCalc calc = new EquityCalc(obs);
                double equity = calc.eqPercents(allhands, board, dead.toArray(new Card[0]))[0];
                equityMap.put(p, equity);
            });
         */
    }

    public double[] bluffRatioByStreet(double flopSize, double turnSize, double riverSize, double overbluff) {
        double[] ret = new double[3];
        double valueComponent = 1 - (riverSize / (1 + 2 * riverSize));
        ret[2] = (1 - valueComponent) * overbluff;
        valueComponent *= 1 - (turnSize / (1 + 2 * turnSize));
        ret[1] = (1 - valueComponent) * overbluff;
        valueComponent *= 1 - (flopSize / (1 + 2 * flopSize));
        ret[0] = (1 - valueComponent) * overbluff;
        return ret;
    }

    public List<Pocket> bestValueCombinations(Set<Pocket> input, Set<Pocket> opp, Set<Card> board, int count) {
        List<Pocket> sortedRange = sortByEquity(input, opp, board);
        List<Pocket> value = new ArrayList<>();
        for (Pocket p : sortedRange) {
            List<Card> hand = new ArrayList<>(board);
            hand.add(p.getCard(0));
            hand.add(p.getCard(1));
            Card[] handArr = hand.toArray(new Card[0]);
            boolean fire = false;
            if (HandValue.getHandValue(handArr) > HandValue.STRAIGHTMUL && HandValue.getHandValue(handArr) < HandValue.BOATMUL) {
                fire = true;
            }
            if (!fire) {
                for (Card c : p.getCards()) {
                    for (Card b : board) {
                        if (c.rank.equals(b.rank)) { //Any other made hand
                            fire = true;
                            break;
                        }
                    }
                    if (fire) break;
                }
            }
            if (fire) {
                value.add(p);
            }
            if (value.size() >= count) {
                return value;
            }
        }
        return value;
    }
}
