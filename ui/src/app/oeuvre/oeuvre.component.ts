import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, ParamMap } from "@angular/router";
import { of } from "rxjs";
import { Oeuvre, OeuvreInTable, EmptyOeuvre } from "../model/Oeuvre";
import { OeuvreService } from "../oeuvre.service";

@Component({
  selector: "app-oeuvre",
  templateUrl: "./oeuvre.component.html",
  styleUrls: ["./oeuvre.component.css"]
})
export class OeuvreComponent implements OnInit {
  currentThemeKey: number;
  newTitle: String;

  id: number;
  x: number = undefined;
  y: number = undefined;
  st: CSSStyleDeclaration;
  draged: OeuvreInTable;
  switchCandidate: OeuvreInTable;
  oeuvres: OeuvreInTable[][] = [];
  _drag = false;
  _edit = true;

  get drag() {
    return this._drag;
  }
  set drag(b: boolean) {
    this._drag = b;
    this._edit = !this.drag;
    while (this.oeuvres.length < 3) {
      this.oeuvres.push([]);
    }
    if (b) {
      let i = 0;
      for (const o of this.oeuvres) {
        if (o.length === 0 && i != 0) {
          o.push(new EmptyOeuvre(i, 0, -1));
        }
        i++;
      }
    } else {
      this.removeEmpty();
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
  get edit() {
    return this._edit;
  }
  set edit(b: boolean) {
    this._edit = b;
    this.drag = !this._edit;
  }

  show(b: boolean) {
    if (!b) {
      return "hidden";
    }
  }

  swap(o1: OeuvreInTable, o2: OeuvreInTable) {
    if (o1 !== o2 && this.drag) {
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

  constructor(private route: ActivatedRoute, private mService: OeuvreService) {
    this.oeuvres = [];
  }
  addOeuvre() {
    this.mService.add(this.newTitle, this.currentThemeKey).subscribe(e => {
      this.ngOnInit();
    });
  }
  description(e: OeuvreInTable): Oeuvre {
    if (e && e.isNonEmpty()) {
      return e as Oeuvre;
    } else {
      return undefined;
    }
  }
  // move(e: MouseEvent) {
  //   if (this.drag && this.draged) {
  //     const toMove = e.currentTarget as any;
  //     this.st = toMove.style as CSSStyleDeclaration;
  //     this.st.position = "fixed";
  //     this.st.top = e.y - +toMove.offsetHeight / 2 + "px";
  //     this.st.left = e.x - +toMove.offsetWidth / 2 + "px";
  //   }
  // }
  style() {
    if (this.x) {
      return `position : fixed; left:${this.x};top:${this.y}`;
    } else {
      return "";
    }
  }
  up(o: OeuvreInTable) {
    if (this.drag) {
      if (!this.draged) {
        this.draged = o;
      } else {
        this.st.zIndex = "-1000";
      }
    }
  }
  over(o: OeuvreInTable, e: MouseEvent) {
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
    } else if (this.draged === o) {
      const toMove = e.currentTarget as any;
      this.st = toMove.style as CSSStyleDeclaration;
      this.st.position = "fixed";
      this.st.top = e.y - +toMove.offsetHeight / 2 + "px";
      this.st.left = e.x - +toMove.offsetWidth / 2 + "px";
    }
  }
  down(o: OeuvreInTable) {
    if (this.drag) {
      this.st.zIndex = "-1000";
    }
  }

  addInTable(ele: OeuvreInTable) {
    while (this.oeuvres.length <= ele.x) {
      this.oeuvres.push([]);
    }

    while (this.oeuvres[ele.x].length < ele.y) {
      this.oeuvres[ele.x].push(new EmptyOeuvre(ele.x, this.oeuvres.length, -1));
    }
    const prev = this.oeuvres[ele.x][ele.y];
    if (prev != null && prev.isNonEmpty()) {
      prev.y = prev.y + 1;
      this.addInTable(prev);
    }
    this.oeuvres[ele.x][ele.y] = ele;
  }

  ngOnInit() {
    this.route.paramMap.subscribe(par => {
      this.currentThemeKey = +par.get("id");
      this.oeuvres = [];
      this.mService.oauvres(+par.get("id")).subscribe(e => {
        e.forEach(ele => {
          this.addInTable(ele);
        });
      });
    });
  }
}
