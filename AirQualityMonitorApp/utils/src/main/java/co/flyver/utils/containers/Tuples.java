package co.flyver.utils.containers;

/**
 * Created by Petar Petrov on 11/4/14.
 */
public class Tuples {

    private final String TAG = "IPC";

    public static class Quadruple<K, V1, V2, V3> {
        public K key;
        public V1 value1;
        public V2 value2;

        public V3 value3;

        public V1 getValue1() {
            return value1;
        }

        public V2 getValue2() {
            return value2;
        }

        public V3 getValue3() {
            return value3;
        }

        public K getKey() {
            return key;
        }

        public Quadruple(K key, V1 value1, V2 value2, V3 value3) {
            this.key = key;
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
        }

        public Quadruple() {
        }
    }

    public static class Triple<K, A, V> {

        public K key;
        public A value1;
        public V value2;


        public K getKey() {
            return key;
        }

        public A getValue1() {
            return value1;
        }

        public V getValue2() {
            return value2;
        }

        public Triple() {
        }

        public Triple(K key, A value1, V value2) {
            this.key = key;
            this.value1 = value1;
            this.value2 = value2;
        }

    }

    public static class Tuple<K, V> {
        public K key;
        public V value;


        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public Tuple() {
        }

        public Tuple(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class Hextuple<V1, V2, V3, V4, V5, V6> {
        public V1 value1;
        public V2 value2;
        public V3 value3;
        public V4 value4;
        public V5 value5;
        public V6 value6;

        public Hextuple(V1 value1, V2 value2, V3 value3, V4 value4, V5 value5, V6 value6) {
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
            this.value4 = value4;
            this.value5 = value5;
            this.value6 = value6;
        }

        public V1 getValue1() {
            return value1;
        }

        public V2 getValue2() {
            return value2;
        }

        public V3 getValue3() {
            return value3;
        }

        public V4 getValue4() {
            return value4;
        }

        public V5 getValue5() {
            return value5;
        }

        public V6 getValue6() {
            return value6;
        }
    }

}
