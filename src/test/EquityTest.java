import library.*;
import org.junit.Test;
import ranges.Constants.PFC;
import ranges.Constants.RFI;
import ranges.MDFCalculator;
import ranges.Range;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class EquityTest {


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
    Card[] dead = new Card[0];

    @Test
    public void drawingDead(){
        Pocket nuts = new Pocket("Qs Qh");
        Pocket loser = new Pocket("5s Jh");
        Pocket otherLoser = new Pocket("6s Jh");
        Pocket[] winningHands = {nuts};
        Pocket[] losingHands = {loser, otherLoser};
        Pocket[][] players = {winningHands, losingHands};
        Card[] board = {new Card("Qc"), new Card("Qd"), new Card("Js")};
        double[] results = equityCalc.eqPercents(players, board, dead);
        assert(results[0] == 100);
        assert(results[1] == 0);
    }

    @Test
    public void toppestPairVsOvercards(){
        Pocket tpwk = new Pocket("Qs 2h");
        Pocket overcards = new Pocket("As Kh");
        Pocket[] winningHands = {tpwk};
        Pocket[] losingHands = {overcards};
        Pocket[][] players = {winningHands, losingHands};
        Card[] board = {new Card("Qc"), new Card("Jd"), new Card("4s")};
        double[] results = equityCalc.eqPercents(players, board, dead);
        assert(Math.round(results[0]) == 63);
        assert(Math.round(results[1]) == 37);
    }

    @Test
    public void straightVsFlushmade(){
        Pocket fd = new Pocket("Ad Qd");
        Pocket sd = new Pocket("Th 9c");
        Pocket[] winningHands = {fd};
        Pocket[] losingHands = {sd};
        Pocket[][] players = {winningHands, losingHands};
        Card[] board = {new Card("Qc"), new Card("Jd"), new Card("3d")};
        double[] results = equityCalc.eqPercents(players, board, dead);
        System.out.println(results[0]);
        assert(Math.round(results[0]) == 86);
        assert(Math.round(results[1]) == 14);
    }

    @Test
    public void flushVsStraightMade(){
        Pocket fd = new Pocket("6d 7d");
        Pocket sd = new Pocket("Jh Tc");
        Pocket[] winningHands = {fd};
        Pocket[] losingHands = {sd};
        Pocket[][] players = {winningHands, losingHands};
        Card[] board = {new Card("Kc"), new Card("Qd"), new Card("3d")};
        double[] results = equityCalc.eqPercents(players, board, dead);
        System.out.println(results[0]);
        assert(Math.round(results[0]) == 52);
        assert(Math.round(results[1]) == 48);
    }

    @Test
    public void rangeAdvantageUtgvsAny2GoodFlop(){
        Pocket[] winningHands = HoldemStrings.pocketsToArray("66+, A9s+, KTs+, QTs+, JTs, T9s, 98s, A5s, A4s, A3s, A2s, AKo, AQo");
        Pocket[] losingHands = Range.anyTwo();
        Pocket[][] players = {winningHands, losingHands};
        Card[] board = {new Card("Qc"), new Card("Jd"), new Card("3d")};
        double[] results = equityCalc.eqPercents(players, board, dead);
        assert(Math.round(results[0]) == 70);
        assert(Math.round(results[0]) == 30);
    }

    @Test
    public void rangeAdvantageUtgvsAny2BadFlop(){
        Pocket[] winningHands = HoldemStrings.pocketsToArray("66+, A9s+, KTs+, QTs+, JTs, T9s, 98s, A5s, A4s, A3s, A2s, AKo, AQo");
        Pocket[] losingHands = Range.anyTwo();
        Pocket[][] players = {winningHands, losingHands};
        Card[] board = {new Card("7c"), new Card("4d"), new Card("2d")};
        double[] results = equityCalc.eqPercents(players, board, dead);
        System.out.println(results[0]);
        assert(Math.round(results[0]) == 59);
        assert(Math.round(results[1]) == 41);
    }

    @Test
    public void rangeAdvantageUtgvsAny2WorstFlop(){
        Pocket[] winningHands = HoldemStrings.pocketsToArray("66+, A9s+, KTs+, QTs+, JTs, T9s, 98s, A5s, A4s, A3s, A2s, AKo, AQo");
        Pocket[] losingHands = Range.anyTwo();
        Pocket[][] players = {winningHands, losingHands};
        Card[] board = {new Card("7c"), new Card("6d"), new Card("5h")};
        double[] results = equityCalc.eqPercents(players, board, dead);
        System.out.println(results[0]);
        assert(Math.round(results[0]) == 52);
        assert(Math.round(results[1]) == 48);
    }

    @Test
    //TODO
    public void rangeAdvantageBBvsCO(){
        Pocket nuts = new Pocket("Qs Qh");
        Pocket loser = new Pocket("5s Jh");
        Pocket[] winningHands = {nuts};
        Pocket[] losingHands = {loser};
        Pocket[][] players = {winningHands, losingHands};
        Card[] board = {new Card("Qc"), new Card("Qd"), new Card("Js")};
        double[] results = equityCalc.eqPercents(players, board, dead);
        assert(Math.round(results[0]) == 70);
        assert(Math.round(results[1]) == 30);
    }

    @Test
    public void bbFlatVsUtgRfiWetFlop(){
        Card[] flop = {new Card("Kc"), new Card("Qd"), new Card("4c")};
        Pocket[] utg = HoldemStrings.pocketsToArray(RFI.UTG, flop);
        Pocket[] bbflat = HoldemStrings.pocketsToArray(PFC.BB_v_UTG, flop);
        Set<Pocket> utgCurrent = new HashSet<>(Arrays.asList(utg));
        Set<Pocket> bbCurrent = new HashSet<>(Arrays.asList(bbflat));
        HashSet<Card> board = new HashSet<>(Arrays.asList(flop));

        MDFCalculator mdfCalculator = new MDFCalculator();
        //UTG can cbet 100% on the flop in this spot with their range advantage
        bbCurrent = mdfCalculator.foldLowestEquity(bbCurrent, utgCurrent, board, 0.5);

        board.add(new Card("6c"));
        utgCurrent = Range.removeBoard(utgCurrent, board);
        bbCurrent = Range.removeBoard(bbCurrent, board);
        double rangeAdvantage = Range.rangeAdvantage(utgCurrent, bbCurrent, board);
        double nutAdvantage = Range.nutAdvantage(utgCurrent, bbCurrent, board, .10);
        System.out.println("After betting flop with entire range and getting continuance...");
        System.out.println("For turn card 6c");
        System.out.println("Your range equity is " + rangeAdvantage);
        System.out.println("Your nut equity (10%) is " + nutAdvantage);
        List<Pocket> valueRange = mdfCalculator.bestValueCombinations(utgCurrent, bbCurrent, board, 10);
        System.out.println("barrel");
        for(Pocket p : valueRange){
            System.out.println(p);
        }
        //utg = Range.reasonableTurnBarrels(utg, board, .5);
        //Pocket[] bbTurnContinue = mdfCalculator.mdfFolds(bbFlopContinue, utg, board, 0.75);
        //mdfCalculator.mdfFolds(bbTurnContinue, utg, board, 1.2);
    }

    @Test
    public void bluffToValueTest(){
        MDFCalculator mdfCalculator = new MDFCalculator();
        double[] btv = mdfCalculator.bluffRatioByStreet(1.0, 1.0, 1.0, 1.0);
        assert(Math.round(btv[0] * 100.0) == 70);
        assert(Math.round(btv[1] * 100.0) == 56);
        assert(Math.round(btv[2] * 100.0) == 33);
        //underbluff
        btv = mdfCalculator.bluffRatioByStreet(1.0, 1.0, 1.0, 0.7);
        assert(Math.round(btv[0] * 100.0) == 49);
        assert(Math.round(btv[1] * 100.0) == 39);
        assert(Math.round(btv[2] * 100.0) == 23);
        //sizing smaller
        btv = mdfCalculator.bluffRatioByStreet(0.3, 0.75, 0.75, 1.0);
        assert(Math.round(btv[0] * 100.0) == 60);
        assert(Math.round(btv[1] * 100.0) == 51);
        assert(Math.round(btv[2] * 100.0) == 30);
        //overbet river
        btv = mdfCalculator.bluffRatioByStreet(1.0, 1.0, 1.25, 1.0);
        assert(Math.round(btv[0] * 100.0) == 71);
        assert(Math.round(btv[1] * 100.0) == 57);
        assert(Math.round(btv[2] * 100.0) == 36);
    }

    @Test
    public void bbMdfCalldownTest(){
        String[] board = {"Qh", "Td", "2h", "4s","6d"};
        MDFCalculator mdfCalculator = new MDFCalculator();
        mdfCalculator.mdfCalldown(board, RFI.HJ, PFC.BB_v_HJ, 0.33, 0.66, 1.0);
    }

    @Test
    public void hjTripleBarrelLineTest(){
        //String[] board = {"Qh", "Td", "2h", "4s","6d"}; //no draws
        //String[] board = {"Qh", "Td", "2h", "4s","4h"}; //draw gets there
        String[] board = {"Kh", "7d", "2c", "9h","Th"}; //double backdoors hits
        //String[] board = {"Qh", "Td", "2h", "9h","Jh"}; //everything gets there lol

        MDFCalculator mdfCalculator = new MDFCalculator();
        mdfCalculator.estimateBarrelLine(board, RFI.HJ, PFC.BB_v_HJ, 0.5, 0.75, 1.0);
    }

    @Test
    public void utgTripleBarrelLineTest(){
        String[] board = {"Th", "7d", "2h", "8h","6d"};
        MDFCalculator mdfCalculator = new MDFCalculator();
        mdfCalculator.estimateBarrelLine(board, RFI.UTG, PFC.BB_v_UTG, 0.5, 0.7, 1.0);
    }

    @Test
    public void condenserTest(){
        Pocket[] arr = HoldemStrings.pocketsToArray(RFI.UTG);
        Set<Pocket> p = new HashSet<>();
        for(Pocket a : arr){
            p.add(a);
        }
        System.out.println(Range.beautify(p, new HashSet<>()));
    }

    @Test
    public void allInDownbetSizingTest(){
        MDFCalculator mdfCalculator = new MDFCalculator();
        //universal short strategies
        System.out.println(mdfCalculator.sprGetsIn(.66, 0.75, 1.0));
        System.out.println(mdfCalculator.sprGetsIn(1,0.0,0.0));
        System.out.println(mdfCalculator.sprGetsIn(2,0.0,0.0));

        //strategies for dry flop with range advantage
        System.out.println(mdfCalculator.sprGetsIn(3,0.0,0.0));
        System.out.println(mdfCalculator.sprGetsIn(.25, 0.66,1.0)); //can't get it in

        //strategies for wet flop, or no range advantage
        System.out.println("w");
        System.out.println(mdfCalculator.sprGetsIn(.5, 1.2,0.0));
        System.out.println(mdfCalculator.sprGetsIn(.5, 0.66,1.0));

        //strategies for wet flop with no range advantage
        System.out.println("r");
        System.out.println(mdfCalculator.sprGetsIn(.66, 1.0,0.0));
        System.out.println(mdfCalculator.sprGetsIn(.66, 0.75,1.0));
        //universal deep stack strategies

        /*
        for(double i=1; i < 10; i+=0.3){
            double[] res = mdfCalculator.allInDownbetSizing(i, 1.0, 0.66);
            System.out.println("behind: " + i + " [" + res[0] + " " + res[1] + " " + res[2] + "]");
        }
        for(double i=1; i < 10; i+=0.3){
            double[] res = mdfCalculator.allInDownbetSizing(i, 1.0, 0.33);
            System.out.println("behind: " + i + " [" + res[0] + " " + res[1] + " " + res[2] + "]");
        }*/
    }

    @Test
    public void bbFlatVsUtgRfiDryFlop(){
        Card[] flop = {new Card("4d"), new Card("3s"), new Card("2h")};
        Pocket[] utg = HoldemStrings.pocketsToArray(RFI.UTG, flop);
        Pocket[] bbflat = HoldemStrings.pocketsToArray(PFC.BB_v_UTG, flop);
        Set<Pocket> utgCurrent = new HashSet<>(Arrays.asList(utg));
        Set<Pocket> bbCurrent = new HashSet<>(Arrays.asList(bbflat));
        HashSet<Card> board = new HashSet<>(Arrays.asList(flop));

        double rangeAdvantage = Range.rangeAdvantage(utgCurrent, bbCurrent, board);
        System.out.println("After flop range advantage " + rangeAdvantage);

        MDFCalculator mdfCalculator = new MDFCalculator();
        //UTG can cbet 100% on the flop in this spot with their range advantage
        bbCurrent = mdfCalculator.foldLowestEquity(bbCurrent, utgCurrent, board, 0.33);

        board.add(new Card("7h"));
        utgCurrent = Range.removeBoard(utgCurrent, board);
        bbCurrent = Range.removeBoard(bbCurrent, board);
        rangeAdvantage = Range.rangeAdvantage(utgCurrent, bbCurrent, board);
        double nutAdvantage = Range.nutAdvantage(utgCurrent, bbCurrent, board, .10);
        System.out.println("After betting flop with entire range and getting continuance...");
        System.out.println("For turn card 7h");
        System.out.println("Your range equity is " + rangeAdvantage);
        System.out.println("Your nut equity (10%) is " + nutAdvantage);
        List<Pocket> valueRange = mdfCalculator.bestValueCombinations(utgCurrent, bbCurrent, board, 10);
        System.out.println("barrel");
        for(Pocket p : valueRange){
            System.out.println(p);
        }
        //utg = Range.reasonableTurnBarrels(utg, board, .5);
        //Pocket[] bbTurnContinue = mdfCalculator.mdfFolds(bbFlopContinue, utg, board, 0.75);
        //mdfCalculator.mdfFolds(bbTurnContinue, utg, board, 1.2);
    }

    /*
    Solved flops for range advantage (UTG vs BB)
    "Strong"
    rank matching suits: range equity
    AKJ 2: 55.4
    KJ2 0: 55.82
    QQ5 0: 61.19

    "Moderate"
    776 2: 60.84
    853 3: 58.89
    543 0: 59.58

    "Weak to no advantage"
    975 0: 60.32
    765 2: 59.67

    Solved flops for range advantage (HJ vs BB)
    AKJ 2: 55.4
    KJ2 0: 54.86
    QQ5 0: 56.32

    "Moderate"
    776 2: 60.84
    853 3: 58.89
    543 0: 58.03

    "Weak to no advantage"
    975 0: 54.94
    765 2: 59.67

     */
}
