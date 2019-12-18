import { of, Observable } from 'rxjs';

export class AutoId {
  id = 0;
  getId() {
    this.id++;
    return this.id;
  }
}
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
export interface Id {
  id: number;
}
export class CacheEnv<IdType> {
  private data: AutoMap<number, Id>;
  private idGen: AutoId;
  public constr : () => Id;
  constructor(f: () => Id) {
    this.data = new AutoMap<number, Id>(f);
    this.idGen = new AutoId();
    this.constr = f;

  }

  newWithId():IdType {
    const ret = this.constr();
    ret.id = this.idGen.getId();
    return ret as any  as IdType;
  }
  asObservable() : Observable<IdType[]>{
    const ret : IdType[] = [];

    for(let e  of  Array.from( this.data._under.values())){
      ret.push(e as any as IdType);
    }
    return of(ret)
  }
  get(id: number) {
    const ret = this.data.get(id);
    if (!ret.id) {
      ret.id = this.idGen.getId();
    }
    return ret;
  }
  set(id: number, el: Id) {
    return this.data.set(id, el);
  }
}

