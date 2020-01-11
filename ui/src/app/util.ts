import { of, Observable } from "rxjs";
import { PageElement, EmptyOeuvre } from "./model/Oeuvre";
import { environment } from "src/environments/environment";
import { PageElementDisplay } from "./PageElementDisplay";

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
  public constr: () => Id;

  constructor(f: () => Id) {
    this.data = new AutoMap<number, Id>(f);
    this.idGen = new AutoId();
    this.constr = f;
  }

  getData(): Id[] {
    return Array.from(this.data.values());
  }

  setData(els : Id[]){
     this.data._under.clear();
     els.forEach(e=> this.data.set(e.id,e))
  }
  newWithId(): IdType {
    const ret = this.constr();
    ret.id = this.idGen.getId();
    return (ret as any) as IdType;
  }
  asObservable(): Observable<IdType[]> {
    const ret: IdType[] = [];
    debugger;
    for (let e of Array.from(this.data._under.values())) {

      ret.push((e as any) as IdType);
    }
    return of(ret);
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

export class PageComponentBase {
  x: number = undefined;
  y: number = undefined;
  st: CSSStyleDeclaration;
  draged: PageElement;
  switchCandidate: PageElement;
  oeuvres: PageElement[][] = [];
  pDisplay: PageElementDisplay;
  addIncolumn = 1;
  _navigation = false;
  currentThemeKey: number;
  constructor() {
    this.pDisplay = environment.pDisplay;
  }
  sDefaultColumn(e: number) {
    console.log(e);
    if (e === 1) {
      console.log("true");
      return "selected";
    } else {
      return false;
    }
  }
  removeEmpty() {
    let x = 0;
    for (const o of this.oeuvres) {
      let y = 0;
      this.oeuvres[x] = o.filter(e => e.isNonEmpty());
      for (const oo of this.oeuvres[x]) {
        oo.y = y;
        y++;
      }
      x++;
    }
  }

  swap(o1: PageElement, o2: PageElement) {
    if (o1 !== o2 && this.pDisplay.drag) {
      const x = o1.x;
      const y = o1.y;
      o1.x = o2.x;
      o1.y = o2.y;
      o2.x = x;
      o2.y = y;

      this.oeuvres[o1.x][o1.y] = o1;
      this.oeuvres[o2.x][o2.y] = o2;
    }
  }

  click(o: PageElement, e: MouseEvent) {
    if (this.pDisplay.drag) {
      if (!this.draged) {
        this.draged = o;
        const toMove = e.currentTarget as any;
        this.st = toMove.style as CSSStyleDeclaration;
        let x = 0;
        while (x < 3) {
          if (x === this.oeuvres.length) {
            this.oeuvres.push([]);
          }

          const oo = this.oeuvres[x];
          const y = oo.length;
          oo.push(new EmptyOeuvre(x, y, -1));

          x++;
        }
      } else {
        this.st.zIndex = "-1000";
      }
    }
  }
  class(o: PageElement) {
    if (this.pDisplay.drag) {
      if (this.draged === o) {
        return "dragged";
      } else {
        return "notdragged";
      }
    } else if (this.pDisplay.edit) {
      return "edit";
    } else if (this.pDisplay.navigation) {
      return "p-5";
    }
  }
  moushover(o: PageElement, e: MouseEvent) {
    if (!this.switchCandidate && this.draged && this.draged !== o) {
      this.switchCandidate = o;
      this.st.zIndex = "0";

      this.st.position = "relative";
      this.st.top = "0";
      this.st.left = "0";
      this.st = undefined;

      this.swap(this.draged, this.switchCandidate);
      this.switchCandidate = undefined;
      this.draged = undefined;
      this.removeEmpty();
    }
  }
  mousemove(o: PageElement, e: MouseEvent) {
    if (this.draged === o) {
      // this.st.position = "fixed";
      this.st.top = e.y - 50 + "px"; // - +toMove.offsetHeight / 2 + "px";
      this.st.left = e.x - 50 + "px"; // - +toMove.offsetWidth / 2 + "px";
    }
  }

  addInTable(ele: PageElement) {

    while (this.oeuvres.length <= ele.x) {
      this.oeuvres.push([]);
    }

    while (this.oeuvres[ele.x].length < ele.y) {
      this.oeuvres[ele.x].push(null);
    }
    const prev = this.oeuvres[ele.x][ele.y];
    if (prev != null) {
      prev.y = prev.y + 1;
      this.addInTable(prev);
    }
    this.oeuvres[ele.x][ele.y] = ele;
  }
}
