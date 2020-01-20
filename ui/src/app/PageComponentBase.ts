import { EmptyOeuvre } from "./model/EmptyOeuvre";
import { PageElement } from "./model/PageElement";
import { PageElementDisplay, pDisplay } from "./PageElementDisplay";
import { ComponentUtil } from "./image-view/image-view.component";
export abstract class PageComponentBase extends ComponentUtil {
  x: number = undefined;
  y: number = undefined;
  st: CSSStyleDeclaration;
  draged: PageElement;
  switchCandidate: PageElement;
  oeuvres: PageElement[][] = [];
  pDisplay: PageElementDisplay;

  _navigation = false;
  currentThemeKey: number;
  newItem: PageElement;

  constructor() {
    super();
    this.pDisplay = pDisplay;
  }
  sDefaultColumn(e: number) {
    if (e === 1) {
      return "selected";
    } else {
      return false;
    }
  }
  getFreeY(x: number): number {
    return this.oeuvres[x] ? this.oeuvres[x].length : 0;
  }
  createEntity() {
    this.newItem.y = this.getFreeY(this.newItem.x);
    this.newItem.themeKey = this.currentThemeKey;
    this.mainService()
      .createEntity(this.newItem)
      .subscribe(e => {
        this.newItem.id = e.id;
        this.addInTable(this.newItem);
        this.newItem = this.mainService().needNew() as PageElement;
      });
  }

  removeFromView(id: number) {
    let x = 0;
    for (const o of this.oeuvres) {
      let y = 0;
      this.oeuvres[x] = o.filter(e => e.id !== id);
      for (const oo of this.oeuvres[x]) {
        oo.y = y;
        y++;
      }
      x++;
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
      this.draged.updated = true;
      this.update(this.draged);
      if (this.switchCandidate.isNonEmpty()) {
        this.switchCandidate.updated = true;
      }
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
  reindex() {
    for (let i = 0; i < this.oeuvres.length; i++) {
      this.oeuvres[i] = this.oeuvres[i].filter(e => e != null && e.isNonEmpty());
      for (let j = 0; j < this.oeuvres.length[i]; j++) {
        const o = this.oeuvres[i][j];
        if (o.y !== j) {
          o.y = j;
          o.updated = true;
        }
      }
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
