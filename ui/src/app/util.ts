
export class AutoMap<K, V> {
  _under = new Map<K, V>();

  size: number = this._under.size;

  newV: () => V;
  constructor(newV: () => V) {
    this.newV = newV;
  }

  delete(key: K): boolean {
    return this._under.delete(key);
  }
  forEach(
    callbackfn: (value: V, key: K, map: Map<K, V>) => void,
    thisArg?: any
  ): void {
    return this._under.forEach(callbackfn, thisArg);
  }
  has(key: K): boolean {
    return this._under.has(key);
  }
  set(key: K, value: V): this {
    this._under.set(key, value);
    return this;
  }
  entries(): IterableIterator<[K, V]> {
    return this._under.entries();
  }
  keys(): IterableIterator<K> {
    return this._under.keys();
  }
  values(): IterableIterator<V> {
    return this._under.values();
  }

  get(k: K): V {
    const f = this._under.get(k);
    if (f) {
      return f;
    } else {
      this._under.set(k, this.newV());
      return this._under.get(k);
    }
  }
}

