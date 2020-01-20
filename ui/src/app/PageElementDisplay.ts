import { Oeuvre } from "./model/Oeuvre";
import { PageElement } from "./model/PageElement";
import { environment } from "src/environments/environment";
import { MenuItem } from "./MenuItem";
export class PageElementDisplay {
  _navigation = false;
  _drag = false;
  _edit = true;
  _showAdd = false;
  get showAdd() {
    return this._showAdd;
  }
  set showAdd(b: boolean) {
    this._showAdd = b;
  }
  get edit() {
    return this._edit;
  }
  set edit(b: boolean) {
    this._edit = b;
    this.drag = !this._edit;
  }
  src(e: PageElement): string {

    if (environment.online && e && e.isNonEmpty()) {
      if (e.src) {
        return e.src;
      } else {
        return "/assets/img/mamie_muguette_acc.jpg";
      }

    } else {
      return "/assets/img/mamie_muguette_acc.jpg";
    }
  }
  buildLink(e: PageElement): string {
    if (e.isMenu()) {
      return `/${e.type}/${e.title}/${e.id}`;
    } else if (e.isNonEmpty && !this.edit) {
      return `/gallery/${e.title}/${e.id}`;
    } else if(this.edit) {
      return "/admin/image";
    } else {
      return "";
    }
  }

  link(o: PageElement): PageElement {
    if (this.navigation || this.edit) {
      return o;
    } else {
      return undefined;
    }
  }
  get islink(): boolean {
    if (this.edit) {
      return true;
    } else {
      return false;
    }
  }
  description(e: PageElement): Oeuvre {
    if (e && e.isOeuvre()) {
      return e.asOeuvre();
    } else {
      return undefined;
    }
  }
  menu(e: PageElement): MenuItem {
    if (e && e.isMenu()) {
      return e.asMenu();
    } else {
      return undefined;
    }
  }
  get navigation() {
    return this._navigation;
  }
  alt(e: PageElement): string {
    return e ? e.alt : undefined;
  }
  set navigation(b: boolean) {
    if (b) {
      this._drag = false;
      this._edit = false;
    } else {
      this._drag = false;
      this._edit = true;
    }
    this._navigation = b;
  }
  get drag() {
    return this._drag && !this.navigation;
  }
  set drag(b: boolean) {
    this._drag = b;
    this._edit = !this.drag;
  }
  show(b: boolean) {
    if (!b) {
      return "hidden";
    }
  }
}

export const pDisplay = new PageElementDisplay()
