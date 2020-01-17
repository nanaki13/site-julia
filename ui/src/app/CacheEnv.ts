import { of, Observable } from "rxjs";
import { Id } from './Id';
import { AutoId } from './AutoId';
import { AutoMap } from './util';
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
  setData(els: Id[]) {
    this.data._under.clear();
    els.forEach(e => this.data.set(e.id, e));
  }
  newWithId(): IdType {
    const ret = this.constr();
    ret.id = this.idGen.getId();
    return (ret as any) as IdType;
  }
  asObservable(): Observable<IdType[]> {
    const ret: IdType[] = [];
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
