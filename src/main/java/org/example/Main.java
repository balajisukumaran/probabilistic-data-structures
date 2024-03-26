package org.example;


import BloomFilter.BloomFilter;
import ConcurrentSkipList.SkipList;
import CuckooFilter.CuckooFilter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        SkipList s = new SkipList(10);

        s.add(10,"hi");
        s.add(20,"hello");
        s.add(21,"how");
        s.add(22,"are");
        s.add(23,"you");
        s.add(24,"doing");
        s.add(25,"hey");
        s.add(26,"a");
        s.add(27,"is");
        s.add(28,"ronaldo");
        s.display();
        String value = s.search(26);

        BloomFilter<Integer> b= new BloomFilter<Integer>(1000*1000,0.01d);
        b.add(10);
        b.add(20);
        b.add(21);
        b.add(22);
        b.add(23);
        b.add(24);
        b.add(25);
        b.add(26);
        b.add(27);
        b.add(28);

        System.out.println(b.contains(24));
        System.out.println(b.contains(30));


        CuckooFilter c= new CuckooFilter(10,10);
        c.insert(10);
        c.insert(20);
        c.insert(21);
        c.insert(22);
        c.insert(23);
        c.insert(24);
        c.insert(25);
        c.insert(26);
        c.insert(27);
        c.insert(28);

        c.delete(24);
        System.out.println(c.contains(24));
    }
}