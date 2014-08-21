/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jayaraj Poroor
 */
public class BasicTest {
    
    public BasicTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void basicTest() 
    {
        long item = 2;
        LinkedList<Long> list = new LinkedList<>();
        list.addLast(1L);
        list.addLast(2L);
        list.addLast(4L);
        ListIterator<Long> lit = list.listIterator();
        
        lit.next();
        lit.next();
        //lit.add(item);
        //lit.previous();
        lit.previous();
        System.out.println(lit.next());
    }
}
