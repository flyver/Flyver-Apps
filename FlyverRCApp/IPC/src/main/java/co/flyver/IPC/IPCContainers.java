package co.flyver.IPC;

/**
 * Created by skaarjbg on 11/4/14.
 */
public class IPCContainers {

    private final String TAG = "IPC";

    public static class JSONQuadruple<K, V1, V2, V3> {
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

        public JSONQuadruple(K key, V1 value1, V2 value2, V3 value3) {
            this.key = key;
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
        }

        public JSONQuadruple() {
        }
    }

    public static class JSONTriple<K, A, V> {

        public K key;
        public A action;
        public V value;


        public K getKey() {
            return key;
        }

        public A getAction() {
            return action;
        }

        public V getValue() {
            return value;
        }

        public JSONTriple() {
        }

        public JSONTriple(K key, A action, V value) {
            this.key = key;
            this.action = action;
            this.value = value;
        }

    }

    public static class JSONTuple<K, V> {
        public K key;
        public V value;


        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public JSONTuple() {
        }

        public JSONTuple(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

}
