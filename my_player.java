//6838119248:Priyanshi Vora
import java.io.*;
import java.util.*;

class Position {
    private int row;
    private int col;

    public void setCol(int col) {
        this.col = col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }
}

public class my_player {
    private int player_side;
    int[][] prevState = new int[5][5];
    int[][] currentState = new int[5][5];
    int[][] tempState = new int[5][5];
    boolean diedPieces = false;
    Map<String,LinkedList<String>> map = new LinkedHashMap<>();
    Map<String,String> map1 = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        ArrayList<String> arrList = new ArrayList<>();
        File file = new File("input.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        while ((st = br.readLine()) != null){
            arrList.add(st);
        }
        my_player my_player = new my_player();
        my_player.populateValues(arrList);
        my_player.beginGame();

    }
    private void populateValues(ArrayList<String> arrayList) throws IOException{
        int counter =0;
        player_side = Integer.parseInt(arrayList.get(0));
        for(int i =1;i<=5;i++){
            for(int j = 0;j<5;j++){
                prevState[i-1][j] = Integer.parseInt((Character.toString(arrayList.get(i).charAt(j))));
            }
        }
        for(int i =6;i<=10;i++){
            for(int j = 0;j<5;j++){
                currentState[counter][j] = Integer.parseInt((Character.toString(arrayList.get(i).charAt(j))));
            }
            counter++;
        }
    }

    public void addVertex(String s)
    {
        map.put(s, new LinkedList<String>());
    }

    public void addEdge(String source,
                        String destination)
    {

        if (!map.containsKey(source))
            addVertex(source);

        if (!map.containsKey(destination))
            addVertex(destination);

        map.get(source).add(destination);
    }

    public String encode_state(int[][] state){
        String encoded_state = "";
        for(int i = 0;i<5;i++){
            for(int j = 0;j<5;j++) {
                encoded_state += state[i][j];
            }
        }
        return encoded_state;
    }

    private int[][] decode_string(String s){
        int counter = 0;
        int[][] state = new int[5][5];
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                state[i][j]=Integer.parseInt(String.valueOf(s.charAt(counter)));
                counter++;
            }
        }
        return state;
    }

    private ArrayList<Position> getValidMoves(String state) throws IOException {
        int[][] stateCopy = decode_string(state);
        currentState = copyState(stateCopy);
        ArrayList<Position> positions = new ArrayList<>();
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                if(currentState[i][j]==0){
                    if(validCheck(i,j,player_side)){
                        Position position = new Position();
                        position.setRow(i);
                        position.setCol(j);
                        positions.add(position);
                    }
                }
            }
        }
        return  positions;
    }

    private boolean checkEmptyBoard(int[][] state){
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                if(state[i][j]!=0)
                    return false;
            }
        }
        return true;
    }

    private void beginGame() throws IOException {
        String rootString = encode_state(currentState);
        int[][] currState = copyState(currentState);
        int finalPlayer = player_side;
        ArrayList<Position> positions = getValidMoves(rootString);
        File tempFile = new File("output.txt");
        if(tempFile.exists())
            tempFile.delete();
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        if(positions.size()==0){
            writer.append("PASS");
        }
        else if(positions.size()==1){
            Position pos = positions.get(0);
            writer.append(pos.getRow()+","+pos.getCol());
        }
        else if(checkEmptyBoard(currState)){
            for(Position position:positions){
                if(position.getRow()==2 && position.getCol()==2)
                    writer.append("2,2");
            }
        }

        else {
            String bestMove = "";
            int maxScore = Integer.MIN_VALUE;
            Map<String,Position> levelPos = new LinkedHashMap<>();
            Position prevCord = getPrevCord(currState, 3-finalPlayer);
            for (Position position : positions) {
                int[][] x = copyState(currState);
                x[position.getRow()][position.getCol()] = player_side;
                String original = encode_state(x);
                tempState = copyState(x);
                remove_died_pieces(3 - player_side);
                String newStr = encode_state(tempState);
                addEdge(rootString, newStr);
                map1.put(newStr, original);
                levelPos.put(newStr,position);
            }
            //player_side = 3 - player_side;
            LinkedList<String> list = map.get(rootString);
            for(String s: list) {
                boolean check = false;
                int[][] y = decode_string(s);
                positions = getValidMoves(s);
                Position p = levelPos.get(s);
                int parentDistance = calcDistance(p,prevCord);
                tempState = decode_string(s);
                // int parentNeighbours = searchNeighbourAlly(p.getRow(),p.getCol()).size();
                int parentNeighbours = searchAlly(p.getRow(),p.getCol()).size();
                int rootParentCount = countNeighbourAllies(rootString,3-player_side);
                int parentCount = countNeighbourAllies(s,3-player_side);
                player_side=3-player_side;
                int rootVM = getValidMoves(rootString).size();
                int parentVM = getValidMoves(s).size();
                int oppNeighbour = countNeighbourAlly(p.getRow(),p.getCol(),3-player_side).size();
                player_side=3-player_side;
                int score = findDiff(s,rootString)+positions.size()-parentDistance+parentNeighbours+(rootParentCount-parentCount)-oppNeighbour+(rootVM-parentVM);
                for (Position position : positions) {
                    check=true;
                    int[][] x = copyState(y);
                    x[position.getRow()][position.getCol()] = player_side;
                    int oppNeighbour1 = countNeighbourAlly(position.getRow(),position.getCol(),3-player_side).size();
                    //String original = encode_state(x);
                    tempState = copyState(x);
                    remove_died_pieces(3 - player_side);
                    String newStr = encode_state(tempState);
                    int childCount = countNeighbourAllies(newStr,3-player_side);
                    player_side=3-player_side;
                    int childVM = getValidMoves(newStr).size();
                    player_side=3-player_side;
                    //int neighbours = searchNeighbourAlly(position.getRow(),position.getCol()).size();
                    int neighbours = searchAlly(position.getRow(),position.getCol()).size();
                    score+=findDiff(s,newStr)+getValidMoves(newStr).size()+neighbours-oppNeighbour1+(parentCount-childCount)+(parentVM-childVM);
                    if(score>maxScore){
                        maxScore = score;
                        bestMove = s;
                    }
                    addEdge(s, newStr);
                }
                if(!check){
                    if(score>maxScore){
                        maxScore=score;
                        bestMove=s;
                    }
                }
            }
            //map.putAll(map3);
            //map3 = new LinkedHashMap<>();

            int[][] returnedState = decode_string(map1.get(bestMove));
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    if (returnedState[i][j] != currState[i][j]) {
                        writer.append(i + "," + j);
                    }
                }
            }
        }
        writer.close();
    }

    private int calcDistance(Position p, Position prevCord) {
        int x = p.getRow();
        int y = p.getCol();
        int x1 = prevCord.getRow();
        int y1 = prevCord.getCol();
        return (int)Math.sqrt(Math.pow(x-x1,2)+Math.pow(y-y1,2));
    }

    private Position getPrevCord(int[][] currState, int player) {
        Position pos = new Position();
        for(int i=0;i<5;i++){
            for(int j = 0;j<5;j++){
                if(currState[i][j]!=prevState[i][j] && currState[i][j]==player){
                    pos.setRow(i);
                    pos.setCol(j);
                }
            }
        }
        return pos;
    }

    private ArrayList<Position> countNeighbourAlly(int i, int j, int player) {
        ArrayList<Position> neighbours = detectNeighbour(i,j);
        ArrayList<Position> groupAllies = new ArrayList<>();
        for(Position n: neighbours){
            if(tempState[n.getRow()][n.getCol()]==player){
                groupAllies.add(n);}
        }
        return groupAllies;
    }


    private int countNeighbourAllies(String s, int player){
        int count = 0;
        int x[][] = decode_string(s);
        tempState = copyState(x);
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                if(tempState[i][j]==player){
                    count+=searchNeighbourAlly(i,j).size();
                }
            }
        }
        return count;
    }

    private int findDiff(String s, String s1) {
        int count = 0;
        int x[][] = decode_string(s);
        int y[][] = decode_string(s1);
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                if(x[i][j]!=y[i][j])
                    count++;
            }
        }
        return count;
    }

    public boolean validCheck(int i,int j,int player) throws IOException {
        if(!(i>=0 && i<5))
            return false;
        if(!(j>=0 && j<5))
            return false;
        if(currentState[i][j]!=0)
            return false;

        tempState = copyState(currentState);
        tempState[i][j]=player;
        if(findLiberty(i,j))
            return true;
        remove_died_pieces(3-player);
        if(!findLiberty(i,j)){
            return false;
        }

        else{
            if(diedPieces && compareBoard(prevState,tempState)){
                return false;
            }
        }
        return true;
    }

    private int[][] copyState(int[][] currentState) {
        int[][] tempState = new int[5][5];
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                tempState[i][j] = currentState[i][j];
            }
        }
        return tempState;
    }

    private boolean compareBoard(int[][] previousState, int[][] tempState) {
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                if(previousState[i][j]!=tempState[i][j])
                    return false;
            }
        }
        return true;
    }

    private void remove_died_pieces(int player) {
        for(int i=0;i<5;i++){
            for(int j=0;j<5;j++){
                if(tempState[i][j]==player){
                    if(!findLiberty(i,j)){
                        tempState[i][j]=0;
                        diedPieces=true;
                    }
                }
            }
        }
    }

    private boolean findLiberty(int i, int j) {
        ArrayList<Position> allies = searchAlly(i,j);
        for(Position s:allies){
            ArrayList<Position> neighbours = detectNeighbour(s.getRow(),s.getCol());
            for(Position s1:neighbours){
                if(tempState[s1.getRow()][s1.getCol()]==0)
                    return true;
            }
        }
        return false;
    }

    private ArrayList<Position> searchAlly(int i, int j) {
        Stack<String> stack = new Stack<>();
        ArrayList<String> ally_members = new ArrayList<>();
        ArrayList<Position> ally_members1 = new ArrayList<>();
        String position = "";
        stack.push(i+" "+j);
        while(!stack.empty()){
            position = stack.pop();
            int x = Integer.parseInt(position.split(" ")[0]);
            int y = Integer.parseInt(position.split(" ")[1]);
            Position p = new Position();
            p.setRow(x);
            p.setCol(y);
            ally_members.add(position);
            ally_members1.add(p);
            ArrayList<Position> groupAllies = searchNeighbourAlly(x,y);
            for(Position s:groupAllies){
                String temp = s.getRow()+" "+s.getCol();
                if(!stack.contains(temp) && !ally_members.contains(temp)){
                    stack.push(temp);
                }
            }
        }
        return ally_members1;
    }

    private ArrayList<Position> searchNeighbourAlly(int i, int j) {
        ArrayList<Position> neighbours = detectNeighbour(i,j);
        ArrayList<Position> groupAllies = new ArrayList<>();
        for(Position n: neighbours){
            if(tempState[n.getRow()][n.getCol()]==tempState[i][j]){
                groupAllies.add(n);}
        }
        return groupAllies;
    }

    private ArrayList<Position> detectNeighbour(int i, int j) {
        ArrayList<Position> neighbours = new ArrayList<>();
        if (i > 0){
            Position position = new Position();
            position.setRow(i-1);
            position.setCol(j);
            neighbours.add(position);
        }
        if(i<4){
            Position position = new Position();
            position.setRow(i+1);
            position.setCol(j);
            neighbours.add(position);
        }
        if(j>0){
            Position position = new Position();
            position.setRow(i);
            position.setCol(j-1);
            neighbours.add(position);
        }
        if(j<4){
            Position position = new Position();
            position.setRow(i);
            position.setCol(j+1);
            neighbours.add(position);
        }
        return neighbours;
    }

}