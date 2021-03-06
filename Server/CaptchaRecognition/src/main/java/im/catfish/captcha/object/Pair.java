package im.catfish.captcha.object;

public class Pair<K, V> {
  public final K k;
  public final V v;

  public Pair(K k, V v) {
    this.k= k;
    this.v= v;
  }

  public K getKey() {
    return this.k;
  }

  public V getValue() {
    return this.v;
  }
}
