import { Injectable } from "@angular/core";
import { PageElement } from "./model/PageElement";

@Injectable({
  providedIn: "root"
})
export class SourceService {
  private _source: PageElement;
  constructor() {}

  get source(): PageElement {
    const ret= this._source;
    this._source = null;
    return ret;
  }

  set source(s: PageElement) {
    this._source = s;
  }
}
