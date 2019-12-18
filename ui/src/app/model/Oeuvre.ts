import { Id } from "../util";

export class Oeuvre implements Id,OeuvreInTable {
  id: number;
  themeKey: number;
  title: String;
  date: String;
  dimension: String;
  description: String;
  x: number;
  y: number;
  isNonEmpty() {return true;};
}

export interface OeuvreInTable {
  x: number;
  y: number;
  isNonEmpty() : boolean;
}
export class EmptyOeuvre implements Id, OeuvreInTable {
  constructor( public x: number,
    public y: number,
  public id: number){}
  isNonEmpty() {return false;};
}
