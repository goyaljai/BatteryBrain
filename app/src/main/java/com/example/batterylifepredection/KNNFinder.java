package com.example.batterylifepredection;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

public class KNNFinder {
    PriorityQueue<Pair> pq;

    public Neighbours getNeighbours(int k, double[] currentContext) {
        String path = Constants.USER_HISTORY_PATH;
        Neighbours neighbours = new Neighbours(k);
        pq = new PriorityQueue<>(k,
                new NeighbourCompare());
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), path);
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            double res = 0;
            int cnt = 0;
            while ((line = br.readLine()) != null) {
                res = 0;
                cnt++;
                String[] ctx = line.split(",");
                if (ctx.length != currentContext.length) {
                    throw new Exception("length of context tuples are not same");
                }
                for (int i = 0; i < currentContext.length; i++) {
                    res += Math.pow(currentContext[i] - Double.parseDouble(ctx[i]), 2);
                }
                res = Math.sqrt(res);
                if (pq.isEmpty() || pq.size() < k) {
                    pq.add(new Pair(cnt, res));

                } else {
                    if (pq.peek().dist > res) {
                        pq.poll();
                        pq.add(new Pair(cnt, res));
                    }
                }
            }
            br.close();
            for (int i = k - 1; i >= 0; i--) {
                neighbours.nbrList[i] = pq.poll().row;
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return neighbours;
    }
}

class Pair {

    int row;
    double dist;

    Pair(int r, double d) {
        row = r;
        dist = d;
    }
}

class NeighbourCompare implements Comparator<Pair> {

    @Override
    public int compare(Pair p1, Pair p2) {
        // TODO Auto-generated method stub
        if (p1.dist < p2.dist) {
            return 1;
        }
        return -1;
    }

}
