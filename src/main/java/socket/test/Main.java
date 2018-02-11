package socket.test;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    static Map<String, Map<String, List<Person>>> persons = new HashMap<String, Map<String, List<Person>>>();

    public static void main(String[] args) throws Exception{
        final BufferedReader br = new BufferedReader(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream("stuff.csv")));
        br.readLine(); //Skip the header line
        while(br.ready()){
            add(new Person(br.readLine()));
        }
        System.out.println(persons);
    }

    private static void add(Person person) {
        if(!persons.containsKey(person.getName())){
            persons.put(person.getName(), new HashMap<String, List<Person>>());
        }
        if(!persons.get(person.getName()).containsKey(person.getRoll())){
            persons.get(person.getName()).put(person.getRoll(), new ArrayList<Person>());
        }
        persons.get(person.getName()).get(person.getRoll()).add(person);
    }

    static class Person {

        String name, roll, address, phone;

        public Person(String string) {
            final StringTokenizer tok = new StringTokenizer(string, " ", false);
            name = tok.nextToken();
            roll = tok.nextToken();
            address = tok.nextToken();
            phone = tok.nextToken();
        }

        public String getName() {
            return name;
        }

        public String getRoll() {
            return roll;
        }

        @Override
        public String toString() {
            return address + "=[" + phone + "]";
        }
    }
}