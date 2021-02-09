package ranges;

import library.Card;
import library.HandValue;
import library.HoldemStrings;
import library.Pocket;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Range {

    public static Pocket[] anyTwo(){
        return HoldemStrings.pocketsToArray("22+, A2o+, K2o+, Q2o+, J2o+ T2o+, 92o+, 82o+, 72o+, 62o+, 52o+, 42o+, 32o, A2s+, K2s+, Q2s+, J2s+ T2s+, 92s+, 82s+, 72s+, 62s+, 52s+, 42s+, 32s");
    }
    public static Pocket[] anyTwo(Set<Card> dead){
        Pocket[] temp =  anyTwo();
        Set<Pocket> tmp = new HashSet<>(Arrays.asList(temp));
        return removeBoard(tmp, dead).toArray(new Pocket[0]);
    }

    static Comparator<Card> boardSorts = (o1, o2) -> (o2.rank.value - o1.rank.value);

    public static Set<Pocket> removeBoard(Set<Pocket> temp, Set<Card> dead) {
        Set<Pocket> ret = new HashSet<>();
        for(Pocket p : temp){
            boolean deadCard = false;
            for(Card c : dead){
                if(p.getCard(0).equals(c)){
                    deadCard = true;
                    break;
                }
                if(p.getCard(1).equals(c)){
                    deadCard = true;
                    break;
                }
            }
            if(!deadCard){
                ret.add(p);
            }
        }
        return ret;
    }

    public static Pocket[] reasonableTurnBarrels(Set<Pocket> input, Card[] board){
        return reasonableTurnBarrels(input, board, 1.0);
    }

    public static Pocket[] reasonableTurnBarrels(Set<Pocket> input, Card[] board, double bluffToValue){
        Arrays.sort(board, boardSorts);
        Set<Pocket> value = new HashSet<>();
        value = addMadeStraightsAndFlushes(value, input, board);
        value = addTpgkAndBetter(value, input, board);
        System.out.println("Identified " + value.size() + " turn barrels for value");
        for(Pocket p : value){
            System.out.println(p.toString());
        }

        //TODO rather than running turn/river equity against any 2 cards we need this

        Set<Pocket> bluffs = new HashSet<>();
        bluffs = addEightOutStraightDraws(bluffs, input, board);
        bluffs = addGutshots(bluffs, input, board);
        bluffs = addFlushDraws(bluffs, input, board);
        bluffs.removeAll(value);
        Comparator<Pocket> showdownValue = (o1, o2) -> {
            Card[] hand1 = {o1.getCard(0), o1.getCard(1), board[0], board[1], board[2]};
            Card[] hand2 = {o2.getCard(0), o2.getCard(1), board[0], board[1], board[2]};
            return HandValue.getHandValue(hand1) - HandValue.getHandValue(hand2);
        };
        List<Pocket> sortedBluffs = bluffs.stream().collect(Collectors.toList());
        sortedBluffs.sort(showdownValue);
        if(sortedBluffs.size() > value.size() * bluffToValue){
            sortedBluffs = sortedBluffs.subList(0, (int) (value.size() * bluffToValue));
        }

        System.out.println("Polarizing range with the  " + sortedBluffs.size() + " weakest showdown value draws as bluffs");
        for(Pocket p : sortedBluffs){
            System.out.println(p.toString());
        }

        //bluffs
        //openenders
        //flush draws
        //gutshots with 2 overcards or BDFD
        Pocket[] arr = new Pocket[0];
        return arr;
    }

    private static Set<Pocket> addFlushDraws(Set<Pocket> barrels, Set<Pocket> input, Card[] board) {
        for(Pocket p : input) {
            List<Card> hand = new ArrayList<>(Arrays.asList(board));
            hand.add(p.getCard(0));
            hand.add(p.getCard(1));
            int clubs =0, diamonds=0, hearts=0, spades=0;
            for(Card c : hand){
                if(c.suit.value == 0){
                    clubs++;
                }
                if(c.suit.value == 1){
                    diamonds++;
                }
                if(c.suit.value == 2){
                    hearts++;
                }
                if(c.suit.value ==3){
                    spades++;
                }
            }
            if(clubs == 4 || hearts == 4 || spades == 4 || diamonds == 4){
                barrels.add(p);
            }
        }
        return barrels;
    }

    //Only valid on the turn, not the river or flop
    private static Set<Pocket> addEightOutStraightDraws(Set<Pocket> barrels, Set<Pocket> input, Card[] board) {
        for(Pocket p : input) {
            List<Card> handL = new ArrayList<>(Arrays.asList(board));
            handL.add(p.getCard(0));
            handL.add(p.getCard(1));
            Card[] hand = handL.toArray(new Card[0]);
            long bitmask = hand[0].getSingleRanksMap();
            for(int i=1; i< hand.length; i++){
                bitmask = bitmask | hand[i].getSingleRanksMap();
            }
            String straightString = pad14(Long.toBinaryString(bitmask));
            if(straightString.contains("011110")){
                barrels.add(p);
            }else{
                if(straightString.startsWith("1111")){
                    barrels.add(p);
                }
                if(straightString.endsWith("1111")){
                    barrels.add(p);
                }
            }
        }
        return barrels;
    }


    public static Set<Pocket> generateBluffs(Set<Pocket> input, Set<Pocket> value, Set<Pocket> opponent, Card[] board, double bluffToValue, double equityOfValueHands){
        int trueValueCombos = (int) (equityOfValueHands / 100.0 * value.size());
        int bluffCombos = (int) (bluffToValue * trueValueCombos / (1-bluffToValue));
        if(board.length == 4){ //bluffs have no equity on the river, and too many backdoor or junk hands bluff the flop to account for this
            bluffCombos *=  1.15; //assuming bluffs will have 15% equity on the turn (oesd+fds minus some times when drawing dead or the board pairs)
        }
        Comparator<Pocket> showdownValue = (o1, o2) -> {
            Card[] hand1 = {o1.getCard(0), o1.getCard(1), board[0], board[1], board[2]};
            Card[] hand2 = {o2.getCard(0), o2.getCard(1), board[0], board[1], board[2]};
            return HandValue.getHandValue(hand1) - HandValue.getHandValue(hand2);
        };

        Set<Pocket> bluffs = new HashSet<>();
        if(board.length == 5){//no draws on river
            input.removeAll(value);
            MDFCalculator mdfCalculator = new MDFCalculator();
            Set<Card> b = new HashSet<>();
            b.addAll(Arrays.asList(board));
            List<Pocket> sortedBluffs = mdfCalculator.sortByEquity(input, opponent, b);
            Collections.reverse(sortedBluffs);
            if(sortedBluffs.size() > bluffCombos){
                sortedBluffs = sortedBluffs.subList(0, bluffCombos);
            }
            return sortedBluffs.stream().collect(Collectors.toSet());
        }//flop or turn
        bluffs = addFlushDraws(bluffs, input, board);
        bluffs = addEightOutStraightDraws(bluffs, input, board);
        bluffs.removeAll(value);
        if(bluffs.size() < bluffCombos){
            bluffs = addGutshots(bluffs, input, board);
            bluffs.removeAll(value);
            if(bluffs.size() < bluffCombos){
                if(board.length == 3){
                    List<Pocket> additionalBluffs = addDoubleBackdoors(input, board).stream().collect(Collectors.toList()); //no draw equity on turn but idc need more bluffs
                    additionalBluffs.removeAll(bluffs);
                    additionalBluffs.removeAll(value);
                    additionalBluffs.sort(showdownValue);
                    if(additionalBluffs.size() + bluffs.size() < bluffCombos){
                        bluffs.addAll(additionalBluffs);
                    }else{
                        bluffs.addAll(additionalBluffs.subList(0, bluffCombos - bluffs.size()));
                    }
                    bluffs.removeAll(value);
                }
                if(bluffs.size() < bluffCombos) {
                    List<Pocket> junkyBluffs = input.stream().collect(Collectors.toList());
                    junkyBluffs.sort(showdownValue);
                    junkyBluffs.removeAll(value);
                    junkyBluffs.removeAll(bluffs);
                    if(junkyBluffs.size() + bluffs.size() < bluffCombos){
                        bluffs.addAll(junkyBluffs);
                    }else{
                        bluffs.addAll(junkyBluffs.subList(0, bluffCombos - bluffs.size()));
                    }
                }
            }
        }
        bluffs.removeAll(value);

        List<Pocket> sortedBluffs = bluffs.stream().collect(Collectors.toList());
        sortedBluffs.sort(showdownValue);
        if(sortedBluffs.size() > bluffCombos){
            sortedBluffs = sortedBluffs.subList(0, bluffCombos);
        }

        //System.out.println("Polarizing range with the  " + sortedBluffs.size() + " weakest showdown value draws as bluffs");
        for(Pocket p : sortedBluffs){
            //System.out.println(p.toString());
        }
        return bluffs;
    }

    private static Set<Pocket> addDoubleBackdoors(Set<Pocket> input, Card[] board) {
        Set<Pocket> barrels = new HashSet<>();
        for(Pocket p : input) {
            List<Card> handL = new ArrayList<>(Arrays.asList(board));
            handL.add(p.getCard(0));
            handL.add(p.getCard(1));
            Card[] hand = handL.toArray(new Card[0]);
            if(p.getCard(0).suit == p.getCard(1).suit && (p.getCard(0).suit == board[0].suit || p.getCard(0).suit == board[1].suit || p.getCard(0).suit == board[2].suit)){
                long bitmask = hand[0].getSingleRanksMap();
                for(int i=1; i< hand.length; i++){
                    bitmask = bitmask | hand[i].getSingleRanksMap();
                }
                String straightString = pad14(Long.toBinaryString(bitmask));
                if(straightString.contains("111") || straightString.contains("1101") || straightString.contains("1011")){
                    barrels.add(p);
                }
            }
        }
        return barrels;
    }

    //Only valid on the turn, not the river or flop
    private static Set<Pocket> addGutshots(Set<Pocket> barrels, Set<Pocket> input, Card[] board) {
        for(Pocket p : input) {
            List<Card> handL = new ArrayList<>(Arrays.asList(board));
            handL.add(p.getCard(0));
            handL.add(p.getCard(1));
            Card[] hand = handL.toArray(new Card[0]);
            long bitmask = hand[0].getSingleRanksMap();
            for(int i=1; i< hand.length; i++){
                bitmask = bitmask | hand[i].getSingleRanksMap();
            }
            String straightString = pad14(Long.toBinaryString(bitmask));
            if(straightString.contains("11101") || straightString.contains("11011") || straightString.contains("10111")){
                barrels.add(p);
            }else{
                if(straightString.startsWith("1111")){
                    barrels.add(p);
                }
                if(straightString.endsWith("1111")){
                    barrels.add(p);
                }
            }
        }
        return barrels;
    }

    private static String pad14(String toBinaryString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            sb.append('0');
        }
        return sb.substring(toBinaryString.length()) + toBinaryString;
    }

    private static Set<Pocket> addMadeStraightsAndFlushes(Set<Pocket> barrels, Set<Pocket> input, Card[] board) {
        for(Pocket p : input) {
            List<Card> hand = new ArrayList<>(Arrays.asList(board));
            hand.add(p.getCard(0));
            hand.add(p.getCard(1));
            Card[] handArr = hand.toArray(new Card[0]);
            if(HandValue.getHandValue(handArr) > HandValue.STRAIGHTMUL && HandValue.getHandValue(handArr) < HandValue.BOATMUL){
                barrels.add(p);
            }
        }
        return barrels;
    }

    private static Set<Pocket> addTwoPairAndBetter(Set<Pocket> barrels, Set<Pocket> input, Card[] board) {
        for(Pocket p : input){
            int pairTripsCount = 0;
            for(Card c : p.getCards()){
                for(Card b : board){
                    if(c.rank.equals(b.rank)){
                        pairTripsCount++;
                    }
                }
            }
            if(pairTripsCount >= 2) { //TPGK or better for value
                barrels.add(p);
            }
        }
        return barrels;
    }

    private static Set<Pocket> addTpgkAndBetter(Set<Pocket> barrels, Set<Pocket> input, Card[] board) {
        for(Pocket p : input){
            int pairTripsCount = 0;
            for(Card c : p.getCards()){
                for(Card b : board){
                    if(c.rank.equals(b.rank)){
                        pairTripsCount++;
                    }
                }
            }
            if(p.getCard(0).rank == p.getCard(1).rank){
                if(p.getCard(0).getSingleRanksMap() > board[0].getSingleRanksMap()){//overpairs
                    pairTripsCount+=2;
                }
            }
            if(board[0].rank.equals(p.getCard(0).rank)) { //top pair
                if(p.getCard(1).rank.value > 13){ //good kicker
                    pairTripsCount++;
                }
            }
            if(board[0].rank.equals(p.getCard(1).rank)) { //other top pair
                if(p.getCard(0).rank.value > 13){ //good kicker
                    pairTripsCount++;
                }
            }
            if(pairTripsCount >= 2) { //TPGK or better for value
                barrels.add(p);
            }
        }
        return barrels;
    }

    public static double rangeAdvantage(Set<Pocket> yourRange, Set<Pocket> opponentRange, Set<Card> board){
        MDFCalculator mdfCalculator = new MDFCalculator();
        Pocket[][] arr = new Pocket[][]{yourRange.toArray(new Pocket[0]), opponentRange.toArray(new Pocket[0])};
        double[] res = mdfCalculator.equityCalc.eqPercents(arr, board.toArray(new Card[0]), new Card[0]);
        return res[0];
    }

    public static double betAndContinueRangeAdvantage(Set<Pocket> yourRange, Set<Pocket> opponentRange, Set<Card> board, double betPercent, double betPotRatio){
        MDFCalculator mdfCalculator = new MDFCalculator();
        List<Pocket> sortedRange = mdfCalculator.sortByEquity(yourRange, opponentRange, board);
        //TODO reduce ranges by ratio and mdf to bet, respectively
        //return rangeAdvantage(bettingRange, opponentContinuanceRange, board);
        return 0.0;
    }

    public static double nutAdvantage(Set<Pocket> yourRange, Set<Pocket> opponentRange, Set<Card> board, double topHands){
        MDFCalculator mdfCalculator = new MDFCalculator();
        List<Pocket> sortedRange = mdfCalculator.sortByEquity(yourRange, opponentRange, board);
        List<Pocket> sortedOppRange = mdfCalculator.sortByEquity(opponentRange, yourRange, board);
        int yourCombos = (int) (topHands * sortedRange.size());
        int theirCombos = (int) (topHands * sortedOppRange.size());
        Set<Pocket> yourtop = new HashSet<>();
        Set<Pocket> theirtop = new HashSet<>();
        for(int i=0; i<yourCombos; i++){
            yourtop.add(sortedRange.get(i));
        }
        for(int i=0; i<theirCombos; i++){
            theirtop.add(sortedOppRange.get(i));
        }
        return rangeAdvantage(yourtop, theirtop, board);
    }

    static String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "T", "J", "Q", "K", "A"};

    public static String regex(String hi, String lo){
        return hi + "[hscd]" + lo + "[hscd]";
    }
    public static String sRegex(String hi, String lo){
        return hi + "[h]" + lo + "[h]|" + hi + "[d]" + lo + "[d]|" + hi + "[c]" + lo + "[c]|" + hi + "[s]" + lo + "[s]|";
    }

    private static int blockersPairs(String hi, String lo, Set<Card> board) {
        int blockers = 0;
        for(Card c : board){
            if(c.getRank() == hi.charAt(0) || c.getRank() == lo.charAt(0)){
                blockers++;
            }
        }
        switch(blockers) {
            case 0:
                return 6;
            case 1:
                return 3;
            case 2:
                return 1;
            default:
                return 0;
        }
    }

    private static int blockersSuited(String hi, String lo, Set<Card> board) {
        String suitBlockers ="";
        for(Card c : board){
            if(c.getRank() == hi.charAt(0)){
                suitBlockers += c.suit;
            }
            if(c.getRank() == lo.charAt(0)){
                suitBlockers += c.suit;
            }
        }
        suitBlockers.replaceAll("hh", "h");
        suitBlockers.replaceAll("ss", "s");
        suitBlockers.replaceAll("cc", "c");
        suitBlockers.replaceAll("dd", "d");
        return (4 - suitBlockers.length());
    }

    private static int blockers(String hi, String lo, Set<Card> board) {
        int hiBlockers = 0;
        int loBlockers = 0;
        for(Card c : board){
            if(c.getRank() == hi.charAt(0)){
                hiBlockers++;
            }
            if(c.getRank() == lo.charAt(0)){
                loBlockers++;
            }
        }
        return (4 - hiBlockers) * (4 - loBlockers);
    }

    public static String beautify(Set<Pocket> rangeToSimplify, Set<Card> board) {
        StringBuilder tmp = new StringBuilder();
        for(Pocket p : rangeToSimplify){
            tmp.append(p.toString() + ",");
        }
        String search = tmp.toString();
        for(String h : ranks) { //nonpockets
            for (String l : ranks) {
                if(l.equals(h))
                    continue;
                Pattern pattern = Pattern.compile(regex(h,l), Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(search);
                int matches = 0;
                while(matcher.find()){
                    matches++;
                }
                if(matches == blockers(h, l, board)){
                    search = h + l + "," + search.replaceAll(regex(h,l), "");
                }
                pattern = Pattern.compile(sRegex(h,l), Pattern.MULTILINE);
                matcher = pattern.matcher(search);
                matches = 0;
                while(matcher.find()){
                    matches++;
                }
                if(matches == blockersSuited(h, l, board)){
                    search = h + l + "s," + search.replaceAll(sRegex(h,l), "");
                }
            }
        }
        for(String r : ranks){ //pocket pairs
            final Pattern pattern = Pattern.compile(regex(r,r), Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(search);
            int matches = 0;
            while(matcher.find()){
                matches++;
            }
            if(matches == blockersPairs(r, r, board)){
                search = r + r + "," + search.replaceAll(regex(r,r), "");
            }
        }
        for(int i=0; i<12; i++)
            search = search.replaceAll(",,", ",");
        return search;
    }
}
